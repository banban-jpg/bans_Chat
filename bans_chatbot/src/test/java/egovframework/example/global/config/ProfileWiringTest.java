package egovframework.example.global.config;

import egovframework.example.ai.client.AiAnswerPort;
import egovframework.example.organization.service.EmployeeDirectoryService;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProfileWiringTest {
    @Autowired ApplicationContext context;
    @Autowired EmployeeDirectoryService employeeDirectory;
    @Autowired AiAnswerPort aiAnswerPort;

    @Test
    void testProfileHasOnlySyntheticAdapters() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
        assertThat(context.getBeanNamesForType(SqlSessionFactory.class)).isEmpty();
        assertThat(employeeDirectory.syntheticEmployees()).hasSize(20);
        assertThat(employeeDirectory.syntheticEmployees().stream()
                .map(employee -> employee.departmentCode()).distinct()).hasSize(5);
        assertThat(aiAnswerPort.answerSyntheticQuestion("합성 질문"))
                .isEqualTo("가상 자료에 대한 모의 AI 응답입니다.");
    }
}
