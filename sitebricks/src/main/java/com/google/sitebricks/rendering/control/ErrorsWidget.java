package com.google.sitebricks.rendering.control;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Localizer;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.i18n.ResourceBundle;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.SelfRendering;

@SelfRendering
@EmbedAs("errors")
public class ErrorsWidget implements Renderable {

    @Inject
    private Map<Class<?>, Map<Locale, Localizer.Localization>> localizationsMap;

    @Inject
    private Locale locale;

    public ErrorsWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
    }

    @Override
    public void render(Object bound, Respond respond) {
        if (!respond.getErrors().isEmpty()) {
            Localizer.Localization localization = null;
            ResourceBundle resourceBundle = bound.getClass().getAnnotation(ResourceBundle.class);
            if (resourceBundle != null) {
                Map<Locale, Localizer.Localization> localizerMap = localizationsMap.get(resourceBundle.value());
                if (localizerMap != null) {
                    localization = localizerMap.get(locale);
                }
                if (localization == null) {
                    localization = Localizer.defaultLocalizationFor(resourceBundle.value());
                }
            }
            respond.write("<div class=\"errors\">");
            respond.write("<ul>");
            for (String errorKey : respond.getErrors()) {
                String errorMessage = null;
                if (localization != null) {
                    errorMessage = localization.getMessageBundle().get(errorKey);
                }
                if (errorMessage != null) {
                    respond.write("<li>" + errorMessage + "</li>");
                }
                else {
                    respond.write("<li> !!! " + errorKey + " !!! </li>");
                }
            }
            respond.write("</ul>");
            respond.write("</div");
        }
    }

    @Override
    public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return Collections.emptySet();
    }

}
