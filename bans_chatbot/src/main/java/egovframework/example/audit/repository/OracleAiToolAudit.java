package egovframework.example.audit.repository;

import egovframework.example.audit.domain.AiToolEvent;
import egovframework.example.audit.mapper.AiToolAuditMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository @Profile("oracle")
public class OracleAiToolAudit implements AiToolAudit {
    private final AiToolAuditMapper mapper;
    public OracleAiToolAudit(AiToolAuditMapper mapper) { this.mapper = mapper; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AiToolEvent event) {
        if (mapper.insert(event) != 1) throw new IllegalStateException("AI_AUDIT_REQUIRED");
    }
}
