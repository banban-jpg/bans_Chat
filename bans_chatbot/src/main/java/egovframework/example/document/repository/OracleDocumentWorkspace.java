package egovframework.example.document.repository;
import egovframework.example.document.domain.ManagedDocument;
import egovframework.example.document.mapper.ManagedDocumentMapper;
import egovframework.example.common.exception.ApiException;
import java.util.*;
import java.util.function.Function;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository @Profile("oracle")
public class OracleDocumentWorkspace implements DocumentWorkspace {
 private final ManagedDocumentMapper mapper;
 public OracleDocumentWorkspace(ManagedDocumentMapper mapper){this.mapper=mapper;}
 private String scope(String value){return switch(value){case "INTERNAL"->"COMPANY";case "SELECTED"->"PRIVATE";default->value;};}
 private String apiScope(String value){return switch(value){case "COMPANY"->"INTERNAL";case "PRIVATE"->"SELECTED";default->value;};}
 private ManagedDocument load(ManagedDocumentMapper.DocumentRow row){
  if(row==null)return null;
  var d=new ManagedDocument(row.id(),row.code(),row.title(),row.category(),row.owner(),apiScope(row.scope()),row.department());
  d.status=row.status();d.reviewer=row.reviewer();d.currentVersionId=row.currentVersionId()==null?0:row.currentVersionId();
  for(var v:mapper.versions(d.id))d.versions.add(new ManagedDocument.Version(v.id(),v.number(),v.format(),v.body(),v.state(),"Y".equals(v.aiSearchable()),"Y".equals(v.externalAllowed())));
  for(var grant:mapper.grants(d.id))d.grants.computeIfAbsent(grant.employeeCode(),k->new HashSet<>()).add(grant.permission());
  d.tags.addAll(mapper.tags(d.id));
  return d;
 }
 @Transactional(readOnly=true) public ManagedDocument find(String code){return load(mapper.find(code));}
 @Transactional(readOnly=true) public List<ManagedDocument> all(){return mapper.all().stream().map(this::load).toList();}
 private void exact(int count){if(count!=1)throw new IllegalStateException("DOCUMENT_WRITE_FAILED");}
 private void audit(String actor,String code,String event){exact(mapper.audit(actor,code,event));}
 private String yn(boolean flag){return flag?"Y":"N";}
 @Transactional public ManagedDocument create(ManagedDocument input,ManagedDocument.Version first,String actor){
  exact(mapper.create(input.code,input.title,input.category,input.owner,scope(input.scope),input.department));
  var row=mapper.find(input.code);if(row==null)throw new IllegalStateException("DOCUMENT_CREATE_FAILED");
  for(var tag:input.tags)exact(mapper.insertTag(row.id(),tag));
  exact(mapper.insertVersion(row.id(),1,first.format(),first.body()));
  Long version=mapper.versionId(row.id(),1);if(version==null)throw new IllegalStateException("DOCUMENT_VERSION_FAILED");
  exact(mapper.insertFragment(version,first.body(),yn(first.aiSearchable())));
  audit(actor,input.code,"DOCUMENT_CREATED");return load(row);
 }
 @Transactional public <T>T locked(String code,Function<ManagedDocument,T> operation){
  var row=mapper.lock(code);if(row==null)throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Resource not found.");
  var doc=load(row);T result=operation.apply(doc);
  if(doc.action==null)return result;
  var v=doc.actionVersion;
  switch(doc.action){
   case "VERSION_ADDED" -> {exact(mapper.insertVersion(doc.id,v.number(),v.format(),v.body()));Long id=mapper.versionId(doc.id,v.number());if(id==null)throw new IllegalStateException("DOCUMENT_VERSION_FAILED");exact(mapper.insertFragment(id,v.body(),yn(v.aiSearchable())));}
   case "DRAFT_EDITED" -> {exact(mapper.editVersion(v.id(),v.format(),v.body()));exact(mapper.editFragment(v.id(),v.body(),yn(v.aiSearchable())));}
   case "REVIEW_REQUESTED" -> {exact(mapper.versionState(doc.pending().id(),"DRAFT","SUBMITTED"));exact(mapper.setReview(doc.id,"IN_REVIEW",doc.reviewer));}
   case "PUBLISHED" -> {exact(mapper.versionState(v.id(),"SUBMITTED","PUBLISHED"));exact(mapper.complete(doc.id,"APPROVED",v.id()));exact(mapper.insertDecision(doc.id,v.id(),doc.actionActor,"APPROVED"));}
   case "REJECTED" -> {exact(mapper.versionState(v.id(),"SUBMITTED","DISCARDED"));exact(mapper.complete(doc.id,doc.status,doc.currentVersionId));exact(mapper.insertDecision(doc.id,v.id(),doc.actionActor,"REJECTED"));}
   case "WITHDRAWN" -> exact(mapper.withdraw(doc.id));
   case "GRANT_ADDED" -> exact(mapper.grant(doc.id,doc.actionTarget,doc.actionPermission,doc.actionActor));
   case "GRANT_REVOKED" -> mapper.revoke(doc.id,doc.actionTarget,doc.actionPermission,doc.actionActor);
   default -> throw new IllegalStateException("DOCUMENT_ACTION_UNKNOWN");
  }
  audit(doc.actionActor,doc.code,"DOCUMENT_"+doc.action);return result;
 }
}
