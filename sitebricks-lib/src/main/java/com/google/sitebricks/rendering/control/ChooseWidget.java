package com.google.sitebricks.rendering.control;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.Immutable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable @SelfRendering
class ChooseWidget implements Renderable {
    private final WidgetChain widgetChain;
    private final Map<String, String> map;
    private final Evaluator evaluator;

    private volatile Provider<FlashCache> cache;

    public ChooseWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
        this.evaluator = evaluator;
        this.map = Parsing.toBindMap(expression);
        this.widgetChain = widgetChain;

        //TODO validate expression
    }

    public void render(Object bound, Respond respond) {
        final String from = map.get("from");

        Object o = evaluator.read(from, bound);

        if (!(o instanceof Collection))
            throw new IllegalArgumentException("@Choose widget's from argument MUST be of type java.util.Collection " +
                    "but was: " + (null == o ? "null" : o.getClass()));

        respond.write("<select name=\"");
        respond.write(map.get("bind"));
        respond.write("\">");

        Collection<?> collection = (Collection<?>) o;
        for (Object obj : collection) {
            respond.write("<option value=\"[C/");
            respond.write(from);
            respond.write('/');
            respond.write(Integer.toString(obj.hashCode()));
            respond.write("\">");

            widgetChain.render(obj, respond);
            respond.write("</option>");
        }

        respond.write("</select>");

        //store for later retrieval during binding
        cache.get().put(from, collection);
    }

    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return widgetChain.collect(clazz);
    }

    @Inject
    public void setCache(Provider<FlashCache> cache) {
        this.cache = cache;
    }
}
