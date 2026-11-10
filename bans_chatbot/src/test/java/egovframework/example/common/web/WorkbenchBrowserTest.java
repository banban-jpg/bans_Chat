package egovframework.example.common.web;

import egovframework.example.auth.repository.InMemoryAccountRepository;
import java.nio.file.*;
import java.time.Duration;
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

/** Real headless browser, real local server, synthetic in-memory services and Mock Gemini only. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "fixture-test"}) @DirtiesContext
class WorkbenchBrowserTest {
    @LocalServerPort int port;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired PasswordEncoder passwords;
    @Test @Timeout(150) @EnabledIfEnvironmentVariable(named="BANS_UI_BROWSER_TEST",matches="YES")
    void coreScreensRunInActualChromeWithoutOracleOrExternalApi() throws Exception {
        Path chrome=Path.of("C:/Program Files/Google/Chrome/Application/chrome.exe");
        Path node=Path.of("C:/Program Files/nodejs/node.exe");
        Assumptions.assumeTrue(Files.isExecutable(chrome)&&Files.isExecutable(node),"BLOCKED: LOCAL_CHROME_OR_NODE_NOT_AVAILABLE");
        String password=UUID.randomUUID()+"Aa1!";
        accounts.resetForTest();accounts.provisionForTest("EMP-001",passwords.encode(password));
        Path log=Path.of("local-notes/step15-browser.log");Files.createDirectories(log.getParent());
        var builder=new ProcessBuilder(node.toString(),"src/test/ui/workbench-browser.mjs","http://127.0.0.1:"+port,chrome.toString())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("BANS_UI_TEST_PASSWORD",password);
        var process=builder.start();
        try {
            assertThat(process.waitFor(Duration.ofSeconds(120).toMillis(),TimeUnit.MILLISECONDS)).as("browser timeout; inspect ignored browser log").isTrue();
            assertThat(process.exitValue()).as("browser exit; inspect ignored local-notes/step15-browser.log").isZero();
            assertThat(Files.readString(log)).contains("BROWSER_PASS", "screens=6", "reconnect", "429", "503", "XSS");
        } finally { if(process.isAlive())process.destroyForcibly(); }
    }
}
