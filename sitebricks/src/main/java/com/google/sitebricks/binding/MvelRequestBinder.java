package com.google.sitebricks.binding;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.rendering.Strings;
import net.jcip.annotations.Immutable;
import org.mvel2.PropertyAccessException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@Singleton
class MvelRequestBinder implements RequestBinder {
  private final Evaluator evaluator;
  private final Provider<FlashCache> cacheProvider;
  private final Logger log = Logger.getLogger(MvelRequestBinder.class.getName());

  private static final String VALID_BINDING_REGEX = "[\\w\\.$]*";

  @Inject
  public MvelRequestBinder(Evaluator evaluator, Provider<FlashCache> cacheProvider) {
    this.evaluator = evaluator;
    this.cacheProvider = cacheProvider;
  }

  public void bind(HttpServletRequest request, Object o) {
    @SuppressWarnings("unchecked")
    final Map<String, String[]> map = request.getParameterMap();

    //bind iteratively (last incoming param-value per key, gets bound)
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      String key = entry.getKey();

      // If there are multiple entry, then this is a collection bind:
      final String[] values = entry.getValue();

      validate(key);

      Object value;

      if (values.length > 1) {
        value = Lists.newArrayList(values);
      } else {
        // If there is only one value, bind as per normal
        String rawValue = values[0];   //choose first (and only value)

        //bind from collection?
        if (rawValue.startsWith(COLLECTION_BIND_PREFIX)) {
          final String[] binding = rawValue.substring(COLLECTION_BIND_PREFIX.length()).split("/");
          if (binding.length != 2)
            throw new InvalidBindingException(
                "Collection sources must be bound in the form '[C/collection/hashcode'. "
                    + "Was the request corrupt? Or did you try to bind something manually"
                    + " with a key starting '[C/'? Was: " + rawValue);

          final Collection<?> collection = cacheProvider.get().get(binding[0]);

          value = search(collection, binding[1]);
        } else
          value = rawValue;
      }

      //apply the bound value to the page object property
      try {
        evaluator.write(key, o, value);
      } catch (PropertyAccessException e) {

        // Do some better error reporting if this is a real exception.
        if (e.getCause() instanceof InvocationTargetException) {
          Throwable cause = e.getCause().getCause();
          StackTraceElement[] stackTrace = cause.getStackTrace();
          throw new RuntimeException(String.format(
              "Exception [%s - \"%s\"] thrown by setter [%s] for value[%s]\n\nat %s\n"
              + "(See below for entire trace.)\n",
              cause.getClass().getSimpleName(),
              cause.getMessage(), key, value,
              stackTrace[0]), e);
        }
        // Log missing property.
        if (log.isLoggable(Level.FINE)) {
          log.fine(String.format("A property [%s] could not be bound,"
              + " but not necessarily an error.", key));
        }
      }
    }
  }

  //TODO optimize this to be aggressive based on collection type
  //Linear collection search by hashcode
  private Object search(Collection<?> collection, String hashKey) {
    int hash = Integer.valueOf(hashKey);

    for (Object o : collection) {
      if (o.hashCode() == hash)
        return o;
    }

    //nothing found
    return null;
  }

  private void validate(String binding) {
    //guard against expression-injection attacks
    // TODO use an optimized algorithm, rather than a regex?
    if (Strings.empty(binding) || !binding.matches(VALID_BINDING_REGEX))
      throw new InvalidBindingException(
          "Binding expression (request/form parameter) contained invalid characters: " + binding);
  }
}
