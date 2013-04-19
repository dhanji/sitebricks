package com.google.sitebricks.compiler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Template;
import com.google.sitebricks.conversion.generics.Generics;
import com.google.sitebricks.rendering.Strings;
import com.google.sitebricks.rendering.control.Chains;
import com.google.sitebricks.rendering.control.WidgetChain;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION;
import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION_CONTENT;
import static com.google.sitebricks.compiler.AnnotationNode.ANNOTATION_KEY;
import static com.google.sitebricks.compiler.HtmlParser.LINE_NUMBER_ATTRIBUTE;
import static com.google.sitebricks.compiler.HtmlParser.SKIP_ATTR;

/**
 * @author Shawn based on XMLTemplateCompiler by Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Singleton
public class HtmlTemplateCompiler implements TemplateCompiler {
    
  private final WidgetRegistry registry;
    private final PageBook pageBook;
    private final SystemMetrics metrics;

    //special widget types (built-in symbol table)
    private static final String REQUIRE_WIDGET = "@require";
    private static final String REPEAT_WIDGET = "repeat";
    private static final String CHOOSE_WIDGET = "choose";

    @Inject
    public HtmlTemplateCompiler(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
        this.registry = registry;
        this.pageBook = pageBook;
        this.metrics = metrics;
    }
    
    //
    // compiler state
    //
    class PageCompilingContext {
      Class<?> page;
      Template template;
      List<CompileError> errors = Lists.newArrayList();
      List<CompileError> warnings = Lists.newArrayList();
      Element form;
      Stack<EvaluatorCompiler> lexicalScopes = new Stack<EvaluatorCompiler>();      
    }
    
    public Renderable compile(Class<?> page, Template template) {
      
        PageCompilingContext pc = new PageCompilingContext();
        pc.page = page;
        pc.template = template;
        pc.lexicalScopes.push(new MvelEvaluatorCompiler(page));
      
        WidgetChain widgetChain;
        widgetChain = walk(pc, HtmlParser.parse(template.getText()));

        // TODO - get the errors when !(isValid)
        if (!pc.errors.isEmpty() || !pc.warnings.isEmpty()) {
            // If there were any errors we must track them.
            metrics.logErrorsAndWarnings(page, pc.errors, pc.warnings);

            // Only explode if there are errors.
            if (!pc.errors.isEmpty())
                throw new TemplateCompileException(page, template.getText(), pc.errors, pc.warnings);
        }

      return widgetChain;
    }

    private WidgetChain walk(PageCompilingContext pc, List<Node> nodes) {
        WidgetChain chain = Chains.proceeding();

        for (Node n: nodes)
            chain.addWidget(widgetize(pc, n, walk(pc, n)));

        return chain;
    }

    /**
     * Walks the DOM recursively, and converts elements into corresponding sitebricks widgets.
     */
    @NotNull
    private <N extends Node> WidgetChain walk(PageCompilingContext pc, N node) {
        WidgetChain widgetChain = Chains.proceeding();
        for (Node n: node.childNodes()) {
            if (n instanceof Element) {
                final Element child = (Element) n;

                //push form if this is a form tag
                if (child.tagName().equals("form"))
                    pc.form = (Element) n;

                //setup a lexical scope if we're going into a repeat widget (by reading the previous node)
                final boolean shouldPopScope = lexicalClimb(pc, child);

                //continue recursing down, perform a post-order, depth-first traversal of the DOM
                WidgetChain childsChildren;
                try {
                    childsChildren = walk(pc, child);

                    //process the widget itself into a Renderable with child tree
                    widgetChain.addWidget(widgetize(pc, child, childsChildren));
                } finally {
                    lexicalDescend(pc, child, shouldPopScope);
                }

            } else if (n instanceof TextNode) {
            	TextNode child = (TextNode)n;
            	Renderable textWidget;
            	
                //setup a lexical scope if we're going into a repeat widget (by reading the previous node)
                final boolean shouldPopScope = lexicalClimb(pc, child);

                // construct the text widget
                try {
                	textWidget = registry.textWidget(cleanHtml(n), pc.lexicalScopes.peek());
                	
                	// if there are no annotations, add the text widget to the chain
                	if (!child.hasAttr(ANNOTATION_KEY))	{
                		widgetChain.addWidget(textWidget);
                	}
                	else	{
                		// construct a new widget chain for this text node 
                		WidgetChain childsChildren = Chains.proceeding().addWidget(textWidget);
                		
                		// make a new widget for the annotation, making the text chain the child
                		String widgetName = child.attr(ANNOTATION_KEY).toLowerCase();
                		Renderable annotationWidget = registry.newWidget(widgetName, child.attr(ANNOTATION_CONTENT), childsChildren, pc.lexicalScopes.peek());
                		widgetChain.addWidget(annotationWidget);
                	}
                	
                } catch (ExpressionCompileException e) {
                    pc.errors.add(
                            CompileError.in(node.outerHtml())
                            .near(line(n))
                            .causedBy(e)
                    );
                }

                if (shouldPopScope)
                	pc.lexicalScopes.pop();
            	
            } else if ((n instanceof Comment) || (n instanceof DataNode)) {
                //process as raw text widget
                try {
                    widgetChain.addWidget(registry.textWidget(cleanHtml(n), pc.lexicalScopes.peek()));
                } catch (ExpressionCompileException e) {

                    pc.errors.add(
                            CompileError.in(node.outerHtml())
                            .near(line(node))
                            .causedBy(e)
                    );
                }
            } else if (n instanceof XmlDeclaration) {
                try {
                    widgetChain.addWidget(registry
                        .xmlDirectiveWidget(((XmlDeclaration)n).getWholeDeclaration(),
                        pc.lexicalScopes.peek()));
                } catch (ExpressionCompileException e) {
                    pc.errors.add(
                            CompileError.in(node.outerHtml())
                            .near(line(node))
                            .causedBy(e)
                  );

                }
            }
        }

        //return computed chain, or a terminal
        return widgetChain;
    }


    /**
     * Complement of HtmlTemplateCompiler#lexicalClimb().
     *  This method pops off the stack of lexical scopes when
     *  we're done processing a sitebricks widget.
     */
    private void lexicalDescend(PageCompilingContext pc, Element element, boolean shouldPopScope) {

        //pop form
        if ("form".equals(element.tagName()))
            pc.form = null;

        //pop compiler if the scope ends
        if (shouldPopScope) {
            pc.lexicalScopes.pop();
        }
    }


    /**
     * Called to push a new lexical scope onto the stack.
     */
    private boolean lexicalClimb(PageCompilingContext pc, Node node) {
        if (node.attr(ANNOTATION).length()>1) {

            // Setup a new lexical scope (symbol table changes on each scope encountered).
            if (REPEAT_WIDGET.equalsIgnoreCase(node.attr(ANNOTATION_KEY))
                || CHOOSE_WIDGET.equalsIgnoreCase(node.attr(ANNOTATION_KEY))) {

                String[] keyAndContent = {node.attr(ANNOTATION_KEY), node.attr(ANNOTATION_CONTENT)};
                pc.lexicalScopes.push(new MvelEvaluatorCompiler(parseRepeatScope(pc, keyAndContent, node)));
                return true;
            }

            // Setup a new lexical scope for compiling against embedded pages (closures).
            final PageBook.Page embed = pageBook.forName(node.attr(ANNOTATION_KEY));
            if (null != embed) {
                final Class<?> embedClass = embed.pageClass();
                MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(embedClass);
                checkEmbedAgainst(pc, compiler, Parsing.toBindMap(node.attr(ANNOTATION_CONTENT)),
                    embedClass, node);

              pc.lexicalScopes.push(compiler);
              return true;
            }
        }

        return false;
    }

    /**
     * This method converts an XML element into a specific kind of widget.
     * Special cases are the XML widget, Header, @Require widget. Otherwise a standard
     * widget is created.
     */
    @SuppressWarnings({"JavaDoc"}) @NotNull
    private <N extends Node> Renderable widgetize(PageCompilingContext pc, N node, WidgetChain childsChildren) {
        if (node instanceof XmlDeclaration) {
            try {
              XmlDeclaration decl = (XmlDeclaration)node;
              return registry.xmlDirectiveWidget(decl.getWholeDeclaration(), pc.lexicalScopes.peek());
            } catch (ExpressionCompileException e) {
                pc.errors.add(
                        CompileError.in(node.outerHtml())
                        .near(line(node))
                        .causedBy(e)
              );
            }
        }
      
        // Header widget is a special case, where we match by the name of the tag =(
        if ("head".equals(node.nodeName())) {
          try {
            return registry.headWidget(childsChildren, parseAttribs(node.attributes()), pc.lexicalScopes.peek());
          } catch (ExpressionCompileException e) {
            pc.errors.add(
                CompileError.in(node.outerHtml())
                .near(line(node))
                .causedBy(e)
            );

          }
        }

        String annotation = node.attr(ANNOTATION);

        //if there is no annotation, treat as a raw xml-widget (i.e. tag)
        if ((null == annotation) || 0 == annotation.trim().length())
            try {
                checkUriConsistency(pc, node);
                checkFormFields(pc, node);

                return registry.xmlWidget(childsChildren, node.nodeName(), parseAttribs(node.attributes()),
                        pc.lexicalScopes.peek());
            } catch (ExpressionCompileException e) {
                pc.errors.add(
                    CompileError.in(node.outerHtml())
                    .near(line(node)) 
                    .causedBy(e)
                );

                return Chains.terminal();
            }

        // Special case: is this annotated with @Require
        //   if so, tags in head need to be promoted to head of enclosing page.
        if (REQUIRE_WIDGET.equalsIgnoreCase(annotation.trim()))
            try {
                return registry.requireWidget(registry.xmlWidget(childsChildren, node.nodeName(),
                    parseAttribs(node.attributes()), pc.lexicalScopes.peek()));
            } catch (ExpressionCompileException e) {
                pc.errors.add(
                    CompileError.in(node.outerHtml())
                    .near(line(node))
                    .causedBy(e)
                );

                return Chains.terminal();
            }

        // If this is NOT a self-rendering widget, give it a child.
        // final String widgetName = node.attr(ANNOTATION_KEY).trim().toLowerCase());
        final String widgetName = node.attr(ANNOTATION_KEY).toLowerCase();

        if (!registry.isSelfRendering(widgetName))
            try {
                childsChildren = Chains.singleton(registry.xmlWidget(childsChildren, node.nodeName(),
                        parseAttribs(node.attributes()), pc.lexicalScopes.peek()));
            } catch (ExpressionCompileException e) {
                pc.errors.add(
                    CompileError.in(node.outerHtml())
                    .near(line(node))
                    .causedBy(e)
                );
            }


        // Recursively build widget from [Key, expression, child widgets].
        try {
            return registry.newWidget(widgetName, node.attr(ANNOTATION_CONTENT), childsChildren, pc.lexicalScopes.peek());
        } catch (ExpressionCompileException e) {
            pc.errors.add(
                CompileError.in(node.outerHtml())
                .near(line(node))
                .causedBy(e)
            );

            // This should never be used.
            return Chains.terminal();
        }
    }




    private Map<String,Type> parseRepeatScope(PageCompilingContext pc, String[] extract, Node node) {
        RepeatToken repeat = registry.parseRepeat(extract[1]);
        Map<String, Type> context = Maps.newHashMap();

        // Verify that @Repeat was parsed correctly.
        if (null == repeat.var()) {
            pc.errors.add(
                        CompileError.in(node.outerHtml())
                        .near(node.siblingIndex()) // TODO - line number
                        .causedBy(CompileErrors.MISSING_REPEAT_VAR)
                );
        }
        if (null == repeat.items()) {
            pc.errors.add(
                    CompileError.in(node.outerHtml())
                    .near(node.siblingIndex())  // TODO  - line number
                    .causedBy(CompileErrors.MISSING_REPEAT_ITEMS)
            );
        }

        try {
            Type egressType = pc.lexicalScopes.peek().resolveEgressType(repeat.items());
            
            // convert to collection if we need to
            Type elementType;
            Class<?> egressClass = Generics.erase(egressType);
			if (egressClass.isArray()) {
				elementType = Generics.getArrayComponentType(egressType);
            }
            else if (Collection.class.isAssignableFrom(egressClass)) {
            	elementType = Generics.getTypeParameter(egressType, Collection.class.getTypeParameters()[0]);
            }
            else {
            	pc.errors.add(
            			CompileError.in(node.outerHtml())
            			.near(node.siblingIndex()) // TODO - line number
            			.causedBy(CompileErrors.REPEAT_OVER_ATOM)
            	);
            	return Collections.emptyMap();
            }

            context.put(repeat.var(), elementType);
            context.put(repeat.pageVar(), pc.page);
            context.put("__page", pc.page);
            context.put("index", int.class);
            context.put("isLast", boolean.class);

        } catch (ExpressionCompileException e) {
                pc.errors.add(
                    CompileError.in(node.outerHtml())
                    .near(node.siblingIndex()) // TODO - line number
                    .causedBy(e)
                );
        }

        return context;
    }




    private void checkFormFields(PageCompilingContext pc, Node element) {
        if (null == pc.form)
            return;

        String action = pc.form.attr("action");

        // Only look at contextual uris (i.e. hosted by us).
        // TODO - relative, not starting with '/'
        if (null == action || (!action.startsWith("/")))
            return;

        final PageBook.Page page = pageBook.get(action);

        // Only look at pages we actually have registered.
        if (null == page) {
            pc.warnings.add(
                CompileError.in(element.outerHtml())
                .near(line(element))
                .causedBy(CompileErrors.UNRESOLVABLE_FORM_ACTION)
            );

            return;
        }

        // If we're inside a form do a throw-away compile against the target page.
        if ("input".equals(element.nodeName()) || "textarea".equals(element.nodeName())) {
            String name = element.attr("name");

            // Skip submits and buttons.
            if (skippable(element.attr("type")))
                return;

            //TODO Skip empty?
            if (null == name) {
                pc.warnings.add(
                        CompileError.in(element.outerHtml())
                        .near(line(element)) 
                        .causedBy(CompileErrors.FORM_MISSING_NAME)
                );

                return;
            }

            // Compile expression path.
          try {
                new MvelEvaluatorCompiler(page.pageClass())
                        .compile(name);

            } catch (ExpressionCompileException e) {
                //TODO Very hacky, needed to strip out xmlns attribution.
                pc.warnings.add(
                    CompileError.in(element.outerHtml())
                    .near(element.siblingIndex()) // TODO - line number
                    .causedBy(CompileErrors.UNRESOLVABLE_FORM_BINDING, e)
                );
            }

        }

    }

    private void checkUriConsistency(PageCompilingContext pc, Node element) {
        String uriAttrib = element.attr("action");
        if (null == uriAttrib)
            uriAttrib = element.attr("src");
        if (null == uriAttrib)
            uriAttrib = element.attr("href");

        if (null != uriAttrib) {

            // Verify that such a uri exists in the page book,
            // only if it is contextual--ignore abs & relative URIs.
          if (uriAttrib.startsWith("/"))
                if (null == pageBook.nonCompilingGet(uriAttrib))
                    pc.warnings.add(
                        CompileError.in(element.outerHtml())
                        .near(element.siblingIndex()) // TODO - line number
                        .causedBy(CompileErrors.UNRESOLVABLE_FORM_ACTION, uriAttrib)
                );
        }
    }




  /**
   * @param attributes A list of attribs
   * @return Returns a mutable map parsed out of the attribute list
   */
  public static Map<String, String> parseAttribs(Attributes attributes) {

      Map<String, String> attrs = new LinkedHashMap<String, String>(attributes.size() + 4);

      for (Attribute a : attributes.asList())
          if (!SKIP_ATTR.contains(a.getKey()))
              attrs.put(a.getKey(), a.getValue());

      return attrs;
  }

  // Ensures that embed bound properties are writable
  private void checkEmbedAgainst(PageCompilingContext pc, EvaluatorCompiler compiler, Map<String, String> properties, Class<?> embedClass, Node node) {

    // TODO also type check them against expressions
    for (String property : properties.keySet()) {
        try {
            if (!compiler.isWritable(property)) {
                pc.errors.add(
                    CompileError.in(node.outerHtml())
                      //TODO we need better line number detection if there is whitespace between the annotation and tag.
                      .near(node.siblingIndex()-1) // TODO -  line number of the annotation
                      .causedBy(CompileErrors.PROPERTY_NOT_WRITEABLE,
                          String.format("Property %s#%s was not writable. Did you forget to create "
                              + "a setter or @Visible annotation?", embedClass.getSimpleName(), property))
                );
            }
        } catch (ExpressionCompileException ece) {
            pc.errors.add(
                CompileError.in(node.outerHtml())
                    .near(node.siblingIndex()) // TODO - line number
                    .causedBy(CompileErrors.ERROR_COMPILING_PROPERTY)
            );
        }
     }
  }


  static boolean skippable(String kind) {
    return null != kind && ("submit".equals(kind)
            || "button".equals(kind)
            || "reset".equals(kind)
            || "file".equals(kind));
  }





  // TESTING jsoup.nodes.Node

  public List<Node> findSiblings(Node node) {
      Preconditions.checkNotNull(node);
    
      Node parent = node.parent();
      if (null == parent) return null;

      return parent.childNodes();               
  }

  protected static <N extends Node> Integer indexInList(N search, List<N> nodes) {
        Preconditions.checkNotNull(search);
        Preconditions.checkNotNull(nodes);

        for (int i = 0; i < nodes.size(); i++) {
            N node = nodes.get(i);
            if (node.equals(search))
                return i;
        }
        return null;
    }

    private static int line(Node node) {
        return Integer.valueOf(node.attr(LINE_NUMBER_ATTRIBUTE));
    }

    // outerHtml from jsoup.Node, Element with suppressed _attribs

    private static String cleanHtml(final Node node) {
        if (node instanceof Element) {
            Element element = ((Element) node);
            StringBuilder accum = new StringBuilder();
            accum.append("<").append(element.tagName());
            for (Attribute attribute: element.attributes()) {
                if (!(attribute.getKey().startsWith("_"))) {
                    accum.append(" ");
                    accum.append(attribute.getKey());
                    accum.append("=\"");
                    accum.append(attribute.getValue());
                    accum.append('"');
                }
            }

            if (element.childNodes().isEmpty() && element.tag().isEmpty()) {
                accum.append(" />");
            } else {
                accum.append(">");
                for (Node child : element.childNodes())
                    accum.append(cleanHtml(child));

                accum.append("</").append(element.tagName()).append(">");
            }
            return accum.toString();
        } else if (node instanceof TextNode) {
            return ((TextNode) node).getWholeText();
        } else if (node instanceof XmlDeclaration) {

          // HACK
          if (node.childNodes().isEmpty()) {
            return "";
          }
            return node.outerHtml();
        } else if (node instanceof Comment) {
          // HACK: elide comments for now.
          return "";
        } else if (node instanceof DataNode && node.childNodes().isEmpty()) {
        	// No child nodes are defined but we have to handle content if such exists, example
            // <script language="JavaScript">var a =  { name: "${user.name}"}</script>  

            String content = node.attr("data");
            if (Strings.empty(content)) {
                return "";
            }

            return content;
        } else {
            return node.outerHtml();
        }
    }
}
