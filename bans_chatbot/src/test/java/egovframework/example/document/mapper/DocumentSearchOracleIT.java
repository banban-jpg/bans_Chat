package egovframework.example.document.mapper;

import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Explicit seeded 26ai schema only. Ordinary Maven test excludes this class. */
@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_DOCUMENT_SEARCH_TEST",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class DocumentSearchOracleIT {
 @Autowired DocumentSearchMapper mapper;
 @Autowired JdbcTemplate jdbc;
 @Test @Transactional void currentPublishedAclAndRevocationUseOracle() {
  jdbc.update("UPDATE employee_account SET account_status='ACTIVE' WHERE employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002')");
  var vacation=mapper.find("EMP-002","휴가",0,10);
  assertThat(vacation).extracting(r->r.documentCode()).contains("DOC-001");
  assertThat(mapper.find("EMP-002","배포",0,10)).isEmpty();
  jdbc.update("MERGE INTO document_grant g USING (SELECT d.document_id,e.employee_id,a.employee_id actor_id FROM document d CROSS JOIN employee e CROSS JOIN employee a WHERE d.document_code='DOC-003' AND e.employee_code='EMP-002' AND a.employee_code='EMP-008') s ON (g.document_id=s.document_id AND g.employee_id=s.employee_id AND g.permission_code='READ') WHEN MATCHED THEN UPDATE SET revoked_at=NULL,revoked_by_employee_id=NULL WHEN NOT MATCHED THEN INSERT (document_id,employee_id,permission_code,granted_by_employee_id) VALUES (s.document_id,s.employee_id,'READ',s.actor_id)");
  var deployment=mapper.find("EMP-002","배포",0,10);
  assertThat(deployment).extracting(r->r.documentCode()).contains("DOC-003");
  var link=deployment.stream().filter(r->r.documentCode().equals("DOC-003")).findFirst().orElseThrow();
  assertThat(mapper.source("EMP-002",link.documentId(),link.versionId(),link.fragmentId())).isNotNull();
  jdbc.update("UPDATE document_grant SET revoked_at=SYSTIMESTAMP,revoked_by_employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-008') WHERE document_id=(SELECT document_id FROM document WHERE document_code='DOC-003') AND employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002') AND permission_code='READ'");
  assertThat(mapper.source("EMP-002",link.documentId(),link.versionId(),link.fragmentId())).isNull();
 }
}
