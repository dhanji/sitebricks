package com.google.sitebricks.rendering.control;

import com.google.inject.Injector;
import com.google.sitebricks.MvelEvaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.routing.PageBook;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class WidgetRegistryTest {
    private static final String WIDGETS_AND_KEYS = "widgetsAndKeys";

    @DataProvider(name = WIDGETS_AND_KEYS)
    public Object[][] get() {
        return new Object[][] {
            { "twidg", TextFieldWidget.class },
            { "teasdasxt", RepeatWidget.class },
            { "sastext", ShowIfWidget.class },
        };
    }

    @Test(dataProvider = WIDGETS_AND_KEYS)
    public final void storeRetrieveWidgets(final String key, final Class<Renderable> expected) throws ExpressionCompileException {
        final WidgetRegistry registry = new DefaultWidgetRegistry(new MvelEvaluator(), createNiceMock(PageBook.class), createNiceMock(Injector.class));
        registry.add(key, expected);

        Renderable widget = registry.newWidget(key, "some=expression", new ProceedingWidgetChain(), createMock(EvaluatorCompiler.class));

        assert expected.isInstance(widget) : "Wrong widget returned";
    }
}
