package egovframework.example.global.config;

import egovframework.example.ai.client.AiAnswerPort;
import egovframework.example.ai.client.MockGeminiClient;
import egovframework.example.organization.repository.EmployeeDirectory;
import egovframework.example.organization.repository.InMemoryEmployeeDirectory;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local-mock")
class LocalMockWiringTest {
    @Autowired ApplicationContext context;
    @Autowired EmployeeDirectory directory;
    @Autowired AiAnswerPort aiAnswerPort;

    @Test
    void mockProfileUsesOnlyNonpersistentAdapters() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
        assertThat(directory).isInstanceOf(InMemoryEmployeeDirectory.class);
        assertThat(directory.findAll()).hasSize(20);
        assertThat(aiAnswerPort).isInstanceOf(MockGeminiClient.class);
    }
}
