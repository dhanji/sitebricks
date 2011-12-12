package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import net.jcip.annotations.NotThreadSafe;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 *
 *  Marker represents the end of a widget chain/branch
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
class TerminalWidgetChain implements WidgetChain {

    public void render(Object bound, Respond respond) { }

    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return Collections.emptySet();
    }

    public WidgetChain addWidget(Renderable renderable) { return this; }


}