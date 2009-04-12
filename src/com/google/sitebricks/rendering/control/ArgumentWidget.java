package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.Immutable;

import java.util.Set;

/**
 * Used to embed an argument inside an embedding widget (for later inclusion by the @Include widget).
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@SelfRendering @Immutable
class ArgumentWidget implements Renderable {
    private final WidgetChain widgetChain;
    private final String expression;
    private final Evaluator evaluator;

    public ArgumentWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
        this.widgetChain = widgetChain;
        this.expression = Parsing.stripQuotes(expression);
        this.evaluator = evaluator;
    }

    public void render(Object bound, Respond respond) {
        widgetChain.render(bound, respond);
    }

    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return widgetChain.collect(clazz);
    }

    /**
     *
     * @return Returns the embedded argument text. For example, in {@code @When("red")}, will return "red".
     *
     */
    public String getName() {
        return expression;
    }
}
