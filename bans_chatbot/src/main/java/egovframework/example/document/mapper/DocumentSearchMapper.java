package egovframework.example.document.mapper;

import egovframework.example.document.repository.DocumentSearchRepository.FragmentRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DocumentSearchMapper {
 List<FragmentRow> find(@Param("employee")String employee,@Param("query")String query,
                        @Param("offset")int offset,@Param("limit")int limit);
 FragmentRow source(@Param("employee")String employee,@Param("documentId")long documentId,
                    @Param("versionId")long versionId,@Param("fragmentId")long fragmentId);
}
