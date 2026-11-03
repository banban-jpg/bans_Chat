package egovframework.example.document.mapper;

import java.nio.file.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ManagedDocumentMapperSourceTest {
    @Test void parsesOracleMapperAndReviewerMigrationOffline() throws Exception {
        var path="egovframework/example/document/mapper/ManagedDocumentMapper.xml";
        var config=new Configuration();
        try(var xml=getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(xml).isNotNull();
            new XMLMapperBuilder(xml,config,path,config.getSqlFragments()).parse();
        }
        assertThat(config.hasStatement(ManagedDocumentMapper.class.getName()+".lock")).isTrue();
        assertThat(config.hasStatement(ManagedDocumentMapper.class.getName()+".insertDecision")).isTrue();
        assertThat(Files.readString(Path.of("db/oracle/V004__document_reviewer_assignment.sql")))
                .contains("review_assignee_id","FOREIGN KEY");
    }
}
