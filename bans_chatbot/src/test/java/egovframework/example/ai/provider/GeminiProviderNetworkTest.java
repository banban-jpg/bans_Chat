package egovframework.example.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import egovframework.example.auth.domain.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Every actual HTTP request in this class targets a server bound to 127.0.0.1. No Google key needed. */
@ExtendWith(OutputCaptureExtension.class)
class GeminiProviderNetworkTest {
    private static final String KEY = "FAKE_STEP12_KEY_SENTINEL";
    private static final String VALID = "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"Hello synthetic user\"}]}}]}";
    private HttpServer server;
    private ExecutorService workers;
    private HttpClient http;
    private URI base;
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> response = new AtomicReference<>(VALID);
    private final AtomicReference<String> retryAfter = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>("application/json");
    private final AtomicReference<String> capturedBody = new AtomicReference<>();
    private final AtomicReference<HttpRequestData> captured = new AtomicReference<>();
    private final CallBudgetTest.MutableClock clock = new CallBudgetTest.MutableClock();
    private final CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
    private volatile boolean stallHeaders, stallBody;
    private record HttpRequestData(String method, URI uri, com.sun.net.httpserver.Headers headers) { }

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        workers = Executors.newVirtualThreadPerTaskExecutor(); server.setExecutor(workers);
        server.createContext("/", this::handle); server.start();
        base = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta/models/");
        http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(100))
                .followRedirects(HttpClient.Redirect.NEVER).proxy(new NoProxy()).build();
    }
    @AfterEach void stop() {
        release.countDown(); server.stop(0); http.shutdownNow(); workers.shutdownNow();
    }
    private static class NoProxy extends ProxySelector {
        public List<Proxy> select(URI uri) { return List.of(Proxy.NO_PROXY); }
        public void connectFailed(URI uri, SocketAddress address, java.io.IOException failure) { }
    }
    private void handle(HttpExchange exchange) throws java.io.IOException {
        try (exchange) {
            calls.incrementAndGet(); captured.set(new HttpRequestData(exchange.getRequestMethod(), exchange.getRequestURI(), exchange.getRequestHeaders()));
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            entered.countDown();
            if (stallHeaders) awaitRelease();
            exchange.getResponseHeaders().set("Content-Type", contentType.get());
            exchange.getResponseHeaders().set("Location", "https://never-follow.example/" + KEY);
            exchange.getResponseHeaders().set("Set-Cookie", "never-retain=" + KEY);
            if (retryAfter.get() != null) exchange.getResponseHeaders().set("Retry-After", retryAfter.get());
            byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            if (stallBody) {
                exchange.getResponseBody().write(bytes, 0, 1); exchange.getResponseBody().flush(); awaitRelease();
            } else exchange.getResponseBody().write(bytes);
        }
    }
    private void awaitRelease() {
        try { release.await(3, TimeUnit.SECONDS); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
    }
    private GeminiProvider provider() { return provider(Duration.ofSeconds(2)); }
    private GeminiProvider provider(Duration timeout) {
        return new GeminiProvider(CallBudgetTest.settings(10, 100, timeout), KEY, clock, http, base);
    }
    private ChatProvider.Result chat(ChatProvider provider) { return provider.chat(ProviderConfigurationTest.actor(), ChatProvider.SyntheticPrompt.HELLO_KO); }

    @Test void wireContractUsesOnlyFixedGreetingTokenCapAndKeyHeader() throws Exception {
        var result = chat(provider());
        assertThat(result.text()).isEqualTo("Hello synthetic user"); assertThat(result.mock()).isFalse();
        var request = captured.get();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.uri().toString()).isEqualTo("/v1beta/models/gemini-2.5-flash-lite:generateContent");
        assertThat(request.headers().getFirst("x-goog-api-key")).isEqualTo(KEY);
        assertThat(request.headers().getFirst("Authorization")).isNull();
        assertThat(request.headers().getFirst("Cookie")).isNull();
        assertThat(capturedBody.get()).doesNotContain(KEY, "EMP-001", "department", "tools", "history", "systemInstruction");
        var body = new ObjectMapper().readTree(capturedBody.get());
        assertThat(body.size()).isEqualTo(2);
        assertThat(body.at("/contents/0/parts/0/text").asText()).isEqualTo(ChatProvider.SyntheticPrompt.HELLO_KO.text());
        assertThat(body.at("/generationConfig/maxOutputTokens").asInt()).isEqualTo(64);
        assertThat(body.at("/generationConfig/candidateCount").asInt()).isEqualTo(1);
        assertThat(result.toString()).doesNotContain(result.text());
        assertThat(new ObjectMapper().writeValueAsString(result)).doesNotContain(result.text());
        assertThat(calls).hasValue(1);
    }

    @ParameterizedTest @ValueSource(ints = {403, 429, 500, 503, 302, 400})
    void failuresAreSanitizedAndNeverRetriedOrRedirected(int upstream, CapturedOutput output) {
        status.set(upstream); response.set("{\"error\":\"" + KEY + " RAW_QUESTION_SENTINEL\"}"); retryAfter.set("2");
        var failure = catchThrowableOfType(() -> chat(provider()), ProviderFailure.class);
        assertThat(failure).isNotNull(); assertThat(failure.getCause()).isNull();
        var printed = new StringWriter(); failure.printStackTrace(new PrintWriter(printed));
        assertThat(printed.toString()).doesNotContain(KEY, "RAW_QUESTION_SENTINEL", "never-follow", "never-retain");
        assertThat(output.getAll()).doesNotContain(KEY, "RAW_QUESTION_SENTINEL");
        assertThat(calls).hasValue(1);
    }

    @Test void rateLimitHonorsRetryAfterAcrossCallsWithoutAutomaticRetry() {
        status.set(429); retryAfter.set("2"); var provider = provider();
        assertThatThrownBy(() -> chat(provider)).hasMessage("AI_UPSTREAM_RATE_LIMITED");
        assertThatThrownBy(() -> chat(provider)).hasMessage("AI_COOLDOWN"); assertThat(calls).hasValue(1);
        clock.advance(Duration.ofSeconds(2)); status.set(200);
        assertThat(chat(provider).mock()).isFalse(); assertThat(calls).hasValue(2);
        assertThat(captured.get().headers().getFirst("Cookie")).isNull();
    }

    @ParameterizedTest @ValueSource(strings = {"date", "bad-value", "9999999999999999999999999999", "-1"})
    void retryAfterAcceptsDatesAndBoundsUntrustedValues(String value) {
        status.set(503);
        retryAfter.set(value.equals("date") ? DateTimeFormatter.RFC_1123_DATE_TIME.format(clock.instant().plusSeconds(7).atZone(ZoneOffset.UTC)) : value);
        var provider = provider();
        var failure = catchThrowableOfType(() -> chat(provider), ProviderFailure.class);
        long expected = value.equals("date") ? 7 : value.startsWith("999") ? 86400 : 1;
        assertThat(failure.retryAfterSeconds()).isEqualTo(expected);
        assertThatThrownBy(() -> chat(provider)).hasMessage("AI_COOLDOWN"); assertThat(calls).hasValue(1);
    }

    @ParameterizedTest @ValueSource(strings = {"", "not json", "{}", "{\"candidates\":[]}",
            "{\"candidates\":[],\"candidates\":[]}", "{} {}", "null", "blocked", "empty", "tool", "truncated", "echo-key", "oversized", "wrong-type"})
    void malformedEmptyBlockedAndUnexpectedResponsesFailClosed(String input) {
        response.set(switch (input) {
            case "blocked" -> VALID.replace("STOP", "SAFETY");
            case "empty" -> VALID.replace("Hello synthetic user", " ");
            case "tool" -> VALID.replace("\"text\":\"Hello synthetic user\"", "\"functionCall\":{\"name\":\"runSql\"}");
            case "truncated" -> VALID.replace("STOP", "MAX_TOKENS");
            case "echo-key" -> VALID.replace("Hello synthetic user", KEY);
            case "oversized" -> "x".repeat(40000);
            case "wrong-type" -> VALID;
            default -> input;
        });
        if (input.equals("wrong-type")) contentType.set("text/html");
        assertThatThrownBy(() -> chat(provider())).isInstanceOf(ProviderFailure.class)
                .hasMessage("AI_INVALID_RESPONSE").hasNoCause();
        assertThat(calls).hasValue(1);
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void boundsBothHeaderAndBodyStalls(boolean headers) {
        stallHeaders = headers; stallBody = !headers;
        long started = System.nanoTime();
        assertThatThrownBy(() -> chat(provider(Duration.ofMillis(250)))).hasMessage("AI_TIMEOUT").hasNoCause();
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(2));
        assertThat(calls).hasValue(1);
    }

    @Test void concurrentRequestIsRejectedImmediatelyAndSlotIsReleased() throws Exception {
        stallHeaders = true; var provider = provider();
        var first = workers.submit(() -> chat(provider));
        assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
        assertThatThrownBy(() -> chat(provider)).hasMessage("AI_BUSY");
        release.countDown(); assertThat(first.get(2, TimeUnit.SECONDS).mock()).isFalse();
        assertThat(chat(provider).mock()).isFalse(); assertThat(calls).hasValue(2);
    }

    @Test void rejectedActorAndNullPromptNeverReachHttp() {
        var provider = provider();
        assertThatThrownBy(() -> provider.chat(null, ChatProvider.SyntheticPrompt.HELLO_KO)).hasMessage("AI_TRANSFER_DENIED");
        var inactive = new SessionActor("EMP-001", "D01", AccountStatus.SUSPENDED, Set.of(SecurityRole.EMPLOYEE));
        assertThatThrownBy(() -> provider.chat(inactive, ChatProvider.SyntheticPrompt.HELLO_KO)).hasMessage("AI_TRANSFER_DENIED");
        assertThatThrownBy(() -> provider.chat(ProviderConfigurationTest.actor(), null)).hasMessage("AI_TRANSFER_DENIED");
        assertThat(calls).hasValue(0);
    }

    @Test void connectionTimeoutAndTransportErrorsDiscardOriginalException() {
        HttpClient broken = mock(HttpClient.class);
        when(broken.sendAsync(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any()))
                .thenReturn(CompletableFuture.failedFuture(new HttpConnectTimeoutException(KEY)));
        var provider = new GeminiProvider(CallBudgetTest.settings(10, 100, Duration.ofSeconds(1)), KEY, clock, broken, base);
        assertThatThrownBy(() -> chat(provider)).hasMessage("AI_TIMEOUT").hasNoCause();
        verify(broken, times(1)).sendAsync(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<byte[]>>any());
        assertThat(calls).hasValue(0);
    }

    @Test void endpointCannotBeOverriddenToAnotherExternalHost() {
        assertThat(GeminiProvider.ENDPOINT.toString()).isEqualTo("https://generativelanguage.googleapis.com/v1beta/models/");
        assertThatThrownBy(() -> new GeminiProvider(CallBudgetTest.settings(10, 100, Duration.ofSeconds(1)), KEY, clock,
                http, URI.create("https://attacker.example/"))).hasMessage("AI_ENDPOINT_FORBIDDEN");
    }

    @Test void liveProviderHasNoToolPayloadTransportPath() {
        assertThatThrownBy(() -> provider().previewTools(null)).hasMessage("AI_TRANSFER_DENIED");
        assertThat(calls).hasValue(0);
    }

    @Test void groundedWireIsSingleFragmentJsonWithNoCallerIdsOrHistory() throws Exception {
        String excerpt="가상 직원은 예시 양식을 사용합니다.";
        String quote=new ObjectMapper().writeValueAsString(Map.of("evidenceId","S1","quote",excerpt));
        var outer=(com.fasterxml.jackson.databind.node.ObjectNode)new ObjectMapper().readTree(VALID);
        ((com.fasterxml.jackson.databind.node.ObjectNode)outer.at("/candidates/0/content/parts/0")).put("text",quote); response.set(outer.toString());
        var prompt=egovframework.example.ai.question.QuestionTestSupport.prompt(excerpt,Duration.ofSeconds(1));
        var reply=provider().grounded(ProviderConfigurationTest.actor(),prompt); assertThat(reply.text()).isEqualTo(quote);
        var wire=new ObjectMapper().readTree(capturedBody.get());
        assertThat(wire.at("/generationConfig/responseMimeType").asText()).isEqualTo("application/json");
        assertThat(wire.at("/generationConfig/maxOutputTokens").asInt()).isEqualTo(64);
        assertThat(capturedBody.get()).contains("S1",excerpt).doesNotContain("documentId","employeeId","departmentId","title",KEY,"history");
        assertThat(calls).hasValue(1);
    }
    @ParameterizedTest @ValueSource(ints={429,503})
    void groundedFailuresAreOneAttemptAndShareProviderCooldown(int code) {
        status.set(code); retryAfter.set("2"); var provider=provider();
        var prompt=egovframework.example.ai.question.QuestionTestSupport.prompt("합성 근거",Duration.ofSeconds(1));
        assertThatThrownBy(()->provider.grounded(ProviderConfigurationTest.actor(),prompt)).isInstanceOf(ProviderFailure.class);
        assertThatThrownBy(()->provider.grounded(ProviderConfigurationTest.actor(),prompt)).hasMessage("AI_COOLDOWN");
        assertThat(calls).hasValue(1);
    }
    @Test void groundedRequestUsesRemainingDeadlineIncludingBodyCompletion() {
        stallBody=true;
        var prompt=egovframework.example.ai.question.QuestionTestSupport.prompt("합성 근거",Duration.ofMillis(250));
        assertThatThrownBy(()->provider().grounded(ProviderConfigurationTest.actor(),prompt)).hasMessage("AI_TIMEOUT");
        assertThat(calls).hasValue(1);
    }
}
