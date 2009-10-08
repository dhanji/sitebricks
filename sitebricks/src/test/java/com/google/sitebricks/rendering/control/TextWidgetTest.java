package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.MvelEvaluatorCompiler;
import static org.easymock.EasyMock.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TextWidgetTest {
    private static final String NAME_VALUES = "nameValues";
    private static final String MVEL_NAMES = "mvelNames";

    @DataProvider(name = NAME_VALUES)
    Object[][] getNameValues() {
        return new Object[][] {
                { "Dhanji" },
                { "Joe" },
                { "Josh" },
        };
    }

    @DataProvider(name = MVEL_NAMES)    //creates a path for expr: ${names.first}
    Object[][] getMvelNames() {
        return new Object[][] {
                {  new TestBackingType(new ANestedType("Dhanji", "NotDhanji")), "Dhanji" },
                {  new TestBackingType(new ANestedType("Joei", "NotDhanji")), "Joei" },
                {  new TestBackingType(new ANestedType("Jill", "NotDhanji")), "Jill" },


        };
    }

    public static class TestBackingType {
        private ANestedType names;

        public TestBackingType(ANestedType names) {
            this.names = names;
        }

        public ANestedType getNames() {
            return names;
        }
    }

    public static class ANestedType {
        private String first;
        private String second;

        public ANestedType(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    @Test(dataProvider = NAME_VALUES)
    public final void renderATemplateWithObject(final String name) throws ExpressionCompileException {
        final String[] out = new String[1];
        Respond respond = createMock(Respond.class);
        respond.write("Hello " + name);


        replay(respond);

        new TextWidget("Hello ${name}", new MvelEvaluatorCompiler(ATestType.class))
                .render(new ATestType(name), respond);

//        assert ("Hello " + name).equals(out[0]) : "template render failed: " + out[0];
        verify(respond);
    }

    public static class ATestType {
        private String name;

        public ATestType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test(dataProvider = MVEL_NAMES)
    public final void renderATemplateWithObjectGraph(final TestBackingType data, String name) throws ExpressionCompileException {
        final String[] out = new String[1];
        Respond respond = createMock(Respond.class);

        respond.write("Hello " + name);

        replay(respond);

        new TextWidget("Hello ${names.first}", new MvelEvaluatorCompiler(TestBackingType.class))
                .render(data, respond);

//        assert ("Hello " + name).equals(out[0]) : "template render failed: " + out[0];
        verify(respond);
    }
}
