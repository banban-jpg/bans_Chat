package egovframework.example.document.dto;
import jakarta.validation.constraints.*;
public final class DocumentModels {
 private DocumentModels() {}
 public record Create(@NotBlank String documentCode,@NotBlank String title,@NotBlank String category,
   @NotBlank String scope,String departmentId,@NotBlank String format,@NotBlank String body,
   boolean aiSearchable,boolean externalAiAllowed,java.util.List<String> tags) {
   public Create(String documentCode,String title,String category,String scope,String departmentId,String format,String body,boolean aiSearchable,boolean externalAiAllowed){this(documentCode,title,category,scope,departmentId,format,body,aiSearchable,externalAiAllowed,java.util.List.of());}
 }
 public record Version(@NotBlank String format,@NotBlank String body,boolean aiSearchable,boolean externalAiAllowed) {}
 public record Review(@NotBlank String reviewerEmployeeId) {}
 public record Grant(@NotBlank String employeeId,@NotBlank String permission) {}
 public record Published(String documentCode,String title,int versionNo,String format,String renderedHtml,boolean aiSearchable) {}
 public record SearchHit(long documentId,long versionId,long fragmentId,String title,String excerpt,String updatedAt) {}
 public record SearchPage(java.util.List<SearchHit> items,int page,int size,boolean hasMore) {}
 public record SourceFragment(long documentId,long versionId,long fragmentId,String title,String content,String updatedAt) {}
}
