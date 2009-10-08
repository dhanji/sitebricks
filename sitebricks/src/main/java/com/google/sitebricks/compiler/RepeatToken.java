package com.google.sitebricks.compiler;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface RepeatToken {
    String VAR = "var";
    String PAGE_VAR = "pageVar";
    String ITEMS = "items";
    String DEFAULT_PAGEVAR = "__page";

    String items();
    String var();
    String pageVar();
}
