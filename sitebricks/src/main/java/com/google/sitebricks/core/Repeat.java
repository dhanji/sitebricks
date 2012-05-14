package com.google.sitebricks.core;

import java.util.Collection;

/**
 * <p>
 * Repeats tagged content over a collection. This widget is the equivalent of a
 * closure being projected across the given collection. Where the closure is around
 * the content of the annotated tag (and any nested tags). Use attribute {@code var}
 * to specify the name of the variable each collection entry will be bound to.
 * For example, to create a list of movie titles:
 * </p>
 *
 * <pre>
 * &lt;ul&gt;
 *   {@code @Repeat}(<b>items=movies</b>, var="movie")
 *   &lt;li&gt;${movie.title} starring ${movie.star}&lt;/li&gt;
 * &lt;/ul&gt;
 * </pre>
 *
 * <p>
 * This dynamically produces a repetition of {@code &lt;li&gt;} tags containing the movie title
 * and star for each entry in a collection property {@code movies}. Repeat widgets only
 * work on items in a {@code java.util.Collection} (or subtype like {@code java.util.List}).
 * It does *not* take arrays.
 * </p>
 *
 * <p>
 * Since the items attribute takes the result of an expression, you can place any expression in
 * it that evaluates to a collection:
 * </p>
 *
 * <pre>
 * &lt;ul&gt;
 *   {@code @Repeat}(<b>items=person.siblings</b>, var="sib")
 *   &lt;li&gt;${sib.name}&lt;/li&gt;
 * &lt;/ul&gt;
 * </pre>
 *
 * <p>
 *  This bit of code reads the siblings of a property {@code person} and binds it to a temporary
 * variable {@code sib} that when repeating the list elements.
 *
 * Of course, the repeat widget repeats *any* content inside it, so you are free to nest any
 * content you like inside, and they will be projected over the given collection:
 * </p>
 *
 * <pre>
 * &lt;div&gt;
 *   {@code @Repeat}(items=person.siblings, var="sib")
 *   &lt;div&gt;
 *      {@code @ShowIf}(sib.age > 1)
 *      &lt;div&gt;
 *          &lt;p&gt;${sib.name} is &lt;b&gt;${sib.age}&lt;/b&gt; years old.&lt;/p&gt;
 *      &lt;/div&gt;
 *  &lt;/div&gt;
 * &lt;/div&gt;
 * </pre>
 * 
 * <p>
 * Repeat makes two additional variables available: {@code index} and {@code isLast}. {@code index} 
 * is a zero-based counter that indicates the current item number. {@code isLast} is a boolean that
 * will be true when the last item is being processed.
 * </p>
 *
 * 
 * <p>
 *
 * </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@interface Repeat {
    /**
     * @instanceof java.util.Collection
     * @return Returns any subclass of collection to project across.
     */
    Class<? extends Collection> items();     //not really a Class

    String var() default "__this";
    String pageVar() default "__page";
}
