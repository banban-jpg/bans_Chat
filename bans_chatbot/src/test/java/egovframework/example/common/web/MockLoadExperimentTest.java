package egovframework.example.common.web;

import egovframework.example.auth.fixture.SyntheticAccountFixture;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.ai.provider.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

/** Explicit short workload experiment, never a database throughput benchmark. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "fixture-test"}) @DirtiesContext
@EnabledIfEnvironmentVariable(named="BANS_MOCK_LOAD_TEST",matches="YES")
class MockLoadExperimentTest {
 @LocalServerPort int port;
 @Autowired InMemoryAccountRepository accounts;
 @Autowired PasswordEncoder passwords;
 @Autowired ChatProvider provider;
 @Test @Timeout(180) void repeatTenTwentyThirtyConnectionsAgainstEphemeralServices() throws Exception {
  assertThat(provider).isInstanceOf(MockGeminiProvider.class);
  Path node=Path.of("C:/Program Files/nodejs/node.exe");Assumptions.assumeTrue(Files.isExecutable(node),"BLOCKED: NODE_NOT_AVAILABLE");
  String password=UUID.randomUUID()+"Aa1!";
  accounts.resetForTest();for(var account:SyntheticAccountFixture.accounts())accounts.provisionForTest(account.employeeCode(),passwords.encode(password));
  Path log=Path.of("local-notes/step17-track-a.log");Files.createDirectories(log.getParent());
  var builder=new ProcessBuilder(node.toString(),"../performance/scripts/track-a.mjs","http://127.0.0.1:"+port)
      .redirectErrorStream(true).redirectOutput(log.toFile());
  builder.environment().put("BANS_MOCK_LOAD_TEST","YES");builder.environment().put("BANS_LOAD_PASSWORD",password);
  var process=builder.start();
  try{
   assertThat(process.waitFor(160,TimeUnit.SECONDS)).as("load process timeout").isTrue();
   assertThat(process.exitValue()).as("inspect local-notes/step17-track-a.log").isZero();
   assertThat(Files.readString(log)).contains("TRACK_A_PASS");
  }finally{if(process.isAlive())process.destroyForcibly();}
 }
}
