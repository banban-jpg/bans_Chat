package egovframework.example.ai.tool;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import egovframework.example.common.exception.ApiException;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** A fixed registry and bounded validator. Never resolves methods, classes, SQL or fields from model text. */
@Component
public class ToolRegistry {
    public static final int MAX_INPUT_BYTES = 8192, MAX_CALLS = 4, MAX_ITEMS = 5, MAX_TOTAL_ITEMS = 20;
    public static final int MAX_REQUESTS_PER_MINUTE = 5, MAX_QUERY = 80, MAX_EXCERPT = 200;
    private static final Set<String> IDENTITY = Set.of("callerId", "userId", "employeeId", "role", "roles", "departmentId");
    private final ObjectMapper json = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(10).maxStringLength(500).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public enum Name { searchEmployees, findDepartment, findTaskOwner, searchDocuments, getDocumentExcerpt }
    public record Definition(String name, JsonNode inputSchema) { }
    public record Call(Name name, @com.fasterxml.jackson.annotation.JsonIgnore String query, int limit, long documentId, long versionId, long fragmentId) {
        @Override public String toString() { return "ToolCall[name=" + name + ", arguments=<redacted>]"; }
    }
    public record Plan(String questionId, @com.fasterxml.jackson.annotation.JsonIgnore String question, boolean acknowledged,
                       @com.fasterxml.jackson.annotation.JsonIgnore List<Call> calls) {
        public Plan { calls = List.copyOf(calls); }
        @Override public String toString() { return "ToolPlan[calls=" + calls.size() + ", question=<redacted>]"; }
    }

    public List<Definition> definitions() {
        return Arrays.stream(Name.values()).map(name -> {
            var schema = json.createObjectNode(); schema.put("type", "object"); schema.put("additionalProperties", false);
            var properties = schema.putObject("properties"); var required = schema.putArray("required");
            if (name == Name.searchEmployees || name == Name.findTaskOwner || name == Name.searchDocuments) {
                String field = name == Name.searchEmployees ? "name" : "query";
                properties.putObject(field).put("type", "string").put("minLength", 1).put("maxLength", MAX_QUERY); required.add(field);
                properties.putObject("limit").put("type", "integer").put("minimum", 1).put("maximum", MAX_ITEMS).put("default", MAX_ITEMS);
            } else if (name == Name.getDocumentExcerpt) {
                for (String field : List.of("documentId", "versionId", "fragmentId")) {
                    properties.putObject(field).put("type", "integer").put("minimum", 1).put("maximum", Long.MAX_VALUE); required.add(field);
                }
            }
            return new Definition(name.name(), schema);
        }).toList();
    }

    public Plan parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_INPUT_BYTES) throw invalid();
        try {
            JsonNode root = json.readTree(bytes);
            fields(root, Set.of("questionId", "question", "externalTransferAcknowledged", "calls"));
            String id = text(root, "questionId", 40), question = text(root, "question", 500);
            if (!root.path("externalTransferAcknowledged").isBoolean()) throw invalid();
            var calls = root.path("calls");
            if (!calls.isArray() || calls.isEmpty() || calls.size() > MAX_CALLS) throw invalid();
            var result = new ArrayList<Call>();
            for (JsonNode call : calls) {
                fields(call, Set.of("name", "arguments"));
                Name name;
                try { name = Name.valueOf(text(call, "name", 32)); } catch (IllegalArgumentException unknown) { throw invalid(); }
                var args = call.path("arguments");
                String query = ""; int limit = 1; long document = 0, version = 0, fragment = 0;
                if (name == Name.searchEmployees || name == Name.findTaskOwner || name == Name.searchDocuments) {
                    String field = name == Name.searchEmployees ? "name" : "query";
                    fields(args, Set.of(field, "limit")); query = text(args, field, MAX_QUERY);
                    limit = args.has("limit") ? integer(args, "limit", MAX_ITEMS) : MAX_ITEMS;
                } else if (name == Name.findDepartment) { fields(args, Set.of()); }
                else {
                    fields(args, Set.of("documentId", "versionId", "fragmentId"));
                    document = positive(args, "documentId"); version = positive(args, "versionId"); fragment = positive(args, "fragmentId");
                }
                result.add(new Call(name, query, limit, document, version, fragment));
            }
            return new Plan(id, question, root.path("externalTransferAcknowledged").booleanValue(), result);
        } catch (ApiException safe) { throw safe; }
        catch (Exception discarded) { throw invalid(); }
    }

    private void fields(JsonNode node, Set<String> allowed) {
        if (!node.isObject()) throw invalid();
        node.fieldNames().forEachRemaining(field -> { if (!allowed.contains(field) && !IDENTITY.contains(field)) throw invalid(); });
    }
    private String text(JsonNode node, String name, int max) {
        var value = node.path(name);
        if (!value.isTextual()) throw invalid();
        String text = value.textValue();
        if (text.isBlank() || text.codePointCount(0, text.length()) > max || text.chars().anyMatch(Character::isISOControl)) throw invalid();
        return text;
    }
    private long positive(JsonNode node, String name) {
        var value = node.path(name);
        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() < 1) throw invalid();
        return value.longValue();
    }
    private int integer(JsonNode node, String name, int max) {
        long value = positive(node, name); if (value > max) throw invalid(); return (int) value;
    }
    static ApiException invalid() { return new ApiException(HttpStatus.BAD_REQUEST, "AI_TOOL_INPUT_INVALID", "Invalid tool request."); }
}
