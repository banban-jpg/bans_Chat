package egovframework.example.document.domain;
import java.util.*;
/** Mutable only inside repository's serialized/transactional callback. */
public final class ManagedDocument {
 public record Version(long id,int number,String format,String body,String state,boolean aiSearchable,boolean externalAllowed) {}
 public final long id; public final String code,title,category,owner,scope,department;
 public String status="DRAFT",reviewer; public long currentVersionId;
 public final List<Version> versions=new ArrayList<>();
 public final Set<String> tags=new TreeSet<>();
 public final Map<String,Set<String>> grants=new HashMap<>();
 public String action,actionActor,actionTarget,actionPermission; public Version actionVersion;
 public ManagedDocument(long id,String code,String title,String category,String owner,String scope,String department) {
  this.id=id;this.code=code;this.title=title;this.category=category;this.owner=owner;this.scope=scope;this.department=department;
 }
 public Version current() {return versions.stream().filter(v->v.id()==currentVersionId).findFirst().orElse(null);}
 public Version pending() {return versions.stream().filter(v->v.state().equals("DRAFT")||v.state().equals("SUBMITTED")).findFirst().orElse(null);}
 public ManagedDocument copy() {
  var copy=new ManagedDocument(id,code,title,category,owner,scope,department);copy.status=status;copy.reviewer=reviewer;copy.currentVersionId=currentVersionId;copy.versions.addAll(versions);
  copy.tags.addAll(tags);grants.forEach((key,value)->copy.grants.put(key,new HashSet<>(value)));return copy;
 }
}
