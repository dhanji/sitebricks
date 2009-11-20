package com.google.sitebricks.example;


import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;


/**
 * @author Peter Knego
 */

public class HiddenFieldMethod {
  private String text = "initial textfield value";

  private String putMessage = "";

  public String getPutMessage() {
    return putMessage;
  }

  public void setPutMessage(String putMessage) {
    this.putMessage = putMessage;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Put
  public void put() {
    putMessage = "Submitted via PUT";
  }

  @Post
  public void post() {
    putMessage = "Submitted via POST";
  }
}
