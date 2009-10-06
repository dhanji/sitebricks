package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import net.jcip.annotations.Immutable;

import java.util.Set;

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