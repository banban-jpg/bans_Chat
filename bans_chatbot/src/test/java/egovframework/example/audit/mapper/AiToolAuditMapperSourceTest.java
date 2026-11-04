package egovframework.example.audit.mapper;

import egovframework.example.audit.domain.AiToolEvent;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AiToolAuditMapperSourceTest {
    @Test void bindsOnlyFixedMetadataToExistingOracleAuditTable() throws Exception {
        String path = "egovframework/example/audit/mapper/AiToolAuditMapper.xml"; var config = new Configuration();
        try (var xml = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(xml).isNotNull(); new XMLMapperBuilder(xml, config, path, config.getSqlFragments()).parse();
        }
        var event = new AiToolEvent("EMP-002", AiToolEvent.Outcome.DENIED, 2, 0);
        var bound = config.getMappedStatement(AiToolAuditMapper.class.getName() + ".insert").getBoundSql(event);
        assertThat(bound.getSql()).contains("INSERT INTO audit_event", "'AI_TOOL_PREVIEW'", "'AI_TOOL'")
                .doesNotContain("EMP-002", "question", "answer", "body_text", "${");
        assertThat(bound.getParameterMappings()).extracting(mapping -> mapping.getProperty())
                .containsExactly("actorEmployeeCode", "resourceSummary", "outcome");
        assertThat(bound.getParameterMappings().getFirst().getJdbcType()).isEqualTo(org.apache.ibatis.type.JdbcType.VARCHAR);
        assertThat(SystemMetaObject.forObject(event).getValue("resourceSummary")).isEqualTo("calls=2;items=0");
    }
}
