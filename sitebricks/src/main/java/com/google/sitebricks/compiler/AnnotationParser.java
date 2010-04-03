package com.google.sitebricks.compiler;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import static com.google.sitebricks.compiler.Dom.WIDGET_ANNOTATION_REGEX;
import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION;
import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION_KEY;
import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION_CONTENT;

import java.util.regex.Matcher;


/**
 * @author shawn
 */
public class AnnotationParser {

  // TODO - value will be the type of the annotation ...

  public static String readAnnotation(String text) {
    String annotation = null;    
    final Matcher matcher = WIDGET_ANNOTATION_REGEX.matcher(text);
      if (matcher.find())
        annotation = matcher.group().trim();
    return annotation;
  }

  public static String readAnnotation(Node node) {
      if (null == node) return null;

      Node preceding = node.previousSibling();

      //if this is a text node, then match for annotations
      return readAnnotation(node.outerHtml());        
  }

  public static String stripAnnotation(String text) {
      return Dom.stripAnnotation(text);  // TODO - move it here?
  }

  /**
   * @param annotation A string representing an unparsed annotation of the form: <pre>
   * "{@literal @}MyAnn(property = [expr], ...)"</pre>
   * @return A partially parsed array following this structure:<pre>
   *  [0] -> "MyAnn" <br/>
   *  [1] -> "prop = [expr], ..."
   * </pre>
   */
  public static String[] extractKeyAndContent(String annotation) {
      return Dom.extractKeyAndContent(annotation); // TODO - move it here?
  }



}
