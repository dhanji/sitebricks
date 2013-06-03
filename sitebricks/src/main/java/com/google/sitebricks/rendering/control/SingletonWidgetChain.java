package com.google.sitebricks.rendering.control;

import java.util.Set;

import net.jcip.annotations.Immutable;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
class SingletonWidgetChain implements WidgetChain {
    private final Renderable widget;

    public SingletonWidgetChain(Renderable widget) {
        this.widget = widget;
    }

    public void render(Object bound, Respond respond) {
        widget.render(bound, respond);
    }

    public WidgetChain addWidget(Renderable renderable) {
        throw new IllegalStateException("Cannot add children to singleton widget chain");
    }

    public synchronized <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return widget.collect(clazz);
    }
}