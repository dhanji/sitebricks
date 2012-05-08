package com.google.sitebricks.example;


import com.google.sitebricks.At;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;


/**
 * @author Peter Knego
 */
@At("/hiddenfieldmethod")
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

  @Patch
  public void patch() {
    putMessage = "Submitted via PATCH";
  }

  @Post
  public void post() {
    putMessage = "Submitted via POST";
  }
}
