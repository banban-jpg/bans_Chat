package egovframework.example.document.repository;
import egovframework.example.document.domain.ManagedDocument;
import java.util.*;
import java.util.function.Function;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import egovframework.example.common.exception.ApiException;
import egovframework.example.document.fixture.SyntheticDocumentFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
@Repository @Profile({"test"})
public class InMemoryDocumentWorkspace implements DocumentWorkspace {
 public record Event(String actor,String code,String action) {}
 private final Map<String,ManagedDocument> docs=new HashMap<>();
 private final List<Event> events=new ArrayList<>();
 private long nextId=1,nextVersion=1;
 public InMemoryDocumentWorkspace() {}
 @Autowired public InMemoryDocumentWorkspace(Environment environment){if(environment.acceptsProfiles(Profiles.of("fixture-test")))loadSyntheticFixture();}
 private synchronized void loadSyntheticFixture(){
  for(var row:SyntheticDocumentFixture.documents()){
   var scope=switch(row.scope()){case COMPANY->"INTERNAL";case PRIVATE->"SELECTED";default->"DEPARTMENT";};
   var d=new ManagedDocument(nextId++,row.documentCode(),row.title(),row.category(),row.ownerCode(),scope,row.departmentCode());
   d.status=row.documentStatus();d.tags.add(row.category());
   var state=row.versionState();var v=new ManagedDocument.Version(nextVersion++,1,row.format(),row.body(),state,row.aiSearchable(),row.externalAiAllowed(),row.synthetic(),row.nonSensitive());
   d.versions.add(v);if("PUBLISHED".equals(state))d.currentVersionId=v.id();
   row.readGrants().forEach(id->d.grants.computeIfAbsent(id,k->new HashSet<>()).add("READ"));
   row.approveGrants().forEach(id->d.grants.computeIfAbsent(id,k->new HashSet<>()).add("APPROVE"));
   docs.put(d.code,d);
  }
 }
 public synchronized void resetForTest(){docs.clear();events.clear();nextId=1;nextVersion=1;}
 public synchronized List<Event> eventsForTest(){return List.copyOf(events);}
 public synchronized ManagedDocument find(String code){var doc=docs.get(code);return doc==null?null:doc.copy();}
 public synchronized List<ManagedDocument> all(){return docs.values().stream().map(ManagedDocument::copy).toList();}
 public synchronized ManagedDocument create(ManagedDocument input,ManagedDocument.Version first,String actor){
  if(docs.containsKey(input.code))throw new ApiException(HttpStatus.CONFLICT,"DOCUMENT_CONFLICT","Document unavailable.");
  var doc=new ManagedDocument(nextId++,input.code,input.title,input.category,input.owner,input.scope,input.department);
  doc.tags.addAll(input.tags);
  doc.versions.add(new ManagedDocument.Version(nextVersion++,1,first.format(),first.body(),"DRAFT",first.aiSearchable(),false));
  docs.put(doc.code,doc);events.add(new Event(actor,doc.code,"CREATED"));return doc.copy();
 }
 public synchronized <T>T locked(String code,Function<ManagedDocument,T> operation){
  var prior=docs.get(code);if(prior==null)throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Resource not found.");
  var working=prior.copy();T result=operation.apply(working);
  if(working.action!=null){
   if("VERSION_ADDED".equals(working.action)){
    var v=working.actionVersion;var created=new ManagedDocument.Version(nextVersion++,v.number(),v.format(),v.body(),v.state(),v.aiSearchable(),false);
    working.versions.add(created);working.actionVersion=created;
   }
   docs.put(code,working.copy());events.add(new Event(working.actionActor,code,working.action));
  }
  return result;
 }
}
