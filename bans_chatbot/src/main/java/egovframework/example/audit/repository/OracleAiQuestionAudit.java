package egovframework.example.audit.repository;
import egovframework.example.audit.domain.AiQuestionEvent;
import egovframework.example.audit.mapper.AiQuestionAuditMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
@Repository @Profile("oracle")
public class OracleAiQuestionAudit implements AiQuestionAudit {
    private final AiQuestionAuditMapper mapper;
    public OracleAiQuestionAudit(AiQuestionAuditMapper mapper) { this.mapper = mapper; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AiQuestionEvent event) { if (mapper.insert(event) != 1) throw new IllegalStateException("AI_QUESTION_AUDIT_REQUIRED"); }
}
