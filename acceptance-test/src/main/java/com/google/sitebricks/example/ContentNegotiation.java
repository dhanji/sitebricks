package com.google.sitebricks.example;


import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.negotiate.Accept;


@At("/conneg")
public class ContentNegotiation {

  // By default we serve gif, hehehehe.
  private String content = "GIF";

  @Get @Accept("image/jpeg")
  public void jpeg() {
    content = "JPEG";
  }


  @Get @Accept("image/png")
  public void png() {
    content = "PNG";
  }

  public String getContent() {
    return content;
  }
}
