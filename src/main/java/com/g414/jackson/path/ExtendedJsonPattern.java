package com.g414.jackson.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonToken;

import com.g414.jackson.path.impl.JsonPathContext;
import com.g414.jackson.path.impl.JsonPathContext.DLLNode;

public class ExtendedJsonPattern implements JsonPattern {
    protected final List<List<Condition>> target;

    public ExtendedJsonPattern(List<List<Condition>> inTarget) {
        List<List<Condition>> newTarget = new ArrayList<List<Condition>>();
        for (List<Condition> conds : inTarget) {
            newTarget.add(Collections.unmodifiableList(conds));
        }

        this.target = Collections.unmodifiableList(newTarget);
    }

    public boolean matches(Iterable<String> state) {
        JsonPathContext context = (JsonPathContext) state;
        Iterator<DLLNode> nodes = context.nodeIterator();

        boolean isSatisfied = true;

        for (int i = 0; i < target.size(); i++) {
            List<Condition> conds = target.get(i);
            DLLNode node = nodes.hasNext() ? nodes.next() : null;

            for (Condition c : conds) {
                if (node == null) {
                    c.setSatisfied(false);
                } else {
                    c.updateSatisfied(context, node);
                }

                if (!c.isSatisfied()) {
                    isSatisfied = false;
                }
            }
        }

        return isSatisfied;
    }

    public static class Builder {
        private final List<List<Condition>> targets = new ArrayList<List<Condition>>();

        public void add(Condition condition) {
            targets.add(Arrays.asList(condition));
        }

        public void add(Condition... conditions) {
            targets.add(Arrays.asList(conditions));
        }

        public void add(List<Condition> conditions) {
            targets.add(conditions);
        }

        public ExtendedJsonPattern build() {
            return new ExtendedJsonPattern(targets);
        }
    }

    public enum ConditionType {
        PATH_EQUALS, HAVE_SEEN
    }

    public static class Condition {
        private final ConditionType type;
        private final String field;
        private final String value;
        private boolean satisfied;

        protected Condition(ConditionType type, String field, String value) {
            this.type = type;
            this.field = field;
            this.value = value;
            satisfied = false;
        }

        public static Condition newEquals(String field, String value) {
            return new Condition(ConditionType.PATH_EQUALS, field, value);
        }

        public static Condition newHaveSeen(String field, String value) {
            return new Condition(ConditionType.HAVE_SEEN, field, value);
        }

        public ConditionType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public void updateSatisfied(JsonPathContext context, DLLNode node) {
            switch (type) {
            case PATH_EQUALS:
                this.satisfied = evaluateNodeCondition(node);
                break;
            case HAVE_SEEN:
                if (!this.satisfied) {
                    this.satisfied = evaluateNodeCondition(node);
                }
                break;
            }
        }

        private boolean evaluateNodeCondition(DLLNode node) {
            boolean sat = false;

            if (field != null) {
                boolean okField = node.token.equals(JsonToken.FIELD_NAME)
                        && node.next != null
                        && (field.equals(WILDCARD) || node.value.equals(field));
                if (okField && value != null) {
                    sat = (value.equals(WILDCARD) || node.next.value
                            .equals(value));
                } else {
                    sat = okField;
                }
            } else {
                if (value != null) {
                    sat = (value.equals(WILDCARD) || node.value.equals(value));
                }
            }
            return sat;
        }

        public void setSatisfied(boolean satisfied) {
            this.satisfied = satisfied;
        }

        public boolean isSatisfied() {
            return satisfied;
        }
    }
}
