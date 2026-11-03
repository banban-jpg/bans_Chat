package egovframework.example.organization.mapper;

import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.organization.repository.OrganizationRepository;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

/** Explicit dedicated migrated/seeded synthetic Oracle schema only. Never selected by Surefire. */
@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_ORGANIZATION_TEST", matches="YES")
@SpringBootTest
@ActiveProfiles({"oracle", "oracle-it"})
class OrganizationOracleIT {
 @Autowired OrganizationMapper mapper;
 @Autowired OrganizationRepository repository;
 @Autowired DataSource dataSource;
 @Test void filtersLiteralSearchAndPagesOnOracle() {
  var q=new Search("","D01","",0,2,"employeeId");
  assertThat(mapper.count(q)).isEqualTo(4);
  assertThat(mapper.search(q)).hasSize(2).allMatch(e->e.departmentCode().equals("D01"));
  assertThat(mapper.search(new Search("","D01","",1,2,"employeeId")))
   .extracting(EmployeeView::employeeId).doesNotContainAnyElementsOf(mapper.search(q).stream().map(EmployeeView::employeeId).toList());
  assertThat(mapper.count(new Search("' OR 1=1 --","","",0,20,"name"))).isZero();
  for(String sort:java.util.List.of("employeeId","name","departmentId")) assertThat(mapper.search(new Search("","","",0,100,sort))).hasSize(20);
 }
 @Test @Transactional void rowLockMutationAuditAndCreateParticipateInRollback() {
  var jdbc=new JdbcTemplate(dataSource);
  Long before=jdbc.queryForObject("SELECT COUNT(event_id) FROM audit_event",Long.class);
  assertThat(mapper.lockEmployee("EMP-002")).isEqualTo("EMP-002");
  repository.change("EMP-002",null,"D02","EMP-001");
  assertThat(mapper.state("EMP-002").departmentCode()).isEqualTo("D02");
  repository.change("EMP-002","DEPARTED",null,"EMP-001");
  assertThat(mapper.count(new Search("","","",0,100,"employeeId"))).isEqualTo(19);
  String code="EMP-"+(100000+Math.floorMod(UUID.randomUUID().hashCode(),900000));
  repository.create(new Create(code,"가상검증","D01","사원",code+"@bans.example","1234","합성 검증"),"EMP-001");
  assertThat(mapper.state(code).status()).isEqualTo("ACTIVE");
  assertThat(jdbc.queryForObject("SELECT COUNT(event_id) FROM audit_event",Long.class)).isEqualTo(before+3);
  // Spring rolls back this test transaction. Cross-connection visibility/concurrency still needs a separate IT.
 }
}
