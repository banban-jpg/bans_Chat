package egovframework.example.chat.mapper;

import java.nio.file.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ChatMapperSourceTest {
    @Test void parsesBoundOracleSqlAndLengthMigrationSource() throws Exception {
        var path="egovframework/example/chat/mapper/ChatMapper.xml";var config=new Configuration();
        try(var stream=getClass().getClassLoader().getResourceAsStream(path)) {
            new XMLMapperBuilder(stream,config,path,config.getSqlFragments()).parse();
        }
        var sql=config.getMappedStatement(ChatMapper.class.getName()+".messages")
                .getBoundSql(java.util.Map.of("id",1,"before",0,"size",20)).getSql();
        assertThat(sql).contains("FETCH FIRST ? ROWS ONLY","ORDER BY roomSeq ASC").doesNotContain("${","SELECT *");
        var migration=Files.readString(Path.of("db/oracle/V003__chat_message_length_guard.sql"));
        assertThat(migration).contains("REGEXP_COUNT(:NEW.message_text, '.', 1, 'n')","2000");
    }
}
