package egovframework.example.document.mapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Tagged suite reuses existing Maven Failsafe isolation; dedicated seeded schema only. */
@Tag("oracle-integration") @EnabledIfEnvironmentVariable(named="ORACLE_SECURITY_TEST",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class SecurityAttackOracleIT {
 @Autowired JdbcTemplate jdbc; @Autowired DocumentSearchMapper mapper;
 @Test @Transactional void sqlInjectionIsLiteralAndRevokedGrantCannotAuthorizeSource(){
  jdbc.update("UPDATE employee_account SET account_status='ACTIVE' WHERE employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002')");
  assertThat(mapper.find("EMP-002","' OR 1=1 --",0,10)).isEmpty();
  jdbc.update("MERGE INTO document_grant g USING (SELECT d.document_id,e.employee_id,a.employee_id actor_id FROM document d CROSS JOIN employee e CROSS JOIN employee a WHERE d.document_code='DOC-003' AND e.employee_code='EMP-002' AND a.employee_code='EMP-008') s ON (g.document_id=s.document_id AND g.employee_id=s.employee_id AND g.permission_code='READ') WHEN MATCHED THEN UPDATE SET revoked_at=NULL,revoked_by_employee_id=NULL WHEN NOT MATCHED THEN INSERT (document_id,employee_id,permission_code,granted_by_employee_id) VALUES (s.document_id,s.employee_id,'READ',s.actor_id)");
  var hit=mapper.find("EMP-002","배포",0,10).stream().filter(row->row.documentCode().equals("DOC-003")).findFirst().orElseThrow();
  assertThat(mapper.source("EMP-002",hit.documentId(),hit.versionId(),hit.fragmentId())).isNotNull();
  jdbc.update("UPDATE document_grant SET revoked_at=SYSTIMESTAMP,revoked_by_employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-008') WHERE document_id=? AND employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002') AND permission_code='READ'",hit.documentId());
  assertThat(mapper.source("EMP-002",hit.documentId(),hit.versionId(),hit.fragmentId())).isNull();
  // Outer rollback only. Cross-connection commit/revoke races and lock ordering remain separate BLOCKED work.
 }
}
