package egovframework.example.document.mapper;

import java.nio.file.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DocumentSearchMapperSourceTest {
 @Test void parsesBoundAclFirstSearchAndSourceSqlOffline() throws Exception {
  var path="egovframework/example/document/mapper/DocumentSearchMapper.xml";var config=new Configuration();
  try(var xml=getClass().getClassLoader().getResourceAsStream(path)){
   assertThat(xml).isNotNull();new XMLMapperBuilder(xml,config,path,config.getSqlFragments()).parse();
  }
  var name=DocumentSearchMapper.class.getName();
  assertThat(config.hasStatement(name+".find")).isTrue();assertThat(config.hasStatement(name+".source")).isTrue();
  var sql=config.getMappedStatement(name+".find").getBoundSql(java.util.Map.of("employee","EMP-001","query","%_ OR 1=1","offset",0,"limit",10)).getSql();
  assertThat(sql).contains("document_status IN ('APPROVED','IN_REVIEW')","current_version_id=v.version_id","revoked_at IS NULL","DBMS_LOB.INSTR","document_tag","OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
  assertThat(sql).doesNotContain("%_ OR 1=1","external_ai_allowed");
  assertThat(Files.readString(Path.of("db/oracle/V005__document_search_tags_indexes.sql"))).contains("CREATE TABLE document_tag","ix_document_search_current");
 }
}
