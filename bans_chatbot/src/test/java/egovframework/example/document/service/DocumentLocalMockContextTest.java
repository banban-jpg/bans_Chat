package egovframework.example.document.service;

import egovframework.example.auth.domain.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("local-mock")
class DocumentLocalMockContextTest {
 @Autowired DocumentSearchService search;
 @Test void processLocalFixtureProvidesVacationEvidenceWithoutOracle(){
  var actor=new SessionActor("EMP-001","D01",AccountStatus.ACTIVE,Set.of(SecurityRole.EMPLOYEE));
  assertThat(search.search(actor,"휴가",0,10).items()).extracting(hit->hit.title()).contains("가상 휴가 신청 안내");
 }
}
