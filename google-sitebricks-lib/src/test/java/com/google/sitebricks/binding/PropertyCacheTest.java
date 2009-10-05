package com.google.sitebricks.binding;

import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class PropertyCacheTest {

    @Test
    public final void doesPropertyExist() {
        assert new ConcurrentPropertyCache()
                .exists("name", MvelRequestBinderTest.AnObject.class);

        assert new ConcurrentPropertyCache()
                .exists("name", MvelRequestBinderTest.AnObject.class);

        assert !new ConcurrentPropertyCache()
                .exists("notExists", MvelRequestBinderTest.AnObject.class);
    }
}
