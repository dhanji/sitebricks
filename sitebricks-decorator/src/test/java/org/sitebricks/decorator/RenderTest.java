package org.sitebricks.decorator;


import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sitebricks.extractor.ExtractResult;
import org.sitebricks.extractor.XhtmlExtractor;
import org.sonatype.guice.bean.containers.InjectedTestCase;

public class RenderTest extends InjectedTestCase {

  @Inject
  @Named("${basedir}/src/test/xhtml")
  private File xhtml;

  @Inject
  @Named("${basedir}/target/output")
  private File outputDirectory;  
  
  @Inject
  @Named("jsoup")
  private XhtmlExtractor xhtmlExtractor; 
  
  @Inject
  private Decorator skinner;
  
  public void testFoo() throws Exception {
    File skin = new File(xhtml,"template.html");
    File page = new File(xhtml,"page.html");
    ExtractResult er = xhtmlExtractor.extract(page);
    Map<String,Object> context = new HashMap<String,Object>();
    context.put("title", er.getTitle());
    context.put("body", er.getBody());
    context.put("head", er.getHead());
    outputDirectory.mkdirs();
    Writer writer = new FileWriter(new File(outputDirectory,"full.html"));
    skinner.decorate(skin, context, writer);
  }
}
