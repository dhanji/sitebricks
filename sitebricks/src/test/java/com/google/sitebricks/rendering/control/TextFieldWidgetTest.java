package com.google.sitebricks.rendering.control;

import com.google.sitebricks.MvelEvaluator;
import com.google.sitebricks.Respond;
import static org.easymock.EasyMock.createMock;

import java.util.HashMap;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TextFieldWidgetTest {

//    @Test
    public final void textTagRender() {

        final String[] out = new String[1];
        Respond mockRespond = createMock(Respond.class);
        final String boundTo = "aString";

        new TextFieldWidget(new ProceedingWidgetChain(), "boundTo", new MvelEvaluator())
                .render(new HashMap<String, Object>() {{
                    put("boundTo", boundTo);
                }}, mockRespond);


        //assert the validity of the text tag:
        assert out[0] != null : "Nothing rendered!";
        String tag = out[0].trim();

        assert tag.startsWith("<input ");
        assert tag.endsWith(">");
        assert tag.contains("value=\"" + boundTo + "\"");
        assert tag.contains("name=\"boundTo\"");
        assert tag.contains("type=\"text\"");
    }
}
