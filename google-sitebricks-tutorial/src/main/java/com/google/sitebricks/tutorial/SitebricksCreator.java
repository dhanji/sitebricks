package com.google.sitebricks.tutorial;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;

public class SitebricksCreator extends GuiceServletContextListener {
    @Override
    public Injector getInjector() {
        return Guice.createInjector(new SitebricksModule() {
            @Override
            protected void configureSitebricks() {
                scan(Example.class.getPackage());    //scan class Example's package and all descendants
            }
        });
    }
}

