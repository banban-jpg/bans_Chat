package egovframework.example.ai.tool;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import egovframework.example.ai.provider.ChatProvider;
import egovframework.example.ai.service.ReviewedSyntheticQuestions;
import egovframework.example.audit.domain.AiToolEvent;
import egovframework.example.audit.repository.AiToolAudit;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.dto.DocumentModels.SourceFragment;
import egovframework.example.document.service.DocumentSearchService;
import egovframework.example.organization.dto.DirectoryModels.Search;
import egovframework.example.organization.service.OrganizationService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AiToolService {
    public record Preview(boolean mock, int toolCalls, int returnedItems, String notice) { }
    private final CurrentSessionService sessions;
    private final ToolRegistry registry;
    private final ReviewedSyntheticQuestions questions;
    private final OrganizationService organization;
    private final DocumentSearchService documents;
    private final ChatProvider provider;
    private final AiToolAudit audit;
    private final Clock clock;
    public AiToolService(CurrentSessionService sessions, ToolRegistry registry, ReviewedSyntheticQuestions questions,
                         OrganizationService organization, DocumentSearchService documents, ChatProvider provider,
                         AiToolAudit audit, Clock clock) {
        this.sessions = sessions; this.registry = registry; this.questions = questions; this.organization = organization;
        this.documents = documents; this.provider = provider; this.audit = audit; this.clock = clock;
    }

    public Preview preview(HttpServletRequest request) {
        String actorCode = null; int callCount = 0, itemCount = 0;
        AiToolEvent.Outcome outcome = AiToolEvent.Outcome.ERROR;
        ToolRunBudget budget = null; boolean acquired = false;
        try {
            actorCode = sessions.requireCurrent(request).employeeId();
            budget = ToolRunBudget.of(request.getSession(false)); Instant started = budget.begin(clock); acquired = true;
            ToolRegistry.Plan plan;
            try { plan = registry.parse(request.getInputStream().readNBytes(ToolRegistry.MAX_INPUT_BYTES + 1)); }
            catch (java.io.IOException discarded) { throw ToolRegistry.invalid(); }
            callCount = plan.calls().size(); questions.requireReviewed(plan.questionId(), plan.question(), plan.acknowledged());
            if (!(provider instanceof egovframework.example.ai.provider.MockGeminiProvider)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "AI_TOOL_LIVE_DISABLED", "Live tool transfer is disabled.");
            }
            // First pass checks each proposed operation. Re-run reads at the final Mock send boundary:
            // never reuse a cached result after another tool or a permission/status change.
            readAll(request, plan, started);
            questions.requireReviewed(plan.questionId(), plan.question(), plan.acknowledged());
            var results = readAll(request, plan, started);
            sessions.requireCurrent(request); ToolRunBudget.checkTime(started, clock);
            var payload = new ToolPayload(plan.question(), results); payload.outboundJson();
            itemCount = payload.itemCount();
            var response = provider.previewTools(payload);
            if (!response.mock()) throw new ApiException(HttpStatus.FORBIDDEN, "AI_TOOL_LIVE_DISABLED", "Live tool transfer is disabled.");
            outcome = AiToolEvent.Outcome.SUCCESS;
            return new Preview(true, callCount, itemCount, ReviewedSyntheticQuestions.NOTICE);
        } catch (ApiException safe) {
            outcome = safe.status().is4xxClientError() ? AiToolEvent.Outcome.DENIED : AiToolEvent.Outcome.ERROR; throw safe;
        } finally {
            if (acquired) budget.finish();
            try { audit.record(new AiToolEvent(actorCode, outcome, callCount, itemCount)); }
            catch (RuntimeException discarded) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_AUDIT_UNAVAILABLE", "Tool audit unavailable.");
            }
        }
    }

    private List<ToolPayload.Item> readAll(HttpServletRequest request, ToolRegistry.Plan plan, Instant started) {
        var results = new ArrayList<ToolPayload.Item>(); int total = 0;
        for (ToolRegistry.Call call : plan.calls()) {
            ToolRunBudget.checkTime(started, clock);
            var actor = sessions.requireCurrent(request); var rows = new ArrayList<ObjectNode>();
            switch (call.name()) {
                case searchEmployees, findTaskOwner -> {
                    var q = new Search(call.name() == ToolRegistry.Name.searchEmployees ? call.query() : "", "",
                            call.name() == ToolRegistry.Name.findTaskOwner ? call.query() : "", 0, call.limit(), "employeeId");
                    for (var e : organization.syntheticForExternalAi(actor, q)) {
                        var row = JsonNodeFactory.instance.objectNode();
                        row.put("employeeId", e.employeeId()); row.put("displayName", e.displayName());
                        row.put("departmentCode", e.departmentCode()); row.put("jobTitle", e.jobTitle());
                        row.put("companyEmail", e.companyEmail()); row.put("extensionNo", e.extensionNo()); rows.add(row);
                    }
                }
                case findDepartment -> organization.syntheticDepartmentForExternalAi(actor).forEach(d -> {
                    var row = JsonNodeFactory.instance.objectNode(); row.put("departmentCode", d.departmentCode()); row.put("departmentName", d.departmentName()); rows.add(row);
                });
                case searchDocuments -> {
                    for (var hit : documents.search(actor, call.query(), 0, call.limit()).items()) {
                        try { rows.add(evidence(documents.externalSource(sessions.requireCurrent(request), hit.documentId(), hit.versionId(), hit.fragmentId()))); }
                        catch (ApiException denied) { if (denied.status() != HttpStatus.NOT_FOUND) throw denied; }
                    }
                }
                case getDocumentExcerpt -> rows.add(evidence(documents.externalSource(actor, call.documentId(), call.versionId(), call.fragmentId())));
            }
            if (rows.size() > ToolRegistry.MAX_ITEMS || (total += rows.size()) > ToolRegistry.MAX_TOTAL_ITEMS) throw ToolRegistry.invalid();
            results.add(new ToolPayload.Item(call.name(), rows));
        }
        ToolRunBudget.checkTime(started, clock); return List.copyOf(results);
    }
    private ObjectNode evidence(SourceFragment source) {
        var row = JsonNodeFactory.instance.objectNode(); row.put("documentId", source.documentId());
        row.put("versionId", source.versionId()); row.put("fragmentId", source.fragmentId());
        String body = source.content(); int end = body.offsetByCodePoints(0, Math.min(ToolRegistry.MAX_EXCERPT, body.codePointCount(0, body.length())));
        row.put("excerpt", body.substring(0, end)); return row;
    }
}
