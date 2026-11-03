package egovframework.example.global.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration(proxyBeanMethods = false)
@Profile("oracle")
@MapperScan({"egovframework.example.auth.mapper", "egovframework.example.organization.mapper", "egovframework.example.audit.mapper", "egovframework.example.chat.mapper"})
public class OracleMyBatisConfiguration {
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        var factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:egovframework/example/**/mapper/*.xml"));
        return factory.getObject();
    }
}
