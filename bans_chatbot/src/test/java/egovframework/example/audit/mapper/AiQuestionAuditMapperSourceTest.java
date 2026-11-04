package egovframework.example.audit.mapper;

import egovframework.example.audit.domain.AiQuestionEvent;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AiQuestionAuditMapperSourceTest {
    @Test void questionAuditBindsOnlyEnumsCountsAndLatencyToExistingOracleTable() throws Exception {
        var config = new Configuration(); String path = "egovframework/example/audit/mapper/AiQuestionAuditMapper.xml";
        try(var xml=getClass().getClassLoader().getResourceAsStream(path)){ new XMLMapperBuilder(xml,config,path,config.getSqlFragments()).parse(); }
        var event = new AiQuestionEvent(null, AiQuestionEvent.Model.GEMINI_FLASH_LITE, AiQuestionEvent.AnswerKind.UNANSWERABLE, AiQuestionEvent.Outcome.DENIED, 4, 1, 60000);
        var bound = config.getMappedStatement(AiQuestionAuditMapper.class.getName()+".insert").getBoundSql(event);
        assertThat(bound.getSql()).contains("INSERT INTO audit_event", "'AI_QUESTION'").doesNotContain("question_text", "answer_text", "body_text", "${");
        assertThat(bound.getParameterMappings()).extracting(mapping->mapping.getProperty()).containsExactly("actorEmployeeCode", "resourceSummary", "outcome");
        assertThat(bound.getParameterMappings().getFirst().getJdbcType()).isEqualTo(org.apache.ibatis.type.JdbcType.VARCHAR);
        assertThat(SystemMetaObject.forObject(event).getValue("resourceSummary").toString().length()).isLessThanOrEqualTo(80);
    }
}
