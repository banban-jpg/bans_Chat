package egovframework.example.audit.repository;
import egovframework.example.audit.domain.AiToolEvent;
public interface AiToolAudit { void record(AiToolEvent event); }
