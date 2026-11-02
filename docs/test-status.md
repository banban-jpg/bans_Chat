# Test status

## STEP 01 baseline

Environment: Windows 11, Asia/Seoul, 2026-11-02; Temurin Java 21.0.3; Maven 3.9.6 via `bans_chatbot/mvnw.cmd` with `MAVEN_HOME` set to the locally installed distribution and `-Dmaven.repo.local=C:\Users\user\.m2\repository`. All Maven invocations below used `-o` (offline). No Oracle connection, SQL execution, or Gemini request occurred.

| Check / command | Status | Evidence and cause |
|---|---|---|
| `java -version` | PASS | Temurin 21.0.3, 2026-11-02 16:36 KST |
| `mvn -version` | FAIL | `mvn` absent from PATH; the existing local Maven 3.9.6 `mvn.cmd -version` passed at 16:37 |
| `mvnw.cmd -o ... clean compile` | PASS | 16:49, two sources compiled with `release 21`; `javap` reports major version 65 |
| `mvnw.cmd -o ... clean test` | FAIL | 16:44: parent-managed JUnit Platform Launcher 1.12.2 absent from local cache. Earlier attempts exposed an incorrect test import (fixed), incomplete clean 3.4.1 and Surefire 3.5.4 cache entries (pinned cached 3.2.0/3.5.2). Current source compiles; Maven test lifecycle still cannot complete offline. No dependency download was performed. |
| `mvnw.cmd -o ... dependency:build-classpath` | PASS | 16:49, generated ignored `target/classpath.txt` from cached project dependencies |
| Direct `java -cp target/classes;<generated classpath> egovframework.example.EgovBootApplication` | PASS | 16:50, Boot 3.5.6 / Java 21.0.3 / default `local-mock`; Tomcat started port 8080 without DB configuration |
| `Invoke-WebRequest http://127.0.0.1:8080/health` | PASS | 16:50, HTTP 200 `{"status":"UP"}`; process stopped afterward |
| Manual JUnit Platform launch of `BootSmokeTest` | PASS | 16:53, 2 found / 2 successful / 0 failed in `test` profile. Used cached launcher 1.12.1 with project-managed JUnit engine because 1.12.2 launcher is absent; this does **not** count as Maven `clean test` success. Runner was compiled under ignored `target`. |
| `spring-boot:run` Maven goal | FAIL | 16:49, Boot Maven plugin transitive artifacts missing from offline cache; direct Java boot succeeded |
| `-Poracle-it verify` / `oracle` startup / JDBC | BLOCKED: ORACLE_26AI_NOT_INSTALLED | Oracle server absent; not invoked |
| Gemini network test | NOT_RUN | No Gemini integration in STEP 01 and no key; real call count 0 |
| Oracle SQL, migrations, transactions, lock and performance checks | BLOCKED: ORACLE_26AI_NOT_INSTALLED | No Oracle server or schema yet; SQL execution count 0 |

Normal `test` includes no Oracle or external API. `*OracleIT.java` cases are reserved for an explicit `-Poracle-it verify` run; none exist yet. `local-mock` currently means a DB-free server only; in-memory repositories and Gemini mock are future work. The two passing smoke tests verify HTTP routing and a genuine eGov runtime JAR on the classpath. They do not verify Oracle or Gemini behavior.

Follow-up to obtain a standard `clean test` PASS: make the parent-managed JUnit Platform Launcher 1.12.2 and missing plugin artifacts available in an approved local Maven cache, then rerun the wrapper command. Do not claim this gate passed until executed.

## Pre-STEP 02 package cleanup verification (2026-11-02, 17:01–17:05 KST)

Environment: same Windows/Java 21.0.3 and Maven wrapper 3.9.6 as STEP 01; Maven `-o`, existing `C:/Users/user/.m2/repository`, no network retrieval. Paths below are relative to `bans_chatbot`; `MAVEN_HOME` was set to the existing Maven 3.9.6 distribution. No Oracle connection or Gemini call occurred. The production change was a package move of `/health`, its test, and Java 21 IDE alignment; no STEP 02 feature was implemented.

| Check / actual command | Status | Evidence and cause |
|---|---|
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean test` | FAIL | 17:01: javac reported `AccessDeniedException` while closing cached `reactive-streams-1.0.4.jar`; clean build did not complete. An earlier unquoted Windows-path invocation resolved incorrectly to `C:\.m2\repository` and failed before build. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' compile` | PASS (incremental only) | 17:01: compiled classes were already present; Maven said nothing to compile. This does not establish a clean build pass. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' test` | FAIL | 17:02: test class compiled, but javac again reported access denied on the cached JAR. A repeat with `-e` reached Surefire and failed because `junit-platform-launcher:1.12.2` is absent offline. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean compile` | FAIL | 17:04: reproduced Java `AccessDeniedException` on `reactive-streams-1.0.4.jar`; `-X` stack trace identifies `jdk.zipfs.ZipFileSystemProvider.removeFileSystem` / `toRealPath`. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' '-Dmaven.compiler.fork=true' clean compile` | FAIL (misleading Maven exit 0) | 17:05: Maven printed `BUILD SUCCESS`, but forked javac printed the same uncaught `AccessDeniedException`. This is not accepted as a valid clean build. |
| `mvnw.cmd -o ... org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath` | PASS | 17:02: wrote ignored `target/classpath.txt` from cached dependencies. The unversioned plugin goal failed offline because its selected 3.8.1 is absent; no version was changed in the POM. |
| Manual JUnit Platform launch of `egovframework.example.global.health.BootSmokeTest` | PASS | 17:03: cached Launcher 1.12.1 with compiled test and project classpath, 2 found / 2 passed / 0 failed, active `test` profile. Runner was temporary in ignored `target`. This does not count as Maven `test` PASS. |
| Direct Java `EgovBootApplication --spring.profiles.active=local-mock --server.port=18080`; `Invoke-WebRequest http://127.0.0.1:18080/health` | PASS | 17:03: Boot 3.5.6 / Java 21.0.3 started; HTTP 200 `{"status":"UP"}`. Process then stopped. |
| Oracle integration and SQL | BLOCKED: ORACLE_26AI_NOT_INSTALLED | No Oracle server; no connection, schema, or SQL was executed. |
| Gemini real API | NOT_RUN | No integration or key; real call count 0. |

The package move preserved component scanning and the existing HTTP contract. Clean Maven build and normal test remain failing environment gates; resolve cached-JAR access and the missing launcher before treating the default test gate as green. No Oracle claim follows from these DB-free checks.

## STEP 02 verification (2026-11-02, 17:16–17:35 KST)

Environment: Windows 11 / Temurin Java 21.0.3 / Maven wrapper 3.9.6. All Maven calls used `-o` and `-Dmaven.repo.local=C:/Users/user/.m2/repository`, with `MAVEN_HOME` set to the existing local Maven distribution. Direct verification copied 144 cached project JARs and cached JUnit Platform Launcher 1.12.1 into ignored `bans_chatbot/target/deps`; it did not download dependencies. The final JUnit run removed Oracle and Gemini variables from its process environment. Oracle connection count 0; real Gemini call count 0.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean test` | FAIL | 17:28: javac `AccessDeniedException` while closing cached `reactive-streams-1.0.4.jar` outside the workspace; lifecycle stopped before tests. A 17:16 attempt showed the same error. Removing the unused reactive module temporarily caused the same error on `mybatis-3.5.19.jar`; it was restored. This is a cache/filesystem issue, not a Java source diagnostic. |
| `mvnw.cmd -o ... test` | FAIL | 17:29: test compilation hit the same JAR access error. Repeating after classes existed reached Surefire at 17:30, then failed because `junit-platform-launcher:1.12.2` is absent from the offline cache. Normal Maven `test` is **not** PASS. |
| `mvnw.cmd -o ... org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath` | PASS | 17:28: generated ignored `target/classpath.txt`; no network download. |
| Direct `javac -proc:none -encoding UTF-8 --release 21 -cp 'target/deps/*'` and test compilation with `target/direct-classes` | PASS | 17:29 and 17:34: final 16 main Java sources and 8 test Java sources compiled into ignored `target/direct-*`. `OracleConnectionOracleIT` compiled but was not executed. This is a separate source compilation, not Maven lifecycle success. |
| Direct JUnit Platform launch selecting seven DB-free test classes | PASS | 17:34: 13 found / 13 successful / 0 failed, including MockMvc response/validation/sanitization, Mockito service port, local-mock Controller → Service → port, test/local-mock Bean wiring, profile guard and eGov smoke. OracleIT was deliberately absent from discovery. Cached launcher 1.12.1 was used only for this offline verification. |
| Direct Java `--spring.profiles.active=local-mock --server.port=18082/18083`; HTTP `/health`, `/api/health`, `/api/demo/employees` | PASS | 17:31 and final 17:35: Boot 3.5.6 started without DB/Google key. Both health routes returned HTTP 200; `/api/health` returned `{"data":{"status":"UP"}}` and a server-generated request ID different from the client header. The mock-only employee route returned 20 invented records across 5 departments. Processes stopped afterward. |
| Direct Java `--spring.profiles.active=oracle` with Oracle environment variables removed | PASS (expected failure) | 17:30: startup exited 1 with `ORACLE_CONFIG_MISSING: ORACLE_JDBC_URL`; it did not fall back to mock or open a connection. This verifies the missing-config guard only. |
| Direct Java `--spring.profiles.active=oracle-it` alone | PASS (expected failure) | 17:30: exited 1 with `PROFILE_CONFLICT: oracle-it requires oracle`. |
| `oracleIntegrationTest.cmd` with Oracle variables removed | PASS (precondition only) | 17:30: exited 2 with `BLOCKED: ORACLE_CONFIG_MISSING ORACLE_JDBC_URL`; Maven/Failsafe and database test were not invoked. |
| `mvnw.cmd -o ... -Poracle-it validate` | FAIL | 17:19: Failsafe 3.5.4 is absent from offline cache. This is not an Oracle test result. |
| Real Oracle 26ai connection, SQL, migrations, transactions, locks, performance | BLOCKED: ORACLE_26AI_NOT_INSTALLED | Oracle server absent; no JDBC connection or SQL execution attempted. The preliminary `OracleConnectionOracleIT` remains unrun. |
| Real Gemini transport/network test | NOT_RUN | No real transport was implemented in STEP 02. The offline mock adapter wiring passed; no external request was made. |
| Real Gemini API invocation | SKIPPED | Intentionally disabled; no key or paid fallback configured. |

The independent DB-free tests are executable and passed, but the default Maven `clean test` gate remains FAIL. Restore the missing artifacts through an authorized dependency-cache refresh and resolve the JAR-access issue, then rerun the Maven wrapper before claiming that gate is green. These passes do not verify Oracle 26ai schema or production security.
