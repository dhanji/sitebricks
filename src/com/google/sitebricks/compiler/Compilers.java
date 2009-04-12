package com.google.sitebricks.compiler;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Renderable;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(CompilersImpl.class)
public interface Compilers {
    Renderable compileXml(Class<?> page, String template);

    Renderable compileFlat(Class<?> page, String template);
}
