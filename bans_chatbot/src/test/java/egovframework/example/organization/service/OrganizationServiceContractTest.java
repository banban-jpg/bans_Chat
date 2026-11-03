package egovframework.example.organization.service;
import egovframework.example.organization.repository.OrganizationRepository;
import egovframework.example.organization.dto.DirectoryModels.*;
import egovframework.example.auth.domain.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class OrganizationServiceContractTest {
 @Test void validatesBeforeRepositoryAndEnforcesRoleInService() {
  var repo=mock(OrganizationRepository.class); var service=new OrganizationService(repo);
  var actor=new SessionActor("EMP-002","D01",AccountStatus.ACTIVE,Set.of(SecurityRole.EMPLOYEE));
  assertThatThrownBy(()->service.change(actor,"EMP-002","ACTIVE",null)).hasMessageContaining("403");
  assertThatThrownBy(()->service.search(actor,new Search("","","",0,101,"name"))).hasMessageContaining("400");
  verifyNoInteractions(repo);
  var q=new Search("가상","D01","지원",1,10,"name"); service.search(actor,q); verify(repo).search(q);
 }
}
