package com.google.sitebricks;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.TemplateCompiler;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.util.HashMap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TemplateLoaderTest {
  private static final String CLASSES_AND_TEMPLATES = "classesAndTemplates";

  @DataProvider(name = CLASSES_AND_TEMPLATES)
  public Object[][] get() {
    return new Object[][]{
        {MyXmlPage.class},
        {My.class},
        {MyXhtml.class},
        {MyHtml.class},
    };
  }

  private TemplateSystem templateSystem() {
    return new DefaultTemplateSystem(new HashMap<String, Class<? extends TemplateCompiler>>(), createNiceMock(Injector.class)) {
      @Override
      public String[] getTemplateExtensions() {
        return new String[]{"%s.html", "%s.xhtml", "%s.xml", "%s.txt", "%s.fml", "%s.dml",
            "%s.mvel"};
      }
    };
  }

  @Test(dataProvider = CLASSES_AND_TEMPLATES)
  public final void loadExplicitXmlTemplate(final Class<MyXmlPage> pageClass) {
    String template = new TemplateLoader(new MockServletContextProvider(createMock(ServletContext.class)),
        templateSystem()).load(pageClass, null).getText();
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
    expect(ctx.getRealPath("/WEB-INF/MetaInfPage.html")).andReturn(realPath);

    replay(ctx);
    String template = new TemplateLoader(new MockServletContextProvider(ctx),
        templateSystem()).load(MyMetaInfPage.class, null).getText();
    verify(ctx);

    assert null != template : "no template found!";
    assert template.contains("hello") : "template was not loaded correctly?";
  }

  @Test
  public void testItShouldLoadDefaultValueFromWebInf() {
    ServletContext ctx = createMock(ServletContext.class);

    // we are telling that WEB-INF folder contains MetaInfPage.html
    String realPath = TemplateLoaderTest.class.getResource("My.xml").getPath();

    expect(ctx.getRealPath("MyDefaultMetaInfPage.html")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.xhtml")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.xml")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.txt")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.fml")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.dml")).andReturn("unknown");
    expect(ctx.getRealPath("MyDefaultMetaInfPage.mvel")).andReturn("unknown");
    expect(ctx.getRealPath("/WEB-INF/MyDefaultMetaInfPage.html")).andReturn(realPath);

    replay(ctx);
    String template = new TemplateLoader(new MockServletContextProvider(ctx),
        templateSystem()).load(MyDefaultMetaInfPage.class, null).getText();
    verify(ctx);

    assert null != template : "no template found!";
    assert template.contains("hello") : "template was not loaded correctly?";
  }

  @Show("MetaInfPage.html")
  public static class MyMetaInfPage {
  }

  @Show()
  public static class MyDefaultMetaInfPage {
  }

  @Show("My.xml")
  public static class MyXmlPage {
  }


  public static class My {
  }

  public static class MyXhtml {
  }

  public static class MyHtml {
  }

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
