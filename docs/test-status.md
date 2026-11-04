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

## Pre-STEP 06 recheck (2026-11-03 KST)

Environment: Windows 11, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov Boot parent 5.0.0 / inherited Spring Boot 3.5.6. `MAVEN_HOME` pointed to the existing Maven distribution. Commands ran from `bans_chatbot` with `-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo`, the ignored workspace cache. No production source or dependency version changed.

| Check | Status | Evidence / limit |
|---|---|---|
| `java -version`; `mvnw.cmd -version` | **PASS** | Java 21.0.3 and Maven 3.9.6, same Java runtime. |
| `mvnw.cmd -o ... clean test` | **PASS** | 17:15:39 KST: 41 main and 21 test Java sources compiled; 38 tests, 0 failures, 0 errors, 0 skips; `BUILD SUCCESS`. Unit, Mockito, MockMvc, Spring profile, fixture and Oracle-source tests ran. Compilation also confirms current package imports. |
| Three `*OracleIT` classes in ordinary Surefire | **SKIPPED** by configured suite selection; **NOT_RUN** as Oracle tests | Sources compiled, but Surefire excludes their name and tag. No Oracle connection or SQL execution. The `38/0/0/0` Surefire summary describes selected tests only. |
| First `mvnw.cmd -o ... -DskipTests package` | **FAIL**, environment | 17:16:10 KST: Boot `repackage` could not resolve uncached plugin dependencies offline. Plain JAR goal ran, but full package failed. |
| Approved dependency-cache refresh and `mvnw.cmd ... -DskipTests package` | **PASS** | 17:18:11 KST: full Boot repackage created `target/bans_chatbot-1.0.0.jar`; tests were intentionally skipped in this packaging command. |
| Repeated `mvnw.cmd -o ... -DskipTests package` | **PASS** | 17:19:09 KST: full Boot package succeeded offline after cache refresh. |
| SQLcl migration/seed and `oracleIntegrationTest.cmd` | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | Not invoked; Oracle SQL behavior, constraints, triggers, ACL, transactions, locks, and performance remain unverified. |
| Real Gemini transport | **SKIPPED** | No production adapter or real call. |

The initial packaging failure was resolved by fetching missing plugin artifacts into the ignored cache after approval; no POM change was needed. The package PASS and 38-test PASS are separate checks. Oracle readiness and production authentication remain unverified; see `docs/handoff.md` and `docs/oracle-validation-backlog.md`.

## STEP 06 verification (2026-11-03 17:20-18:24 KST)

Environment: Windows PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGovFrame parent 5.0.0 / Spring Boot 3.5.6. Maven ran from `bans_chatbot`; `MAVEN_HOME` selected the existing wrapper distribution and `-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo` selected the ignored cache. No Oracle URL, real Gemini key, Oracle server, or real Gemini client was used. Security starter artifacts were fetched into the ignored cache after the offline `test-compile` initially failed to resolve them; the subsequent compile passed. No framework version upgrade or H2 dependency was added.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o ... test-compile` before Security artifacts were cached | **FAIL**, resolved | Offline dependency resolution lacked the newly required Security artifacts; this was an environment/cache failure, not an Oracle failure. An approved online `test-compile` then **PASS** after retrieval. |
| Intermediate `.\mvnw.cmd -o ... clean test` during implementation | **FAIL**, resolved | 38 tests selected; 10 failures while legacy MockMvc tests still asserted the interim fake-session boundary and a concurrent edit changed the schema manifest. Tests were updated to exercise the real security chain and the final manifest was regenerated. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test` | **PASS** at 17:50:43 KST | 59 main / 25 test Java files compiled; 45 tests, 0 failures, 0 errors, 0 skips; `BUILD SUCCESS`. Includes security-chain MockMvc activation/login/logout/me, CSRF, fixation, failure lock, inactive/version revocation, in-memory concurrent activation, existing policy tests, Oracle DDL manifest and Mapper XML parsing. This is not SQL execution. |
| Same offline Maven arguments with `-DskipTests package` | **PASS** at 17:52:43 KST | Repackaged `target/bans_chatbot-1.0.0.jar`; tests intentionally **SKIPPED** in this separate packaging command. The preceding `clean test` is the test evidence. |
| Direct `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.port=18086` plus live HTTP | **PASS** around 17:55 KST | A random, non-committed `BANS_LOCAL_ADMIN_PASSWORD` was supplied only to the process environment. `/api/auth/csrf`, CSRF-protected admin `/api/auth/login`, and `/api/auth/me` returned the expected `EMP-001` identity; process was stopped. This demonstrates local ephemeral login only. |
| `git diff --check` | **PASS** after STEP 06 edits | Exit 0; Git may warn about future line-ending normalization. |
| `AuthOracleIT` and other `*OracleIT` in ordinary Surefire | **SKIPPED** by suite selection; as Oracle tests **NOT_RUN** | Test source compiled, but no Failsafe Oracle test ran. |
| `@db/oracle/apply.sql`, seed, `.\oracleIntegrationTest.cmd`, real DB auth/lock/restart | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle JDBC connection, schema application, or SQL execution. `AuthOracleIT` has an additional `ORACLE_AUTH_TEST=YES` opt-in for a dedicated migrated/seeded schema. |
| Real Gemini Developer API | **SKIPPED** | No real transport or network request; 0 real calls. |

The selected 45/0/0/0 Surefire result excludes Oracle-only cases. Offline XML parsing and mock concurrency do not establish Oracle transaction atomicity, JDBC timestamp mapping, SQL validity, or persistence after restart. Actual Oracle concurrency/reboot cases are still **NOT_RUN** because their executable test code has not been completed; see `docs/oracle-validation-backlog.md`.

Final STEP 06 rerun after redacting the internal account object's string/JSON representation: `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package` **PASS at 18:24:22 KST**. The command compiled 59 main and 25 test sources, ran 45 selected tests with 0 failures/errors/skips, and created the Boot JAR. Surefire did not rerun the same tests during the `package` phase. This is the final source/test/package gate; Oracle IT remains **BLOCKED**, not passed.

## STEP 07 verification (2026-11-03 18:27–18:35 KST)

Environment rechecked: Windows 11 PowerShell, Temurin Java **21.0.3** (build/runtime), Maven Wrapper **3.9.6**, eGov parent **5.0.0**, Boot **3.5.6**. Existing 33 modified/untracked status entries on `dev` were preserved; STEP 07 introduced no dependency/version/schema change. No Oracle connection, SQL execution, Gemini request or dependency download was made. Maven used the ignored existing workspace cache.

Commands from repository root used `.\bans_chatbot\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' -f bans_chatbot/pom.xml` plus the goals below. The equivalent from `bans_chatbot` is `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package`. `MAVEN_HOME` was `C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6-bin\439sdfsg2nbdob9ciift5h5nse\apache-maven-3.9.6`.

| Actual check / goals | Status | Evidence / limits |
|---|---|---|
| `java -version`, wrapper `-version` | **PASS** | Java 21.0.3, wrapper Maven 3.9.6 running on the same JDK. |
| `test-compile` | **PASS** 18:29:24 | 68 main / then 25 test sources compiled; no tests executed by this command. Ignored log `step07-build.log`. |
| `test` | **PASS** 18:30:44 | 51 tests, 0 failures/errors/skips. Ignored log was under target and was removed by the subsequent clean. |
| First `clean test package` after extra suspension test | **FAIL**, resolved, 18:32:11 | 52 tests, 0 assertion failures, 1 error. The new test tried provisioning EMP-003, which is one of 20 employees but not one of the 12 fixture accounts. Corrected the test to use existing account EMP-010; no fixture expansion or production workaround. Ignored `step07-final.log`. |
| Final `clean test package` | **PASS** 18:34:26 | 68 main / 29 test source files compiled with release 21; **52 tests, 0 failures, 0 errors, 0 skips**, BUILD SUCCESS; repackaged Boot JAR. `package` did not rerun identical Surefire tests. Ignored `step07-final-rerun.log` and `bans_chatbot/target/surefire-reports/`. |
| Organization MockMvc / Mockito / XML source checks | **PASS** within final suite | Five real security-chain MockMvc cases plus service-port validation and Mapper XML/bound-SQL test. Department filter, count/page limit, exact six public fields, departments, literal injection-shaped search, sort whitelist, numeric errors, CSRF, 401/403, create/conflict, role-field rejection, department/ACL refresh, departure exclusion, suspension/session/login denial and minimal success audit checked. These are DB-free tests. |
| First `git diff --check` | **FAIL**, resolved | Four appended docs had an extra blank line at EOF. Trimmed only trailing whitespace; no existing content discarded. |
| Optional packaged local-mock process + live localhost HTTP | **NOT_RUN** | Automatic approval review rejected the proposed process/HTTP command with `blocked by policy` before execution. No startup/live endpoint PASS is claimed for STEP 07. Existing STEP 06 live evidence remains historical. MockMvc login and API demonstration did run. |
| All `*OracleIT` / `oracle-integration` in ordinary Surefire | **SKIPPED** by suite selection; Oracle execution **NOT_RUN** | New OrganizationOracleIT compiled; no Failsafe suite invoked. The selected suite's zero skips does not mean Oracle ran. |
| Migration/seed, real organization Mapper SELECT/UPDATE/row locks/audit transaction/sort/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; `oracleIntegrationTest.cmd` not executed. Separate backlog lists executable preliminary IT and missing concurrency/restart/constraint cases. |
| Real Gemini API / Mock Gemini network tests | Real API **SKIPPED**; network tests **NOT_RUN** | Organization-only STEP added no Gemini transport or network test; real call count 0. Existing offline AI policy/mock tests remained in the passing suite. |

Final whitespace check result is recorded below after document synchronization. Passing fake search or XML parsing proves no Oracle predicate, collation, paging, lock, transaction, constraint or durability. STEP 07's DB-free API contract gate is met; final persistent DB/production readiness remains pending.

STEP 07 file inventory (relative to repository root; separate from preserved STEP 06 changes):

- Added production: `bans_chatbot/src/main/java/egovframework/example/organization/controller/OrganizationController.java`, `organization/dto/DirectoryModels.java`, `organization/service/OrganizationService.java`, `organization/repository/OrganizationRepository.java`, `organization/repository/InMemoryOrganizationRepository.java`, `organization/repository/OracleOrganizationRepository.java`, `organization/mapper/OrganizationMapper.java`, `audit/domain/PersonnelEvent.java`, `audit/mapper/PersonnelAuditMapper.java` (short paths share the same Java base directory).
- Added XML: `bans_chatbot/src/main/resources/egovframework/example/organization/mapper/OrganizationMapper.xml`, `bans_chatbot/src/main/resources/egovframework/example/audit/mapper/PersonnelAuditMapper.xml`.
- Incrementally updated production: `auth/repository/InMemoryAccountRepository.java`, `global/config/OracleMyBatisConfiguration.java`, `common/exception/ApiExceptionHandler.java` under the same Java base directory.
- Added tests: `organization/controller/OrganizationMockMvcTest.java`, `organization/service/OrganizationServiceContractTest.java`, `organization/mapper/OrganizationMapperSourceTest.java`, `organization/mapper/OrganizationOracleIT.java` under `bans_chatbot/src/test/java/egovframework/example`; updated existing `auth/service/ActivationConcurrencyTest.java` for the injected organization port.
- Updated docs: `docs/api-contract.md`, `docs/decisions.md`, `docs/handoff.md`, `docs/test-status.md`, `docs/oracle-validation-backlog.md`.

Final synchronized `git diff --check`: **PASS**, exit 0 (only existing LF/CRLF normalization warnings). The organization Controller also labels local-mock responses `X-Demo-Data: SYNTHETIC_EPHEMERAL`; a final gate is rerun after this label addition.

Final source gate after the local-mock synthetic-response label: same offline wrapper `clean test package` **PASS at 2026-11-03 18:36:46 KST**, 68 main / 29 test sources, **52 tests, 0 failures/errors/skips**, repackaged JAR. Ignored log: `step07-label-final.log`. This supersedes the earlier final timestamp for STEP 07. The label itself was not verified by a live HTTP run. Oracle execution remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

## STEP 08 verification (2026-11-03 KST)

Windows PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6; offline workspace cache. Before changes `git status --short` was empty on `dev`. No Oracle connection, SQL execution, Gemini call, dependency download or live network test occurred.

From `bans_chatbot`, with `MAVEN_HOME` set to the existing Maven 3.9.6 wrapper distribution: `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package`.

| Check | Status / evidence |
|---|---|
| Initial `clean test` before new chat tests were created | **PASS**, 18:59:50 KST, 52 existing tests. A Python test-file creation command failed because it used a root-relative path while running in the module directory; no test was created by that command. Log `bans_chatbot/target-step08-test.log` is ignored. |
| First `clean test package` with new tests | **FAIL**, 19:01:15 KST, 56 selected tests, one assertion failure. Test expected numeric HTTP status in `ApiException` message, while the exception exposes a public code. Production code did not cause the failure. |
| Immediate rerun while test-file correction was made | **FAIL**, 19:01:56 KST, 56 tests, one same assertion failure because Maven had already compiled the old test before the edit. |
| Final independent `clean test package` after test correction | **PASS**, 19:02:40 KST, 75 main/33 test sources, **56 tests, zero failures/errors/skips**, repackaged Boot JAR. Log `bans_chatbot/step08-verified.log` is ignored. Includes real security-chain MockMvc, service cursor/idempotency/read/ACL tests, fake two-thread race, Oracle XML/source/manifest checks. It does not execute Oracle. |
| Default `*OracleIT` | **SKIPPED** by Surefire selection; Oracle execution **NOT_RUN**. New ChatOracleIT compiled only. |
| Oracle V003, Mapper SELECT/INSERT/UPDATE, locking/uniqueness/concurrency/CLOB semantics | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No actual SQL execution; see Oracle backlog. |
| Real Gemini API | **SKIPPED**; 0 calls. Mock Gemini network test **NOT_RUN**, outside chat STEP. |
| Live local-mock HTTP process | **NOT_RUN**. REST exercised through MockMvc, not a packaged process. |

STEP 08 files: new `chat/controller/ChatController.java`, `chat/dto/ChatModels.java`, `chat/service/ChatService.java`, `chat/repository/{ChatRepository,InMemoryChatRepository,OracleChatRepository}.java`, `chat/mapper/ChatMapper.java` under `bans_chatbot/src/main/java/egovframework/example`; matching chat Mapper XML in resources; new V003 under `bans_chatbot/db/oracle`; tests `ChatMockMvcTest`, `ChatServiceTest`, `ChatMapperSourceTest`, `ChatOracleIT`. Updated MyBatis MapperScan, migration apply/verify/manifest, schema source/Oracle IT and five relevant docs plus database-design.md.

After adding an offline V003 single-statement parser assertion, the same wrapper/cache command with `test` (without `clean package`) **PASS at 19:04:11 KST**: 56 tests, 0 failures/errors/skips, BUILD SUCCESS. This is the final test source state; the prior `clean test package` is the package evidence. Oracle validation is unchanged.

## STEP 09 verification (2026-11-03 KST)

Environment: Windows 11 PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6. At STEP entry `dev` contained uncommitted STEP 08 migration/chat/source/docs changes, all preserved. Maven commands ran in `bans_chatbot` with `MAVEN_HOME` set to the existing wrapper distribution and `-o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`. No dependency download, Oracle connection, Oracle SQL or Gemini request occurred. E2E's only network traffic was to its embedded local test server.

| Executed command/check | Result / evidence |
|---|---|
| `.\mvnw.cmd -o ... test-compile` after declaring `spring-boot-starter-websocket` | **FAIL** 19:16:25: starter 3.5.6 not cached offline. Replaced with Boot-managed, already cached `spring-websocket` and `spring-messaging` modules. No version changed. |
| Same `test-compile` after direct module declarations | **FAIL** 19:17:01: `ChatStompSecurity.current` accepted `StompHeaderAccessor` while handler injection supplied `SimpMessageHeaderAccessor`. Corrected parameter type. |
| Same `test-compile` after type correction | **PASS** 19:17:29: 78 main/33 then-current test source files compiled. |
| `.\mvnw.cmd -o ... test` before E2E source | **PASS** 19:17:51: 56 existing tests, zero failures/errors/skips. This did not exercise STOMP client traffic. |
| `.\mvnw.cmd -o ... '-Dtest=ChatStompE2ETest' test` | **PASS** 19:18:47, 2 embedded-server STOMP tests (A/B/C queues and CSRF/Origin rejection). |
| Same targeted test after outbound guard | **PASS** 19:19:48, 2 tests. |
| Same targeted test after duplicate/logout extension | **FAIL** 19:20:20, 1 error: test cleanup called `disconnect()` on a socket already closed after logout; message isolation assertions had passed. Fixed test cleanup. |
| `.\mvnw.cmd -o ... clean test package` after cleanup | **PASS** 19:21:22, 58 tests and Boot JAR. |
| Final `.\mvnw.cmd -o ... clean test package` after account-suspension E2E addition | **PASS** **19:22:46 KST**: 78 main/34 test sources, **59 tests, 0 failures, 0 errors, 0 skips**, Boot JAR repackaged. Three tests used the real embedded Tomcat/WebSocket server with process-local `test` repositories; no Oracle. Ignored log `bans_chatbot/step09-final2.log` and Surefire reports record the result. Surefire did not rerun identical tests during the package phase. |
| `git diff --check` | **PASS** after docs synchronization; only line-ending normalization warnings. |
| Ordinary Surefire `*OracleIT` | **SKIPPED** by suite selection; Oracle execution **NOT_RUN**. |
| Real Oracle 26ai commit/event order, persistence, locks, broker fault recovery | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; no JDBC or SQL execution. |
| Real Gemini API | **SKIPPED**; 0 calls. Mock Gemini network test **NOT_RUN**, outside chat scope. |
| Browser UI/manual production deployment | **NOT_RUN**. The E2E used a real test STOMP client and embedded server, not a browser UI or Oracle-backed deployment. |

STEP 09 file inventory: modified `bans_chatbot/pom.xml`, `auth`-agnostic `global/security/SecurityConfiguration.java`, `chat/service/ChatService.java`, and five relevant docs (`api-contract`, `decisions`, `handoff`, `test-status`, `oracle-validation-backlog`). Added `chat/websocket/ChatWebSocketConfiguration.java`, `ChatStompSecurity.java`, `ChatStompController.java` under the production domain and `chat/websocket/ChatStompE2ETest.java` under the mirrored test domain. Existing uncommitted STEP 08 files were not rewritten for STEP 09. No Oracle DDL/Mapper changed in this STEP.

## STEP 10 verification (2026-11-03 KST)

Environment: Windows 11 PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6. At entry, `dev` had uncommitted STEP 08–09 files; those were preserved. Commands ran from `bans_chatbot` with the wrapper, `-o`, the existing ignored `.maven-repo`, and `MAVEN_HOME` set to the existing Maven 3.9.6 distribution. No Oracle connection/SQL, Gemini call, dependency retrieval or external network operation occurred.

| Actual command/check | Status / evidence |
|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' test-compile` | **PASS** 19:49:01 KST, 86 main and 34 then-current test Java sources. No tests executed by this goal. |
| Same wrapper/cache with `'-Dtest=ManagedDocumentServiceTest,ManagedDocumentMapperSourceTest,OracleSchemaSourceTest' test` | **PASS** 19:50:11 KST, 9 selected tests. Offline lifecycle, ACL, source/manifest checks; no Oracle execution. |
| Same wrapper/cache with `'-Dtest=ManagedDocumentMockMvcTest' test` | **PASS** 19:51:05 KST, 1 real Spring Security MockMvc test, session/CSRF/approval/grant revoke/XSS. |
| First full `clean test package` | **FAIL** 19:52:09 KST, 64 tests, one failure in pre-existing STOMP E2E suspended-account outbound assertion. New document tests passed. The outbound interceptor checked STOMP command only; broker delivery can have `SimpMessageType.MESSAGE` without a STOMP command. |
| Targeted `'-Dtest=ChatStompE2ETest' test` before fix compiled | **FAIL** 19:53:14 KST, reproduced suspended-account delivery issue. |
| Same targeted test after checking Spring message type | **PASS** 19:54:02 KST, all 3 embedded-server STOMP tests, including suspended-account delivery block. This fixed a real STEP 09 security gap discovered by STEP 10's broader gate. |
| Final full `clean test package` after fix | **PASS 19:54:44 KST**: 86 main/38 test sources; **64 tests, 0 failures/errors/skips**, Boot JAR repackaged. Ignored `bans_chatbot/step10-final-fixed.log` has the evidence. |
| Ordinary `*OracleIT`/tag | **SKIPPED** by suite selection; real Oracle test **NOT_RUN**. New `ManagedDocumentOracleIT` compiles but needs explicit opt-in. |
| V004 migration, document Mapper/lock/trigger/FK/audit rollback/concurrent approval on Oracle | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; no SQL execution. |
| Real Gemini API | **SKIPPED**, 0 calls. External disclosure remains disabled for user-authored content. |

New STEP 10 files: document `ManagedDocument` domain, `DocumentModels` DTO, `ManagedDocumentService`, `DocumentWorkspace` port, in-memory and Oracle adapters, `ManagedDocumentMapper` interface/XML, `ManagedDocumentController`, V004 reviewer migration, unit/MockMvc/Mapper-source/Oracle-IT tests. Updated Oracle apply/manifest/source/IT, MapperScan, STEP 09 STOMP outbound check, and relevant docs. No H2 or alternate persistent DB was added.

Final `git diff --check` **PASS** after documentation synchronization (only Git LF/CRLF normalization warnings). The 64 selected Surefire tests exclude all Oracle-only cases; no Oracle or Gemini request ran.

## STEP 11 verification (2026-11-03 KST)

Environment: Windows PowerShell, Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / resolved Spring Boot 3.5.6. Commands ran in `bans_chatbot`, with `MAVEN_HOME` set to the existing Maven 3.9.6 wrapper distribution and `-o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`. No dependency download, real Oracle connection/SQL or Gemini request occurred. The prior STEP 10 and older uncommitted work was preserved.

| Actual command/check | Result / evidence |
|---|---|
| `.\mvnw.cmd -o ... test-compile` (before new test source) | **PASS 20:20:15 KST**; 91 main / 38 then-current test sources compiled, no tests executed. A preceding checksum helper in the same shell had **FAIL** because its relative path repeated `bans_chatbot`; it did not change the manifest. The corrected `Get-FileHash db/oracle/V005__document_search_tags_indexes.sql` then **PASS** and appended the exact V005 hash. |
| `.\mvnw.cmd -o ... '-Dtest=DocumentSearchServiceTest,DocumentSearchMapperSourceTest,ManagedDocumentMockMvcTest,OracleSchemaSourceTest' test` | **PASS 20:22:16 KST**, 10 selected tests, 0 failures/errors/skips. Covers synthetic 휴가/배포, ACL/grant revoke, stale links, bounds/literal SQL-shaped input, real security MockMvc and offline XML/hash. |
| First `.\mvnw.cmd -o ... clean test package` | **FAIL 20:23:35 KST**, 67 tests, one failure: `OracleSeedSourceTest` detected the manually appended V005 tag SQL differed from the canonical seed renderer. No package was produced by this run. |
| Updated `SyntheticOracleSeedRenderer`, `.\mvnw.cmd -o ... test-compile`, then `java -cp 'target/test-classes;target/classes' ...SyntheticOracleSeedRenderer 'db/oracle/seed/S001__synthetic_company.sql'` | **PASS** around 20:24 KST. Regenerated the checked-in seed from the canonical fixture. Seed and V005 source only; no Oracle SQL executed. |
| `.\mvnw.cmd -o ... clean test package` after generated seed | **PASS 20:25:10 KST**, 67 tests, 0 failures/errors/skips and Boot JAR. |
| `.\mvnw.cmd -o ... '-Dtest=DocumentSearchServiceTest,ManagedDocumentMockMvcTest' test` after added bounds/401 assertions | **PASS 20:25:57 KST**, 3 selected tests, 0 failures/errors/skips. |
| `.\mvnw.cmd -o ... clean test package` after local-mock context test | **PASS 20:28:37 KST**, **68 tests, 0 failures/errors/skips**, Boot JAR. Local-mock Spring context loaded synthetic search data; MockMvc exercised protected API routes. |
| Final `.\mvnw.cmd -o ... clean test package` after retaining the prior published version during review | **PASS 20:30:16 KST**, **68 tests, 0 failures/errors/skips**, Boot JAR. `bans_chatbot/step11-final3.log` and Surefire reports contain the final evidence. Oracle SQL remains unexecuted. |
| Ordinary Surefire `*OracleIT` | **SKIPPED** by suite selection. New `DocumentSearchOracleIT` compiled but was **NOT_RUN**; it requires explicit opt-in and real 26ai. |
| V005 DDL/seed, Mapper JOIN/`DBMS_LOB.INSTR`, locks, plan/Korean analyzer, concurrent revoke on real Oracle | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Source parsing and fake tests are not database execution. |
| Real Gemini API | **SKIPPED**, 0 calls. Mock Gemini network test **NOT_RUN**; no AI endpoint added in STEP 11. |
| Browser/manual local-mock deployment | **NOT_RUN**. Spring context/MockMvc tested, but no browser or separately deployed process was used. |
| `git diff --check` | **PASS** after final docs update, exit 0; only Windows LF/CRLF normalization warnings. |

### Follow-up live local-mock HTTP smoke (2026-11-03 20:50 KST)

The first hidden `Start-Process` attempt was **BLOCKED by automatic approval review** (`blocked by policy`); no server was launched by that attempt. A foreground `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.port=18081` then started successfully with a runtime-generated, temporary admin password outside the workspace. Actual localhost HTTP calls **PASS**: `/api/health` returned `UP`; CSRF/session login and `/api/auth/me` returned `EMP-001`; `GET /api/documents/search?q=휴가&page=0&size=10` returned two fixture hits; a returned fragment source link returned matching content; `q=배포` returned zero hits for D01 admin; `GET /api/documents/DOC-003/published` returned 404; anonymous search returned 401. The temporary password file was deleted and the exact server PID was stopped. This is a real local-mock HTTP result, not Oracle SQL or Gemini execution. Browser UI, mutating document workflow over live HTTP, and Oracle-backed HTTP remain **NOT_RUN** or **BLOCKED** as applicable.

STEP 11 files: new domain search repository port, local-mock/Oracle adapters, `DocumentSearchService`, `DocumentSearchMapper` interface/XML and source/Oracle-IT/service/local-mock tests; modified document Controller/DTO/domain/workspace/Mapper/service/MockMvc tests; V005 migration, runner/manifest/metadata/source checks, canonical seed renderer/SQL/preflight/verification and relevant docs. No H2 or alternate DB was added. See `oracle-validation-backlog.md` for executable Oracle conditions and remaining races.

## STEP 12 provider verification (2026-11-04 KST)

Entry: clean `dev`, commit `1c8fd0a`. Windows PowerShell / Temurin **21.0.3**, wrapper Maven **3.9.6**, actual eGov parent/runtime **5.0.0** / resolved Boot **3.5.6**. Rechecked source, profiles, resolved parent and official eGov/Google documentation before edits. No dependency/framework version, database technology, schema/SQL/Mapper or existing route changed. Logs below are Git-ignored module files; Surefire results are under target/surefire-reports. Official documentation browsing and an unsuccessful Maven artifact download attempt are separate from Gemini API calls. Real Google model requests and Oracle connections were **0**.

Common environment and arguments for actual module-directory wrapper commands (the cache argument below abbreviates the same absolute value used in execution):

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Actual command / check | Result, time and evidence |
|---|---|
| `.\mvnw.cmd -o $cacheArg org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get '-Dartifact=com.google.genai:google-genai:1.80.0' '-Dtransitive=false'` | **FAIL 14:57:21**, artifact/POM absent from offline cache. `step12-sdk-evaluation.log`. No SDK runtime execution and no model request. |
| `.\mvnw.cmd $cacheArg '-Dmaven.wagon.http.retryHandler.count=0' '-Dmaven.wagon.rto=5000' org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get '-Dartifact=com.google.genai:google-genai:1.80.0' '-Dtransitive=false'` | **FAIL 15:05:52**, actual repository transfer denied: `Permission denied: getsockopt`. `step12-sdk-resolution.log`. Latest SDK dependency resolution **BLOCKED by network environment**; SDK Java 21 runtime/transitive validation **NOT_RUN**. No version was guessed or old SDK adopted. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=ProviderConfigurationTest,CallBudgetTest,GeminiProviderNetworkTest' test` first attempt | **FAIL 15:02:50**, 37 tests, 29 failures / 1 error. A relative URI beginning with `gemini-...:generateContent` was parsed as a URI scheme, causing requests to fail before HTTP. Fixed with an explicitly relative `./` model path. `step12-targeted.log`. |
| Same targeted command after URI fix | **PASS 15:03:42**, 37 tests, 0 failures/errors/skips. `step12-targeted2.log`. Contains 32 real loopback HTTP/Mockito network cases, two budget tests and then-three configuration tests. |
| `.\mvnw.cmd -o $cacheArg clean test package` after smoke/error-contract source | **PASS 15:07:05**, 106 tests, 0 failures/errors/skips, Boot JAR. `step12-full.log`. |
| `$env:BANS_GEMINI_SMOKE='NO'` then `.\mvnw.cmd -o $cacheArg -Pgemini-smoke test` | **PASS for the disabled opt-in gate, 15:08:45**; selected `GeminiLiveSmokeIT`, 1 test **SKIPPED**, zero executed test bodies. `step12-smoke-gate.log`. This is not a Gemini smoke success: real model call remains **NOT_RUN**. The permission variable was set only in that child process. |
| Final `.\mvnw.cmd -o $cacheArg clean test package` after wire-debug rejection test | **PASS 15:11:01**, **98 main / 47 test sources, 107 tests, 0 failures/errors/skips**, repackaged Boot JAR. `step12-final.log` and fresh Surefire reports. 39 new selected cases plus 68 prior cases; the package phase did not execute the tests twice. |
| Default Mock/context wiring, active-actor checks, configuration limits and explicit live activation/profile/key/model/acknowledgement gates | **PASS** in final suite. A missing key and an ambient fake key both retain Mock by default. Actual local-mock Spring context remains DB-free. Wire-debug rejects live activation. AI-only `oracle` profile selection test is not an Oracle application startup test. |
| Mock HTTP protocol, filtering and failure paths | **PASS** in final suite. Every actual provider HTTP request targets 127.0.0.1. Checks fixed model path, key only in header, greeting-only body, token cap, no actor/history/tools, no cookie forwarding, 403/429/5xx/302/400, exactly one attempt, Retry-After seconds/date/invalid/large input, cooldown, connect-error classification, header/body stalls, response-size cap, malformed/duplicate/trailing JSON, empty/blocked/truncated/tool/key-echo replies, concurrency rejection/release and no requests for invalid actors. Public error MockMvc and captured output check sanitized fields/no upstream key or payload. |
| Eight Oracle-only classes in ordinary Surefire | **SKIPPED by suite selection**; compiled, not executed. No Failsafe/Oracle command invoked. |
| Oracle 26ai connection/migration/seed/SQL/ACL/transaction/lock/concurrency/restart/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Existing backlog unchanged in execution status. STEP 12 adds no DB operation. |
| Real Gemini synthetic greeting / project-specific Free-tier quota/access | **NOT_RUN**, zero calls. Separate smoke source exists; its disabled gate was tested, not the actual API. No key was inspected, printed or used for a real request. |
| Arbitrary-question safety review, document-to-Gemini integration, AI HTTP/UI | **NOT_RUN / intentionally not implemented in STEP 12**. Only fixed synthetic greetings are accepted. |
| Separate packaged browser/manual live deployment | **NOT_RUN** in STEP 12. Spring contexts, common-error MockMvc, existing embedded-server STOMP E2E and provider loopback tests did execute. |

Java emitted the existing Mockito dynamic-agent warning and an AssertJ deprecated test-helper warning; these were warnings, not build failures. Provider implementation has no persistence or raw request/response logging. The passing REST adapter is built from Java 21 HttpClient and existing Jackson; it does not establish latest Google SDK compatibility, actual Google quota/billing, Oracle behavior or production readiness. Counters are per-process and reset on restart. See `gemini-free-tier.md` for official links/activation details and the STEP 12 handoff for the complete file inventory.

Final documentation/source `git diff --check`: **PASS**, exit 0 (Windows line-ending normalization warnings only). Aggregated 37 fresh Surefire XML reports confirm 107 tests / 0 failures / 0 errors / 0 skips. Git diff contains no database or Mapper XML changes. Source changes are uncommitted for user review; no existing user edits were overwritten.

## STEP 13 read-only tools verification (2026-11-04 KST)

Entry: dev at 1c8fd0a, with all prior STEP 12 modified/untracked files preserved. Windows PowerShell, Temurin **21.0.3**, Maven Wrapper **3.9.6**, actual eGov parent/runtime **5.0.0**, resolved Boot **3.5.6**. Rechecked repository/profile/build state, parent POM and official eGov and Google documents. No framework/dependency version, DB dialect, migration/seed or key activation setting changed. Oracle SQL source changes are classification SELECT columns and fixed audit metadata INSERT only. No Oracle/Gemini API connection, installation, paid activation or dependency retrieval was performed. Documentation web browsing is separate from API execution.

All commands ran in bans_chatbot with the existing workspace cache and wrapper environment:

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Actual command/check | Status, time and scope |
|---|---|
| `.\mvnw.cmd -o $cacheArg compile` | **PASS 15:30:35**, 110 main sources, no tests. `step13-compile.log`. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=AiToolSecurityTest,ToolRegistryTest,AiToolMockMvcTest,AiToolAuditMapperSourceTest,DocumentSearchMapperSourceTest' test` first attempt | **FAIL 15:35:17**, test compilation: missing closing `);` in new mutation-test lambda. No tests executed. Corrected test syntax. `step13-targeted.log`. |
| Same targeted command after syntax correction | **FAIL 15:36:15**, 39 tests / two assertion failures. New test counted successful returns instead of all invocations after expected 404; changed to verify both invocations. Existing mapper source test forbade external flag text anywhere in SQL; updated it to require classification SELECT columns while still forbidding those flags in internal predicates. These were test expectations, not a bypass in production. `step13-targeted2.log`. |
| Same selected classes plus `ManagedDocumentMapperSourceTest` | **PASS 15:38:36**, 41 tests / 0 failures/errors/skips. Added draft-state/current workspace classification validation. `step13-targeted3.log`. |
| `.\mvnw.cmd -o $cacheArg clean test package` after additional within-run status/session and immutable/excerpt tests | **PASS 15:42:15**, 150 tests / 0 failures/errors/skips, Boot JAR. `step13-full.log`. |
| Final `.\mvnw.cmd -o $cacheArg clean test package` after explicit constructor/null binding and transport/forged-actor tests | **PASS 15:44:44**, **110 main / 52 test sources, 152 tests / 0 failures/errors/skips**, repackaged Boot JAR. `step13-final.log` and fresh Surefire XML. Package did not rerun the same tests. |
| New tool/security/projection/budget tests | **PASS** in final suite: 21 AiToolSecurityTest, 17 ToolRegistryTest, 5 actual-security AiToolMockMvcTest, 1 audit XML source test, plus 1 new existing-provider network guard = 45 new selected cases over STEP 12. |
| Forged caller/user/role/department and legacy session actor | **PASS**: current account/session identity is used; cross-department/private document stays hidden. Supplied old admin authority does not bypass resource ACL. Missing real security context is rejected. |
| Revoke/flag/withdraw/version/status/credentials/logout/department changes during runs | **PASS** with serialized memory adapters/Mockito. No stale excerpt reaches Mock after the tested pre-send changes; changed department is reloaded. This is not proof of Oracle transaction races. |
| Question injection/SQL/salary/write tools/unknown fields/numeric or JSON abuse | **PASS**: exact reviewed question gate, registry/schema/size/call bounds reject invalid operations. Allowed document instructions remain data and cannot add a tool or permission. Non-exportable readable document text and noncanonical employee input are absent from captured Mock payload. |
| Outbound/HTTP/audit allowlists | **PASS**: employee six fields, source four fields/200 code points, immutable copies; HTTP only counts/notice. Metadata audit has only actor/outcome/call/item fields; no question/answer/source/argument/key. UI contains transfer notice/unchecked acknowledgement and uses textContent. It was checked by MockMvc, not a visual browser run. |
| Request/run limits and live transport boundary | **PASS**: per-session five-per-minute/one active run, input/tool/result limits, execution budget and safe audit-failure error. Concrete non-Mock provider rejected before reads; actual live adapter default tool method makes 0 HTTP requests in loopback test. |
| Nine Oracle-only `*OracleIT` classes / real-Gemini smoke in ordinary Surefire | **SKIPPED by suite selection**, compiled but not executed. New AiToolOracleIT is opt-in via ORACLE_AI_TOOL_TEST=YES. Final suite's zero skips counts selected tests only. |
| Actual Oracle SELECT/JDBC record mapping, classification flags, audit INSERT/REQUIRES_NEW, ACL JOIN/race/locking/transactions/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No Oracle suite or SQL was executed. |
| Actual Gemini API/project quota/access and latest SDK runtime validation | **NOT_RUN** in STEP 13, real API requests 0. Inherited STEP 12 SDK dependency/network limitation remains unresolved; no retry or upgrade attempted. |
| Real model function-call loop, arbitrary-question safety review, real outbound tool context | **NOT_RUN / intentionally not implemented**. Only fixed reviewed examples and Mock payloads are enabled. |
| Packaged process/browser/manual UI workflow | **NOT_RUN** in STEP 13. Actual-security MockMvc, Spring contexts, existing embedded STOMP E2E and provider loopback tests did run. |

Warnings about Mockito dynamic agents, a prior deprecated AssertJ helper and a classpath persistence-provider probe did not fail the build or create a DataSource. Ordinary tests retain zero Oracle connections and zero real Gemini calls. Source/mapper parsing is not Oracle SQL validation. Per-session counters are process-local; the final recheck does not remove a concurrent change after that check. Full modified-file inventory and next-step conditions are in the STEP 13 handoff; security/operational bounds are in ai-tool-security.md.

Final checks after documentation synchronization: wrapper `-version` **PASS** (Maven 3.9.6 / Java 21.0.3 / Windows 11); `git diff --check` **PASS**, exit 0 with line-ending warnings only. Aggregating 41 fresh Surefire XML reports confirms 152/0/0/0. Git diff shows no db/oracle migration/seed/manifest changes. Changes remain uncommitted, including preserved STEP 12 work.

## STEP 14 executed validation (2026-11-04 KST)

Entry: dev at 1c8fd0a with prior uncommitted STEP 12/13 work preserved. Windows 11 PowerShell / Java 21.0.3 / Maven Wrapper 3.9.6 / eGovFrame 5.0.0 / Boot 3.5.6. Rechecked actual source, git status, profiles and official version/Google evidence; no versions/dependencies/configuration/DDL/seed changed. Real Oracle connections **0**, real Gemini API calls **0**; loopback HTTP and documentation browsing are separate from external model invocation.

Actual commands ran from bans_chatbot with:

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Executed command/check | Status / evidence |
|---|---|
| `.\mvnw.cmd -o $cacheArg compile` | **FAIL 16:14:12 KST**, new generate timeout parameter conflicted with a catch variable named timeout. No tests ran. Renamed catch variable to expired. Ignored `step14-compile.log` preserves the failure. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=AiQuestionServiceTest,QuestionPolicyTest,AiQuestionMockMvcTest,AiQuestionAuditMapperSourceTest,GeminiProviderNetworkTest' test` | **PASS 16:19:41 KST**, 77 tests / 0 failures/errors/skips, 121 main and 58 test sources. `step14-targeted.log`. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 16:22:38 KST**, **196 tests / 0 failures/errors/skips**, Java 21 compile of 121 main / 58 test sources and repackaged executable JAR. `step14-final.log`. Package reports Surefire already run, not a second execution. |
| Fresh Surefire XML aggregation | **PASS**, 45 reports total 196/0/0/0, matching the final log. 44 additional selected cases over STEP 13: 26 service + 2 policy/budget + 11 MockMvc + 1 audit mapper + 4 loopback cases. |
| Vacation source / expense owner / canonical public contact | **PASS**, server-built answer/source and six public DTO fields; structured path has zero Provider calls. Source link itself is exercised through authenticated document GET. |
| Retired version, nonexistent rule, other department, ambiguous evidence, forged caller/role | **PASS**, no invented answer, no unauthorized sources or unreviewed outbound. Current actual security session is authoritative. |
| Post-Provider revoke, withdrawal, changed version/content/external flag; structured department change | **PASS**, deterministic Mockito/memory tests return UNANSWERABLE without stale facts/sources. This proves the Java recheck only, not an Oracle cross-transaction race guarantee. |
| Prompt injection/PII/unknown question, invented quote/ID, extra/duplicate/trailing/malformed model JSON | **PASS**, unreviewed questions do not reach Provider; model drafts never become arbitrary facts. Outbound contains only reviewed question/S1/excerpt. |
| Provider 429/503/timeout, zero retries, minimized wire JSON, remaining-time timeout | **PASS**, Mockito and actual loopback HttpServer; no Google requests. AI MockMvc failures leave chat REST and auth available. Existing localhost STOMP regression also PASS; concurrent AI-failure/STOMP load is not separately exercised. |
| Auth/CSRF, shared request limits, stages/tools/provider/time bounds, audit failure and metadata allowlist | **PASS**, actual-security MockMvc and focused tests. No question/answer/source/key fields in audit; answer/prompt toString redacts text. HTML uses textContent, no innerHTML/browser storage. |
| Ten Oracle-only classes and real Gemini smoke in normal Surefire | **SKIPPED by suite selection** (compiled, excluded by name/tag); final zero skips applies only to selected tests. New AiQuestionOracleIT also requires ORACLE_AI_QUESTION_TEST=YES. |
| Oracle ACL JOIN, mapper/JDBC/enum/CLOB mapping, audit INSERT/REQUIRES_NEW, migrations/locks/races/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Oracle suite/SQL execution not attempted. Mapper XML parsing is not SQL success. |
| Real Gemini greeting/question, account quota/model access, Oracle+Gemini E2E | **NOT_RUN**. No key used and no live activation. SDK runtime evaluation retains the unresolved STEP 12 dependency limitation; not retried in STEP 14. |
| Packaged live process/manual browser/visual UI verification | **NOT_RUN**. Executable packaging and MockMvc HTML/service integration passed. No final production-readiness assertion. |

The intermediate compile FAIL is resolved by the final PASS; it is not erased. Warnings about Mockito dynamic-agent loading, the existing deprecated assertion helper, missing optional templates and the persistence-provider probe were non-fatal. Profile tests still verify DB-free default/test/local-mock wiring. Counters are process/session-local, and ten seconds is a stage/HTTP budget rather than a forced interrupt of repository/audit I/O. Oracle validation remains pending as listed in oracle-validation-backlog.md.

Post-document checks at **16:28 KST**: wrapper `-version` PASS (Maven 3.9.6, Java 21.0.3, Windows 11); `git diff --check` found no whitespace errors (line-ending warnings only); `git diff --numstat -- bans_chatbot/db/oracle` is empty; new question main/test source trailing-whitespace search is empty. `jar tf target/bans_chatbot-1.0.0.jar` confirms production question/audit classes and XML are packaged, with no QuestionTestSupport or OracleIT entry. No tests were rerun after documentation-only edits. Changes remain uncommitted, including preserved STEP 12/13 work.

## Full regression, route access and Git hygiene (2026-11-04 KST)

User requested a full run of every available feature, API access checks and ignore rules for local notes/diagnostics. This is maintenance of STEP 01–14, not implementation of STEP 15. Preserved existing uncommitted work and framework/DB/model/profile choices. Environment: Windows 11, Java 21.0.3, Maven Wrapper 3.9.6, eGovFrame 5.0.0, Boot 3.5.6. No dependency retrieval or real Oracle/Gemini request.

Executed from bans_chatbot using the same MAVEN_HOME and cacheArg defined in STEP 14 above:

| Actual command/check | Result |
|---|---|
| `.\mvnw.cmd -o $cacheArg '-Dtest=RouteAccessMockMvcTest' test` | **PASS 16:47:12**, 60 tests / 0 failures/errors/skips; ignored regression-routes.log. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 16:48:29**, **256 tests / 0 failures/errors/skips**, 121 main / 59 test sources, executable JAR; ignored regression-full.log. No duplicate Surefire run during package. |
| Fresh Surefire aggregation | **PASS**, 46 XML reports, total 256/0/0/0. Includes all previous 196 cases and 60 additional route checks. |
| Registered MVC mappings versus explicit test inventory | **PASS**, all **37 method/path pairs**: 17 GET and 20 mutations, including the local-mock-only demo and two UI routes. Framework /error, implicit HEAD/OPTIONS and WebSocket upgrade are not counted as these MVC controller routes. |
| Anonymous access to every mapping | **PASS**, 32 protected mappings return 401 with valid CSRF supplied where needed. Public health/CSRF GETs return 200; public login/activation with empty JSON correctly return 400. |
| Every mutation with real login and missing CSRF | **PASS**, all 20 return sanitized 403. This is expected denial, not a broken endpoint. |
| Authenticated read paths, unknown path and wrong method | **PASS**, 17 mapped GETs return 200 with appropriate current test resources/parameters; unknown API returns 404 and POST to GET-only health returns 405. |
| Previously service-only document transitions through MVC | **PASS**, create/edit draft/review/reject/add version/review/publish/withdraw and search hiding. Existing tests also exercise grants/revoke/resource denial and retired sources. |
| Packaged JAR loopback HTTP smoke | **PASS by 16:50 KST**, actual `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.address=127.0.0.1 --server.port=0`; cookie-bearing HttpClient fetches CSRF then checks all 37 anonymous mappings against the above expected statuses. Ignored regression-http-check.log ends PACKAGED_HTTP_PASS routes=37. No credentials/CSRF bodies printed. Process stopped in finally. Authenticated business flows use MockMvc and existing embedded STOMP tests, not this anonymous wire smoke. |
| First local smoke-script attempt | **FAIL before HTTP**, startup log was empty and Regex.Match received null. Added an empty-log guard, retried successfully. This was a disposable verification-script defect, not an application startup/API failure; preserved here separately from final PASS. Script is intentionally under ignored .scratch/route-smoke.ps1. |
| Git ignore behavior | **PASS**, 9 required source/doc/example paths remain eligible and 17 representative local artifact/secret paths are ignored using git check-ignore --no-index. No existing source/docs remain silently ignored. |
| Oracle-only ten classes | **SKIPPED by normal-suite selection**; actual SQL/DB/transaction/race/performance validation **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, no Oracle suite attempted. |
| Real Gemini smoke/E2E and latest SDK runtime | **NOT_RUN**, real Gemini requests 0. Real API is not authorized by this request to run offline regression. |

Feature coverage in the executed full suite:

| Area | Executed checks / result |
|---|---|
| Bootstrap/profiles/common | **PASS**: actual eGov module smoke, keyless Mock, no DataSource in normal profiles, unsafe profile/config fail-fast, API error/request-ID contract, validation and source-level Oracle migration/seed invariants. |
| Authentication | **PASS**: activation use/expiry/reissue/concurrency, actual login/logout/session fixation/expiry/credential/status boundaries, roles and CSRF. |
| Organization | **PASS**: 20 invented employees/5 departments, public DTO/filter/page validation, admin create/status/move, privilege injection and session refresh. |
| Direct chat REST | **PASS**: participants, pair reuse, send/idempotency/conflict, pagination/read-state/unread counts, outsiders and invalid input. |
| WebSocket/STOMP | **PASS**: all 3 ChatStompE2ETest scenarios on an actual embedded localhost server: two-party ack/delivery/outsider/reconnect; CSRF/origin rejection; suspended-account outbound denial. |
| Documents/search | **PASS**: role+ACL workflow, review/grants/revoke/current publication, sanitized rendering, source/version/search bounds, internal/external distinction and synthetic fixtures. |
| AI Provider/tools/questions/audit | **PASS**: safe defaults/config/quotas, loopback failure/timeout/JSON/key boundaries, fixed read-only tools, synthetic disclosure, current-source/structured responses, prompt injection/unanswerable and metadata-only audit. No real model output or Oracle facts certified. |

Git hygiene correction: previous `**/*credential*` silently excluded production `auth/domain/CredentialStatus.java`, leaving it absent from Git's index despite builds depending on it. Narrowed secret matching to filenames/directories; this existing source is now visible as untracked and **must be included in a future code commit**. It was not rewritten or staged. Added *.local.md/*.local.txt, docs/local, notes/local, scratch/.scratch, editor/merge backups and JVM dumps/recordings. Existing *.log/target/cache/env rules remain. Shared decisions/contracts/handoff/test evidence stay versioned; do not blanket-ignore Markdown/text or remove tracked docs from the index.

Final checks at **16:52:55 KST**: git diff --check **PASS** (exit 0); actual packaged smoke PID no longer exists; .scratch/route-smoke.ps1 and regression logs match ignore rules; no Oracle migration/seed diff. No application code changed after the full PASS. Changes are not staged/committed.

## 로컬 자료 폴더 정리 (2026-11-04)

과거 로그의 현재 위치는 모두 `bans_chatbot/local-notes/`다. 로그 55개·javac 진단 1개·로컬 HTTP 확인 스크립트 1개를 이동했고, 이동 직후 SHA-256 일치를 확인했다. 스크립트는 이동 후 모듈/출력 경로를 새 위치에 맞게 수정했다. 해당 폴더를 Git에서 제외하고 .gitignore 주석을 한글로 번역했다. 원래 경로와 해시는 이 로컬 폴더의 moved-files.json에 보관한다. 공용 docs는 이동하거나 Git에서 제외하지 않았다.

애플리케이션 기능 테스트·JAR 실행은 이번 정리에서 **NOT_RUN**이며 앞선 전체 256건 PASS/경로 37개 PASS 기록을 그대로 유지한다. 파일 이동은 삭제·덮어쓰기 없이 수행했다. Oracle/Gemini 실행 상태와 SQL은 바뀌지 않았다.

**PASS 16:57:26 KST**: 이동 파일 57개 존재 및 기존 경로 정리, 수정한 스크립트 외 파일 해시 일치, PowerShell AST 구문 검사, local-notes Git 제외, CredentialStatus.java 제외되지 않음, 루트/모듈 최상위 잔여 로그 0개, git diff --check exit 0을 실제 확인했다.
