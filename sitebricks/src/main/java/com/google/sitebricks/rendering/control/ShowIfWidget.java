package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.rendering.EmbedAs;
import net.jcip.annotations.Immutable;

import java.util.Set;


/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable @EmbedAs("ShowIf")
class ShowIfWidget implements Renderable {
    private final WidgetChain widgetChain;
    private final String expression;
    private final Evaluator evaluator;

    public ShowIfWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
        this.widgetChain = widgetChain;
        this.expression = expression;
        this.evaluator = evaluator;
    }

    public void render(Object bound, Respond respond) {
        //messy =(
        final Object o = evaluator.evaluate(expression, bound);

        if ((Boolean) o)
            widgetChain.render(bound, respond);
    }


    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return widgetChain.collect(clazz);
    }
}
