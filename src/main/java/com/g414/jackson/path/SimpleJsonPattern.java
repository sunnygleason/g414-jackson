package com.g414.jackson.path;

import java.util.List;

import com.g414.jackson.path.impl.JsonPathContext;

public class SimpleJsonPattern implements JsonPattern {
    protected final List<String> target;
    protected final int targetSize;
    protected final boolean prefixMatch;

    public SimpleJsonPattern(List<String> target, boolean prefixMatch) {
        this.target = target;
        this.targetSize = target.size();
        this.prefixMatch = prefixMatch;
    }

    public boolean matches(Iterable<String> state) {
        if (state instanceof JsonPathContext) {
            JsonPathContext context = (JsonPathContext) state;

            return context.matches(target, prefixMatch);
        } else {
            int size = 0;

            for (String s : state) {
                if (size == targetSize) {
                    return false;
                }

                String t = target.get(size);
                if (!t.equals(JsonPattern.WILDCARD) && !t.equals(s)) {
                    return false;
                }

                size += 1;
            }

            if ((targetSize < size) && !prefixMatch) {
                return false;
            }

            return true;
        }
    }
}
