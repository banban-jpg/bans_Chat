package egovframework.example.ai.tool;

import egovframework.example.audit.domain.AiToolEvent;
import egovframework.example.audit.mapper.AiToolAuditMapper;
import egovframework.example.auth.domain.*;
import egovframework.example.document.service.DocumentSearchService;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Explicit dedicated migrated/seeded Oracle 26ai schema only. Never selected by ordinary tests. */
@Tag("oracle-integration") @EnabledIfEnvironmentVariable(named="ORACLE_AI_TOOL_TEST", matches="YES")
@SpringBootTest @ActiveProfiles({"oracle", "oracle-it"})
class AiToolOracleIT {
    @Autowired DocumentSearchService documents;
    @Autowired AiToolAuditMapper audit;
    @Autowired JdbcTemplate jdbc;
    @Test @Transactional void existingAclQueriesReadCurrentExternalFlagsAndAuditContainsOnlyMetadata() {
        jdbc.update("UPDATE employee_account SET account_status='ACTIVE' WHERE employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002')");
        var actor = new SessionActor("EMP-002", "D01", AccountStatus.ACTIVE, Set.of(SecurityRole.EMPLOYEE));
        var hits = documents.search(actor, "휴가", 0, 5).items();
        var first = hits.stream().filter(hit -> hit.title().equals("가상 휴가 신청 안내")).findFirst().orElseThrow();
        assertThat(documents.externalSource(actor, first.documentId(), first.versionId(), first.fragmentId()).content()).contains("합성 직원");
        var internal = hits.stream().filter(hit -> hit.title().equals("가상 휴가 FAQ")).findFirst().orElseThrow();
        assertThatThrownBy(() -> documents.externalSource(actor, internal.documentId(), internal.versionId(), internal.fragmentId())).hasMessage("NOT_FOUND");
        assertThat(audit.insert(new AiToolEvent("EMP-002", AiToolEvent.Outcome.SUCCESS, 1, 1))).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_event WHERE event_code='AI_TOOL_PREVIEW' AND resource_type='AI_TOOL' AND resource_id='calls=1;items=1' AND outcome_code='SUCCESS'", Integer.class)).isGreaterThanOrEqualTo(1);
        // Test transaction rolls back; multi-connection revoke/outbound/audit-failure races remain separate IT work.
    }
}
