package io.smallrye.graphql.execution;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import static org.junit.jupiter.api.Assertions.*;

public class CoroutineTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/coroutine");
    }

    @Test
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonValue jsonValue = data.get("book");
        assertNotNull(jsonValue);

        JsonObject book = jsonValue.asJsonObject();

        assertNotNull(book);

        assertFalse(book.isNull("title"), "title should not be null");
    }

    @Test
    public void testFailureQuery() {
        JsonArray errors = executeAndGetErrors(FAILURE_TEST_QUERY);

        assertNotNull(errors);
        assertEquals(errors.size(), 1);

        String code = errors.get(0).asJsonObject().getJsonObject("extensions").getString("code");

        assertEquals(code, "custom-error", "expected error code: custom-error");
    }

    private static final String TEST_QUERY = "{\n" +
            "  book(name: \"Lord of the Flies\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

    private static final String FAILURE_TEST_QUERY = "{\n" +
            "  failedBook(name: \"Nekonomicon\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

}
