package egovframework.example.organization.mapper;
import java.util.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import egovframework.example.organization.dto.DirectoryModels.Search;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class OrganizationMapperSourceTest {
 @Test void parsesOracleXmlAndBindsSearchWithoutInterpolatingInput() throws Exception {
  var config=new Configuration();
  for(String domain:List.of("organization/mapper/OrganizationMapper.xml","audit/mapper/PersonnelAuditMapper.xml")) {
   String path="egovframework/example/"+domain;
   try(var stream=getClass().getClassLoader().getResourceAsStream(path)) { new XMLMapperBuilder(stream,config,path,config.getSqlFragments()).parse(); }
  }
  var q=new Search("' OR 1=1 --","D01","%_",0,20,"name");
  var sql=config.getMappedStatement(OrganizationMapper.class.getName()+".search").getBoundSql(q);
  assertThat(sql.getSql()).contains("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY","NLSSORT").doesNotContain(q.name(),"SELECT *","${");
  assertThat(sql.getParameterMappings()).hasSize(5);
 }
}
