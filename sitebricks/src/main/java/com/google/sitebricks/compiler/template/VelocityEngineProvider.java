package com.google.sitebricks.compiler.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.velocity.app.VelocityEngine;

import com.google.inject.Provider;


public class VelocityEngineProvider implements Provider<VelocityEngine> {

    @Override
    @Singleton
    public VelocityEngine get() {
        Properties properties = new Properties();
        try {
            InputStream propertyStream = getClass().getResourceAsStream("/velocity.properties");
            if (propertyStream != null)
                properties.load(propertyStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new VelocityEngine(properties);
    }

}
