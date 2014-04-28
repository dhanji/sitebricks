package com.google.sitebricks.binding;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.rendering.Strings;
import net.jcip.annotations.Immutable;
import org.mvel2.PropertyAccessException;

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
public class MvelRequestBinder implements RequestBinder {
  private final Evaluator evaluator;
  private final Provider<FlashCache> cacheProvider;
  private final Logger log = Logger.getLogger(MvelRequestBinder.class.getName());

  private static final String VALID_BINDING_REGEX = "[\\w\\.$]*";

  @Inject
  public MvelRequestBinder(Evaluator evaluator, Provider<FlashCache> cacheProvider) {
    this.evaluator = evaluator;
    this.cacheProvider = cacheProvider;
  }

  public void bind(Request request, Object o) {
    final Multimap<String, String> map = request.params();

    //bind iteratively (last incoming param-value per key, gets bound)
    for (Map.Entry<String, Collection<String>> entry : map.asMap().entrySet()) {
      String key = entry.getKey();

      // If there are multiple entry, then this is a collection bind:
      final Collection<String> values = entry.getValue();

      // We guard against expression-injection with a regex validator.
      if (!validate(key))
        continue;

      Object value;

      if (values.size() > 1) {
        value = Lists.newArrayList(values);
      } else {
        // If there is only one value, bind as per normal
        String rawValue = Iterables.getOnlyElement(values);   //choose first (and only value)

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
    			  addContextAndThrow(o, key, value, e.getCause());
    		  }
        // Log missing property.
        if (log.isLoggable(Level.FINER)) {
          log.finer("A property [" + key +"] could not be bound,"
              + " but not necessarily an error.");
        }
      } catch (Exception e) {
        addContextAndThrow(o, key, value, e);
      }
    }
  }

	private void addContextAndThrow(Object bound, String key, Object value, Throwable cause) {
	  throw new RuntimeException(String.format(
	    "Problem setting [%s] on instance [%s] with value [%s]",
	    key, bound, value), cause);
	}

  // Linear collection search by hashcode
  private Object search(Collection<?> collection, String hashKey) {
    int hash = Integer.valueOf(hashKey);

    for (Object o : collection) {
      if (o.hashCode() == hash)
        return o;
    }

    //nothing found
    return null;
  }

  private boolean validate(String binding) {
    //Guards against expression-injection attacks.
    if (Strings.empty(binding) || !binding.matches(VALID_BINDING_REGEX)) {
      log.warning(
          "Binding expression (request/form parameter) contained invalid characters: " + binding
              + " (ignoring)");
      return false;
    }

    return true;
  }
  
}
