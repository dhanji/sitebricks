package com.google.sitebricks.headless;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.ImmutableMap;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.client.transport.Xml;
import org.testng.annotations.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * A unit test for the reply builder/response populate pipeline.
 */
public class HeadlessReplyTest {
  private static final String HELLO_THERE = "Hello there!";
  private static final String INK_SPOTS = "Ink Spots";
  private static final String MAYBE = "Maybe";
  private static final int SONG_LENGTH_MAYBE = 3456;
  private static final String X_MY_HEADER = "X-My-Header";
  private static final String X_MY_HEADER_VAL = "X-My-Haisdjfoiajsd";
  private static final String X_YOUR_HEADER = "X-Your-Header";
  private static final String X_YOUR_HEADER_VAL = "2838L";

  private static class FakeServletOutputStream extends ServletOutputStream {
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    @Override
    public void write(int b) throws IOException {
      bout.write(b);
    }

    @Override
    public String toString() {
      return bout.toString();
    }
  }

  @Test
  public void textReply() throws IOException {
    Injector injector = Guice.createInjector();
    HeadlessRenderer renderer = injector.getInstance(HeadlessRenderer.class);
    HttpServletResponse response = createNiceMock(HttpServletResponse.class);
    ServletOutputStream outputStream = fakeServletOutputStream();


    // The script to expect for our mock.
    response.setStatus(HttpServletResponse.SC_OK);
    expect(response.getOutputStream())
        .andReturn(outputStream);

    response.setContentType(injector.getInstance(Text.class).contentType());

    replay(response);

    renderer.render(response, Reply.with(HELLO_THERE));

    verify(response);

    assert HELLO_THERE.equals(outputStream.toString());
  }

  @Test
  public void jsonReply() throws IOException {
    Injector injector = Guice.createInjector();
    HeadlessRenderer renderer = injector.getInstance(HeadlessRenderer.class);
    HttpServletResponse response = createNiceMock(HttpServletResponse.class);
    ServletOutputStream outputStream = fakeServletOutputStream();


    // The script to expect for our mock.
    response.setStatus(HttpServletResponse.SC_OK);
    expect(response.getOutputStream())
        .andReturn(outputStream);

    response.setContentType(injector.getInstance(Json.class).contentType());

    replay(response);

    Song maybeByTheInkspots = new Song(MAYBE, INK_SPOTS, SONG_LENGTH_MAYBE);
    renderer.render(response, Reply.with(maybeByTheInkspots).as(Json.class));

    verify(response);

    String output = outputStream.toString();
    assert output.contains(MAYBE);
    assert output.contains(INK_SPOTS);
    assert output.contains("" + SONG_LENGTH_MAYBE);

    // Now the real test, unmarshall it back into Java.
    Song song = injector.getInstance(Json.class)
        .in(new ByteArrayInputStream(output.getBytes()), Song.class);

    assert maybeByTheInkspots.hashCode() == song.hashCode();
    assert maybeByTheInkspots.equals(song);
  }

  @Test
  public void xmlReplyWithHeaders() throws IOException {
    Injector injector = Guice.createInjector();
    HeadlessRenderer renderer = injector.getInstance(HeadlessRenderer.class);
    HttpServletResponse response = createNiceMock(HttpServletResponse.class);
    ServletOutputStream outputStream = fakeServletOutputStream();


    ImmutableMap<String, String> headerMap = ImmutableMap.of(
        X_MY_HEADER, X_MY_HEADER_VAL,
        X_YOUR_HEADER, X_YOUR_HEADER_VAL
        );

    // The script to expect for our mock.
    response.setStatus(HttpServletResponse.SC_OK);
    expect(response.getOutputStream())
        .andReturn(outputStream);

    response.setContentType(injector.getInstance(Xml.class).contentType());
    response.setHeader(X_MY_HEADER, X_MY_HEADER_VAL);
    response.setHeader(X_YOUR_HEADER, X_YOUR_HEADER_VAL);

    replay(response);

    Song maybeByTheInkspots = new Song(MAYBE, INK_SPOTS, SONG_LENGTH_MAYBE);
    renderer.render(response, Reply.with(maybeByTheInkspots)
        .as(Xml.class)
        .headers(headerMap)
    );

    verify(response);

    String output = outputStream.toString();
    assert output.contains(MAYBE);
    assert output.contains(INK_SPOTS);
    assert output.contains("" + SONG_LENGTH_MAYBE);

    // Now the real test, unmarshall it back into Java.
    Song song = injector.getInstance(Xml.class)
        .in(new ByteArrayInputStream(output.getBytes()), Song.class);

    assert maybeByTheInkspots.hashCode() == song.hashCode();
    assert maybeByTheInkspots.equals(song);
  }

  private static class Song {
    private String name;
    private String artist;
    private int length;

    // Needed for Jackson (crappy)
    public Song() {
    }

    public Song(String name, String artist, int length) {
      this.name = name;
      this.artist = artist;
      this.length = length;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getArtist() {
      return artist;
    }

    public void setArtist(String artist) {
      this.artist = artist;
    }

    public int getLength() {
      return length;
    }

    public void setLength(int length) {
      this.length = length;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Song song = (Song) o;

      if (length != song.length) return false;
      if (artist != null ? !artist.equals(song.artist) : song.artist != null) return false;
      if (name != null ? !name.equals(song.name) : song.name != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (artist != null ? artist.hashCode() : 0);
      result = 31 * result + length;
      return result;
    }
  }

  private static ServletOutputStream fakeServletOutputStream() {
    return new FakeServletOutputStream();
  }
}
