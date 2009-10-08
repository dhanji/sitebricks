package com.google.sitebricks.rendering.control;

import com.google.inject.Provider;
import com.google.sitebricks.MvelEvaluator;
import com.google.sitebricks.Respond;
import com.google.sitebricks.RespondersForTesting;
import com.google.sitebricks.binding.FlashCache;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class ChooseWidgetTest {

    @Test
    public final void chooseWidgetSelectTagRender() {

        Respond respond = RespondersForTesting.newRespond();
        final String s1 = "aString";
        final String s2 = "anotherString";
        final String s3 = "aThirdStr";
        final List<String> boundTo = Arrays.asList(s1, s2, s3);

        @SuppressWarnings("unchecked")
        final Provider<FlashCache> cacheProvider = createMock(Provider.class);
        final FlashCache cache = createMock(FlashCache.class);

        expect(cacheProvider.get())
              .andReturn(cache);

        cache.put("strings", boundTo);

        replay(cacheProvider, cache);


        final ChooseWidget widget = new ChooseWidget(new ProceedingWidgetChain(), "from=strings, bind=choice", new MvelEvaluator());
        widget.setCache(cacheProvider);

        widget.render(new HashMap<String, Object>() {{
                    put("strings", boundTo);
                }}, respond);


        //assert the validity of the text tag:
        String tag = respond.toString();

//        System.out.println(tag);
        assert tag.startsWith("<select name=\"choice\">");
        assert tag.contains(String.format("<option value=\"[C/strings/%d\">", s1.hashCode()));
        assert tag.contains(String.format("<option value=\"[C/strings/%d\">", s2.hashCode()));
        assert tag.contains(String.format("<option value=\"[C/strings/%d\">", s3.hashCode()));
        assert tag.endsWith("</select>");

        verify(cacheProvider, cache);
    }
}