package com.google.sitebricks.rendering.resource;

import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;
import com.google.sitebricks.Export;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ThreadSafe
@Singleton
class ClasspathResourcesService implements ResourcesService {
  private final Map<String, Resource> resources = new MapMaker().makeMap();

  private static final AtomicReference<Map<String, String>> mimes =
      new AtomicReference<Map<String, String>>();

  private static final String DEFAULT_MIME = "__defaultMimeType";

  public ClasspathResourcesService() {
    if (null == mimes.get()) {
      final Properties properties = new Properties();
      try {
        properties.load(
            ClasspathResourcesService.class.getResourceAsStream("mimetypes.properties"));
      } catch (IOException e) {
        throw new ResourceLoadingException("Can't find mimetypes.properties", e);
      }

      //noinspection unchecked
      mimes.compareAndSet(null, (Map) properties);
    }
  }

  public void add(Class<?> clazz, Export export) {
    resources.put(export.at(), new Resource(export, clazz));
  }

  public Respond serve(String uri) {
    final Resource resource = resources.get(uri);

    //nothing registered
    if (null == resource) {
      return null;
    }

    //load and render resource to responder
    return new StaticResourceRespond(resource);
  }


  static String mimeOf(String file) {
    final Map<String, String> mimeTypes = mimes.get();
    for (Map.Entry<String, String> mime : mimeTypes.entrySet()) {
      if (file.matches(mime.getKey()))
        return mime.getValue();
    }

    //no match, use the default?
    return mimeTypes.get(DEFAULT_MIME);
  }

  private static class Resource {
    private final Export export;
    private final Class<?> clazz;
    private final String mimeType;

    private Resource(Export export, Class<?> clazz) {
      this.export = export;
      this.clazz = clazz;

      this.mimeType = mimeOf(export.resource());
    }

    public String toString() {
      return new StringBuilder()
          .append("Resource {")
          .append("export=")
          .append(export)
          .append(", class=")
          .append(clazz).append('}')

          .toString();
    }
  }

  private static class StaticResourceRespond implements Respond {
    private final Resource resource;

    public StaticResourceRespond(Resource resource) {
      this.resource = resource;
    }

    public String getContentType() {
      return resource.mimeType;
    }

    @Override
    public String toString() {
      //load and render
      List list;
      try {
        final InputStream stream = resource.clazz.getResourceAsStream(resource.export.resource());

        if (null == stream)
          throw new ResourceLoadingException(
              "Couldn't find static resource (did you spell it right?) specified by: "
                  + resource);

        list = IOUtils.readLines(stream);
      } catch (IOException e) {
        throw new ResourceLoadingException(
            "Error loading static resource specified by: " + resource, e);
      }

      StringBuilder buffer = new StringBuilder();
      for (Object o : list) {
        buffer.append((String) o);
      }

      return buffer.toString();
    }

    public void write(String text) {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public HtmlTagBuilder withHtml() {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public void write(char c) {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public void chew() {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public void writeToHead(String text) {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public void require(String requireString) {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public void redirect(String to) {
      throw new UnsupportedOperationException("Static resource responders can't be written to");
    }

    public String getRedirect() {
      return null;
    }

    public Renderable include(String argument) {
      return null;
    }

    public String getHead() {
      return null;
    }
  }
}
