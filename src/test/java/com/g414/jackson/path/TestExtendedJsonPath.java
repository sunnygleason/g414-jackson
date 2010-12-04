package com.g414.jackson.path;

import java.io.FileInputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.testng.annotations.Test;

import com.g414.jackson.path.ExtendedJsonPattern.Builder;
import com.g414.jackson.path.ExtendedJsonPattern.Condition;
import com.g414.jackson.path.JsonMatcher.JsonPathResult;

@Test
public class TestExtendedJsonPath {
    public void testExtended() throws Exception {
        // create a pattern builder:
        // someday, somebody will build a pretty parser so this isn't a
        // manual construction...
        Builder find = new ExtendedJsonPattern.Builder();
        find.add(Condition.newEquals(null, "{"));
        find.add(Condition.newHaveSeen("foo", "bar"), Condition.newEquals("b",
                null));
        find.add(Condition.newEquals(null, "["));
        find.add(Condition.newEquals(null, "{"));
        find.add(Condition.newEquals("ok", "*"));

        // create a pattern matcher to iterate over matches
        JsonMatcher m = new JsonMatcher(getJson(), find.build(), true);

        // iterate over results
        for (JsonPathResult r : m) {
            System.out.println(r.context.toString() + "\t\t" + r.toString());
        }
    }

    private static JsonParser getJson() throws Exception {
        JsonFactory fact = new JsonFactory();

        return fact.createJsonParser(new FileInputStream(
                "src/test/json/sample.json.txt"));
    }
}
