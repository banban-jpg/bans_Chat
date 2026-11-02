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

## STEP 02 gate update from user run (2026-11-02 18:03:49 KST)

The user ran `cd bans_chatbot; .\mvnw.cmd -U clean test` in ordinary PowerShell. The supplied Maven output showed **PASS: 13 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS** on Java 21.0.3 / Boot 3.5.6. This supersedes the earlier offline-cache failure for that user environment; it is not an Oracle integration result. The assistant did not initiate that network-enabled run.

## STEP 03 verification (2026-11-02 18:10-18:16 KST)

Environment: Windows / Java 21.0.3 / Maven wrapper 3.9.6 / eGov parent 5.0.0 / Boot 3.5.6. Before edits, Git was clean on `dev`. Maven was run **offline** (`-o`), with a copy of the already present Maven cache under ignored workspace `/.maven-repo/`; no dependency download, Oracle connection, or real Gemini call was initiated. The copy exists only to avoid this execution sandbox's access failure when javac closes JARs outside the writable workspace. The POM versions and SQL were not changed.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `cd bans_chatbot; .\mvnw.cmd -o clean test` | FAIL, environment | 18:10: Maven tried `C:\.m2\repository` and could not create it; no tests ran. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' test` | FAIL, environment | 18:10: javac failed while closing cached `reactive-streams-1.0.4.jar` outside the writable workspace. |
| Copy existing cache to ignored `/.maven-repo/`; run `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | FAIL, resolved code issue | 18:11: 24 tests attempted, five WebMvc slice context errors because the new interceptor's `PublicEndpointPolicy` was not imported. The test slice was fixed. |
| Same offline `clean test` | PASS, intermediate | 18:12: 24 tests, zero failures/errors/skips. This preceded per-segment AI metadata and final path adjustment. |
| Same offline `clean test` after per-segment change | FAIL, resolved code issue | 18:16: 26 tests, two health failures. MockMvc provided an empty servlet path; the interceptor switched to URI minus context path. |
| Same offline `clean test` after path fix | **PASS** | 18:16:46: 26 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main source files and 10 test source files compiled. Covers role/self-grant, chat participant, document scope/grant/approval, inactive actors, per-segment external AI decision, anonymous allowlist, 401/403/404/429 contract, profile wiring, and prior smoke tests. |
| Same offline `clean test` after placing tests under their owning domain packages and extending safety cases | **PASS (final)** | 18:22:06: 26 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main and 13 test source files compiled. Tests additionally check approve-only grants, non-synthetic/sensitive segments, and draft rejection. This is the final STEP 03 source state. |
| `*OracleIT` execution under ordinary Surefire | SKIPPED, intentionally excluded | Surefire POM excludes the name/tag; final run contained only DB-free tests. The preliminary Oracle case was compiled, not run. |
| Oracle 26ai connection, ACL joins, grant revocation, SQL, transaction/locking | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; explicit `oracleIntegrationTest.cmd` was not invoked in STEP 03. |
| Spring Security filter chain, persisted session/login, CSRF, STOMP ACL | NOT_RUN | No Spring Security artifact was downloaded or filter chain implemented in this policy-only STEP. Current MVC interceptor tests do not establish those guarantees. |
| Real Gemini API or external question transfer | SKIPPED | No production review adapter or real transport; network call count 0. |

The final PASS covers Java/MVC policy behavior with fake session actors and Mockito ports. It does not prove Oracle permission joins, instant account/grant revocation, or production Spring Security wiring.

## STEP 04 verification (2026-11-02 18:33-18:42 KST)

Before editing, Git showed the uncommitted STEP 03 source/docs work listed in `git status --short`; those files were preserved. Environment: Windows PowerShell, Java 21.0.3, Maven wrapper 3.9.6, eGov 5.0.0 parent, Boot 3.5.6. Commands ran from `bans_chatbot` with `-o` and the ignored workspace-local cache `C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo`. No dependency download, Oracle connection, SQL execution, or real Gemini call occurred.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' '-Dtest=OracleSchemaSourceTest' test` | **PASS**, then **FAIL**, then **PASS** | 18:35:25: five source checks passed. A new parser-order assertion failed at 18:40:54 because it incorrectly expected `ALTER TABLE` to be the last V001 statement; the SQL correctly contains later tables. After fixing that test assertion, six checks passed at 18:41:25. These are source/parser checks, not Oracle execution. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | **PASS (final)** | 18:42:13: 32 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main and 15 test Java sources compiled. No DB URL or Google key required. Earlier full runs passed at 18:36:09 and 18:39:03 before the extra parser check. |
| `git diff --check` | **PASS** | 18:43 KST: exit 0; Git warned about future LF-to-CRLF conversion on tracked files but found no whitespace errors. |
| `OracleConnectionOracleIT` and `OracleSchemaOracleIT` in ordinary Surefire | **SKIPPED** | Excluded by `*OracleIT` name and `oracle-integration` tag. Compilation is not execution. |
| `@db/oracle/apply.sql` on empty Oracle 26ai | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | SQLcl/SQL*Plus and server were not invoked; Oracle DDL execution count 0. |
| Explicit `.\oracleIntegrationTest.cmd` / Oracle schema, UNIQUE/FK/CHECK, rollback, repeat guard, published-version triggers | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | Oracle server absent; preliminary `OracleSchemaOracleIT` exists but was not run. Actual Oracle constraint behavior remains unknown. |
| Flyway implementation/validation | **NOT_RUN** | Official support was reviewed; no verified artifact/runtime combination or Flyway integration was installed. Versioned manual SQL was selected. |
| Real Gemini API | **SKIPPED** | No real transport or key; network call count 0. |

One intermediate STEP 04 source-check test failed because its assertion assumed the wrong statement position; it was corrected and rerun successfully. The final static PASS must not be reported as an installed Oracle schema, migration success, or validated concurrency/performance.

## STEP 05 verification (2026-11-02 18:53-19:04 KST)

Before STEP 05 edits, Git showed the uncommitted STEP 03/04 source/docs work; it was preserved. Environment: Windows PowerShell, Temurin Java 21.0.3, Maven wrapper 3.9.6, eGov parent 5.0.0, Boot 3.5.6. All Maven commands ran from `bans_chatbot` with `-o` and the ignored workspace-local cache `C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo`. The real local-mock run had no `ORACLE_JDBC_URL`, `GOOGLE_API_KEY`, or `GEMINI_API_KEY` environment variables. No network dependency retrieval, Oracle connection, SQL execution, or real Gemini request occurred.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' test-compile` | **PASS** | 18:53:30: 41 main and 16 then-current test sources compiled with Java release 21; no tests executed by this command. |
| `java -cp 'target/classes;target/test-classes' egovframework.example.global.config.SyntheticOracleSeedRenderer db/oracle/seed/S001__synthetic_company.sql` | **PASS, generation only** | 18:53: generated 91,836-byte Oracle-only seed from the same PSV files; no SQL was executed. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | **PASS** | 18:56:23: 38 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 41 main and 21 test sources compiled. Includes 6 new fixture/seed-source checks and prior DB-free tests. Source equality does not establish Oracle SQL validity. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath '-Dmdep.outputFile=target/classpath.txt'` | **PASS** | 18:58:49: wrote ignored classpath file from the existing offline cache; no external download. |
| Direct `java -cp <target/classes + generated classpath> egovframework.example.EgovBootApplication --spring.profiles.active=local-mock --server.port=18085` and `Invoke-WebRequest` | **PASS** | 18:59:27 startup; 18:59:50 `/api/health` HTTP 200 `{"data":{"status":"UP"}}`; unauthenticated `/api/demo/employees` HTTP 401. The process was stopped afterward. This is process/API startup only, not a database or login result. |
| Fixture employee, account, document, grant and seed-source checks | **PASS** | Count/distribution, reserved mail, unique IDs, valid references, published/rejected/discarded cases, external-transfer flags, ACL examples, and deterministic seed equality passed inside the 38-test run. |
| `OracleSeedOracleIT`, `OracleSchemaOracleIT`, `OracleConnectionOracleIT` in ordinary Surefire | **SKIPPED, intentionally excluded** | `*OracleIT.java` and `oracle-integration` tag are excluded from default test execution; these sources compile only. |
| `@db/oracle/apply.sql`, `@db/oracle/seed/apply_seed.sql`, `.\oracleIntegrationTest.cmd` | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; none was invoked. Oracle seed idempotence, FK and transaction results have no PASS evidence. |
| Real Gemini API call | **SKIPPED** | No real transport enabled or key; external call count 0. |
| Production account activation/login and Spring Security chain | **NOT_RUN** | STEP 06 work, deliberately absent from STEP 05. |

The V001 SHA-256 manifest was recomputed after the still-unapplied schema refinement and passed the offline source test. Oracle 26ai parser/constraint/trigger checks, DB authorization joins, concurrency and performance remain pending; no final DB readiness conclusion follows from these results.

Final STEP 05 check: after adding the in-memory document-grant Bean assertion, the same offline `clean test` command passed again at **19:03:10 KST**, with **38 tests, 0 failures, 0 errors, 0 skipped** and 41 main/21 test sources compiled. A final addition of extra orphan/current-version assertions to the Oracle-only IT was then compiled with the same offline Maven arguments and `test-compile` at **19:03:49 KST**: **PASS**, 21 test sources compiled; the Oracle IT was **NOT_RUN**. `git diff --check` exited 0 at 19:02 KST (only Git's LF/CRLF conversion warnings). No Oracle/Gemini call was made during any of these checks.
