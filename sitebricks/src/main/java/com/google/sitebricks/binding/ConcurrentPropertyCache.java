package com.google.sitebricks.binding;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class ConcurrentPropertyCache implements PropertyCache {
    private final ConcurrentMap<Class<?>, Map<String, String>> cache =
                            new ConcurrentHashMap<Class<?>, Map<String, String>>();

    public boolean exists(String property, Class<?> anObjectClass) {

        Map<String, String> properties = cache.get(anObjectClass);

        //cache bean properties if needed
        if (null == properties) {
            PropertyDescriptor[] propertyDescriptors;
            try {
                propertyDescriptors = Introspector
                        .getBeanInfo(anObjectClass)
                        .getPropertyDescriptors();

            } catch (IntrospectionException e) {
                throw new IllegalArgumentException();
            }
    
            properties = new LinkedHashMap<String, String>();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                properties.put(descriptor.getName(), descriptor.getName());    //apply labels here as needed
            }

            cache.putIfAbsent(anObjectClass, properties);
        }

        return properties.containsKey(property);
    }
}
