package egovframework.example.document.repository;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.mapper.DocumentSearchMapper;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @Profile("oracle")
public class OracleDocumentSearchRepository implements DocumentSearchRepository {
 private final DocumentSearchMapper mapper;
 public OracleDocumentSearchRepository(DocumentSearchMapper mapper){this.mapper=mapper;}
 @Transactional(readOnly=true) public List<FragmentRow> find(SessionActor actor,String query,int offset,int limit){return mapper.find(actor.employeeId(),query,offset,limit);}
 @Transactional(readOnly=true) public FragmentRow source(SessionActor actor,long documentId,long versionId,long fragmentId){return mapper.source(actor.employeeId(),documentId,versionId,fragmentId);}
}
