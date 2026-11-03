package egovframework.example.document.repository;

import egovframework.example.auth.domain.SessionActor;
import egovframework.example.document.domain.ManagedDocument;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Deterministic process-local search. It is not evidence of Oracle SQL behavior. */
@Repository @Profile({"test","local-mock"})
public class InMemoryDocumentSearchRepository implements DocumentSearchRepository {
 private final DocumentWorkspace workspace;
 public InMemoryDocumentSearchRepository(DocumentWorkspace workspace){this.workspace=workspace;}
 private boolean readable(SessionActor actor,ManagedDocument d){
  return actor.activeEmployee()&&d.current()!=null&&"PUBLISHED".equals(d.current().state())&&
   !"ARCHIVED".equals(d.status)&&d.current().aiSearchable()&&
   (d.owner.equals(actor.employeeId())||d.grants.getOrDefault(actor.employeeId(),Set.of()).contains("READ")||
    "INTERNAL".equals(d.scope)||"DEPARTMENT".equals(d.scope)&&Objects.equals(d.department,actor.departmentCode()));
 }
 private FragmentRow row(ManagedDocument d){var v=d.current();return new FragmentRow(d.id,d.code,v.id(),v.id(),d.title,v.body(),
   java.time.Instant.parse("2026-01-01T00:00:00Z").plusSeconds(v.id()).toString());}
 public List<FragmentRow> find(SessionActor actor,String query,int offset,int limit){
  return workspace.all().stream().filter(d->readable(actor,d))
   .filter(d->d.title.contains(query)||d.category.contains(query)||d.tags.stream().anyMatch(t->t.contains(query))||d.current().body().contains(query))
   .sorted(Comparator.comparingLong((ManagedDocument d)->d.current().id()).reversed().thenComparingLong(d->d.id))
   .skip(offset).limit(limit).map(this::row).toList();
 }
 public FragmentRow source(SessionActor actor,long documentId,long versionId,long fragmentId){
  return workspace.all().stream().filter(d->d.id==documentId&&readable(actor,d)&&d.currentVersionId==versionId&&d.current().id()==fragmentId)
   .findFirst().map(this::row).orElse(null);
 }
}
