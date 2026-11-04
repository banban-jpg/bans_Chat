package egovframework.example.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class ToolRegistryTest {
    private final ToolRegistry registry = new ToolRegistry();
    private final ObjectMapper json = new ObjectMapper();
    private byte[] plan(List<?> calls) throws Exception {
        return json.writeValueAsBytes(Map.of("questionId", "VACATION_HELP", "question", "가상 회사의 휴가 신청 안내를 찾아주세요.",
                "externalTransferAcknowledged", true, "calls", calls));
    }
    private byte[] call(String name, Object arguments) throws Exception { return plan(List.of(Map.of("name", name, "arguments", arguments))); }

    @Test void registryAdvertisesExactlyTheFiveReadToolsAndTheirBoundedInputSchemas() {
        assertThat(registry.definitions()).extracting(ToolRegistry.Definition::name).containsExactly(
                "searchEmployees", "findDepartment", "findTaskOwner", "searchDocuments", "getDocumentExcerpt");
        assertThat(registry.definitions()).allSatisfy(d -> {
            assertThat(d.inputSchema().path("type").asText()).isEqualTo("object");
            assertThat(d.inputSchema().path("additionalProperties").asBoolean()).isFalse();
        });
        var employees = registry.definitions().getFirst().inputSchema();
        assertThat(employees.at("/properties/name/maxLength").asInt()).isEqualTo(80);
        assertThat(employees.at("/properties/limit/maximum").asInt()).isEqualTo(5);
    }
    @Test void knownIdentityFieldsAreDroppedAndDoNotBecomeFunctionalArguments() throws Exception {
        var parsed = registry.parse(call("findDepartment", Map.of("callerId", "EMP-001", "role", "ACCOUNT_ADMIN", "departmentId", "D03")));
        assertThat(parsed.calls().getFirst().query()).isEmpty();
        assertThat(parsed.calls().getFirst().toString()).doesNotContain("EMP-001", "ACCOUNT_ADMIN", "D03");
    }
    @Test void rejectsExcessCallsBytesUnknownFieldsAndMissingRequiredFields() throws Exception {
        assertThatThrownBy(() -> registry.parse(plan(List.of()))).hasMessage("AI_TOOL_INPUT_INVALID");
        var item = Map.of("name", "findDepartment", "arguments", Map.of());
        assertThatThrownBy(() -> registry.parse(plan(Collections.nCopies(5, item)))).hasMessage("AI_TOOL_INPUT_INVALID");
        assertThatThrownBy(() -> registry.parse(new byte[8193])).hasMessage("AI_TOOL_INPUT_INVALID");
        for (Map<String, Object> args : List.of(Map.<String,Object>of(), Map.<String,Object>of("name", "가상", "sql", "SELECT * FROM employee"),
                Map.<String,Object>of("name", "가상", "salary", true), Map.<String,Object>of("name", "가상", "synthetic", true))) {
            assertThatThrownBy(() -> registry.parse(call("searchEmployees", args))).hasMessage("AI_TOOL_INPUT_INVALID");
        }
    }
    @ParameterizedTest @ValueSource(strings = {"", " ", "oversized", "newline", "nul"})
    void queryLengthAndControlCharacterBoundsAreEnforced(String kind) throws Exception {
        String query = switch(kind) { case "oversized" -> "x".repeat(81); case "newline" -> "a\nb"; case "nul" -> "a\u0000b"; default -> kind; };
        assertThatThrownBy(() -> registry.parse(call("searchDocuments", Map.of("query", query)))).hasMessage("AI_TOOL_INPUT_INVALID");
    }
    @ParameterizedTest @ValueSource(strings = {"0", "6", "-1", "1.5", "\"2\"", "null", "9223372036854775808"})
    void limitMustBeAnIntegerWithinBounds(String literal) throws Exception {
        String body = new String(call("searchDocuments", Map.of("query", "가상", "limit", 1)), StandardCharsets.UTF_8)
                .replace("\"limit\":1", "\"limit\":" + literal);
        assertThatThrownBy(() -> registry.parse(body.getBytes(StandardCharsets.UTF_8))).hasMessage("AI_TOOL_INPUT_INVALID");
    }
    @Test void malformedDuplicateTrailingJsonAndInvalidSourceIdsAreRejected() throws Exception {
        for (String text : List.of("null", "{} {}", "{\"questionId\":\"x\",\"questionId\":\"y\"}", "[{}]", "not json")) {
            assertThatThrownBy(() -> registry.parse(text.getBytes(StandardCharsets.UTF_8))).hasMessage("AI_TOOL_INPUT_INVALID");
        }
        assertThatThrownBy(() -> registry.parse(call("getDocumentExcerpt", Map.of("documentId", 0, "versionId", 1, "fragmentId", 1)))).hasMessage("AI_TOOL_INPUT_INVALID");
        assertThatThrownBy(() -> registry.parse(call("getDocumentExcerpt", Map.of("documentId", "1", "versionId", 1, "fragmentId", 1)))).hasMessage("AI_TOOL_INPUT_INVALID");
        assertThat(registry.parse(call("getDocumentExcerpt", Map.of("documentId", 1, "versionId", 1, "fragmentId", 1))).calls().getFirst().documentId()).isEqualTo(1);
    }
    @Test void sessionBudgetRejectsParallelRequestsWithoutReleasingTheActiveRun() {
        var clock = new AiToolSecurityTest.MutableClock(); var session = new org.springframework.mock.web.MockHttpSession();
        var budget = ToolRunBudget.of(session); budget.begin(clock);
        assertThatThrownBy(() -> budget.begin(clock)).hasMessage("AI_TOOL_LIMIT");
        assertThatThrownBy(() -> budget.begin(clock)).hasMessage("AI_TOOL_LIMIT");
        budget.finish(); budget.begin(clock); budget.finish();
    }
}
