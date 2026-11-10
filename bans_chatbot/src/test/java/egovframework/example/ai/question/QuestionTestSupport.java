package egovframework.example.ai.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.ai.provider.*;
import egovframework.example.audit.repository.*;
import egovframework.example.auth.domain.*;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.document.repository.*;
import egovframework.example.document.service.*;
import egovframework.example.organization.repository.InMemoryOrganizationRepository;
import egovframework.example.organization.service.OrganizationService;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import static org.mockito.Mockito.*;

/** Test-only factory for the package-private prompt, never present in the packaged application. */
public class QuestionTestSupport {
    public static GroundedPrompt prompt(String excerpt, Duration timeout) {
        return new GroundedPrompt(new QuestionPolicy().review("연차 신청 절차").orElseThrow(), excerpt, timeout);
    }
    static class MutableClock extends Clock {
        final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-04T00:00:00Z"));
        void advance(long seconds) { now.updateAndGet(time -> time.plusSeconds(seconds)); }
        public Instant instant() { return now.get(); }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
    }
    final ObjectMapper json = new ObjectMapper();
    final InMemoryOrganizationRepository directory = new InMemoryOrganizationRepository();
    final InMemoryAccountRepository accounts = new InMemoryAccountRepository("", new BCryptPasswordEncoder(4), directory);
    final InMemoryDocumentWorkspace store;
    final ManagedDocumentService workflow;
    final OrganizationService organization;
    final DocumentSearchService documents;
    final MockGeminiProvider provider = spy(new MockGeminiProvider());
    final InMemoryAiQuestionAudit audit = new InMemoryAiQuestionAudit();
    final MutableClock clock = new MutableClock();
    QuestionTestSupport() {
        accounts.provisionForTest("EMP-002", new BCryptPasswordEncoder(4).encode(UUID.randomUUID().toString()));
        var env = new MockEnvironment(); env.setActiveProfiles("fixture-test"); store = new InMemoryDocumentWorkspace(env);
        workflow = new ManagedDocumentService(store, accounts); organization = spy(new OrganizationService(directory));
        documents = spy(new DocumentSearchService(new InMemoryDocumentSearchRepository(store), store, workflow));
    }
    AiQuestionService service() { return service(provider, audit); }
    AiQuestionService service(ChatProvider target, AiQuestionAudit sink) {
        return new AiQuestionService(new CurrentSessionService(accounts), new QuestionPolicy(), organization, documents, target, sink, clock);
    }
    MockHttpServletRequest request(String question) throws Exception { return request(question, null); }
    MockHttpServletRequest request(String question, MockHttpSession session) throws Exception {
        var request = new MockHttpServletRequest();
        if (session == null) {
            session = new MockHttpSession(); var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(new AuthenticatedEmployee("EMP-002", accounts.find("EMP-002").orElseThrow().credentialsVersion()), null, List.of()));
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context); SecurityContextHolder.setContext(context);
        }
        request.setSession(session); request.setContent(json.writeValueAsBytes(Map.of("question", question)));
        request.setAttribute(egovframework.example.common.response.RequestIdFilter.ATTRIBUTE, UUID.randomUUID().toString()); return request;
    }
}
