package com.google.sitebricks.compiler;

import net.jcip.annotations.NotThreadSafe;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Utility class helps XmlTemplateCompiler work with the DOM.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class Dom {
    static final String LINE_NUMBER_ATTRIBUTE = "__WarpWidgetsSaxLineNumber";
    
    static final String FORM_TAG = "form";
    static final String XMLNS_ATTRIB_REGEX = " xmlns=\"[a-zA-Z0-9_+%;#/\\-:\\.]*\"";

    private Dom() {
    }


    //is this a form node?
    static boolean isForm(Node node) {
        return FORM_TAG.equals(node.getName());
    }

    static String stripAnnotation(String text) {
        final Matcher matcher = AnnotationParser.WIDGET_ANNOTATION_REGEX
                .matcher(text);

        //strip off the ending bit (annotation)
        if (matcher.find())
            return text.substring(0, matcher.start());

        return text;
    }

    static String readAnnotation(Node node) {
        String annotation = null;

        //if this is a text node, then match for annotations
        if (isText(node)) {
            final Matcher matcher = AnnotationParser.WIDGET_ANNOTATION_REGEX
                    .matcher(node.asXML());

            if (matcher.find()) {
                annotation = matcher.group();
            }
        }
        return annotation;
    }

    static String asRawXml(Element element) {
        final Element copy = element.createCopy();

        final Attribute lineNumber = copy.attribute(LINE_NUMBER_ATTRIBUTE);

        if (null != lineNumber)
            copy.remove(lineNumber);
        
        copy.remove(copy.getNamespace());

        return copy.asXML();
    }

    static boolean skippable(Attribute type) {
        if (null == type)
            return false;

        final String kind = type.getValue();
        return ( "submit".equals(kind) || "button".equals(kind) || "reset".equals(kind) || "file".equals(kind) );
    }

    /**
     *
     * @param list A list of dom4j attribs
     * @return Returns a mutable map parsed out of the dom4j attribute list
     */
    static Map<String, String> parseAttribs(List list) {
        Map<String, String> attrs = new LinkedHashMap<String, String>(list.size() + 4);

        for (Object o : list) {
            Attribute attribute = (Attribute)o;

            //skip special attributes
            if (LINE_NUMBER_ATTRIBUTE.equals(attribute.getName()))
                continue;

            attrs.put(attribute.getName(), attribute.getValue());
        }

        return attrs;
    }

  /**
   * @param annotation A string reprenting an unparsed annotation of the form: <pre>
   * "{@literal @}MyAnn(property = [expr], ...)"</pre>
   * @return A partially parsed array following this structure:<pre>
   *  [0] -> "MyAnn" <br/>
   *  [1] -> "prop = [expr], ..."
   * </pre>
   */
    static String[] extractKeyAndContent(String annotation) {
        final int index = annotation.indexOf('(');

        //there's no content
        if (index < 0)
            return new String[] { annotation.substring(1).toLowerCase(), "" };

        String content = annotation.substring(index + 1, annotation.lastIndexOf(')'));

        //normalize empty string to null
        if ("".equals(content))
            content = null;

        return new String[] { annotation.substring(1, index).toLowerCase(), content };
    }

    static boolean isTextCommentOrCdata(Node node) {
        final short nodeType = node.getNodeType();

        return isText(node) || Node.COMMENT_NODE == nodeType || Node.CDATA_SECTION_NODE == nodeType;
    }

    static boolean isText(Node node) {
        return null != node && Node.TEXT_NODE == node.getNodeType();
    }

    static boolean isElement(Node node) {
        return Node.ELEMENT_NODE == node.getNodeType();
    }

    //removes special attributes, so rendering can happen normally
    public static void normalizeAttributes(Element element) {
        final Attribute toRemove = element.attribute(LINE_NUMBER_ATTRIBUTE);

        if (null != toRemove)
            element.remove(toRemove);
    }

    /**
     * An XML filter used to generate line numbers as a special attribute.
     *
     * @author Dhanji R. Prasanna (dhanji@gmail com)
     */
    @NotThreadSafe
    private static class SaxLineNumbersFilter extends XMLFilterImpl {
        private Locator locator;

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;

            super.setDocumentLocator(locator);
        }

        public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {

            //replace existing attributes with a decorator that stores line numbers
            AttributesImpl attr = new AttributesImpl(attributes);
            attr.addAttribute("", "", LINE_NUMBER_ATTRIBUTE, "int", String.valueOf(locator.getLineNumber()));

            super.startElement(s, s1, s2, attr);
        }

    }

    public static XMLFilter newLineNumberFilter() {
        return new SaxLineNumbersFilter();
    }

    public static int lineNumberOf(Element element) {
        final Attribute attribute = element.attribute(LINE_NUMBER_ATTRIBUTE);
        
        return null == attribute ? -1 : Integer.parseInt(attribute.getValue());
    }
}
