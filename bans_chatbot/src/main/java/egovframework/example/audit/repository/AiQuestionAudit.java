package egovframework.example.audit.repository;
import egovframework.example.audit.domain.AiQuestionEvent;
public interface AiQuestionAudit { void record(AiQuestionEvent event); }
