package com.google.sitebricks.binding;

import net.jcip.annotations.NotThreadSafe;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.servlet.SessionScoped;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 *
 * Used to store binding (or forwarding) information between successive requests.
 */
@SessionScoped
@NotThreadSafe
class BindingFlashCache implements FlashCache {
    private final Map<String, Object> cache = new HashMap<String, Object>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    public <T> void put(String key, T t) {
        cache.put(key, t);
    }
}
