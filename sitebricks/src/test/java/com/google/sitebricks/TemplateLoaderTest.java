package com.google.sitebricks;

import com.google.inject.Provider;
import com.google.sitebricks.compiler.PluggableCompilers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;

import static org.easymock.EasyMock.*;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TemplateLoaderTest {
    private static final String CLASSES_AND_TEMPLATES = "classesAndTemplates";

    @DataProvider(name = CLASSES_AND_TEMPLATES)
    public Object[][] get() {
        return new Object[][] {
            { MyXmlPage.class },
            { My.class },
            { MyXhtml.class },
            { MyHtml.class },
        };
    }

    @Test(dataProvider = CLASSES_AND_TEMPLATES)
    public final void loadExplicitXmlTemplate(final Class<MyXmlPage> pageClass) {
        String template = new TemplateLoader(createMock(PluggableCompilers.class), null)
                    .load(pageClass).getText();

        assert null != template : "no template found!";
        template = template.trim();
        assert template.startsWith("<xml>") && template.endsWith("</xml>"); //a weak sauce test
    }


    @Test
    public void testItShouldLoadShowValueFromWebInf() {
        ServletContext ctx = createMock(ServletContext.class);

        // we are telling that WEB-INF folder contains MetaInfPage.html
        String realPath = TemplateLoaderTest.class.getResource("My.xml").getPath();


        expect(ctx.getRealPath("MetaInfPage.html")).andReturn("unknown"); 
        expect(ctx.getRealPath("MyMetaInfPage.html")).andReturn("unknown");
        expect(ctx.getRealPath("/WEB-INF/MyMetaInfPage.html")).andReturn("unknown");
        expect(ctx.getRealPath("/WEB-INF/MetaInfPage.html")).andReturn(realPath);

        replay(ctx);
        String template = new TemplateLoader(createMock(PluggableCompilers.class), new MockServletContextProvider(ctx)).load(MyMetaInfPage.class).getText();
        verify(ctx);
        
        assert  null != template : "no template found!";
        assert  template.contains("hello") : "template was not loaded correctly?";
    }

    @Show("MetaInfPage.html")
    public static class MyMetaInfPage { }

    @Show("My.xml")
    public static class MyXmlPage { }



    public static class My { }
    public static class MyXhtml { }
    public static class MyHtml { }

    class MockServletContextProvider implements Provider<ServletContext> {
        private final ServletContext ctx;

        public MockServletContextProvider(ServletContext ctx) {
          this.ctx = ctx;
        }

        public ServletContext get() {
          return ctx;
        }
    }
}
