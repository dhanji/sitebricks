package com.google.sitebricks.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.jcip.annotations.NotThreadSafe;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Template;
import com.google.sitebricks.conversion.generics.Generics;
import com.google.sitebricks.rendering.control.Chains;
import com.google.sitebricks.rendering.control.WidgetChain;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * 
 * TODO share code with HtmlTemplateCompiler
 */
@NotThreadSafe
public class XmlTemplateCompiler implements TemplateRenderer {
    private final Class<?> page;
    private final WidgetRegistry registry;
    private final PageBook pageBook;
    private final SystemMetrics metrics;

    private final List<CompileError> errors = Lists.newArrayList();
    private final List<CompileError> warnings = Lists.newArrayList();

    //state variables
    private Element form;
    private final Stack<EvaluatorCompiler> lexicalScopes = new Stack<EvaluatorCompiler>();


    //special widget types (built-in symbol table)
    private static final String REQUIRE_WIDGET = "@require";
    private static final String REPEAT_WIDGET = "repeat";
    private static final String CHOOSE_WIDGET = "choose";


    public XmlTemplateCompiler(Class<?> page,
                               WidgetRegistry registry,
                               PageBook pageBook,
                               SystemMetrics metrics) {

        this.page = page;
        this.registry = registry;
        this.pageBook = pageBook;
        this.metrics = metrics;

        this.lexicalScopes.push(new MvelEvaluatorCompiler(page));
    }

    public Renderable compile(Template template) {
        WidgetChain widgetChain;
        try {
            final SAXReader reader = new SAXReader();
            reader.setMergeAdjacentText(true);
            reader.setXMLFilter(Dom.newLineNumberFilter());
            reader.setValidation(false);
            reader.setIncludeExternalDTDDeclarations(true);

            reader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.contains(".dtd")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });

            widgetChain = walk(reader.read(new StringReader(template.getText())));
        } catch (DocumentException e) {
            errors.add(
                    CompileError.in(template.getText())
                    .near(0)
                    .causedBy(CompileErrors.MALFORMED_TEMPLATE)
            );

            // Really this should only have the 1 error, but we need to set errors/warnings atomically.
            metrics.logErrorsAndWarnings(page, errors, warnings);

            throw new TemplateParseException(e);
        }
      
        if (!errors.isEmpty() || !warnings.isEmpty()) {
            // If there were any errors we must track them.
            metrics.logErrorsAndWarnings(page, errors, warnings);

            // Only explode if there are errors.
            if (!errors.isEmpty()) {
              System.out.println("!!!!!!! " + template.getText());  
              throw new TemplateCompileException(page, template.getText(), errors, warnings);
            }
        }

        return widgetChain;
    }

    private WidgetChain walk(Document document) {
        WidgetChain chain = Chains.proceeding();
        handleDocType(document, chain);
        final WidgetChain docChain = walk(document.getRootElement());

        chain.addWidget(widgetize(null, document.getRootElement(), docChain));

        return chain;
    }

    private void handleDocType(Document document, WidgetChain chain) {
        DocumentType docType = document.getDocType();
        if (docType != null) {
            String docTypeRawXml = document.getDocType().asXML();
            try {
                chain.addWidget(registry.textWidget(Dom.stripAnnotation(docTypeRawXml), lexicalScopes.peek()));
            } catch (ExpressionCompileException e) {
                errors.add(
                    CompileError.in(docTypeRawXml)
                        .causedBy(e)
              );
            }
        }
    }

    /**
     * Walks the DOM recursively, and converts elements into
     *  corresponding sitebricks widgets.
     */
    @SuppressWarnings({"JavaDoc"}) @NotNull
    private WidgetChain walk(Element element) {

        WidgetChain widgetChain = Chains.proceeding();

        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);

            if (Dom.isElement(node)) {
                final Element child = (Element) node;

                //push form if this is a form tag
                if (Dom.isForm(node))
                    form = (Element) node;


                //setup a lexical scope if we're going into a repeat widget (by reading the previous node)
                final boolean shouldPopScope = lexicalClimb(element, i);

                //continue recursing down, perform a post-order, depth-first traversal of the DOM
                WidgetChain childsChildren;
                try {
                    childsChildren = walk(child);

                    //process the widget itself into a Renderable with child tree
                    if (i > 0)
                        widgetChain.addWidget(widgetize(element.node(i - 1), child, childsChildren));
                    else
                        widgetChain.addWidget(widgetize(null, child, childsChildren));

                } finally {
                    lexicalDescend(node, shouldPopScope);
                }

            } else if (Dom.isTextCommentOrCdata(node)) {
                //process as raw text widget
                try {
                    widgetChain.addWidget(registry.textWidget(Dom.stripAnnotation(node.asXML()), lexicalScopes.peek()));
                } catch (ExpressionCompileException e) {

                    errors.add(
                            CompileError.in(Dom.asRawXml(element))
                            .near(Dom.lineNumberOf(element))
                            .causedBy(e)
                    );
                }
            }
        }

        //return computed chain, or a terminal
        return widgetChain;
    }


    /**
     * Complement of XmlTemplateCompiler#lexicalClimb().
     *  This method pops off the stack of lexical scopes when
     *  we're done processing a sitebricks widget.
     */
    private void lexicalDescend(Node node, boolean shouldPopScope) {

        //pop form
        if (Dom.isForm(node))
            form = null;

        //pop compiler if the scope ends
        if (shouldPopScope) {
            lexicalScopes.pop();
        }
    }


    /**
     * Called to push a new lexical scope onto the stack.
     */
    private boolean lexicalClimb(Element element, int i) {
        //read annotation on this node only if it is not the root node
        String annotation = i > 0 ? Dom.readAnnotation(element.node(i - 1)) : null;

        if (null != annotation) {
            String[] keyAndContent = Dom.extractKeyAndContent(annotation);

            // Setup a new lexical scope (symbol table changes on each scope encountered).
            final String name = keyAndContent[0];
            if (REPEAT_WIDGET.equalsIgnoreCase(name) || CHOOSE_WIDGET.equalsIgnoreCase(name)) {
                lexicalScopes.push(new MvelEvaluatorCompiler(parseRepeatScope(keyAndContent, element)));
                return true;
            }

            // Setup a new lexical scope for compiling against embedded pages (closures).
            final PageBook.Page embed = pageBook.forName(name);
            if (null != embed) {
                final Class<?> embedClass = embed.pageClass();
                MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(embedClass);
              checkEmbedAgainst(compiler, Parsing.toBindMap(keyAndContent[1]), embedClass,
                  (Element) element.node(i));
              lexicalScopes.push(compiler);
              return true;
            }
        }

        return false;
    }

    // Ensures that embed bound properties are writable
    private void checkEmbedAgainst(EvaluatorCompiler compiler, Map<String, String> properties,
                                   Class<?> embedClass, Element element) {

      // TODO also type check them against expressions
      for (String property : properties.keySet()) {
          try {
              if (!compiler.isWritable(property)) {
                  errors.add(
                      CompileError.in(Dom.asRawXml(element))
                        //TODO we need better line number detection if there is whitespace between the annotation and tag.
                        .near(Dom.lineNumberOf(element) - 1) // Really we want the line number of the annotation not the tag.
                        .causedBy(CompileErrors.PROPERTY_NOT_WRITEABLE,
                            String.format("Property %s#%s was not writable. Did you forget to create "
                                + "a setter or @Visible annotation?", embedClass.getSimpleName(), property))
                  );
              }
          } catch (ExpressionCompileException ece) {
              errors.add(
                  CompileError.in(Dom.asRawXml(element))
                      .near(Dom.lineNumberOf(element))
                      .causedBy(CompileErrors.ERROR_COMPILING_PROPERTY)
              );
          }
       }
    }


    /**
     * This method converts an XML element into a specific kind of widget.
     * Special cases are the XML widget, Header, @Require widget. Otherwise a standard
     * widget is created.
     */
    @SuppressWarnings({"JavaDoc"}) @NotNull
    private Renderable widgetize(Node preceding, Element element, WidgetChain childsChildren) {

        // Header widget is a special case, where we match by the name of the tag =(
        if ("head".equals(element.getName())) {
          try {
            return registry.headWidget(childsChildren, Dom.parseAttribs(element.attributes()), lexicalScopes.peek());
          } catch (ExpressionCompileException e) {
            errors.add(
                CompileError.in(Dom.asRawXml(element))
                .near(Dom.lineNumberOf(element))
                .causedBy(e)
            );

          }
        }

        //read annotation if available
        String annotation = Dom.readAnnotation(preceding);

        //if there is no annotation, treat as a raw xml-widget (i.e. tag)
        if (null == annotation)
            try {
                checkUriConsistency(element);
                checkFormFields(element);

                return registry.xmlWidget(childsChildren, element.getName(), Dom.parseAttribs(element.attributes()),
                        lexicalScopes.peek());
            } catch (ExpressionCompileException e) {
                errors.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(e)
                );

                return Chains.terminal();
            }

        // Special case: is this a "require" widget? (used for exporting
        //  header tags into enclosing pages).
        if (REQUIRE_WIDGET.equalsIgnoreCase(annotation.trim()))
            try {

                return registry.requireWidget(Dom.stripAnnotation(Dom.asRawXml(element)), lexicalScopes.peek());
            } catch (ExpressionCompileException e) {
                errors.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(e)
                );

                return Chains.terminal();
            }

        // Process as "normal" widget.
        String[] extract = Dom.extractKeyAndContent(annotation);

        // If this is NOT a self-rendering widget, give it an XML child.
        final String widgetName = extract[0].trim().toLowerCase();
        if (!registry.isSelfRendering(widgetName))
            try {
                childsChildren = Chains.singleton(registry.xmlWidget(childsChildren, element.getName(),
                        Dom.parseAttribs(element.attributes()), lexicalScopes.peek()));
            } catch (ExpressionCompileException e) {
                errors.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(e)
                );
            }



        // Recursively build widget from [Key, expression, child widgets].
        try {
            return registry.newWidget(widgetName, extract[1], childsChildren, lexicalScopes.peek());
        } catch (ExpressionCompileException e) {
            errors.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(e)
                );

            // This should never be used.
            return Chains.terminal();
        }
    }




    private Map<String, Type> parseRepeatScope(String[] extract, Element element) {
        RepeatToken repeat = registry.parseRepeat(extract[1]);
        Map<String, Type> context = Maps.newHashMap();

        // Verify that @Repeat was parsed correctly.
        if (null == repeat.var()) {
            errors.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(CompileErrors.MISSING_REPEAT_VAR)
                );
        }
        if (null == repeat.items()) {
            errors.add(
                    CompileError.in(Dom.asRawXml(element))
                    .near(Dom.lineNumberOf(element))
                    .causedBy(CompileErrors.MISSING_REPEAT_ITEMS)
            );
        }

        try {
            Type egressType = lexicalScopes.peek().resolveEgressType(repeat.items());
            Type elementType = Generics.getTypeParameter(egressType, Collection.class.getTypeParameters()[0]);

            context.put(repeat.var(), elementType);
            context.put(repeat.pageVar(), page);
            context.put("index", int.class);
            context.put("isLast", boolean.class);

        } catch (ExpressionCompileException e) {
                errors.add(
                    CompileError.in(Dom.asRawXml(element))
                    .near(Dom.lineNumberOf(element))
                    .causedBy(e)
                );
        }

        return context;
    }




    private void checkFormFields(Element element) {
        if (null == form)
            return;

        Attribute action = form.attribute("action");

        // Only look at contextual uris (i.e. hosted by us).
        if (null == action || (!action.getValue().startsWith("/")))
            return;

        final PageBook.Page page = pageBook.get(action.getValue());

        // Only look at pages we actually have registered.
        if (null == page) {
            warnings.add(
                    CompileError.in(Dom.asRawXml(element))
                    .near(Dom.lineNumberOf(element))
                    .causedBy(CompileErrors.UNRESOLVABLE_FORM_ACTION)
            );

            return;
        }

        // If we're inside a form do a throw-away compile against the target page.
        if ("input".equals(element.getName()) || "textarea".equals(element.getName())) {
            Attribute name = element.attribute("name");

            // Skip submits and buttons.
            if (Dom.skippable(element.attribute("type")))
                return;

            //TODO Skip empty?
            if (null == name) {
                warnings.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(CompileErrors.FORM_MISSING_NAME)
                );

                return;
            }

            // Compile expression path.
            final String expression = name.getValue();
            try {
                new MvelEvaluatorCompiler(page.pageClass())
                        .compile(expression);

            } catch (ExpressionCompileException e) {
                //TODO Very hacky, needed to strip out xmlns attribution.
                warnings.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(CompileErrors.UNRESOLVABLE_FORM_BINDING, e)
                );
            }

        }

    }

    private void checkUriConsistency(Element element) {
        Attribute uriAttrib = element.attribute("action");
        if (null == uriAttrib)
            uriAttrib = element.attribute("src");
        if (null == uriAttrib)
            uriAttrib = element.attribute("href");

        if (null != uriAttrib) {

            // Verify that such a uri exists in the page book,
            // only if it is contextual--ignore abs & relative URIs.
            final String uri = uriAttrib.getValue();
            if (uri.startsWith("/"))
                if (null == pageBook.nonCompilingGet(uri))
                    warnings.add(
                        CompileError.in(Dom.asRawXml(element))
                        .near(Dom.lineNumberOf(element))
                        .causedBy(CompileErrors.UNRESOLVABLE_FORM_ACTION, uri)
                );
        }
    }


}
