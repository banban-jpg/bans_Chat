package egovframework.example.document.repository;

import egovframework.example.auth.domain.SessionActor;
import java.util.List;

/** Candidate rows are filtered by the adapter and checked again by the service. */
public interface DocumentSearchRepository {
 record FragmentRow(long documentId,String documentCode,long versionId,long fragmentId,
                    String title,String body,String updatedAt,String externalAllowed,String synthetic,String nonSensitive) {
  @org.apache.ibatis.annotations.AutomapConstructor public FragmentRow {}
  public FragmentRow(long documentId,String documentCode,long versionId,long fragmentId,String title,String body,String updatedAt){
   this(documentId,documentCode,versionId,fragmentId,title,body,updatedAt,"N","N","N");
  }
  @Override public String toString(){return "FragmentRow[documentId="+documentId+", body=<redacted>]";}
 }
 List<FragmentRow> find(SessionActor actor,String query,int offset,int limit);
 FragmentRow source(SessionActor actor,long documentId,long versionId,long fragmentId);
}
