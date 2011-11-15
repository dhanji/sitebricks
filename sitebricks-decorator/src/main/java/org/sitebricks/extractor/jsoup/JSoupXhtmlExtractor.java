package org.sitebricks.extractor.jsoup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sitebricks.extractor.ExtractResult;
import org.sitebricks.extractor.XhtmlExtractor;

@Singleton
@Named("jsoup")
public class JSoupXhtmlExtractor implements XhtmlExtractor {

  public ExtractResult extract(File xhtml) throws IOException {
    Document document = Jsoup.parse(xhtml, "UTF-8");
    return extract(document);
  }

  public ExtractResult extract(String xhtml) {
    Document document = Jsoup.parse(xhtml, "UTF-8");
    return extract(document);
  }
  
  public ExtractResult extract(Document document) {
    //
    // Title
    //
    String title = document.head().select("title").text();
    
    //
    // Body
    //
    String body = document.body().html();
    
    //
    // Head (without the title element)
    //
    String head = document.select("head > :not(title)").outerHtml();
    
    //
    // Links
    //
    List<String> links = new ArrayList<String>();
    for (Element e : document.select("a[href]")) {
      links.add(e.attr("href"));
    }
    
    return new ExtractResult(title, head, body, links);
  }
}
