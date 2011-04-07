package com.google.sitebricks.example;

import com.google.inject.Singleton;
import com.google.inject.stat.Stat;
import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.http.Get;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/")
@Show("index.html") @Singleton
public class Start {
  public static final String PAGE_LOADS = "page-loads";
  public static volatile String HELLO_MSG = "YOU SHOULD NEVER SEE THIS!";
  private String message = HELLO_MSG;

  @Stat(PAGE_LOADS)
  private final AtomicInteger pageLoads = new AtomicInteger();

  public String getMessage() {
    return message;
  }

  @Get void display() {
    pageLoads.incrementAndGet();
  }
}
