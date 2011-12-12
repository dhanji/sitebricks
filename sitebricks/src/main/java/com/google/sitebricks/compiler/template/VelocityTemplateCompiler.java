package com.google.sitebricks.compiler.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;

public class VelocityTemplateCompiler {

    static {
    }

    public Renderable compile(final String templateContent) {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/velocity.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final VelocityEngine velocityEngine = new VelocityEngine(properties);

        return new Renderable() {

            @Override
            public void render(Object bound, Respond respond) {
                final VelocityContext context = new VelocityContext();
                context.put("page", bound);
                StringWriter writer = new StringWriter();
                velocityEngine.evaluate(context, writer, "", templateContent);
                respond.write(writer.toString());
            }

            @Override
            public <T extends Renderable> Set<T> collect(Class<T> clazz) {
                return ImmutableSet.of();
            }
        };
    }
}
