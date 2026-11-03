package egovframework.example.document.repository;

import egovframework.example.auth.domain.SessionActor;
import java.util.List;

/** Candidate rows are filtered by the adapter and checked again by the service. */
public interface DocumentSearchRepository {
 record FragmentRow(long documentId,String documentCode,long versionId,long fragmentId,
                    String title,String body,String updatedAt) {}
 List<FragmentRow> find(SessionActor actor,String query,int offset,int limit);
 FragmentRow source(SessionActor actor,long documentId,long versionId,long fragmentId);
}
