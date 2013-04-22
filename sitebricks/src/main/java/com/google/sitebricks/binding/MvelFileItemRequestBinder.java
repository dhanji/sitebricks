package com.google.sitebricks.binding;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcip.annotations.Immutable;

import org.apache.commons.fileupload.FileItem;
import org.mvel2.PropertyAccessException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.rendering.Strings;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@Singleton
public class MvelFileItemRequestBinder implements RequestBinder<FileItem> {
  private final Evaluator evaluator;
  private final Provider<FlashCache> cacheProvider;
  private final Logger log = Logger.getLogger(MvelFileItemRequestBinder.class.getName());

  private static final String VALID_BINDING_REGEX = "[\\w\\.$]*";

  @Inject
  public MvelFileItemRequestBinder(Evaluator evaluator, Provider<FlashCache> cacheProvider) {
    this.evaluator = evaluator;
    this.cacheProvider = cacheProvider;
  }

  public void bind(Request<FileItem> request, Object o) {

    final Multimap<String, FileItem> map = request.params();

    //bind iteratively (last incoming param-value per key, gets bound)
    for (Map.Entry<String, Collection<FileItem>> entry : map.asMap().entrySet()) {
      String key = entry.getKey();

      // If there are multiple entry, then this is a collection bind:
      final Collection<FileItem> values = entry.getValue();

      // We guard against expression-injection with a regex validator.
      if (!validate(key))
        return;

      Object value;

      if (values.size() > 1) {
        value = Lists.newArrayList(values);
        bindValueToBound(key, o, value);
      } else {
        
        // If there is only one value, bind as per normal
        FileItem fileItem = Iterables.getOnlyElement(values);   //choose first (and only value)

        if (! fileItem.isFormField()) {
            value = fileItem.get();
            bindValueToBound(key, o, value);
            // TODO(eric) there may be a better way to bind the contentType, size...
            bindValueToBound(key + "Name", o, fileItem.getName());
            bindValueToBound(key + "Size", o, fileItem.getSize());
            bindValueToBound(key + "ContentType", o, fileItem.getContentType());
        }
        else {
            
            String rawValue = fileItem.getString();
    
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
            } else {
                value = rawValue;
            }
        
            bindValueToBound(key, o, value);

        }
        
      }
      
    }

  }
  
  private void bindValueToBound(String key, Object o, Object value) {
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
