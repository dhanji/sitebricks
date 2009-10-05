package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public interface WidgetChain extends Renderable {
    WidgetChain addWidget(Renderable renderable);
}
