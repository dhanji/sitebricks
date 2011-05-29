package com.google.sitebricks;

import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import com.google.sitebricks.conversion.MvelConversionHandlers;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.RoutingDispatcher;
import org.mvel2.MVEL;

/**
 * Module encapsulates internal bindings for sitebricks. Can be installed multiple times.
 */
class SitebricksInternalModule extends AbstractModule {

  @Override
  protected void configure() {
    //set up MVEL namespace (when jarjar-ed, it will use the repackaged namespace)
    System.setProperty("mvel.namespace",
        MVEL.class.getPackage().getName().replace('.', '/') + "/");

    // Bind default content negotiation annotations
//    install(new ConnegModule()); TODO(dhanji): Fix this--we have to make SitebricksModule multi-installable

    //initialize startup services and routing modules
    install(PageBook.Routing.module());

    //development mode services
    if (Stage.DEVELOPMENT.equals(binder().currentStage())) {
      bind(PageBook.class).to(DebugModePageBook.class);
      bind(RoutingDispatcher.class).to(DebugModeRoutingDispatcher.class);
    }

    // use sitebricks converters in mvel
    requestInjection(new MvelConversionHandlers());
  }

  @Override
  public int hashCode() {
    return SitebricksInternalModule.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return SitebricksInternalModule.class.isInstance(obj);
  }
}
