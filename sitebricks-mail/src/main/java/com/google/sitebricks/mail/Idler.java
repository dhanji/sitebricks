package com.google.sitebricks.mail;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
interface Idler {
  void done();

  void disconnect();

  void idleEnd();

  void idleStart();
}
