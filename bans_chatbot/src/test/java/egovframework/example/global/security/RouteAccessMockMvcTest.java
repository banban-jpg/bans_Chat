package egovframework.example.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.chat.repository.InMemoryChatRepository;
import egovframework.example.document.repository.InMemoryDocumentWorkspace;
import egovframework.example.organization.repository.InMemoryOrganizationRepository;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Every production MVC mapping must have an explicit authentication probe, including local-only routes. */
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles({"test", "fixture-test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RouteAccessMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder passwords;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryOrganizationRepository organization;
    @Autowired InMemoryChatRepository chat;
    @Autowired InMemoryDocumentWorkspace documents;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;

    record Route(String method, String path) {
        String key() { return method + " " + path; }
        String concrete() { return path.replace("{id}", "1").replace("{versionId}", "1")
                .replace("{fragmentId}", "1").replace("{code}", "DOC-ACCESS"); }
        boolean mutation() { return !method.equals("GET"); }
        @Override public String toString() { return key(); }
    }
    static Stream<Route> routes() {
        return Stream.of(
                "GET /", "POST /api/admin/activations", "GET /health", "GET /api/health", "GET /api/auth/csrf",
                "POST /api/auth/activate", "POST /api/auth/login", "POST /api/auth/logout", "GET /api/auth/me",
                "GET /api/employees", "GET /api/departments", "GET /api/tasks/owners",
                "POST /api/admin/employees", "PATCH /api/admin/employees/{id}/status", "PATCH /api/admin/employees/{id}/department",
                "GET /api/demo/employees", "POST /api/chat/rooms", "GET /api/chat/rooms",
                "GET /api/chat/rooms/{id}/messages", "POST /api/chat/rooms/{id}/messages", "PUT /api/chat/rooms/{id}/read-state",
                "POST /api/documents", "POST /api/documents/{code}/versions", "PUT /api/documents/{code}/versions/draft",
                "POST /api/documents/{code}/review-request", "POST /api/documents/{code}/approve", "POST /api/documents/{code}/reject",
                "POST /api/documents/{code}/withdraw", "POST /api/documents/{code}/grants", "DELETE /api/documents/{code}/grants",
                "GET /api/documents/{code}/published", "GET /api/documents/search",
                "GET /api/documents/{id}/versions/{versionId}/fragments/{fragmentId}",
                "GET /api/ai/tools", "POST /api/ai/tools/preview", "GET /ai/tools",
                "POST /api/ai/questions", "GET /api/ai/questions/examples", "GET /ai/questions")
                .map(line -> { var pair = line.split(" ", 2); return new Route(pair[0], pair[1]); });
    }
    static Stream<Route> mutations() { return routes().filter(Route::mutation); }
    @BeforeEach void reset() {
        accounts.resetForTest(); organization.resetForTest(); chat.resetForTest(); documents.resetForTest();
    }
    @Test void inventoryIncludesEveryProductionMvcMapping() {
        var actual = new TreeSet<String>();
        mappings.getHandlerMethods().forEach((mapping, handler) -> {
            if (handler.getBeanType().getPackageName().startsWith("egovframework.example")) {
                for (var method : mapping.getMethodsCondition().getMethods())
                    for (var path : mapping.getPatternValues()) actual.add(method.name() + " " + path);
            }
        });
        assertThat(actual).containsExactlyInAnyOrderElementsOf(routes().map(Route::key).toList());
    }
    @ParameterizedTest(name = "anonymous: {0}") @MethodSource("routes")
    void everyRouteEnforcesItsAnonymousBoundary(Route route) throws Exception {
        var probe = request(HttpMethod.valueOf(route.method()), route.concrete()).with(csrf())
                .contentType("application/json").content("{}");
        int expected = Set.of("GET /", "GET /health", "GET /api/health", "GET /api/auth/csrf").contains(route.key()) ? 200
                : Set.of("POST /api/auth/login", "POST /api/auth/activate").contains(route.key()) ? 400 : 401;
        var response = mvc.perform(probe).andExpect(status().is(expected)).andReturn().getResponse();
        assertThat(response.getHeader("X-Request-Id")).isNotBlank();
        if (expected >= 400) {
            var error = json.readTree(response.getContentAsString());
            assertThat(error.path("requestId").asText()).isEqualTo(response.getHeader("X-Request-Id"));
            assertThat(error.path("code").asText()).isNotBlank();
        }
    }
    @ParameterizedTest(name = "CSRF required: {0}") @MethodSource("mutations")
    void everyMutationRequiresCsrfEvenWithRealAuthenticatedSession(Route route) throws Exception {
        var browser = login("EMP-001");
        mvc.perform(request(HttpMethod.valueOf(route.method()), route.concrete()).session(browser)
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
    @Test void authenticatedReadRoutesResolveAndUnknownPathIs404() throws Exception {
        var editor = login("EMP-005"); var manager = login("EMP-002");
        publish(editor, manager, false);
        var room = mvc.perform(send(post("/api/chat/rooms"), editor, Map.of("recipientEmployeeId", "EMP-002")))
                .andExpect(status().isOk()).andReturn();
        String roomId = json.readTree(room.getResponse().getContentAsString()).at("/data/conversationId").asText();
        for (String path : List.of("/health", "/api/health", "/api/auth/csrf", "/api/auth/me", "/api/employees",
                "/api/departments", "/api/tasks/owners?query=정산", "/api/demo/employees", "/api/chat/rooms",
                "/api/chat/rooms/" + roomId + "/messages", "/api/documents/DOC-ACCESS/published",
                "/api/documents/search?q=ACCESS", "/api/ai/tools", "/api/ai/questions/examples", "/ai/tools", "/ai/questions")) {
            mvc.perform(get(path).session(editor)).andExpect(status().isOk());
        }
        var search = mvc.perform(get("/api/documents/search").param("q", "ACCESS").session(editor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1)).andReturn();
        var hit = json.readTree(search.getResponse().getContentAsString()).at("/data/items/0");
        String source = "/api/documents/" + hit.path("documentId").asLong() + "/versions/" + hit.path("versionId").asLong()
                + "/fragments/" + hit.path("fragmentId").asLong();
        mvc.perform(get(source).session(editor)).andExpect(status().isOk());
        mvc.perform(get("/api/route-that-does-not-exist").session(editor))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(post("/api/health").session(editor).with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }
    @Test void documentDraftRejectNewVersionPublishAndWithdrawRoutesWorkThroughHttp() throws Exception {
        var editor = login("EMP-005"); var manager = login("EMP-002");
        publish(editor, manager, true);
        mvc.perform(get("/api/documents/DOC-ACCESS/published").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.versionNo").value(2));
        mvc.perform(send(post("/api/documents/DOC-ACCESS/withdraw"), editor, null)).andExpect(status().isOk());
        mvc.perform(get("/api/documents/DOC-ACCESS/published").session(manager)).andExpect(status().isNotFound());
        mvc.perform(get("/api/documents/search").param("q", "ACCESS").session(manager))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(0));
    }
    private void publish(MockHttpSession editor, MockHttpSession manager, boolean rejectFirst) throws Exception {
        var version = Map.of("format", "TEXT", "body", "ACCESS synthetic evidence", "aiSearchable", true, "externalAiAllowed", false);
        var create = new LinkedHashMap<String,Object>(version);
        create.putAll(Map.of("documentCode", "DOC-ACCESS", "title", "ACCESS synthetic", "category", "ONBOARDING", "scope", "SELECTED"));
        mvc.perform(send(post("/api/documents"), editor, create)).andExpect(status().isOk());
        mvc.perform(send(put("/api/documents/DOC-ACCESS/versions/draft"), editor, version)).andExpect(status().isOk());
        for (String permission : List.of("READ", "APPROVE"))
            mvc.perform(send(post("/api/documents/DOC-ACCESS/grants"), editor, Map.of("employeeId", "EMP-002", "permission", permission)))
                    .andExpect(status().isOk());
        review(editor);
        if (rejectFirst) {
            mvc.perform(send(post("/api/documents/DOC-ACCESS/reject"), manager, null)).andExpect(status().isOk());
            mvc.perform(get("/api/documents/DOC-ACCESS/published").session(editor)).andExpect(status().isNotFound());
            mvc.perform(send(post("/api/documents/DOC-ACCESS/versions"), editor, version))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(2));
            review(editor);
        }
        mvc.perform(send(post("/api/documents/DOC-ACCESS/approve"), manager, null)).andExpect(status().isOk());
    }
    private void review(MockHttpSession editor) throws Exception {
        mvc.perform(send(post("/api/documents/DOC-ACCESS/review-request"), editor, Map.of("reviewerEmployeeId", "EMP-002")))
                .andExpect(status().isOk());
    }
    private MockHttpSession login(String code) throws Exception {
        String password = UUID.randomUUID() + "Aa1!";
        accounts.provisionForTest(code, passwords.encode(password));
        var result = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode", code, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
    private MockHttpServletRequestBuilder send(MockHttpServletRequestBuilder request, MockHttpSession browser, Object body) throws Exception {
        request.session(browser).with(csrf());
        if (body != null) request.contentType("application/json").content(json.writeValueAsString(body));
        return request;
    }
}
