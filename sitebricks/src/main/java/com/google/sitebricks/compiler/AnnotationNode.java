package com.google.sitebricks.compiler;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;

/**
 Based on jsoup.nodes.TextNode by Jonathan Hedley, jonathan@hedley.net
 AnnotationNode is for Sitebricks text annotations such as
 @Repeat(...)  or @ShowIf(true)<div ... />
 */
public class AnnotationNode extends TextNode {
    static final String ANNOTATION_KEY = "_annokey";
    static final String ANNOTATION_CONTENT = "_annocontent";
    static final String ANNOTATION = "_annotation";

    /**
     Create a new AnnotationNode representing the supplied (unencoded) text).

     @param annotation raw text
     @param baseUri base uri
     @see #createFromEncoded(String, String)
     */
    public AnnotationNode(String annotation, String baseUri) {
        super(annotation, baseUri);
        this.annotation(annotation);
    }

    public AnnotationNode(String annotation) {
        super(annotation, "");
        this.annotation(annotation);
    }
  
    public String nodeName() {
        return "#annotation";
    }

    /**
     * Set the annotation of this node.
     * @param annotation raw annotation
     * @return this, for chaining
     */
    public AnnotationNode annotation(String annotation) {
        this.attr(ANNOTATION, annotation);
        String[] kc = AnnotationParser.extractKeyAndContent(annotation);
        this.attr(ANNOTATION_KEY, kc[0]);
        this.attr(ANNOTATION_CONTENT, kc[1]);
        return this;
    }

    public Node apply (Node annotate) {
        annotate.attr(ANNOTATION, this.attr(ANNOTATION));
        annotate.attr(ANNOTATION_KEY, this.attr(ANNOTATION_KEY));
        annotate.attr(ANNOTATION_CONTENT, this.attr(ANNOTATION_CONTENT));

        return annotate;
    }

}
