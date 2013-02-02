package com.google.sitebricks;

import java.util.Set;

import com.google.sitebricks.headless.Request;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface Renderable {
    void render(Object bound, Respond respond);

    /**
     *
     * @param clazz A class to match.
     * @return Returns a set of children matching the class, searching down
     *  to the leaves of the tree. 
     */
    <T extends Renderable> Set<T> collect(Class<T> clazz);
}
