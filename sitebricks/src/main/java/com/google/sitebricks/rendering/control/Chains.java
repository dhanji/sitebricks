package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public final class Chains {
    private Chains() {
    }

    public static WidgetChain terminal() {
        return new TerminalWidgetChain();
    }

    public static WidgetChain singleton(Renderable widget) {
        return new SingletonWidgetChain(widget);
    }

    public static WidgetChain proceeding() {
        return new ProceedingWidgetChain();
    }
}
