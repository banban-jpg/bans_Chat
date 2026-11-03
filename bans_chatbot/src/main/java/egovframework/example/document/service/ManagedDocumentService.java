package egovframework.example.document.service;
import egovframework.example.auth.domain.*;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.domain.ManagedDocument;
import egovframework.example.document.dto.DocumentModels.*;
import egovframework.example.document.repository.DocumentWorkspace;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
@Service
public class ManagedDocumentService {
 private final DocumentWorkspace docs; private final AccountRepository accounts;
 public ManagedDocumentService(DocumentWorkspace docs,AccountRepository accounts){this.docs=docs;this.accounts=accounts;}
 private ApiException error(HttpStatus status,String code){return new ApiException(status,code,"Document request unavailable.");}
 private void active(SessionActor actor){if(actor==null||!actor.activeEmployee())throw error(HttpStatus.UNAUTHORIZED,"AUTH_REQUIRED");}
 private void editor(SessionActor actor){active(actor);if(!actor.has(SecurityRole.DOCUMENT_EDITOR))throw error(HttpStatus.FORBIDDEN,"FORBIDDEN");}
 private void owner(SessionActor actor,ManagedDocument d){editor(actor);if(!d.owner.equals(actor.employeeId()))throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");}
 private void versionInput(String format,String body,boolean external){
  if(!List.of("TEXT","MARKDOWN").contains(format)||body==null||body.isBlank()||body.length()>20000||external)throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
 }
 private void mark(ManagedDocument d,SessionActor actor,String event){d.action=event;d.actionActor=actor.employeeId();}
 public String create(SessionActor actor,Create input){
  editor(actor);versionInput(input.format(),input.body(),input.externalAiAllowed());
  if(!input.documentCode().matches("DOC-[A-Za-z0-9-]{1,32}")||input.title().length()>200||!List.of("VACATION","EXPENSE","DEPLOYMENT","EQUIPMENT","CONTRACT","ONBOARDING","SECURITY").contains(input.category())||!List.of("INTERNAL","DEPARTMENT","SELECTED").contains(input.scope()))throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  if("DEPARTMENT".equals(input.scope())&&!actor.departmentCode().equals(input.departmentId()))throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  if(!"DEPARTMENT".equals(input.scope())&&input.departmentId()!=null&&!input.departmentId().isBlank())throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  var state=new ManagedDocument(0,input.documentCode(),input.title(),input.category(),actor.employeeId(),input.scope(),input.departmentId());
  if(input.tags()!=null){if(input.tags().size()>8)throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");for(String tag:input.tags()){if(tag==null||!tag.matches("[\\p{L}\\p{N}_-]{1,32}"))throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");state.tags.add(tag);}}
  docs.create(state,new ManagedDocument.Version(0,1,input.format(),input.body(),"DRAFT",input.aiSearchable(),false),actor.employeeId());
  return input.documentCode();
 }
 public int addVersion(SessionActor actor,String code,Version input){
  versionInput(input.format(),input.body(),input.externalAiAllowed());
  return docs.locked(code,d->{owner(actor,d);if("ARCHIVED".equals(d.status)||d.pending()!=null)throw error(HttpStatus.CONFLICT,"DOCUMENT_STATE_CONFLICT");
   int next=d.versions.stream().mapToInt(ManagedDocument.Version::number).max().orElse(0)+1;
   d.actionVersion=new ManagedDocument.Version(0,next,input.format(),input.body(),"DRAFT",input.aiSearchable(),false);mark(d,actor,"VERSION_ADDED");return next;});
 }
 public void replaceDraft(SessionActor actor,String code,Version input){
  versionInput(input.format(),input.body(),input.externalAiAllowed());
  docs.locked(code,d->{owner(actor,d);var pending=d.pending();if(pending==null||!pending.state().equals("DRAFT"))throw error(HttpStatus.CONFLICT,"DOCUMENT_STATE_CONFLICT");
   var changed=new ManagedDocument.Version(pending.id(),pending.number(),input.format(),input.body(),"DRAFT",input.aiSearchable(),false);
   d.versions.set(d.versions.indexOf(pending),changed);d.actionVersion=changed;mark(d,actor,"DRAFT_EDITED");return null;});
 }
 public void requestReview(SessionActor actor,String code,Review input){
  docs.locked(code,d->{owner(actor,d);var v=d.pending();if(v==null||!v.state().equals("DRAFT")||actor.employeeId().equals(input.reviewerEmployeeId()))throw error(HttpStatus.CONFLICT,"DOCUMENT_STATE_CONFLICT");
   var reviewer=accounts.find(input.reviewerEmployeeId());
   if(reviewer.isEmpty()||!reviewer.get().eligible()||!reviewer.get().roles().contains(SecurityRole.DOCUMENT_MANAGER)||!has(d,input.reviewerEmployeeId(),"READ")||!has(d,input.reviewerEmployeeId(),"APPROVE"))throw error(HttpStatus.FORBIDDEN,"FORBIDDEN");
   d.reviewer=input.reviewerEmployeeId();d.status="IN_REVIEW";replaceState(d,v,"SUBMITTED");mark(d,actor,"REVIEW_REQUESTED");return null;});
 }
 public void decide(SessionActor actor,String code,boolean approve){
  docs.locked(code,d->{active(actor);var v=d.pending();if(v==null||!v.state().equals("SUBMITTED")||!"IN_REVIEW".equals(d.status))throw error(HttpStatus.CONFLICT,"DOCUMENT_STATE_CONFLICT");
   if(!actor.has(SecurityRole.DOCUMENT_MANAGER)||!actor.employeeId().equals(d.reviewer)||actor.employeeId().equals(d.owner)||!has(d,actor.employeeId(),"READ")||!has(d,actor.employeeId(),"APPROVE"))throw error(HttpStatus.FORBIDDEN,"FORBIDDEN");
   if(approve){replaceState(d,v,"PUBLISHED");d.currentVersionId=v.id();d.status="APPROVED";}else{replaceState(d,v,"DISCARDED");d.status=d.currentVersionId>0?"APPROVED":"DRAFT";}
   d.reviewer=null;d.actionVersion=v;mark(d,actor,approve?"PUBLISHED":"REJECTED");return null;});
 }
 public void withdraw(SessionActor actor,String code){docs.locked(code,d->{owner(actor,d);if(d.currentVersionId==0||"ARCHIVED".equals(d.status)||d.pending()!=null)throw error(HttpStatus.CONFLICT,"DOCUMENT_STATE_CONFLICT");d.status="ARCHIVED";mark(d,actor,"WITHDRAWN");return null;});}
 public void grant(SessionActor actor,String code,Grant grant,boolean revoke){
  docs.locked(code,d->{owner(actor,d);if(!List.of("READ","APPROVE").contains(grant.permission())||!grant.employeeId().matches("EMP-[0-9]{3,10}")||accounts.find(grant.employeeId()).isEmpty())throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
   var set=d.grants.computeIfAbsent(grant.employeeId(),k->new HashSet<>());
   if(revoke)set.remove(grant.permission());else set.add(grant.permission());
   d.actionTarget=grant.employeeId();d.actionPermission=grant.permission();mark(d,actor,revoke?"GRANT_REVOKED":"GRANT_ADDED");return null;});
 }
 private boolean has(ManagedDocument d,String id,String permission){return d.grants.getOrDefault(id,Set.of()).contains(permission);}
 private void replaceState(ManagedDocument d,ManagedDocument.Version v,String state){d.versions.set(d.versions.indexOf(v),new ManagedDocument.Version(v.id(),v.number(),v.format(),v.body(),state,v.aiSearchable(),v.externalAllowed()));}
 boolean canRead(SessionActor actor,ManagedDocument d){return actor!=null&&actor.activeEmployee()&&d.current()!=null&&!"ARCHIVED".equals(d.status)&&(d.owner.equals(actor.employeeId())||has(d,actor.employeeId(),"READ")||"INTERNAL".equals(d.scope)||"DEPARTMENT".equals(d.scope)&&d.department.equals(actor.departmentCode()));}
 public Published published(SessionActor actor,String code){active(actor);var d=docs.find(code);if(d==null||!canRead(actor,d))throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");var v=d.current();return new Published(d.code,d.title,v.number(),v.format(),render(v.format(),v.body()),v.aiSearchable());}
 public List<Published> externalCandidates(SessionActor actor){active(actor);return docs.all().stream().filter(d->canRead(actor,d)&&d.current().externalAllowed()).map(d->published(actor,d.code)).toList();}
 private String render(String format,String body){String escaped=body.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
  if(format.equals("TEXT"))return "<pre>"+escaped+"</pre>";
  var out=new StringBuilder();for(String line:escaped.split("\n",-1)){if(line.startsWith("## "))out.append("<h2>").append(line.substring(3)).append("</h2>");else if(line.startsWith("# "))out.append("<h1>").append(line.substring(2)).append("</h1>");else out.append("<p>").append(line).append("</p>");}return out.toString();
 }
}
