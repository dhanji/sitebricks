package com.google.sitebricks.example;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.i18n.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class I18n {
  // These would typically be provided by a translated set resource bundle (external).
  public static final String HELLO = "hello";
  public static final String HELLO_IN_FRENCH = "Bonjour misieu ${person}!";


  private final MyMessages messages;
  private final HttpServletRequest request;

  private String name;

  @Inject
  public I18n(HttpServletRequest request, MyMessages messages) {
    this.messages = messages;
    this.request = request;
  }


  public String getMessage() {
    // only evaluate our message if the user has entered her name.
    return null == name ? "" : messages.hello(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  public Locale getLocale() {
    return request.getLocale();
  }

  /**
   * This is the i18N message interface. By default we use the messages provided
   * in the annotation. You can customize these by using the localize() rule in
   * your sitebricks module with different resource bundles and locales.
   *
   * Note that these messages can be given arguments and even exposed directly to
   * your template if you so wish, you have to invoke the method with parens like
   * so:
   *
   * <!-- in html template -->
   *
   *  ${messages.hello("Friend")}
   *
   * Will invoke the hello() method below with a string argument "Friend". At runtime
   * this will produce localized output in whatever locale the browser requests and
   * is available in your translation sets. 
   */
  public static interface MyMessages {
    @Message(message = "Hello there ${person}!")
    String hello(@Named("person") String name);
  }
}
