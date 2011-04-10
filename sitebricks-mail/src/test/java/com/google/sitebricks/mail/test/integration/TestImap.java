package com.google.sitebricks.mail.test.integration;

import org.apache.james.imapserver.netty.IMAPServer;

/**
 * @author Mike Bain (mike@thealphatester.com)
 */
public class TestImap {
  public static void main(String args[]) {

    IMAPServer imapServer = new IMAPServer();


    imapServer.setPort(7878);

    try {
      imapServer.start();
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    System.out.println("'Imap server started on port: " + imapServer.getPort() + "'");
  }
}
