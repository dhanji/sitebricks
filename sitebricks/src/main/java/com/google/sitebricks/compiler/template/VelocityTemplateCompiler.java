package com.google.sitebricks.compiler.template;

import java.io.StringWriter;
import java.util.Set;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;

public class VelocityTemplateCompiler {

    private final VelocityEngineProvider provider;
    private final VelocityContextProvider velocityContextProvider;

    @Inject
    public VelocityTemplateCompiler(VelocityEngineProvider provider, VelocityContextProvider velocityContextProvider) {
        this.provider = provider;
        this.velocityContextProvider = velocityContextProvider;
    }

    public Renderable compile(final String templateContent) {

        return new Renderable() {

            @Override
            public void render(Object bound, Respond respond) {
              VelocityContext context = velocityContextProvider.get();
                context.put("page", bound);
                StringWriter writer = new StringWriter();
                provider.get().evaluate(context, writer, "", templateContent);
                respond.write(writer.toString());
            }

            @Override
            public <T extends Renderable> Set<T> collect(Class<T> clazz) {
                return ImmutableSet.of();
            }
        };
    }
}
