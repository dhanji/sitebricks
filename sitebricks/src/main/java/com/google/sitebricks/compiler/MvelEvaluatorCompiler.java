package com.google.sitebricks.compiler;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import org.jetbrains.annotations.Nullable;
import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Visible;
import com.google.sitebricks.conversion.generics.Generics;
import com.google.sitebricks.conversion.generics.ParameterizedTypeImpl;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 * 
 * TODO make this thread-safe when pages can be compiled on demand 
 */
@NotThreadSafe
public class MvelEvaluatorCompiler implements EvaluatorCompiler {
  private final Class<?> backingType;
  private final Map<String, Type> backingTypes;

  private static final String CLASS = "class";
  private final Set<String> writeableProperties = Sets.newHashSet();
  private final Map<String, Type> egressTypes = Maps.newHashMap();
  private ParserContext cachedParserContext;

  public MvelEvaluatorCompiler(Class<?> backingType) {
    this.backingType = backingType;
    this.backingTypes = null;
  }

  public MvelEvaluatorCompiler(Map<String, Type> backingTypes) {
    this.backingTypes = Collections.unmodifiableMap(backingTypes);
    this.backingType = null;
  }

  //memo field caches compiled expressions
  private final Map<String, CompiledExpression> compiled =
      new HashMap<String, CompiledExpression>();


  public Type resolveEgressType(String expression) throws ExpressionCompileException {

		// try to get the type from the cache
	    Type type = egressTypes.get(expression);
	    if (type != null) {
	    	return type;
	    }
	    
	    CompiledExpression compiled = compileExpression(expression);
		final Class<?> egressClass = compiled.getKnownEgressType();
	    final Type[] parameters = compiled.getParserContext().getLastTypeParameters();
	    
	    if (parameters == null) {
	        // the class is not parameterised (generic)
	    	type = egressClass;
	    }
	    else {
	        // reconstruct the Type from mvel's generics details
	    	type = new ParameterizedTypeImpl(egressClass, parameters, egressClass.getEnclosingClass());
	    }
	    
	    egressTypes.put(expression, type);
	    
	    return type;
  }

  public boolean isWritable(String property) throws ExpressionCompileException {
    // Ensure we have introspected. Relying on sidefx, ugh.
    getParserContext();

    return writeableProperties.contains(property);
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


  @SuppressWarnings({ "unchecked", "rawtypes" })
private ParserContext backingMapParserContext() {
    ParserContext context = new ParserContext();
    context.setStrongTyping(true);

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
    context.addInput("this", backingType);

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
      // skip getClass()
      if (CLASS.equals(propertyDescriptor.getName()))
        continue;

      if (null != propertyDescriptor.getWriteMethod()) {
        writeableProperties.add(propertyDescriptor.getName());
      }

      // if this is a collection, determine its type parameter
      if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {

        Type propertyType;
        if (propertyDescriptor.getReadMethod() != null) {
          propertyType = propertyDescriptor.getReadMethod().getGenericReturnType();
        }
        else {
          propertyType = propertyDescriptor.getWriteMethod().getGenericParameterTypes()[0];
        }

        ParameterizedType collectionType = (ParameterizedType) Generics
            .getExactSuperType(propertyType, Collection.class);

        Class<?>[] parameterClasses = new Class[1];
        Type parameterType = collectionType.getActualTypeArguments()[0];
        parameterClasses[0] = Generics.erase(parameterType);
        
        context.addInput(propertyDescriptor.getName(), propertyDescriptor.getPropertyType(), parameterClasses);
      } else {
        context.addInput(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
      }
    }

    return context;
  }
}
