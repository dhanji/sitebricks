package com.google.sitebricks.rendering.control;

import com.google.inject.Inject;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import com.google.sitebricks.rendering.Decorated;
import com.google.sitebricks.routing.PageBook;

import java.util.Collections;
import java.util.Set;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 *
 */
public class DecorateWidget implements Renderable {

  @Inject private PageBook book;
  
  private ThreadLocal<Class<?>> templateClassLocal = new ThreadLocal<Class<?>>();
  
  public static String embedNameFor(Class<?> pageClass) {
    String name = pageClass.getName();
    int mark = name.indexOf("$$EnhancerByGuice");
    if (mark >= 0)
        name = name.substring(0, mark);
    return name.toLowerCase() + "-extend";
  }
  
  public DecorateWidget(WidgetChain chain, String expression, Evaluator evaluator){
    // do not need any of the compulsory constructor args
  }
  
  @Override
  public void render(Object bound, Respond respond) {

    Class<?> templateClass;
    Class<?> previousTemplateClass = templateClassLocal.get();
    try {
	    if (previousTemplateClass == null) {
	      if (respond instanceof EmbeddedRespond) {
	          WidgetChain widgetChain = ((EmbeddedRespond)respond).getWidgetChain();
	          if (widgetChain != null) {
	              // Render the widgetChain content here.
	              StringBuilderRespond sbrespond = new StringBuilderRespond(new Object());
	              EmbeddedRespond embedded = new EmbeddedRespond(null, null, sbrespond);
	              widgetChain.render(bound, embedded);
	          
	              // write the head and content to the real respond
	              respond.writeToHead(embedded.toHeadString());
	              respond.write(embedded.toString());
	              
	              return;
	          }
	      }
	      templateClass = bound.getClass();
	    }
	    else {
	      // get the extension subclass above the last
	      templateClass = nextExtensionSubclass(previousTemplateClass, bound.getClass());
	      if (templateClass == null) {
	        throw new IllegalStateException("Could not find subclass of " + previousTemplateClass.getName() + " with @Extension annotation.");
	      }
	    }
	    templateClassLocal.set(templateClass);
	    
	    // get the extension page by name
	    PageBook.Page page = book.forName(DecorateWidget.embedNameFor(templateClass));
	
	    // create a dummy respond to collect the output of the embedded page
	    StringBuilderRespond sbrespond = new StringBuilderRespond(new Object());
	    EmbeddedRespond embedded = new EmbeddedRespond(null, null, sbrespond);
	    page.widget().render(bound, embedded);
	
	    // write the head and content to the real respond
	    respond.writeToHead(embedded.toHeadString());
	    respond.write(embedded.toString());
	
	    // free some memory
	    embedded.clear();
    }
    finally {
      // we are finished with this extension
      if (previousTemplateClass == null) {
        templateClassLocal.set(null);
      }
    }
  }

  // recursively find the next subclass with an @Extension annotation
  private Class<?> nextExtensionSubclass(Class<?> previousTemplagteClass, Class<?> candidate) {
    if (candidate == previousTemplagteClass) {
      // terminate the recursion
      return null;
    }
    else if (candidate == Object.class) {
      // this should never happen - we should terminate recursion first
      throw new IllegalStateException("Did not find previous extension");
    }
    else {
      // check the super class for the result
      Class<?> result = nextExtensionSubclass(previousTemplagteClass, candidate.getSuperclass());
      if (result == null && candidate.isAnnotationPresent(Decorated.class)) {
        // this is the one - retreat!
        return candidate;
      }
      else {
        // we still have not found one
        return null;
      }
    }
  }

  @Override
  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return Collections.emptySet();
  }
}
