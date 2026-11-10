package egovframework.example.common.web;

import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "fixture-test"}) @DirtiesContext
class WorkbenchClientIntegrationTest {
    @LocalServerPort int port;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Test @Timeout(60) void realUiClientRunsAgainstLoopbackHttpAndWebSocket() throws Exception {
        Path node=Path.of("C:/Program Files/nodejs/node.exe");
        Assumptions.assumeTrue(Files.isExecutable(node),"BLOCKED: NODE_NOT_AVAILABLE");
        String password=UUID.randomUUID()+"Aa1!";
        accounts.resetForTest();for(String employee:java.util.List.of("EMP-001","EMP-005"))accounts.provisionForTest(employee,passwords.encode(password));
        Path log=Path.of("local-notes/step15-client.log");Files.createDirectories(log.getParent());
        var builder=new ProcessBuilder(node.toString(),"src/test/ui/client-integration.mjs","http://127.0.0.1:"+port)
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("BANS_UI_TEST_PASSWORD",password);
        var process=builder.start();
        try{
            assertThat(process.waitFor(45,TimeUnit.SECONDS)).as("client timeout, inspect ignored log").isTrue();
            assertThat(process.exitValue()).as("client exit, inspect local-notes/step15-client.log").isZero();
            assertThat(Files.readString(log)).contains("CLIENT_PASS");
        }finally{if(process.isAlive())process.destroyForcibly();}
    }
}
