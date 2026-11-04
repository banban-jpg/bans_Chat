package egovframework.example.ai.question;

import egovframework.example.ai.provider.MockGeminiProvider;
import egovframework.example.audit.mapper.AiQuestionAuditMapper;
import egovframework.example.auth.domain.AuthenticatedEmployee;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.document.service.DocumentSearchService;
import egovframework.example.organization.service.OrganizationService;
import java.time.Clock;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@Tag("oracle-integration") @EnabledIfEnvironmentVariable(named="ORACLE_AI_QUESTION_TEST",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class AiQuestionOracleIT {
    @Autowired CurrentSessionService sessions;
    @Autowired AccountRepository accounts;
    @Autowired OrganizationService organization;
    @Autowired DocumentSearchService documents;
    @Autowired AiQuestionAuditMapper audit;
    @Autowired JdbcTemplate jdbc;
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    @Test @Transactional void seededOracleReadFlowWithMockProviderAndMapperAuditRollsBack() throws Exception {
        jdbc.update("UPDATE employee_account SET account_status='ACTIVE' WHERE employee_id=(SELECT employee_id FROM employee WHERE employee_code='EMP-002')");
        // Direct mapper joins the rollback test transaction; this does not test production REQUIRES_NEW durability.
        var service = new AiQuestionService(sessions,new QuestionPolicy(),organization,documents,new MockGeminiProvider(),event->{assertThat(audit.insert(event)).isEqualTo(1);},Clock.systemUTC());
        for(String question:List.of("연차 신청 절차","출장비 담당자")){
            var context=SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(new AuthenticatedEmployee("EMP-002",accounts.find("EMP-002").orElseThrow().credentialsVersion()),null,List.of()));
            SecurityContextHolder.setContext(context); var session=new MockHttpSession();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,context);
            var request=new MockHttpServletRequest();request.setSession(session);
            request.setContent(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(Map.of("question",question)));
            var answer=service.answer(request); assertThat(answer.answerType()).isNotEqualTo(QuestionModels.AnswerType.UNANSWERABLE);
        }
        // HTTP login, multi-connection revoke races, live Gemini and independent audit commit are separate pending ITs.
    }
}
