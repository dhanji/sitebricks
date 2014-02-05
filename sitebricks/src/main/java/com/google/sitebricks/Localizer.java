package com.google.sitebricks;

import com.google.sitebricks.locale.LocaleProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.MvelEvaluatorCompiler;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.compiler.Token;
import com.google.sitebricks.i18n.Message;
import com.google.sitebricks.rendering.Strings;

/**
 * A Utility that binds a localizable interface to its instance parameters.
 */
public class Localizer {
  private final Binder binder;
  private final Set<Localization> localizations;

  // These are the processed, individual message sets by locale.
  private final Map<String, Map<String, MessageDescriptor>> localizedValues = Maps.newHashMap();

  // A map to track if we have bound the proxy for a given i18n interface yet.
  private Set<Class<?>> i18nedSoFar = Sets.newHashSet();

  /**
   * A value object that represents the localization of an i18n  interface to a locale
   * and corresponding set of messages.
   */
  public static class Localization {
    // TODO(dhanji): Convert class reference to weak?
    private final Class<?> clazz;
    private final Locale locale;
    private final Map<String, String> messageBundle;

    public Localization(Class<?> clazz, Locale locale, Map<String, String> messageBundle) {
      this.clazz = clazz;
      this.locale = locale;
      this.messageBundle = messageBundle;
    }

    public Class<?> getClazz() {
        return this.clazz;
    }
    public Locale getLocale() {
        return this.locale;
    }
    public Map<String, String> getMessageBundle() {
        return this.messageBundle;
    }

  }

  static final Localization DEFAULT = new Localization(null, null, null);

  private Localizer(Binder binder, Set<Localization> localizations) {
    this.binder = binder;
    this.localizations = localizations;
  }

  public static void localizeAll(Binder binder, Set<Localization> localizations) {
    new Localizer(binder, localizations).localize();
  }

  private void localize() {
    for (Localization localization : localizations) {
      // First scan and ensure that all methods on the interface contain i18n params.
      bindMessages(localization);
    }

    // We're done with this so we don't need the set anymore.
    i18nedSoFar = null;
  }

  private void bindMessages(Localization localization) {
    Class<?> iface = localization.clazz;
    Map<String, MessageDescriptor> messages = Maps.newHashMap();

    for (Method method : iface.getMethods()) {
      Message message = method.getAnnotation(Message.class);

      check(null != message,
          "Found an i18n interface method missing @Message annotation: ", iface, method);

      if (null != message) {
        check(!Strings.empty(message.message()),
            "Empty @Message annotation is not allowed ", iface, method);
      }

      String template = localization.messageBundle.get(method.getName());
      check(null != template,
          "Provided resource bundle does not contain a localization for message: ", iface, method);
      check(String.class.equals(method.getReturnType()),
          "All i18n interface methods MUST return String: ", iface, method);

      int argumentCount = method.getParameterTypes().length;
      Map<String, Type> arguments = Maps.newLinkedHashMap();

      for (int i = 0; i < argumentCount; i++) {
        Annotation[] annotations = method.getParameterAnnotations()[i];

        check(annotations.length == 1,
            "Only @Named annotations are allowed on i18n method arguments: ", iface, method);
        if (annotations.length == 0) {
          continue;
        }

        check(Named.class.isInstance(annotations[0]),
            "Named annotation is missing from i18n interface method argument: ", iface, method);

        // Bind each argument to a template parameter a la Dynamic Finders.
        arguments.put(((Named) annotations[0]).value(), method.getParameterTypes()[i]);
      }

      // No point in throwing an NPE ourselves, but we want to keep processing errors so continue
      if (null == template || null == message) {
        continue;
      }

      // Compile arg names against message template to ensure it works.
      List<Token> tokens = null;
      try {
        MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(arguments);

        // Compile both the default message as well as the provided localized one.
        Parsing.tokenize(message.message(), compiler);
        tokens = Parsing.tokenize(template, compiler);
      } catch (ExpressionCompileException e) {
        check(false, "Compile error in i18n message template: \n  " + e.getError().getError() +
            " in expression " + e.getError().getExpression() +"\n\n  ...in: ", iface, method);
      }

      // OK now actually go through and build a map between method names and values.
      messages.put(method.getName(), new MessageDescriptor(tokens, arguments));
    }

    bindMessageProvider(iface, localization, messages);
  }

  @SuppressWarnings("unchecked") // We have a guarantee that Proxy will only return subtypes.
  private void bindMessageProvider(final Class<?> iface,
                                   Localization localization,
                                   Map<String, MessageDescriptor> messages) {

    // Add to the value map.
    localizedValues.put(createLocaleInterfaceKey(iface, localization.locale), messages);

    // Only need to bind the proxy once, for all locales.
    if (!i18nedSoFar.contains(iface)) {
      i18nedSoFar.add(iface);

      binder.bind((Class)iface).toProvider(new Provider() {

        // Wonderful Guice hack to get around not using assisted inject.
        @Inject
		final LocaleProvider localeProvider = null;

        // This is our delegate field that proxies the interface.
        private final Object instance = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] { iface }, new InvocationHandler() {

              /**
               * Returns the localized message bundle value, keyed by the method name invoked.
               */
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Locale locale = localeProvider.getLocale();
                Map<String, MessageDescriptor> messages = getMessagesWithFallback(locale);

                // Use default if we don't support the given locale.
                if (null == messages) {
                  messages = getMessagesWithFallback(Locale.getDefault());
                }

                MessageDescriptor descriptor = messages.get(method.getName());
                if (descriptor == null) {
                	throw new IllegalStateException("Could not find message '"
                			+ method.getName() + "' in " + messages);
                }
				return descriptor.render(args);
              }

			private Map<String, MessageDescriptor> getMessagesWithFallback(Locale locale) {
				String localeInterfaceKey = createLocaleInterfaceKey(iface, locale);
				Map<String, MessageDescriptor> result = localizedValues.get(localeInterfaceKey);
				if (result == null) {
					result = localizedValues.get(new Locale(locale.getLanguage()));
				}
				return result;
			}
          });

        // return our proxy here.
        public Object get() {
          return instance;
        }

      });
    }

  }

  private String createLocaleInterfaceKey(final Class<?> iface, Locale locale) {
    return locale.toString() + ":" + iface.getName();
  }

  private static class MessageDescriptor {
    private final List<Token> tokens;
    private final Map<String, Type> argumentTypes;

    private MessageDescriptor(List<Token> tokens, Map<String, Type> argumentTypes) {
      this.tokens = tokens;
      this.argumentTypes = argumentTypes;
    }

    public String render(Object[] args) {
      Map<String, Object> arguments = Maps.newHashMap();

      int i = 0;
      for (String name : argumentTypes.keySet()) {
        arguments.put(name, args[i]);
        i++;
      }

      return Parsing.render(tokens, arguments);
    }

  }

  private void check(boolean condition, String error, Class<?> key, Method method) {
    if (!condition) {
      binder.addError(error + "\n  at " + key.getName() + "." + method.getName() + "()\n");
    }
  }

  /**
   * Returns a localization value object describing the defaults specified in the @Message
   * annotations of the methods on the given i18n interface. The locale used is the system
   * default.
   */
  public static Localization defaultLocalizationFor(Class<?> iface) {
    Map<String, String> defaultMessages = Maps.newHashMap();

    for (Method method : iface.getMethods()) {
      Message msg = method.getAnnotation(Message.class);
      if (null != msg) {
        defaultMessages.put(method.getName(), msg.message());
      }
    }

    return new Localization(iface, Locale.getDefault(), defaultMessages);
  }

}
