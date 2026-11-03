package egovframework.example.auth.mapper;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Parses Oracle mapper XML offline; does not execute any SQL. */
class AuthMapperSourceTest {
    @Test
    void accountAndActivationXmlBindToDomainMapperInterfaces() throws Exception {
        Configuration configuration = new Configuration();
        for (String name : new String[]{"AccountMapper.xml", "ActivationMapper.xml"}) {
            String resource = "egovframework/example/auth/mapper/" + name;
            try (InputStream xml = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertThat(xml).isNotNull();
                new XMLMapperBuilder(xml, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        assertThat(configuration.hasStatement(AccountMapper.class.getName() + ".findForUpdate")).isTrue();
        assertThat(configuration.hasStatement(AccountMapper.class.getName() + ".activate")).isTrue();
        assertThat(configuration.hasStatement(ActivationMapper.class.getName() + ".consume")).isTrue();
    }
}
