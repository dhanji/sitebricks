package com.google.sitebricks.rendering.resource;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Respond;
import com.google.sitebricks.Export;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(ClasspathResourcesService.class)
public interface ResourcesService {
    void add(Class<?> clazz, Export export);

    Respond serve(String uri);
}
