package com.google.sitebricks.routing;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class PathMatcherTest {
    private static final String EXACT_PATHS = "exactPaths";
    private static final String SINGLE_VAR_PATHS = "getSingleVarPaths";
    private static final String ANTI_VAR_PATHS = "antiVarPaths";
    private static final String VARPATHS_MATCHES = "varpathsMatches";
    private static final String VARPATHS_ANTIMATCHES = "varpaths_antimatches";

    @DataProvider(name = EXACT_PATHS)
    public Object[][] getExactPaths() {
        return new Object[][] {
            { "/wiki", "/wiki",  },
            { "/wiki/pensylvania","/wiki/pensylvania", },
            { "/12", "/12",  },
            { "/", "/",  },
        };
    }

    @Test(dataProvider = EXACT_PATHS)
    public final void matchExactUriPath(final String path, final String incoming) {
        assert new PathMatcherChain.SimplePathMatcher(path)
                .matches(incoming);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Test(dataProvider = EXACT_PATHS)
    public final void matchGreedy(final String path, final String incoming) {
        assert new PathMatcherChain.GreedyPathMatcher("ogog")
                .matches(incoming);
    }


    @DataProvider(name = SINGLE_VAR_PATHS)
    public Object[][] getVarPaths() {
        return new Object[][] {
            { "/wiki/:title", "/wiki/hello",  },
            { "/wiki/:title", "/wiki/ashello",  },
            { "/wiki/:title", "/wiki/hoolig An+*",  },
            { "/wiki/:title/page/:id", "/wiki/hello/page/12",  },
            { "/wiki/:title/page/:id", "/wiki/couwdury/page/12424",  },
            { "/wiki/:title/page/:id", "/wiki/sokdoasd/page/aoskpaokda",  },
            { "/wiki", "/wiki/",  },
            { "/wiki/:title", "/wiki/hello/",  },
        };
    }

    @Test(dataProvider = SINGLE_VAR_PATHS)
    public final void matchPathTemplate(final String path, final String incoming) {
        assert new PathMatcherChain(path)
                .matches(incoming);
    }


    @DataProvider(name = VARPATHS_MATCHES)
    public Object[][] getVarPathsAndMatches() {
        return new Object[][] {
            { "/wiki/:title", "/wiki/hello", new HashMap() {{
                put("title", "hello");
            }}, },
            { "/wiki/:title/:page/:id", "/wiki/hello/page/12", new HashMap() {{
                put("title", "hello");
                put("page", "page");
                put("id", "12");
            }}, },
            { "/wiki/:title/page/:id", "/wiki/sokdoasd/page/aoskpaokda", new HashMap() {{
                put("title", "sokdoasd");
                put("id", "aoskpaokda");
            }}, },
            {"/wiki/:id{\\d+}", "/wiki/123", new HashMap() {{
                put("id", "123");
            }},},
            {"/wiki/:id{[a-z]+}", "/wiki/hello", new HashMap() {{
                put("id", "hello");
            }},},
        };
    }

    @Test(dataProvider = VARPATHS_MATCHES)
    public final void findMatchVariables(final String path, final String incoming, Map<String, String> map) {
        final Map<String, String> stringMap = new PathMatcherChain(path)
                .findMatches(incoming);

        assert null != stringMap;
        assert stringMap.size() == map.size();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            assert map.containsKey(entry.getKey());
            assert map.get(entry.getKey()).equals(entry.getValue());
        }
    }

    @DataProvider(name = VARPATHS_ANTIMATCHES)
    public Object[][] getVarPathsAntiMatches() {
        return new Object[][] {
            { "/wiki/:title", "/wiki/hello", new HashMap() {{
                put("title", "hellol");
            }}, },
            { "/wiki/:title/:page/:id", "/wiki/hello/page/12", new HashMap() {{
                put("title", "hello");
                put("id", "12");
            }}, },
            { "/wiki/:title/page/:id", "/wiki/sokdoasd/page/aoskpaokda", new HashMap() {{
                put("title", "sokdoasd");
                put("id", "aoskpaokda");
                put("pid", "aoskpaokda");
            }}, },
        };
    }

    @Test(dataProvider = VARPATHS_ANTIMATCHES, expectedExceptions = AssertionError.class)
    public final void notFindMatchVariables(final String path, final String incoming, Map<String, String> map) {
        final Map<String, String> stringMap = new PathMatcherChain(path)
                .findMatches(incoming);

        assert null != stringMap;
        assert stringMap.size() == map.size();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            assert map.containsKey(entry.getKey());
            assert map.get(entry.getKey()).equals(entry.getValue());
        }
    }

    @DataProvider(name = ANTI_VAR_PATHS)
    public Object[][] getAntiVarPaths() {
        return new Object[][] {
            { "/wiki/:title", "/clicky/hello",  },
            { "/wiki/:title/page/:id", "/wiki/hello/dago/12",  },
            { "/wiki/:title/page/:id", "/wiki/couwdury/1/12424",  },
            { "/wiki/:title/page/:id", "/wikit/sokdoasd/page/aoskpaokda",  },
            { "/wiki/:title", "/wikia",  },
            { "/wiki", "/",  },
            { "/wiki/fencepost", "/",  },
            { "/wiki/fencepost/stupid", "/",  },
            { "/wiki/hicki", "/wiki",  },
            { "/wiki/:title", "/wiki/",  },
            { "/wiki/:hickory/dickory", "/wiki/dickory",  },
            { "/wiki/:title", "/wiki/hello/bye",  },
        };
    }

    @Test(dataProvider = ANTI_VAR_PATHS)
    public final void notMatchPathTemplate(final String path, final String incoming) {
        assert !new PathMatcherChain(path)
                .matches(incoming);
    }
}
