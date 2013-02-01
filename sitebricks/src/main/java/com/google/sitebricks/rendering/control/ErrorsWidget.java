package com.google.sitebricks.rendering.control;

import java.util.Collections;
import java.util.Set;

import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.SelfRendering;

/**
 * 
 */
@SelfRendering
@EmbedAs("errors")
public class ErrorsWidget implements Renderable {

    public ErrorsWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
    }

    public void render(Object bound, Respond respond) {
        if (! respond.getErrors().isEmpty()) {
            respond.write("<div class=\"errors\">");
            respond.write("<ul>");
            for (String error: respond.getErrors()) {
                respond.write("<li>" + error + "</li>");
            }
            respond.write("</ul>");
            respond.write("</div");
        }
    }

    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return Collections.emptySet();
    }

}
