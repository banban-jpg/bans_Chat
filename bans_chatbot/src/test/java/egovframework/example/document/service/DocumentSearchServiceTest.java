package egovframework.example.document.service;

import egovframework.example.auth.domain.*;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.document.dto.DocumentModels.*;
import egovframework.example.document.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentSearchServiceTest {
 private SessionActor actor(String id,String department,SecurityRole... extra){var roles=new HashSet<SecurityRole>();roles.add(SecurityRole.EMPLOYEE);roles.addAll(List.of(extra));return new SessionActor(id,department,AccountStatus.ACTIVE,roles);}
 @Test void syntheticVacationDeploymentAclRevokeAndSourceVersion() {
  var env=new MockEnvironment();env.setActiveProfiles("local-mock");
  var store=new InMemoryDocumentWorkspace(env);var accounts=mock(AccountRepository.class);
  var docs=new ManagedDocumentService(store,accounts);
  var search=new DocumentSearchService(new InMemoryDocumentSearchRepository(store),store,docs);
  var sales=actor("EMP-002","D01");var engineer=actor("EMP-008","D03",SecurityRole.DOCUMENT_EDITOR);
  var reviewer=actor("EMP-009","D03",SecurityRole.DOCUMENT_MANAGER);
  assertThat(search.search(sales,"휴가",0,10).items()).extracting(SearchHit::title).contains("가상 휴가 신청 안내");
  assertThat(search.search(sales,"배포",0,10).items()).isEmpty();
  assertThat(search.search(actor("EMP-010","D03"),"배포",0,10).items()).hasSize(1);
  assertThat(search.search(actor("EMP-010","D01"),"배포",0,10).items()).isEmpty();
  var firstPage=search.search(sales,"가상",0,1);
  assertThat(firstPage.items()).hasSize(1);assertThat(firstPage.hasMore()).isTrue();
  assertThat(search.search(sales,"가상",1,1).items()).hasSize(1).doesNotContain(firstPage.items().getFirst());
  var hit=search.search(engineer,"배포",0,10).items().getFirst();
  assertThat(search.source(engineer,hit.documentId(),hit.versionId(),hit.fragmentId()).content()).contains("가상 배포");
  assertThatThrownBy(()->search.source(sales,hit.documentId(),hit.versionId(),hit.fragmentId())).hasMessageContaining("NOT_FOUND");
  when(accounts.find("EMP-002")).thenReturn(Optional.of(mock(AuthAccount.class)));
  docs.grant(engineer,"DOC-003",new Grant("EMP-002","READ"),false);
  assertThat(search.search(sales,"배포",0,10).items()).hasSize(1);
  assertThat(search.source(sales,hit.documentId(),hit.versionId(),hit.fragmentId()).content()).contains("가상 배포");
  docs.grant(engineer,"DOC-003",new Grant("EMP-002","READ"),true);
  assertThat(search.search(sales,"배포",0,10).items()).isEmpty();
  assertThatThrownBy(()->search.source(sales,hit.documentId(),hit.versionId(),hit.fragmentId())).hasMessageContaining("NOT_FOUND");
  var eligible=mock(AuthAccount.class);when(eligible.eligible()).thenReturn(true);when(eligible.roles()).thenReturn(Set.of(SecurityRole.EMPLOYEE,SecurityRole.DOCUMENT_MANAGER));
  when(accounts.find("EMP-009")).thenReturn(Optional.of(eligible));
  docs.addVersion(engineer,"DOC-003",new Version("TEXT","가상 배포 새 버전",true,false));
  docs.grant(engineer,"DOC-003",new Grant("EMP-009","READ"),false);
  docs.requestReview(engineer,"DOC-003",new Review("EMP-009"));
  assertThat(search.search(engineer,"배포",0,10).items()).contains(hit);
  assertThat(search.source(engineer,hit.documentId(),hit.versionId(),hit.fragmentId()).content()).contains("가상 배포:");
  docs.decide(reviewer,"DOC-003",true);
  assertThatThrownBy(()->search.source(engineer,hit.documentId(),hit.versionId(),hit.fragmentId())).hasMessageContaining("NOT_FOUND");
  assertThat(search.search(engineer,"새 버전",0,10).items()).hasSize(1);
 }
 @Test void boundsLiteralInjectionTagsAndPublishedOnly(){
  var store=new InMemoryDocumentWorkspace();var accounts=mock(AccountRepository.class);var docs=new ManagedDocumentService(store,accounts);
  var search=new DocumentSearchService(new InMemoryDocumentSearchRepository(store),store,docs);
  var editor=actor("EMP-005","D01",SecurityRole.DOCUMENT_EDITOR);var reviewer=actor("EMP-002","D01",SecurityRole.DOCUMENT_MANAGER);
  var eligible=mock(AuthAccount.class);when(eligible.eligible()).thenReturn(true);when(eligible.roles()).thenReturn(Set.of(SecurityRole.EMPLOYEE,SecurityRole.DOCUMENT_MANAGER));when(accounts.find("EMP-002")).thenReturn(Optional.of(eligible));
  docs.create(editor,new Create("DOC-TAG","Synthetic","ONBOARDING","INTERNAL",null,"TEXT","plain body",true,false,List.of("travel")));
  assertThat(search.search(editor,"travel",0,10).items()).isEmpty();
  docs.grant(editor,"DOC-TAG",new Grant("EMP-002","READ"),false);docs.grant(editor,"DOC-TAG",new Grant("EMP-002","APPROVE"),false);
  docs.requestReview(editor,"DOC-TAG",new Review("EMP-002"));docs.decide(reviewer,"DOC-TAG",true);
  assertThat(search.search(editor,"travel",0,10).items()).hasSize(1);
  assertThat(search.search(editor,"' OR 1=1 --",0,10).items()).isEmpty();
  assertThat(search.search(editor,"%_",0,10).items()).isEmpty();
  assertThatThrownBy(()->search.search(new SessionActor("EMP-005","D01",AccountStatus.SUSPENDED,Set.of(SecurityRole.EMPLOYEE)),"travel",0,10)).hasMessageContaining("AUTH_REQUIRED");
  assertThatThrownBy(()->search.search(editor,"x".repeat(101),0,10)).hasMessageContaining("VALIDATION_FAILED");
  assertThatThrownBy(()->search.search(editor,"travel",101,10)).hasMessageContaining("VALIDATION_FAILED");
  assertThatThrownBy(()->search.search(editor,"travel",0,51)).hasMessageContaining("VALIDATION_FAILED");
  var hit=search.search(editor,"travel",0,1).items().getFirst();
  assertThat(search.search(editor,"travel",1,1).items()).isEmpty();
  docs.withdraw(editor,"DOC-TAG");
  assertThatThrownBy(()->search.source(editor,hit.documentId(),hit.versionId(),hit.fragmentId())).hasMessageContaining("NOT_FOUND");
 }
}
