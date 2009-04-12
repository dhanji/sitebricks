package com.google.sitebricks.compiler;

/**
 * An enumeration of possible compile errors when checking a template.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public enum CompileErrors {
    MISSING_REPEAT_VAR,
    MISSING_REPEAT_ITEMS,
    REPEAT_OVER_ATOM, FORM_MISSING_NAME, UNRESOLVABLE_FORM_BINDING, UNRESOLVABLE_FORM_ACTION, MALFORMED_TEMPLATE, ILLEGAL_EXPRESSION,
  PROPERTY_NOT_WRITEABLE,
  ERROR_COMPILING_PROPERTY,
}
