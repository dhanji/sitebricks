package com.google.sitebricks.mail.webapp;

import com.google.sitebricks.At;
import com.google.sitebricks.Visible;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/")
public class Home {
  @Visible
  public String message = "hi";
}
