package com.g414.jackson.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import com.g414.jackson.path.impl.JsonPathContext;

public class JsonMatcher implements Iterable<JsonMatcher.JsonPathResult> {
    protected final List<String> EMPTY_CONTEXT = Collections.emptyList();
    protected final JsonPattern pattern;
    protected final ObjectMapper mapper;
    protected final JsonParser parser;
    protected final boolean returnObject;
    protected final boolean useContext;
    protected final JsonPathContext jsonContext = new JsonPathContext();
    protected ContextIterator iter;
    protected boolean removeField = false;
    protected JsonPathResult next = null;

    public JsonMatcher(JsonParser parser, JsonPattern pattern, boolean useContext)
            throws Exception {
        this(parser, new ObjectMapper(), pattern, true, useContext);
    }

    public JsonMatcher(JsonParser parser, ObjectMapper mapper,
            JsonPattern pattern, boolean returnObject, boolean useContext)
            throws Exception {
        this.parser = parser;
        this.pattern = pattern;
        this.mapper = mapper;
        this.returnObject = returnObject;
        this.useContext = useContext;
    }

    @Override
    public Iterator<JsonPathResult> iterator() {
        if (this.iter != null) {
            throw new IllegalStateException(
                    "iterator() may only be called once!");
        } else {
            this.iter = new ContextIterator();
        }

        return iter;
    }

    protected class ContextIterator implements Iterator<JsonPathResult> {
        {
            try {
                advance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Iterator<String> getContext() {
            return jsonContext.iterator();
        }

        public boolean hasNext() {
            return next != null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance() throws Exception {
            for (JsonToken k = parser.nextToken(); k != null; k = parser
                    .nextToken()) {
                if (removeField) {
                    jsonContext.removeLast();
                    jsonContext.removeLastIfEqual(JsonToken.FIELD_NAME);
                }

                removeField = false;

                switch (k) {
                case FIELD_NAME:
                    jsonContext.append(parser.getParsingContext(), k, parser
                            .getText());
                    break;
                case START_OBJECT:
                case START_ARRAY:
                    jsonContext.append(parser.getParsingContext(), k, parser
                            .getText());
                    break;
                case VALUE_EMBEDDED_OBJECT:
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                case VALUE_STRING:
                case VALUE_TRUE:
                    jsonContext.append(parser.getParsingContext(), k, parser
                            .getText());
                    removeField = true;
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    removeField = true;
                    continue;
                case NOT_AVAILABLE:
                default:
                    break;
                }

                if (pattern.matches(jsonContext)) {
                    List<String> context = useContext ? jsonContext
                            .getContextAsList() : EMPTY_CONTEXT;
                    Object toRet = parser.getText();

                    if (returnObject) {
                        switch (k) {
                        case START_OBJECT:
                            toRet = mapper.readValue(parser,
                                    LinkedHashMap.class);
                            jsonContext.removeLast();
                            jsonContext.removeLastIfEqual(JsonToken.FIELD_NAME);
                            break;
                        case START_ARRAY:
                            toRet = mapper.readValue(parser, ArrayList.class);
                            jsonContext.removeLast();
                            jsonContext.removeLastIfEqual(JsonToken.FIELD_NAME);
                            break;
                        }
                    }

                    next = new JsonPathResult(context, toRet);
                    return;
                }
            }

            next = null;
            parser.close();
        }

        public JsonPathResult next() {
            JsonPathResult temp = next;

            try {
                advance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return temp;
        }
    }

    public static class JsonPathResult {
        protected final List<String> context;
        protected final Object result;

        public JsonPathResult(List<String> context, Object result) {
            this.context = context;
            this.result = result;
        }

        public List<String> getContext() {
            return context;
        }

        public Object getResult() {
            return result;
        }

        public String toString() {
            return result.toString();
        }
    }
}
