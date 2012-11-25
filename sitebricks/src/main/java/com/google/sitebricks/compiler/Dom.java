package com.google.sitebricks.compiler;

import java.util.regex.Matcher;

/**
 * Utility class helps XmlTemplateCompiler work with the DOM.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class Dom {
  static final String LINE_NUMBER_ATTRIBUTE = "__SitebricksSaxLineNumber";

  private Dom() {
  }

  static String stripAnnotation(String text) {
    final Matcher matcher = AnnotationParser.WIDGET_ANNOTATION_REGEX
        .matcher(text);

    //strip off the ending bit (annotation)
    if (matcher.find())
      return text.substring(0, matcher.start());

    return text;
  }

  /**
   * @param annotation A string reprenting an unparsed annotation of the form: <pre>
   *                                     "{@literal @}MyAnn(property = [expr], ...)"</pre>
   * @return A partially parsed array following this structure:<pre>
   *                  [0] -> "MyAnn" <br/>
   *                  [1] -> "prop = [expr], ..."
   *                 </pre>
   */
  static String[] extractKeyAndContent(String annotation) {
    final int index = annotation.indexOf('(');

    //there's no content
    if (index < 0)
      return new String[]{annotation.substring(1).toLowerCase(), ""};

    String content = annotation.substring(index + 1, annotation.lastIndexOf(')'));

    //normalize empty string to null
    if ("".equals(content))
      content = null;

    return new String[]{annotation.substring(1, index).toLowerCase(), content};
  }
}
