package com.google.sitebricks.mail;

import com.google.common.collect.Sets;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A command/response handler for a single mail connection/user.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientHandler extends SimpleChannelHandler {
  private static final Logger log = LoggerFactory.getLogger(MailClientHandler.class);
  public static final String CAPABILITY_PREFIX = "* CAPABILITY";
  static final Pattern COMMAND_FAILED_REGEX =
      Pattern.compile("^[.] (NO|BAD) (.*)", Pattern.CASE_INSENSITIVE);
  static final Pattern SYSTEM_ERROR_REGEX = Pattern.compile("[*]\\s*bye\\s*system\\s*error\\s*",
      Pattern.CASE_INSENSITIVE);

  static final Pattern IDLE_ENDED_REGEX = Pattern.compile(".* OK IDLE terminated \\(success\\)\\s*",
      Pattern.CASE_INSENSITIVE);
  static final Pattern IDLE_EXISTS_REGEX = Pattern.compile("\\* (\\d+) exists\\s*",
      Pattern.CASE_INSENSITIVE);
  static final Pattern IDLE_EXPUNGE_REGEX = Pattern.compile("\\* (\\d+) expunge\\s*",
      Pattern.CASE_INSENSITIVE);

  private final Idler idler;

  private final CountDownLatch loginComplete = new CountDownLatch(2);
  private volatile boolean isLoggedIn = false;
  private volatile List<String> capabilities;
  private volatile FolderObserver observer;
  final AtomicBoolean idling = new AtomicBoolean();

  // Panic button.
  private volatile boolean halt = false;

  private final LinkedBlockingDeque<Error> errorStack = new LinkedBlockingDeque<Error>();
  private final Queue<CommandCompletion> completions =
      new ConcurrentLinkedQueue<CommandCompletion>();
  private volatile PushedData pushedData;

  public MailClientHandler(Idler idler) {
    this.idler = idler;
  }

  private static class PushedData {
    final Set<Integer> pushAdds = Collections.synchronizedSet(Sets.<Integer>newHashSet());
    final Set<Integer> pushRemoves = Collections.synchronizedSet(Sets.<Integer>newHashSet());

  }

  // DO NOT synchronize!
  public void enqueue(CommandCompletion completion) {
    completions.add(completion);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String message = e.getMessage().toString();

    if (SYSTEM_ERROR_REGEX.matcher(message).matches()) {
      log.warn("Disconnected by IMAP Server due to system error: {}", message);
      halt();

      // Disconnect abnormally. The user code should reconnect using the mail client.
      errorStack.push(new Error(completions.poll(), message));
      idler.disconnect();
      return;
    }

    try {
      log.debug("Message received [{}] from {}", e.getMessage(), e.getRemoteAddress());
      if (halt) {
        log.error("This mail client is halted but continues to receive messages, ignoring!");
        return;
      }
      if (message.startsWith(CAPABILITY_PREFIX)) {
        this.capabilities = Arrays.asList(
            message.substring(CAPABILITY_PREFIX.length() + 1).split("[ ]+"));
        loginComplete.countDown();
        return;
      }

      if (!isLoggedIn) {
        if (message.matches("[.] OK .*@.* \\(Success\\)")) { // TODO make case-insensitive
          log.trace("Authentication success.");
          isLoggedIn = true;
          loginComplete.countDown();
        } else {
          Matcher matcher = COMMAND_FAILED_REGEX.matcher(message);
          if (matcher.find()) {
            log.trace("Authentication failed");
            loginComplete.countDown();
            errorStack.push(new Error(null /* logins have no completion */, extractError(matcher)));
          }
        }
        // TODO handle auth failed
        return;
      }

      // Copy to local var as the value can change underneath us.
      FolderObserver observer = this.observer;
      if (idling.get()) {
        message = message.toLowerCase();

        if (IDLE_ENDED_REGEX.matcher(message).matches()) {
          idling.compareAndSet(true, false);
          // Now fire the events.
          PushedData data = pushedData;
          pushedData = null;
          observer.changed(data.pushAdds.isEmpty() ? null : data.pushAdds,
              data.pushRemoves.isEmpty() ? null : data.pushRemoves);
          return;
        }

        // Queue up any push notifications to publish to the client in a second.
        Matcher existsMatcher = IDLE_EXISTS_REGEX.matcher(message);
        boolean matched = false;
        if (existsMatcher.matches()) {
          int number = Integer.parseInt(existsMatcher.group(1));
          pushedData.pushAdds.add(number);
          pushedData.pushRemoves.remove(number);
          matched = true;
        } else {
          Matcher expungeMatcher = IDLE_EXPUNGE_REGEX.matcher(message);
          if (expungeMatcher.matches()) {
            int number = Integer.parseInt(expungeMatcher.group(1));
            pushedData.pushRemoves.add(number);
            pushedData.pushAdds.remove(number);
            matched = true;
          }
        }

        // Stop idling, when we get the stopped idling message we can publish shit.
        if (matched) {
          idler.done();
          return;
        }
      }

      complete(message);
    } catch (Exception ex) {
      CommandCompletion completion = completions.poll();
      if (completion != null)
        completion.error(message, ex);
      else
        log.error("Strange exception during mail processing (no completions available!): {}",
            message, ex);
      throw ex;
    }
  }

  private String extractError(Matcher matcher) {
    return (matcher.groupCount()) > 1 ? matcher.group(2) : matcher.group();
  }

  /**
   * This is synchronized to ensure that we process the queue serially.
   */
  private synchronized void complete(String message) {
    // This is a weird problem with writing stuff while idling. Need to investigate it more, but
    // for now just ignore it.
    if ("* BAD [CLIENTBUG] Invalid tag".equalsIgnoreCase(message)) {
      log.warn("Invalid tag warning, ignored.");
      return;
    }

    CommandCompletion completion = completions.peek();
    if (completion == null) {
      if ("+ idling".equalsIgnoreCase(message))
        log.debug("IDLE entered.");
      else
        log.error("Could not find the completion for message {} (Was it ever issued?)", message);
      return;
    }

    if (completion.complete(message)) {
      completions.poll();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught!", e.getCause());
  }

  public List<String> getCapabilities() {
    return capabilities;
  }

  boolean awaitLogin() {
    try {
      loginComplete.await();

      return errorStack.isEmpty(); // No error == success!
    } catch (InterruptedException e) {
      throw new RuntimeException("Interruption while awaiting server login", e);
    }
  }

  Error lastError() {
    return errorStack.pop();
  }

  /**
   * Registers a FolderObserver to receive events happening with a particular
   * folder. Typically an IMAP IDLE feature. If called multiple times, will
   * overwrite the currently set observer.
   */
  void observe(FolderObserver observer) {
    this.observer = observer;
    pushedData = new PushedData();
  }

  void halt() {
    halt = true;
  }

  public boolean isHalted() {
    return halt;
  }

  static class Error {
    final CommandCompletion completion;
    final String error;

    Error(CommandCompletion completion, String error) {
      this.completion = completion;
      this.error = error;
    }
  }
}
