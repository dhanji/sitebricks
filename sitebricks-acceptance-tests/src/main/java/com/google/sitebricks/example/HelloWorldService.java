package com.google.sitebricks.example;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.rendering.Templates;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Show("HelloWorld.html") @Service
public class HelloWorldService {
  public static final String HELLO_MSG = "Hello from google-sitebricks!";

  private final Templates templates;
  private String message = HELLO_MSG;

  @Inject
  public HelloWorldService(Templates templates) {
    this.templates = templates;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  // Some deterministic mangled representation of the input.
  public String mangle(String s) {
    return "" + s.hashCode();
  }

  @Get
  public Reply<?> get() {
    return Reply.with(this).template(HelloWorldService.class);
  }

  @At("/direct") @Get
  public Reply<?> getDirect() {
    return Reply.with(templates.render(HelloWorldService.class, this));
  }
}