package com.google.sitebricks.compiler;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.rendering.Strings;
import org.apache.commons.lang.Validate;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.jsoup.parser.TokenQueue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses HTML into a List<{@link org.jsoup.nodes.Node}>
 * this is a relaxed version of Jonathan Hedley's {@link org.jsoup.parser.Parser}
 */
public class HtmlParser {
  private static final ImmutableSet<String> closingOptional = ImmutableSet
      .of("a", "form", "label", "dt", "dd", "li",
          "thead", "tfoot", "tbody", "colgroup", "tr", "th", "td");

  private static final ImmutableSet<String> headTags = ImmutableSet
      .of("base", "script", "noscript", "link", "meta", "title", "style", "object");

  private static final String SQ = "'";
  private static final String DQ = "\"";

  private static final Tag htmlTag = Tag.valueOf("html");
  private static final Tag headTag = Tag.valueOf("head");
  private static final Tag bodyTag = Tag.valueOf("body");
  private static final Tag titleTag = Tag.valueOf("title");
  private static final Tag textareaTag = Tag.valueOf("textarea");

  // private final ArrayList<Node> soup = new ArrayList<Node>();
  // private final LinkedList<Node> soup = new LinkedList<Node>();
  private final LinkedList<Node> stack = new LinkedList<Node>();

  // TODO - LineCountingTokenQueue
  static final Pattern LINE_SEPARATOR = Pattern.compile("(\\r\\n|\\n|\\r|\\u0085|\\u2028|\\u2029)");
  static final String LINE_NUMBER_ATTRIBUTE = "_linecount";

  private final TokenQueue tq;

  private String baseUri = "";

  private Element _html = null;
  private Element _head = null;
  private Element _body = null;

  private AnnotationNode pendingAnnotation = null;

  private int linecount = 0;

  static final ImmutableSet<String> SKIP_ATTR = ImmutableSet.of(LINE_NUMBER_ATTRIBUTE,
      AnnotationNode.ANNOTATION, AnnotationNode.ANNOTATION_KEY, AnnotationNode.ANNOTATION_CONTENT);

  private HtmlParser(String html) {
    Validate.notNull(html);
    tq = new TokenQueue(html);
  }

  /**
   * Parse HTML into List<Node>
   *
   * @param html HTML to parse
   */
  public static List<Node> parse(String html) {
    HtmlParser parser = new HtmlParser(html);
    return parser.parse();
  }

  /*     Parse a fragment of HTML into the {@code body} of a Document.
  @param bodyHtml fragment of HTML
  @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
  @return Document, with empty head, and HTML parsed into body
  */
  // public static Document parseBodyFragment(String bodyHtml, String baseUri) {
  // HtmlParser parser = new HtmlParser(bodyHtml, true);
  // return parser.parse();
  // }

  private List<Node> parse() {
    while (!tq.isEmpty()) {
      if (tq.matches("<!--")) {
        parseComment();
      } else if (tq.matches("<![CDATA[")) {
        parseCdata();
      } else if (tq.matches("<?") || tq.matches("<!")) {
        parseXmlDecl();
      } else if (tq.matches("</")) {
        parseEndTag();
      } else if (tq.matches("<")) {
        parseStartTag();
      } else {
        parseTextNode();
      }
    }

    // Pop off body as it is already inside html.
    Iterator<Node> iterator = stack.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().nodeName().equals(bodyTag.getName())) {
        iterator.remove();
      }
    }

    return stack;
  }

  private void parseComment() {
    tq.consume("<!--");
    String data = tq.chompTo("->");

    if (data.endsWith("-")) // i.e. was -->
      data = data.substring(0, data.length() - 1);

    Comment comment = new Comment(data, baseUri);
    annotate(comment); // TODO - should annotations even apply to comments?
    lines(comment, data);
    add(comment);
  }

  private void parseXmlDecl() {
    tq.consume("<");
    Character firstChar = tq.consume(); // <? or <!, from initial match.
    boolean procInstr = firstChar.toString().equals("!");
    String data = tq.chompTo(">");

    XmlDeclaration decl = new XmlDeclaration(data, baseUri, procInstr);
    annotate(decl); // TODO - should annotations even apply to declarations?
    lines(decl, data);
    add(decl);

  }

  private void parseEndTag() {
    tq.consume("</");
    String tagName = tq.consumeTagName();
    tq.chompTo(">");

    if (!Strings.empty(tagName)) {
      Tag tag = Tag.valueOf(tagName);
      popStackToClose(tag);
    }
  }

  private void parseStartTag() {
    tq.consume("<");
    String tagName = tq.consumeTagName();

    if (Strings.empty(tagName)) { // doesn't look like a start tag after all; put < back on stack and handle as text
      tq.addFirst("&lt;");
      parseTextNode();
      return;
    }

    Attributes attributes = new Attributes();
    while (!tq.matchesAny("<", "/>", ">") && !tq.isEmpty()) {
      Attribute attribute = parseAttribute();
      if (attribute != null)
        attributes.put(attribute);
    }

    Tag tag = Tag.valueOf(tagName);
    // TODO - option to create elements without indent
    Element child = new Element(tag, baseUri, attributes);
    annotate(child);

    lines(child, "");

    boolean isEmptyElement = tag.isEmpty(); // empty element if empty tag (e.g. img) or self-closed el (<div/>
    if (tq.matchChomp("/>")) { // close empty element or tag
      isEmptyElement = true;
    } else {
      tq.matchChomp(">");
    }

    // pc data only tags (textarea, script): chomp to end tag, add content as text node
    if (tag.isData()) {
      String data = tq.chompTo("</" + tagName);
      tq.chompTo(">");
      
      // enable annotations on data areas
      parseAnnotatableText (data, child);
    }

    // <base href>: update the base uri
    if (child.tagName().equals("base")) {
      String href = child.absUrl("href");
      if (!Strings.empty(href)) { // ignore <base target> etc
        baseUri = href;
        // TODO - consider updating baseUri for relevant elements in the stack, eg rebase(stack, uri)
        // doc.get().setBaseUri(href); // set on the doc so doc.createElement(Tag) will get updated base
      }
    }

    addChildToParent(child, isEmptyElement);
  }

  private Attribute parseAttribute() {
    whitespace();
    String key = tq.consumeAttributeKey();
    String value = "";
    whitespace();
    if (tq.matchChomp("=")) {
      whitespace();

      if (tq.matchChomp(SQ)) {
        value = tq.chompTo(SQ);
      } else if (tq.matchChomp(DQ)) {
        value = tq.chompTo(DQ);
      } else {
        StringBuilder valueAccum = new StringBuilder();
        // no ' or " to look for, so scan to end tag or space (or end of stream)
        while (!tq.matchesAny("<", "/>", ">") && !tq.matchesWhitespace() && !tq.isEmpty()) {
          valueAccum.append(tq.consume());
        }
        value = valueAccum.toString();
      }
      whitespace();
    }
    if (!Strings.empty(key))
      return Attribute.createFromEncoded(key, value);
    else {
      tq.consume(); // unknown char, keep popping so not get stuck
      return null;
    }
  }
  

  /**
   * Pulls a text segment apart by annotations within it and creates multiple Text Nodes
   * applying the annotation to each text segment as approriate.
   * 
   * @param text the text to be processed for annotations
   * @param parent
   */
  private void parseAnnotatableText(String text, Element parent) {
	  AnnotationNode annotation = null;
	  Matcher matcher = AnnotationParser.WIDGET_ANNOTATION_REGEX.matcher(text);

	  int previousEnd = 0;
	  while (matcher.find()){
		  int start = matcher.start();

		  // build a new text node for what is between last index and current annotation
		  if (start > previousEnd)	{
			  String segment = text.substring(previousEnd, start);
			  // ignore empty white space
			  if (segment.trim().length() > 0){
				  addTextNodeToParent (segment, parent, annotation);
				  annotation = null;
			  }
		  }

		  // parse the annotation
		  String annotationText = matcher.group().trim();
		  if (null != annotationText) {
		      annotation = new AnnotationNode(annotationText);
		      lines(annotation, annotationText);
		  }
		  previousEnd = matcher.end();
	  }
	  
	  // handle leftover text if we parsed some segment
	  if (previousEnd > 0 && previousEnd < text.length()){
		  String segment = text.substring(previousEnd);
		  if (segment.trim().length() > 0){
			  addTextNodeToParent (segment, parent, annotation);
			  annotation = null;
		  }
	  }
	  
	  // store the remaining annotation for use by whatever is parsed next
	  if (annotation != null)
		  add(annotation);
	  
	  // handle no annotations being found
	  if (previousEnd == 0){
		  Node dataNode;
		  if (parent.tagName().equals(titleTag) || parent.tagName().equals(textareaTag))
	        dataNode = TextNode.createFromEncoded(text, baseUri);
	      else // data not encoded but raw (for " in script)
	        dataNode = new DataNode(text, baseUri);
	      lines(dataNode, text);
	      
	      if (pendingAnnotation != null)
	          pendingAnnotation.apply(dataNode);
	      
		  // put the text node on the parent
		  parent.appendChild(dataNode);
	  }
  }

  /** 
   * Break the text up by the first line delimiter.  We only want annotations applied to the first line of a block of text
   * and not to a whole segment.
   * 
   * @param text the text to turn into nodes
   * @param parent the parent node
   * @param annotation the current annotation to be applied to the first line of text
   */
  private void addTextNodeToParent (String text, Element parent, AnnotationNode annotation)	{
	  String [] lines = new String[] {text};
	  
	  if (annotation != null)
		  lines = splitInTwo(text);
	  
	  for (int i = 0; i < lines.length; i++){
		  TextNode textNode = TextNode.createFromEncoded(lines[i], baseUri);
		  lines(textNode, lines[i]);
		  
		  // apply the annotation and reset it to null
		  if (annotation != null && i == 0)
			  annotation.apply(textNode);
		  
		  // put the text node on the parent
		  parent.appendChild(textNode);
	  }
  }
  
  /**
   * Break a text segment apart into two at the first line delimiter which has non-whitespace characters before it.
   * 
   * @param text text to split in two
   * @return
   */
  private String[] splitInTwo(String text)	{
	  Matcher matcher = LINE_SEPARATOR.matcher(text);
	  while (matcher.find()){
		  int start = matcher.start();
		  if (start > 0 && start < text.length())	{
			  String segment = text.substring(0, start);
			  if (segment.trim().length() > 0)
				  return new String[] {text.substring(0, start), text.substring(start)};
		  }
	  }
	  return new String[] {text};
  }
  
  private void parseTextNode() {
    String text = tq.consumeTo("<");
    String annotationText = AnnotationParser.readAnnotation(text);
    text = AnnotationParser.stripAnnotation(text);

    if (text.length() > 0) {
      TextNode textNode = TextNode.createFromEncoded(text, baseUri);
      // if (pendingAnnotation != null) { pendingAnnotation.apply(textNode); }
      lines(textNode, text);
      add(textNode);
    }

    if (null != annotationText) {
      AnnotationNode annotation = new AnnotationNode(annotationText);
      lines(annotation, annotationText);
      add(annotation);
    }
  }

  private void parseCdata() {
    tq.consume("<![CDATA[");
    String rawText = tq.chompTo("]]>");
    TextNode textNode = new TextNode(rawText, baseUri); // constructor does not escape

    if (pendingAnnotation != null)
      pendingAnnotation.apply(textNode);

    lines(textNode, rawText);
    add(textNode);
  }


  private Element addChildToParent(Element child, boolean isEmptyElement) {
    Element parent = popStackToSuitableContainer(child.tag());
    if (parent != null)
      parent.appendChild(child);

    if (!isEmptyElement && !child.tag().isData()) {
      stack.addLast(child);
    }

    return parent;
  }


  private boolean stackHasValidParent(Tag childTag) {
    if (stack.size() == 1 && childTag.equals(htmlTag))
      return true; // root is valid for html node

    for (int i = stack.size() - 1; i >= 0; i--) {
      Node n = stack.get(i);
      if (n instanceof Element)
        return true;
    }
    return false;
  }

  private Element popStackToSuitableContainer(Tag tag) {
    while (!stack.isEmpty() && !(stack.getLast() instanceof XmlDeclaration)) {
      Node lastNode = stack.getLast();
      if (lastNode instanceof Element) {
        Element last = (Element) lastNode;
        if (canContain(last.tag(), tag))
          return last;
        else
          stack.removeLast();
      }
    }
    return null;
  }

  private Element popStackToClose(Tag tag) {
    // first check to see if stack contains this tag; if so pop to there, otherwise ignore
    int counter = 0;
    Element elToClose = null;
    for (int i = stack.size() - 1; i > 0; i--) {
      counter++;
      Node n = stack.get(i);
      if (n instanceof Element) {
        Element el = (Element) n;
        Tag elTag = el.tag();
        if (elTag.equals(bodyTag) || elTag.equals(headTag) || elTag.equals(htmlTag)) { // once in body, don't close past body
          break;
        } else if (elTag.equals(tag)) {
          elToClose = el;
          break;
        }
      }
    }
    if (elToClose != null) {
      for (int i = 0; i < counter; i++) {
        stack.removeLast();
      }
    }
    return elToClose;
  }


  private <N extends Node> void add(N n) {
    Node last = null;

    if (stack.size() == 0) {
      if (n instanceof XmlDeclaration) {
        // only add the first/outermost doctype
        stack.add(n);
        return;
      }
    } else {
      last = stack.getLast();
    }


    // TODO - optionally put the AnnotationNode on the stack
    if (n instanceof AnnotationNode) {
      pendingAnnotation = (AnnotationNode) n;
      return;
    }
//        else if (null != pendingAnnotation) {
//            pendingAnnotation.apply(n);
//        }


    if (n instanceof Element) {
      Element en = (Element) n;
      if (en.tag().equals(htmlTag) && (null == _html))
        _html = en;

      else if (en.tag().equals(htmlTag) && (null != _html))
        for (Node cat : en.childNodes()) _html.appendChild(cat);

      else if (en.tag().equals(headTag) && (null == _head))
        _head = en;

      else if (en.tag().equals(headTag) && (null != _head))
        for (Node cat : en.childNodes()) _head.appendChild(cat);

      else if (en.tag().equals(bodyTag) && (null == _body))
        _body = en;

      else if (en.tag().equals(bodyTag) && (null != _body))
        for (Node cat : en.childNodes()) _body.appendChild(cat);
    }


    if (last == null)
      stack.add(n);

    else if (last instanceof Element) {
      ((Element) last).appendChild(n);
    }

  }


  // from jsoup.parser.Tag

  /**
   * Test if this tag, the prospective parent, can accept the proposed child.
   *
   * @param child potential child tag.
   * @return true if this can contain child.
   */
  boolean canContain(Tag parent, Tag child) {
    Validate.notNull(child);

    if (child.isBlock() && !parent.canContainBlock())
      return false;

    if (!child.isBlock() && parent.isData())
      return false;

    if (closingOptional.contains(parent.getName()) && parent.getName().equals(child.getName()))
      return false;

    if (parent.isEmpty() || parent.isData())
      return false;

    // head can only contain a few. if more than head in here, modify to have a list of valids
    // TODO: (could solve this with walk for ancestor)
    if (parent.getName().equals("head")) {
      if (headTags.contains(child.getName()))
        return true;
      else
        return false;
    }

    // dt and dd (in dl)
    if (parent.getName().equals("dt") && child.getName().equals("dd"))
      return false;
    if (parent.getName().equals("dd") && child.getName().equals("dt"))
      return false;

    return true;
  }


  // TODO - LineCountingTokenQueue
  //  these line numbers are an inaccurate estimate

  private void lines(Node node, String data) {
    linecount += (LINE_SEPARATOR.split(data).length);
    node.attr(LINE_NUMBER_ATTRIBUTE, String.valueOf(linecount));
  }

  private void whitespace() {
    if (tq.peek() == Character.LINE_SEPARATOR)
      linecount++;
    tq.consumeWhitespace();
  }


  private void annotate(Node n) {
    if (null != pendingAnnotation) {
      pendingAnnotation.apply(n);
      pendingAnnotation = null;
    }
  }

}

