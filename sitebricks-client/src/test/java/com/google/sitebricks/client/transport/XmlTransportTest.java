package com.google.sitebricks.client.transport;

import com.thoughtworks.xstream.XStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Unit test for the xml transport supported out of the box.
 */
public class XmlTransportTest {
  private static final String ROBOTS = "robots";

  @DataProvider(name = ROBOTS)
  Object[][] objects() {
    return new Object[][] {
        { new Robot("megatron", new Date(), 333, 12887L, null) },
        { new Robot("meg aoskdoaks", new Date(192839), 3, 12887L,
            new Robot("egatron", new Date(), 2193833, 12312887L, null)) },
        { new Robot("iaisdja aijsd", new Date(1293891283), 333, 12887L, null) },
    };
  }

  @Test(dataProvider = ROBOTS)
  public final void xmlTransport(Robot robot) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new XStreamXmlTransport(new XStream()).out(out, Robot.class, robot);
    Robot in = new XStreamXmlTransport(new XStream()).in(new ByteArrayInputStream(out.toByteArray()), Robot.class);

    assert robot.equals(in) : "Xml transport was not balanced";
  }

  public static class Robot {
    public Robot(String name, Date time, int age, long looong, Robot pet) {
      this.name = name;
      this.time = time;
      this.age = age;
      this.looong = looong;
      this.pet = pet;
    }

    private String name;
    private Date time;
    private int age;
    private long looong = 123L;
    private Robot pet;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Robot that = (Robot) o;

      if (age != that.age) return false;
      if (looong != that.looong) return false;
      if (name != null ? !name.equals(that.name) : that.name != null) return false;
      if (pet != null ? !pet.equals(that.pet) : that.pet != null) return false;
      if (time != null ? !time.equals(that.time) : that.time != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (time != null ? time.hashCode() : 0);
      result = 31 * result + age;
      result = 31 * result + (int) (looong ^ (looong >>> 32));
      result = 31 * result + (pet != null ? pet.hashCode() : 0);
      return result;
    }
  }
}
