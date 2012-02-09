package com.google.sitebricks.compiler.template;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.ToolManager;

public class VelocityContextProvider implements Provider<VelocityContext> {

  @Override
  @Singleton
  public VelocityContext get() {
    try {
      ToolManager velocityToolManager = new ToolManager();
      velocityToolManager.configure("velocity-tools.xml");
      return new VelocityContext(velocityToolManager.createContext());
    } catch (RuntimeException e) {
      return new VelocityContext();
    }
  }
}
