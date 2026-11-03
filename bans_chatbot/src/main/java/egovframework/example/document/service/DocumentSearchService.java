package egovframework.example.document.service;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.dto.DocumentModels.*;
import egovframework.example.document.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DocumentSearchService {
 private final DocumentSearchRepository search;private final DocumentWorkspace workspace;private final ManagedDocumentService documents;
 public DocumentSearchService(DocumentSearchRepository search,DocumentWorkspace workspace,ManagedDocumentService documents){this.search=search;this.workspace=workspace;this.documents=documents;}
 private ApiException error(HttpStatus status,String code){return new ApiException(status,code,"Document request unavailable.");}
 private void active(SessionActor actor){if(actor==null||!actor.activeEmployee())throw error(HttpStatus.UNAUTHORIZED,"AUTH_REQUIRED");}
 private boolean stillReadable(SessionActor actor,DocumentSearchRepository.FragmentRow row){
  var d=workspace.find(row.documentCode());return d!=null&&d.id==row.documentId()&&d.currentVersionId==row.versionId()&&
   d.current()!=null&&"PUBLISHED".equals(d.current().state())&&d.current().aiSearchable()&&documents.canRead(actor,d);
 }
 private String excerpt(String body){int end=body.offsetByCodePoints(0,Math.min(240,body.codePointCount(0,body.length())));return body.substring(0,end);}
 public SearchPage search(SessionActor actor,String query,int page,int size){
  active(actor);if(query==null||query.isBlank()||query.codePointCount(0,query.length())>100||page<0||page>100||size<1||size>50)throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  var rows=search.find(actor,query,page*size,size+1);var items=rows.stream().limit(size).filter(row->stillReadable(actor,row))
   .map(row->new SearchHit(row.documentId(),row.versionId(),row.fragmentId(),row.title(),excerpt(row.body()),row.updatedAt())).toList();
  return new SearchPage(items,page,size,rows.size()>size);
 }
 public SourceFragment source(SessionActor actor,long documentId,long versionId,long fragmentId){
  active(actor);if(documentId<1||versionId<1||fragmentId<1)throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");
  var row=search.source(actor,documentId,versionId,fragmentId);
  if(row==null||!stillReadable(actor,row))throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");
  return new SourceFragment(documentId,versionId,fragmentId,row.title(),row.body(),row.updatedAt());
 }
}
