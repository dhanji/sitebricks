package info.sitebricks.data;

import com.google.sitebricks.Show;
import com.petebevin.markdown.MarkdownProcessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A single wiki page/document.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Show("Document.html")
public class Document {
  private static final DateFormat format = new SimpleDateFormat("MMM dd");

  public static final String HOME = "Home";
  private String author;
  private Date createdOn;
  private String topic;
  private String name; // a unique page-name, typically derived from the topic
  private String text; // markdown format.
  private MarkdownProcessor markdownProcessor;

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;

    // set the name too.
    this.name = topic.replaceAll("[|_!?&%$\"'#;:.,+\\\\(\\){}]+", "")
        .replaceAll("[ ]+", "-")
        .toLowerCase();
  }

  public String getName() {
    return name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String format(Date date) {
    return format.format(date);
  }

  public String markdown() {
    return markdownProcessor.markdown(text);
  }

  public void setTemporaryMarkdownProcessor(MarkdownProcessor temporaryMarkdownProcessor) {
    this.markdownProcessor = temporaryMarkdownProcessor;
  }
}
