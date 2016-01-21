package com.google.sitebricks.routing;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
class PathMatcherChain implements PathMatcher {
    private final List<PathMatcher> path;
    private static final String PATH_SEPARATOR = "/";

    public PathMatcherChain(String path) {
        this.path = toMatchChain(path);
    }

    //converts a string path to a tree of heterogenous matchers
    private static List<PathMatcher> toMatchChain(String path) {
        String[] pieces = path.split(PATH_SEPARATOR);

        List<PathMatcher> matchers = new ArrayList<PathMatcher>();
        for (String piece : pieces) {
            if (piece.startsWith(":") && piece.contains("{") && piece.contains("}")) {
                String name = piece.substring(0, piece.indexOf("{"));
                String pattern = piece.substring(piece.indexOf("{") + 1, piece.lastIndexOf("}"));
                matchers.add(new RegExPathMatcher(pattern, name));
            } else if (piece.startsWith(":") && !piece.contains("{")) {
                matchers.add(new GreedyPathMatcher(piece));
            } else {
                matchers.add(new SimplePathMatcher(piece));
            }
        }

        return Collections.unmodifiableList(matchers);
    }

    public String name() {
        return null;
    }

    public boolean matches(String incoming) {
        final Map<String, String> map = findMatches(incoming);
        return null != map;
    }

    //TODO this whole path matching algorithm is in linear time, could easily be constant time
    public Map<String, String> findMatches(String incoming) {
        String[] pieces = incoming.split(PATH_SEPARATOR);
        int i = 0;

        //too many matchers, short circuit
        if (path.size() > pieces.length)
            return null;

        Map<String, String> values = new HashMap<String, String>();
        for (PathMatcher pathMatcher : path) {

            //sanity to prevent fencepost
            if (i == pieces.length)
                return pathMatcher.matches("") ? values : null;   //go greedy on index paths

            String piece = pieces[i];

            if (!pathMatcher.matches(piece))
                return null;

            //store variable as needed
            final String name = pathMatcher.name();
            if (null != name)
                values.put(name, piece);

            //next piece
            i++;
        }

        return (i == pieces.length) ? values : null;
    }

    @Immutable
    static class SimplePathMatcher implements PathMatcher {
        private final String path;

        SimplePathMatcher(String path) {
            this.path = path;
        }

        public boolean matches(String incoming) {
            return path.equals(incoming);
        }

        //TODO this whole path matching algorithm is in linear time, could easily be constant time
        @NotNull
        public Map<String, String> findMatches(String incoming) {
            return Collections.emptyMap();
        }

        public String name() {
            return null;
        }
    }

    //matches anything, i.e. a variable :blah inside a path template
    @Immutable
    static class GreedyPathMatcher implements PathMatcher {
        private final String variable;

        public GreedyPathMatcher(String piece) {
            this.variable = piece.substring(1);
        }

        public boolean matches(String incoming) {
            return true;
        }

        @NotNull
        public Map<String, String> findMatches(String incoming) {
            return Collections.emptyMap();
        }

        public String name() {
            return variable;
        }
    }

    @Immutable
    static class RegExPathMatcher implements PathMatcher {
        private final Pattern pattern;
        private final String variable;

        public RegExPathMatcher(String pattern, String piece) {
            this.pattern = Pattern.compile(pattern);
            this.variable = piece.substring(1);
        }

        @Override
        public boolean matches(String incoming) {
            return pattern.matcher(incoming).matches();
        }

        @Override
        public String name() {
            return variable;
        }

        @Override
        public Map<String, String> findMatches(String incoming) {
            return Collections.emptyMap();
        }
    }

    //matches nothing, i.e. always returns false (used for blocking sitebricks)
    @Immutable
    static class IgnoringPathMatcher implements PathMatcher {
        public boolean matches(String incoming) {
            return false;
        }

        @NotNull
        public Map<String, String> findMatches(String incoming) {
            return Collections.emptyMap();
        }

        public String name() {
            return "";
        }
    }

    static PathMatcher ignoring() {
        return new IgnoringPathMatcher();
    }

}
