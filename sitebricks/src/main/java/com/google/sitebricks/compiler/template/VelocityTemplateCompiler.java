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

    @Inject
    public VelocityTemplateCompiler(VelocityEngineProvider provider) {
        this.provider = provider;
    }

    public Renderable compile(final String templateContent) {

        return new Renderable() {

            @Override
            public void render(Object bound, Respond respond) {
                final VelocityContext context = new VelocityContext();
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
