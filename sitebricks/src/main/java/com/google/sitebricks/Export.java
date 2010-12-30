package com.google.sitebricks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO Maybe deprecate this and force resources to be explicitly bound.
 *
 * <p> Mark classes with this to configure loading static resource from that
 * class's neighborhood (i.e. using {@code Class.getResourceAsStream}). Example:
 * <pre>
 * {@literal @}Export(at="/my.js", resource="my.js")
 * public class MyWebPage { .. }
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Export {
  String at();

  String resource();
}
