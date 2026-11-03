package egovframework.example.document.repository;
import egovframework.example.document.domain.ManagedDocument;
import java.util.List;
import java.util.function.Function;
public interface DocumentWorkspace {
 ManagedDocument find(String code);
 List<ManagedDocument> all();
 ManagedDocument create(ManagedDocument document,ManagedDocument.Version first,String actor);
 <T> T locked(String code,Function<ManagedDocument,T> operation);
}
