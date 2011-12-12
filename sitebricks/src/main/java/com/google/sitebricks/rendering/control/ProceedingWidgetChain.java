package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
class ProceedingWidgetChain implements WidgetChain {
    private final List<Renderable> widgets = new ArrayList<Renderable>();

    public void render(Object bound, Respond respond) {
        for (Renderable widget : widgets) {
            widget.render(bound, respond);
        }
    }

    public synchronized WidgetChain addWidget(Renderable renderable) {
        widgets.add(renderable);
        return this;
    }

    /**
     * This is an expensive method, never use it when live (used best at startup).
     *
     * @param clazz A class implementing {@code Renderable}.
     * @return Returns a set of widgets that match the given type in this widget chain.
     */
    public synchronized <T extends Renderable> Set<T> collect(Class<T> clazz) {
        Set<T> matches = new HashSet<T>();
        for (Renderable widget : widgets) {

            //add any matching classes to the set
            if (clazz.isInstance(widget))
                //noinspection unchecked
                matches.add((T) widget);

            //traverse down widget chains
            if (widget instanceof WidgetChain)
                matches.addAll(widget.collect(clazz));
        }

        return matches;
    }
}
