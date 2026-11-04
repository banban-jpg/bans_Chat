package egovframework.example.ai.question;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import egovframework.example.ai.provider.*;
import egovframework.example.ai.tool.ToolRunBudget;
import egovframework.example.audit.domain.AiQuestionEvent;
import egovframework.example.audit.repository.AiQuestionAudit;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.common.exception.ApiException;
import egovframework.example.common.response.RequestIdFilter;
import egovframework.example.document.dto.DocumentModels.SourceFragment;
import egovframework.example.document.service.DocumentSearchService;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.service.OrganizationService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static egovframework.example.ai.question.QuestionModels.*;

/** Bounded single turn; models never decide identity, tool selection, structured facts or source URLs. */
@Service
public class AiQuestionService {
    private static final String NO_ANSWER = "확인 가능한 권한 내 근거가 없어 답변할 수 없습니다.";
    private final CurrentSessionService sessions;
    private final QuestionPolicy policy;
    private final OrganizationService organization;
    private final DocumentSearchService documents;
    private final ChatProvider provider;
    private final AiQuestionAudit audit;
    private final Clock clock;
    private final ObjectMapper drafts = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(4).maxStringLength(512).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public AiQuestionService(CurrentSessionService sessions, QuestionPolicy policy, OrganizationService organization,
                             DocumentSearchService documents, ChatProvider provider, AiQuestionAudit audit, Clock clock) {
        this.sessions = sessions; this.policy = policy; this.organization = organization; this.documents = documents;
        this.provider = provider; this.audit = audit; this.clock = clock;
    }
    private static final class Trace {
        String actor;
        AiQuestionEvent.Model model = AiQuestionEvent.Model.NONE;
        AiQuestionEvent.Outcome outcome = AiQuestionEvent.Outcome.SUCCESS;
        AiQuestionEvent.AnswerKind kind = AiQuestionEvent.AnswerKind.NONE;
        QuestionExecution execution;
    }
    public Answer answer(HttpServletRequest request) {
        var trace = new Trace(); long startNanos = System.nanoTime();
        ToolRunBudget budget = null; boolean acquired = false;
        try {
            trace.actor = sessions.requireCurrent(request).employeeId();
            budget = ToolRunBudget.of(request.getSession(false)); var started = budget.begin(clock); acquired = true;
            var execution = new QuestionExecution(clock, started); trace.execution = execution; execution.stage();
            String input;
            try { input = policy.parse(request.getInputStream().readNBytes(QuestionPolicy.MAX_BYTES + 1)); }
            catch (java.io.IOException discarded) { throw QuestionPolicy.invalid(); }
            var reviewed = policy.review(input);
            Answer result;
            String requestId = RequestIdFilter.from(request);
            if (reviewed.isEmpty()) {
                trace.outcome = AiQuestionEvent.Outcome.DENIED; result = unavailable(requestId, execution);
            } else if (reviewed.get().kind() == QuestionPolicy.Kind.DOCUMENT) {
                result = document(request, reviewed.get(), execution, trace, requestId);
            } else result = structured(request, reviewed.get(), execution, requestId);
            trace.kind = AiQuestionEvent.AnswerKind.valueOf(result.answerType().name());
            return result;
        } catch (ApiException safe) {
            trace.outcome = safe.code().startsWith("AI_UPSTREAM") || safe.code().equals("AI_TIMEOUT")
                    || safe.status().is5xxServerError() ? AiQuestionEvent.Outcome.ERROR : AiQuestionEvent.Outcome.DENIED;
            throw safe;
        } catch (Exception discarded) {
            trace.outcome = AiQuestionEvent.Outcome.ERROR;
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "AI request unavailable.");
        } finally {
            if (acquired) budget.finish();
            int millis = (int) Math.min(60000, Math.max(0, (System.nanoTime() - startNanos) / 1_000_000));
            try {
                audit.record(new AiQuestionEvent(trace.actor, trace.model, trace.kind, trace.outcome,
                        trace.execution == null ? 0 : trace.execution.toolCalls(),
                        trace.execution == null ? 0 : trace.execution.providerCalls(), millis));
            } catch (RuntimeException discarded) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_AUDIT_UNAVAILABLE", "AI audit unavailable.");
            }
        }
    }
    private Answer structured(HttpServletRequest request, QuestionPolicy.Intent intent, QuestionExecution execution, String id) {
        var first = people(request, intent, execution);
        if (first.size() != 1) return unavailable(id, execution);
        var current = people(request, intent, execution);
        sessions.requireCurrent(request); execution.stage();
        if (!first.equals(current)) return new Answer(NO_ANSWER, AnswerType.UNANSWERABLE, List.of(), List.of(), id);
        var e = current.getFirst();
        String answer = "확인된 가상 " + (intent.kind() == QuestionPolicy.Kind.CONTACT ? "직원 연락처: " : "담당자: ")
                + e.displayName() + " · " + e.departmentCode() + " · " + e.companyEmail() + " · 내선 " + e.extensionNo();
        return new Answer(answer, AnswerType.STRUCTURED, current, List.of(), id);
    }
    private List<EmployeeView> people(HttpServletRequest request, QuestionPolicy.Intent intent, QuestionExecution execution) {
        execution.tool(); var actor = sessions.requireCurrent(request);
        var query = new Search(intent.kind() == QuestionPolicy.Kind.CONTACT ? intent.query() : "", "",
                intent.kind() == QuestionPolicy.Kind.TASK_OWNER ? intent.query() : "", 0, 5, "employeeId");
        return organization.syntheticForExternalAi(actor, query).stream()
                .filter(e -> intent.kind() != QuestionPolicy.Kind.CONTACT || e.displayName().equals(intent.query())).toList();
    }
    private Answer document(HttpServletRequest request, QuestionPolicy.Intent intent, QuestionExecution execution, Trace trace, String id) {
        var first = uniqueSource(request, intent, execution);
        if (first == null) return unavailable(id, execution);
        String excerpt = excerpt(first);
        var prompt = new GroundedPrompt(intent, excerpt, execution.remaining());
        if (provider instanceof MockGeminiProvider) trace.model = AiQuestionEvent.Model.MOCK;
        else if (provider instanceof GeminiProvider) trace.model = AiQuestionEvent.Model.GEMINI_FLASH_LITE;
        else throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "AI request unavailable.");
        execution.provider();
        var draft = provider.grounded(sessions.requireCurrent(request), prompt);
        if (draft == null || draft.mock() != (provider instanceof MockGeminiProvider) || !validQuote(draft.text(), prompt)) {
            trace.outcome = AiQuestionEvent.Outcome.DENIED; return unavailable(id, execution);
        }
        var current = uniqueSource(request, intent, execution);
        sessions.requireCurrent(request); execution.stage();
        if (current == null || current.documentId() != first.documentId() || current.versionId() != first.versionId()
                || current.fragmentId() != first.fragmentId() || !excerpt(current).equals(excerpt)) {
            return new Answer(NO_ANSWER, AnswerType.UNANSWERABLE, List.of(), List.of(), id);
        }
        String url = "/api/documents/" + current.documentId() + "/versions/" + current.versionId() + "/fragments/" + current.fragmentId();
        return new Answer("확인된 가상 문서 내용: " + excerpt, AnswerType.DOCUMENT, List.of(),
                List.of(new Source(current.documentId(), current.versionId(), current.fragmentId(), current.title(), url)), id);
    }
    private SourceFragment uniqueSource(HttpServletRequest request, QuestionPolicy.Intent intent, QuestionExecution execution) {
        execution.tool(); var hits = documents.search(sessions.requireCurrent(request), intent.query(), 0, 5);
        // Ambiguous or conflicting candidates are not resolved by a model's guess.
        if (hits.hasMore() || hits.items().size() != 1) return null;
        var hit = hits.items().getFirst(); execution.tool();
        try { return documents.externalSource(sessions.requireCurrent(request), hit.documentId(), hit.versionId(), hit.fragmentId()); }
        catch (ApiException denied) { if (denied.status() == HttpStatus.NOT_FOUND) return null; throw denied; }
    }
    private String excerpt(SourceFragment source) {
        String body = source.content(); return body.substring(0, body.offsetByCodePoints(0, Math.min(GroundedPrompt.MAX_EXCERPT, body.codePointCount(0, body.length()))));
    }
    private boolean validQuote(String draft, GroundedPrompt prompt) {
        if (draft == null || draft.getBytes(StandardCharsets.UTF_8).length > 4096) return false;
        try {
            var node = drafts.readTree(draft);
            return node != null && node.isObject() && node.size() == 2 && node.path("evidenceId").isTextual()
                    && node.path("quote").isTextual() && "S1".equals(node.path("evidenceId").textValue())
                    && prompt.excerpt().equals(node.path("quote").textValue());
        } catch (Exception discarded) { return false; }
    }
    private Answer unavailable(String id, QuestionExecution execution) {
        execution.stage(); return new Answer(NO_ANSWER, AnswerType.UNANSWERABLE, List.of(), List.of(), id);
    }
}
