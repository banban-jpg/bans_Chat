package egovframework.example.document.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface ManagedDocumentMapper {
 record DocumentRow(long id,String code,String title,String category,String owner,String scope,String department,String status,Long currentVersionId,String reviewer) {}
 record VersionRow(long id,int number,String format,String body,String state,String aiSearchable,String externalAllowed) {}
 record GrantRow(String employeeCode,String permission) {}
 DocumentRow find(@Param("code") String code);
 DocumentRow lock(@Param("code") String code);
 List<DocumentRow> all();
 List<VersionRow> versions(@Param("id") long id);
 List<GrantRow> grants(@Param("id") long id);
 List<String> tags(@Param("id") long id);
 int insertTag(@Param("id")long id,@Param("tag")String tag);
 int create(@Param("code")String code,@Param("title")String title,@Param("category")String category,@Param("owner")String owner,@Param("scope")String scope,@Param("department")String department);
 int insertVersion(@Param("id")long id,@Param("number")int number,@Param("format")String format,@Param("body")String body);
 Long versionId(@Param("id")long id,@Param("number")int number);
 int insertFragment(@Param("versionId")long versionId,@Param("body")String body,@Param("ai")String ai);
 int editVersion(@Param("id")long id,@Param("format")String format,@Param("body")String body);
 int editFragment(@Param("id")long id,@Param("body")String body,@Param("ai")String ai);
 int versionState(@Param("id")long id,@Param("from")String from,@Param("to")String to);
 int setReview(@Param("id")long id,@Param("status")String status,@Param("reviewer")String reviewer);
 int complete(@Param("id")long id,@Param("status")String status,@Param("current")long current);
 int insertDecision(@Param("id")long id,@Param("version")long version,@Param("reviewer")String reviewer,@Param("decision")String decision);
 int withdraw(@Param("id")long id);
 int grant(@Param("id")long id,@Param("employee")String employee,@Param("permission")String permission,@Param("actor")String actor);
 int revoke(@Param("id")long id,@Param("employee")String employee,@Param("permission")String permission,@Param("actor")String actor);
 int audit(@Param("actor")String actor,@Param("code")String code,@Param("event")String event);
}
