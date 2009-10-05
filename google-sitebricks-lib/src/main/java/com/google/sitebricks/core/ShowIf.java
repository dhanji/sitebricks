package com.google.sitebricks.core;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * Hides or shows content. Place this annotation on any tag to control whether it [and
 *  all children] are hidden or shown. {@code @ShowIf} takes a single direct argument of type
 *  boolean. You must provide this argument as an MVEL expression. For example:
 * </p>
 *
 * <pre>
 * <b>{@code @ShowIf}(true)</b>
 * &lt;p&gt;Hello World!&lt;/p&gt;
 * </pre>
 *
 * <p>
 * Renders the content in the paragraph tags (including the tags themselves), and any
 * nested content. Use the {@code @ShowIf(..)} expression to conditionally render parts of your template,
 * controlled by logic from the page object:
 * </p>
 *
 * <pre>
 * {@code @ShowIf}(<b>movie.length > 2</b>)
 * &lt;div&gt;This movie is really looooong...&lt;/div&gt;
 * </pre>
 *
 * <p>
 * You can, of course, annotate *any* HTML/XML element with {@code @ShowIf}.
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@interface ShowIf {
    boolean value();
}
