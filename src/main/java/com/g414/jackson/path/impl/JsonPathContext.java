package com.g414.jackson.path.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;

import com.g414.jackson.path.JsonPattern;

public class JsonPathContext implements Iterable<String> {
    protected DLLNode head;
    protected DLLNode tail;

    public void removeLastIfEqual(JsonToken type) {
        if (this.tail != null && this.tail.token.equals(type)) {
            this.removeLast();
        }
    }

    public void removeLast() {
        if (this.tail != null) {
            if (this.tail.prev != null) {
                this.tail.prev.next = null;
            }

            this.tail = this.tail.prev;
        }

        if (this.tail == null) {
            this.head = null;
        }
    }

    public void append(JsonStreamContext context, JsonToken token, String value) {
        DLLNode newNode = new DLLNode();
        newNode.context = context;
        newNode.token = token;
        newNode.value = value;

        if (head == null) {
            this.head = newNode;
            this.tail = newNode;
        } else {
            this.tail.append(newNode);
            this.tail = newNode;
        }
    }

    public boolean matches(List<String> context, boolean prefixMatch) {
        DLLNode current = this.head;
        for (Object val : context) {
            if (current == null) {
                return false;
            }

            if (!val.equals(JsonPattern.WILDCARD) && !current.value.equals(val)) {
                return false;
            }

            current = current.next;
        }

        if (current != null && !prefixMatch) {
            return false;
        }

        return true;
    }

    public String toString() {
        return getContextAsList().toString();
    }

    public List<String> getContextAsList() {
        List<String> toRet = new ArrayList<String>();
        for (String c : this) {
            toRet.add(c);
        }

        return Collections.unmodifiableList(toRet);
    }

    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private DLLNode next = head;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public String next() {
                DLLNode temp = next;
                next = next.next;

                return temp.value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "remove() not supported");
            }
        };
    }

    public Iterator<DLLNode> nodeIterator() {
        return new Iterator<DLLNode>() {
            private DLLNode next = head;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public DLLNode next() {
                DLLNode temp = next;
                next = next.next;

                return temp;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "remove() not supported");
            }
        };
    }

    public static class DLLNode {
        public DLLNode prev;
        public DLLNode next;
        public JsonStreamContext context;
        public JsonToken token;
        public String value;

        public void append(DLLNode theNext) {
            this.next = theNext;
            theNext.prev = this;
        }
    }
}
