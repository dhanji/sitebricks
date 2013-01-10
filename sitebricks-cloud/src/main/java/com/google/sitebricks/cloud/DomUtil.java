package com.google.sitebricks.cloud;

import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class DomUtil {
  private static final Map<String, String> POM_NAMESPACE;
  static {
    POM_NAMESPACE = new HashMap<String, String>();
    POM_NAMESPACE.put("m", "http://maven.apache.org/POM/4.0.0");
  }

  @SuppressWarnings("unchecked")
  static List<Node> select(Node document, String expression) {
    DefaultXPath expr = new DefaultXPath(expression);
    expr.setNamespaceURIs(POM_NAMESPACE);

    return expr.selectNodes(document);
  }
}
