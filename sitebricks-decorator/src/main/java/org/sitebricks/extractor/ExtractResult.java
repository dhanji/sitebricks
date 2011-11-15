package org.sitebricks.extractor;

import java.util.List;

public class ExtractResult {
  private String title;
  private String body;
  private String head;
  private List<String> links;

  public ExtractResult(String title, String head, String body, List<String> links) {
    this.title = title;
    this.head = head;
    this.body = body;
    this.links = links;
  }
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }


  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }
}