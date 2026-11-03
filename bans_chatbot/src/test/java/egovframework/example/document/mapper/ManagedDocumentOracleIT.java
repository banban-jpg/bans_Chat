package egovframework.example.document.mapper;

import egovframework.example.document.domain.ManagedDocument;
import egovframework.example.document.repository.DocumentWorkspace;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_DOCUMENT_TEST",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class ManagedDocumentOracleIT {
    @Autowired DocumentWorkspace workspace;
    @Test @Transactional void createsAndLocksDraftWithRollback() {
        String code="DOC-IT-"+java.util.UUID.randomUUID().toString().substring(0,8);
        var d=new ManagedDocument(0,code,"Synthetic IT","ONBOARDING","EMP-005","SELECTED",null);
        workspace.create(d,new ManagedDocument.Version(0,1,"TEXT","synthetic IT body","DRAFT",false,false),"EMP-005");
        assertThat(workspace.find(code).pending().body()).isEqualTo("synthetic IT body");
        workspace.locked(code,locked->{assertThat(locked.pending().state()).isEqualTo("DRAFT");return null;});
        // Spring rolls back the test transaction; real Oracle semantics are only established when this IT runs.
    }
}
