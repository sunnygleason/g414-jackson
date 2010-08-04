package com.g414.jackson.path;

public interface JsonPattern {
    public static final Object WILDCARD = "*";

    public boolean matches(Iterable<String> readContext) throws Exception;
}
