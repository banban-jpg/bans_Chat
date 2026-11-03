package egovframework.example.document.service;

import egovframework.example.auth.domain.*;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.document.dto.DocumentModels.*;
import egovframework.example.document.repository.InMemoryDocumentWorkspace;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagedDocumentServiceTest {
    private final InMemoryDocumentWorkspace store=new InMemoryDocumentWorkspace();
    private final AccountRepository accounts=mock(AccountRepository.class);
    private final ManagedDocumentService service=new ManagedDocumentService(store,accounts);
    private SessionActor actor(String id,String dept,SecurityRole... roles){
        var all=new HashSet<SecurityRole>();all.add(SecurityRole.EMPLOYEE);all.addAll(Arrays.asList(roles));
        return new SessionActor(id,dept,AccountStatus.ACTIVE,all);
    }
    private void reviewer(String id){
        var account=mock(AuthAccount.class);
        when(account.eligible()).thenReturn(true);
        when(account.roles()).thenReturn(Set.of(SecurityRole.EMPLOYEE,SecurityRole.DOCUMENT_MANAGER));
        when(accounts.find(id)).thenReturn(Optional.of(account));
    }
    @Test void reviewPublicationVersionImmutabilityAndMinimalAudit(){
        var editor=actor("EMP-005","D01",SecurityRole.DOCUMENT_EDITOR);
        var manager=actor("EMP-002","D01",SecurityRole.DOCUMENT_MANAGER);
        var otherManager=actor("EMP-009","D02",SecurityRole.DOCUMENT_MANAGER);
        var admin=actor("EMP-001","D01",SecurityRole.ACCOUNT_ADMIN);
        reviewer("EMP-002");reviewer("EMP-009");
        String code=service.create(editor,new Create("DOC-TEST","Synthetic policy","ONBOARDING","INTERNAL",null,"MARKDOWN","# Safe\n<script>alert(1)</script>\n![remote](https://remote.example/x)",true,false));
        assertThatThrownBy(()->service.published(manager,code)).hasMessageContaining("NOT_FOUND");
        assertThatThrownBy(()->service.decide(manager,code,true)).hasMessageContaining("DOCUMENT_STATE_CONFLICT");
        service.grant(editor,code,new Grant("EMP-002","READ"),false);
        service.grant(editor,code,new Grant("EMP-002","APPROVE"),false);
        service.requestReview(editor,code,new Review("EMP-002"));
        assertThatThrownBy(()->service.decide(otherManager,code,true)).hasMessageContaining("FORBIDDEN");
        assertThatThrownBy(()->service.decide(admin,code,true)).hasMessageContaining("FORBIDDEN");
        service.decide(manager,code,true);
        var first=service.published(manager,code);
        assertThat(first.versionNo()).isEqualTo(1);
        assertThat(first.renderedHtml()).doesNotContain("<script>","<img", "<a href").contains("&lt;script&gt;");
        assertThat(new DocumentSearchService(new egovframework.example.document.repository.InMemoryDocumentSearchRepository(store),store,service).search(manager,"Synthetic",0,10).items()).hasSize(1);
        assertThat(service.externalCandidates(manager)).isEmpty();
        assertThatThrownBy(()->service.replaceDraft(editor,code,new Version("TEXT","tamper",false,false))).hasMessageContaining("DOCUMENT_STATE_CONFLICT");
        service.addVersion(editor,code,new Version("TEXT","new synthetic draft",true,false));
        assertThat(service.published(manager,code).versionNo()).isEqualTo(1);
        service.requestReview(editor,code,new Review("EMP-002"));
        service.grant(editor,code,new Grant("EMP-002","APPROVE"),true);
        assertThatThrownBy(()->service.decide(manager,code,true)).hasMessageContaining("FORBIDDEN");
        service.grant(editor,code,new Grant("EMP-002","APPROVE"),false);
        service.decide(manager,code,false);
        assertThat(service.published(manager,code).versionNo()).isEqualTo(1);
        assertThat(store.eventsForTest()).allSatisfy(event->{assertThat(event.toString()).doesNotContain("<script>","new synthetic draft");});
    }
    @Test void departmentSelectedRevokeAndSelfApproval(){
        var editor=actor("EMP-005","D01",SecurityRole.DOCUMENT_EDITOR,SecurityRole.DOCUMENT_MANAGER);
        var sameDept=actor("EMP-002","D01",SecurityRole.DOCUMENT_MANAGER);
        var outsider=actor("EMP-010","D02");
        reviewer("EMP-002");when(accounts.find("EMP-010")).thenReturn(Optional.of(mock(AuthAccount.class)));
        var code=service.create(editor,new Create("DOC-SCOPE","Synthetic","ONBOARDING","DEPARTMENT","D01","TEXT","body",false,false));
        service.grant(editor,code,new Grant("EMP-002","READ"),false);
        service.grant(editor,code,new Grant("EMP-002","APPROVE"),false);
        assertThatThrownBy(()->service.requestReview(editor,code,new Review("EMP-005"))).hasMessageContaining("DOCUMENT_STATE_CONFLICT");
        service.requestReview(editor,code,new Review("EMP-002"));service.decide(sameDept,code,true);
        assertThatThrownBy(()->service.published(outsider,code)).hasMessageContaining("NOT_FOUND");
        service.grant(editor,code,new Grant("EMP-010","READ"),false);
        assertThat(service.published(outsider,code).versionNo()).isEqualTo(1);
        service.grant(editor,code,new Grant("EMP-010","READ"),true);
        assertThatThrownBy(()->service.published(outsider,code)).hasMessageContaining("NOT_FOUND");
        service.withdraw(editor,code);
        assertThatThrownBy(()->service.published(sameDept,code)).hasMessageContaining("NOT_FOUND");
    }
    @Test void mockConcurrentReviewAllowsOneDecision() throws Exception {
        var editor=actor("EMP-005","D01",SecurityRole.DOCUMENT_EDITOR);
        var manager=actor("EMP-002","D01",SecurityRole.DOCUMENT_MANAGER);
        reviewer("EMP-002");
        var code=service.create(editor,new Create("DOC-RACE","Synthetic","ONBOARDING","SELECTED",null,"TEXT","draft",false,false));
        service.grant(editor,code,new Grant("EMP-002","READ"),false);
        service.grant(editor,code,new Grant("EMP-002","APPROVE"),false);
        service.requestReview(editor,code,new Review("EMP-002"));
        var pool=Executors.newFixedThreadPool(2);
        try {
            var start=new CountDownLatch(1);
            Callable<Boolean> approve=()->{start.await();try {service.decide(manager,code,true);return true;}catch(egovframework.example.common.exception.ApiException conflict){return false;}};
            var first=pool.submit(approve);var second=pool.submit(approve);start.countDown();
            assertThat(List.of(first.get(),second.get())).containsExactlyInAnyOrder(true,false);
            assertThat(service.published(manager,code).versionNo()).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
}
