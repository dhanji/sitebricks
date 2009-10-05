package com.google.sitebricks.compiler;

import com.google.common.collect.Sets;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Visible;

import net.jcip.annotations.NotThreadSafe;
import org.jetbrains.annotations.Nullable;
import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@NotThreadSafe
public class MvelEvaluatorCompiler implements EvaluatorCompiler {
  private final Class<?> backingType;
  private final Map<String, Class<?>> backingTypes;

  private static final String CLASS = "class";
  private final Set<String> writeableProperties = Sets.newHashSet();
  private ParserContext cachedParserContext;

  public MvelEvaluatorCompiler(Class<?> backingType) {
    this.backingType = backingType;
    this.backingTypes = null;
  }

  public MvelEvaluatorCompiler(Map<String, Class<?>> backingTypes) {
    this.backingTypes = Collections.unmodifiableMap(backingTypes);
    this.backingType = null;
  }

  //memo field caches compiled expressions
  private final Map<String, CompiledExpression> compiled =
      new HashMap<String, CompiledExpression>();


  public Class<?> resolveEgressType(String expression) throws ExpressionCompileException {
    return compileExpression(expression).getKnownEgressType();
  }

  public boolean isWritable(String property) throws ExpressionCompileException {
    // Ensure we have introspected. Relying on sidefx, ugh.
    getParserContext();

    return writeableProperties.contains(property);
  }

  public Class<?> resolveCollectionTypeParameter(String expression)
      throws ExpressionCompileException {

    return (Class<?>) compileExpression(expression)
        .getParserContext()
        .getLastTypeParameters()[0];
  }

  public Evaluator compile(String expression) throws ExpressionCompileException {

    //do *not* inline
    final CompiledExpression compiled = compileExpression(expression);

    return new Evaluator() {
      @Nullable
      public Object evaluate(String expr, Object bean) {
        return MVEL.executeExpression(compiled, bean);
      }

      public void write(String expr, Object bean, Object value) {
        //lets use mvel to store an expression
        MVEL.setProperty(bean, expr, value);
      }

      public Object read(String property, Object contextObject) {
        return MVEL.getProperty(property, contextObject);
      }
    };
  }

  private CompiledExpression compileExpression(String expression)
      throws ExpressionCompileException {
    final CompiledExpression compiledExpression = compiled.get(expression);

    //use cached copy
    if (null != compiledExpression)
      return compiledExpression;

    //otherwise compile expression and cache
    final ExpressionCompiler compiler = new ExpressionCompiler(expression, getParserContext());

    CompiledExpression tempCompiled;
    try {
      tempCompiled = compiler.compile();
    } catch (CompileException ce) {
      throw new ExpressionCompileException(expression, ce.getErrors());
    }

    //store in memo cache
    compiled.put(expression, tempCompiled);

    return tempCompiled;
  }

  private ParserContext getParserContext() throws ExpressionCompileException {
    if (null != cachedParserContext) {
      return cachedParserContext;
    }

    return cachedParserContext = (null != backingType)
        ? singleBackingTypeParserContext() : backingMapParserContext();
  }


  private ParserContext backingMapParserContext() {
    ParserContext context = new ParserContext();
    context.setStrongTyping(true);

    //noinspection unchecked
    context.addInputs((Map) backingTypes);

    return context;
  }

  public List<Token> tokenizeAndCompile(String template) throws ExpressionCompileException {
    return Parsing.tokenize(template, this);
  }

  //generates a parsing context with type information from the backing type's javabean properties
  private ParserContext singleBackingTypeParserContext() throws ExpressionCompileException {
    ParserContext context = new ParserContext();
    context.setStrongTyping(true);

    PropertyDescriptor[] propertyDescriptors;
    try {
      propertyDescriptors = Introspector.getBeanInfo(backingType).getPropertyDescriptors();
    } catch (IntrospectionException e) {
      throw new ExpressionCompileException("Could not read class " + backingType);
    }

    // read @Visible annotated fields.
    for (Field field : backingType.getDeclaredFields()) {
      if (field.isAnnotationPresent(Visible.class)) {
        context.addInput(field.getName(), field.getType());

        if (!field.getAnnotation(Visible.class).readOnly()) {
          writeableProperties.add(field.getName());
        }
      }
    }

    // read javabean properties -- these override @Visible fields.
    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      //skip getClass()
      if (CLASS.equals(propertyDescriptor.getName()))
        continue;

      if (null != propertyDescriptor.getWriteMethod()) {
        writeableProperties.add(propertyDescriptor.getName());
      }

      //if this is a collection, determine its type parameter
      if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())

          //for now, skips collections that are not parametric
          && null != propertyDescriptor.getReadMethod().getGenericReturnType()) {

        final ParameterizedType returnType = (ParameterizedType) propertyDescriptor
            .getReadMethod()
            .getGenericReturnType();

        //box actual parametric type arguments into a Class<?> array
        List<Class<?>> typeParameters = new ArrayList<Class<?>>(1);
        typeParameters.add((Class<?>) returnType.getActualTypeArguments()[0]);

        //TODO unsafe if parametric type is a nested generic?
        context.addInput(propertyDescriptor.getName(), propertyDescriptor.getPropertyType(),
            typeParameters.toArray(new Class[1]));
      } else {
        context.addInput(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
      }
    }

    return context;
  }
}
