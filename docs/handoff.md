# Handoff

## STEP 01

The base server uses eGov 5.0.0 modules, Java 21, and Boot 3.5.6 through the eGov parent. Default profile is `local-mock`. The only endpoint is `/health`. No domain modules, security, repository, SQL, or Gemini integration have been added. Oracle 26ai Free is absent; keep its validation blocked until the server is installed and explicitly configured.

Verification: `clean compile`, direct Java `local-mock` boot, HTTP 200 health, and two manually launched JUnit smoke tests passed offline. Standard Maven `clean test` and `spring-boot:run` failed because required artifacts are absent from the existing local cache; see `test-status.md`. Oracle tests remain blocked. A next STEP can proceed for DB-free features, while the normal Maven test gate needs an approved artifact cache refresh. Do not mark final readiness or Oracle compatibility as verified.

Before each next STEP: inspect tree, Git status, relevant source/config, and these docs. Preserve existing work and perform only the requested STEP. Recheck the offline Maven dependency situation before changing plugin versions. Do not treat `/health` as database readiness.

## Pre-STEP 02 package cleanup (2026-11-02)

The permanent package-by-domain rule is in root `AGENTS.md`. `egovframework.example.EgovBootApplication` remains the scan-root bootstrap. The existing process probe and its test moved to `egovframework.example.global.health`; `/health` and its response remain unchanged. No domain package exists yet because no domain feature has been implemented. Create `auth`, `organization`, `chat`, `document`, `ai`, and `audit` only as code arrives; use `common` only for genuinely shared code. Future MyBatis interfaces and XML follow the owning domain and matching namespace. Eclipse Java settings were aligned with Maven's Java 21 release. No STEP 02 API, security, persistence, or profile implementation was started in this cleanup.

See `docs/test-status.md` for this cleanup's actual build and test results. Oracle validation remains in `docs/oracle-validation-backlog.md`.

The relocated smoke tests passed through a manual cached JUnit launcher, and `local-mock` returned HTTP 200. The normal offline Maven `test` still fails because Launcher 1.12.2 is absent. A clean compile also hits a Java `AccessDeniedException` on cached `reactive-streams-1.0.4.jar`; forked javac prints the exception despite Maven reporting success. Treat clean build and default tests as unresolved gates. Do not change dependency versions or claim Oracle validation to work around these local cache issues.

During verification, the open Eclipse workspace also changed `.project` builder ordering and `.settings/org.eclipse.wst.common.component` generated-source metadata. These files were not intentionally edited for the package move and were left as found; review them separately if preparing a commit.

## STEP 02 handoff (2026-11-02)

Implemented package-by-domain foundation in `organization`, `ai`, `common`, and `global`. `auth`, `chat`, `document`, and `audit` remain uncreated until their first real feature. The organization adapter contains 20 invented employees in 5 invented departments, but there are **no login accounts** yet. Its Controller → Service → port path is exposed only as `/api/demo/employees` under `local-mock`. The AI adapter is a fixed, offline mock, not Gemini integration. `/health` remains compatible; `/api/health` demonstrates the `{data:...}` envelope. Server-generated request IDs, validation handling, public error bodies, profile conflict checks, and Oracle missing-config fail-fast are in place. No DB table, Mapper, SQL, Spring Security session, ACL, WebSocket, document workflow, or real AI request has been implemented.

The ordinary Maven test excludes `*OracleIT` and `oracle-integration`; explicit `oracleIntegrationTest.cmd` invokes Oracle and Failsafe Maven profiles only after required environment variables are present. A preliminary metadata test exists but was not run. The Oracle JDBC artifact is declared only in the explicit Maven `oracle` profile and is not locally cached. Oracle 26ai Free is absent. `docs/oracle-validation-backlog.md` tracks all outstanding DB checks.

DB-free direct Java compilation and 13 JUnit tests passed; final `local-mock` startup and both health HTTP routes passed. Maven `clean test` is **FAIL** in this PC's offline cache: Java zipfs access denied while closing external cached JARs, and subsequent `test` reaches Surefire but cannot resolve JUnit Platform Launcher 1.12.2. See `docs/test-status.md` for commands, times, and scope. Do not call the Maven test gate green. The next STEP may develop DB-free domain features using these boundaries, while restoring the standard Maven gate remains a required verification task. Production security and Oracle compatibility are still unverified.

## STEP 03 handoff (2026-11-02)

The user's normal PowerShell Maven run at 18:03:49 established the STEP 02 gate as 13/13 PASS; that supersedes the historical offline-cache note above for the user environment. STEP 03 added role/status/session identity policy in `auth`, direct-conversation participant policy in `chat`, document scope/grant/approval policy in `document`, and a separate segment-level AI disclosure decision in `ai`. The exact anonymous route allowlist is enforced for current HTTP MVC requests by an interim session interceptor. The local-mock employee endpoint now returns 401 without a server session; there is no login route or mock-account login yet.

Offline Maven `clean test` at 18:16:46 PASS: 26 tests, zero failures/errors/skips, using an ignored workspace copy of existing Maven artifacts. `docs/security-policy.md`, `docs/authorization-matrix.md`, `docs/threat-model.md`, and `docs/test-status.md` contain the decisions and test limits. No Spring Security dependency/filter chain, Oracle schema/Mapper, persisted ACL/account, document/chat API, real Gemini transport, or production safety reviewer was added. Do not treat the interim MVC interceptor as complete authentication; add Spring Security server sessions and current-status refresh before exposing protected production routes, and enforce ACL in domain services as Oracle Mappers arrive. The STEP 04 Oracle schema/ACL validation list is in `docs/oracle-validation-backlog.md` and remains BLOCKED by the uninstalled Oracle server.

Final package-aligned test run at 18:22:06 also passed: 26/26 tests, 35 main and 13 test source files compiled. Use this latest result for the STEP 03 gate.

## STEP 04 handoff (2026-11-02)

Preserved the uncommitted STEP 03 work. Added all 17 proposed Oracle 26ai tables in `bans_chatbot/db/oracle/V001__initial_schema.sql`, followed by V002 indexes, immutable-published-version/fragment triggers, and five role rows. `apply.sql` checks that the target schema is empty, applies scripts in order, and verifies 17 tables/five roles/two valid triggers. The SHA-256 manifest is checked by an offline JUnit test. `docs/erd.md` and `docs/database-design.md` explain relationships, normalization, Oracle syntax, manual application, and unresolved cross-table authorization rules. No POM version, H2 profile, Mapper, API route, or real AI transport changed.

The final DB-free Maven `clean test` passed at 18:42:13 KST: 32 tests, 0 failures/errors/skips, Java 21.0.3, Maven wrapper 3.9.6, offline local cache. The six new checks inspect structure, hashes, and the JDBC script parser only. `OracleSchemaOracleIT` was compiled but excluded from default tests; no Oracle connection or SQL execution occurred. Actual empty-schema migration, repeat guard, constraints, triggers, rollback, ACL joins, and performance remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. See `docs/test-status.md` and `docs/oracle-validation-backlog.md` for commands and statuses.

Next implementation can use the stable table/column contract, but must not call the schema installed or production-ready. Later domain Mappers must use Oracle SQL under their owning packages. Before real protected document/chat operations, bind Spring Security server sessions, reload account/grant status, and verify cross-table rules in a transaction. After Oracle installation, use a dedicated empty schema and the explicit application/IT procedure in `docs/database-design.md`; review the SQL against the actual 26ai engine before any final readiness judgment.

## STEP 05 handoff (2026-11-02)

The canonical invented company data is now in four PSV files under `bans_chatbot/src/main/resources/fixtures`: five departments with 4/3/6/4/3 employees, 20 employees with reserved `@bans.example` mail/extension/duty, 12 pending accounts, and 20 documents with scope/grant/approval/rejection/discard examples. The existing `organization` in-memory directory now reads those resources; `document` has a local-mock/test in-memory grant adapter. The local-mock employee route labels responses `X-Demo-Data: SYNTHETIC_EPHEMERAL`. There is still no login/activation route, document API, persistent repository, or real Gemini client.

The unapplied V001 Oracle DDL was refined with company mail/extension/duty, stable document business code/category, and `DISCARDED` version state; its manifest hash was updated. A deterministic renderer generated the separate Oracle-only `db/oracle/seed/S001__synthetic_company.sql` from the same PSV data. `apply_seed.sql` has preflight, rollback-on-error and exact-count checks. All account rows remain pending with an unusable marker; STEP 06 must implement random activation-code issuance and hashing. See `fixture-and-seed.md` and `database-design.md` before any manual DB operation.

Offline Maven `clean test` passed again at 19:03:10 KST with 38 tests, zero failures/errors/skips, after the local-mock document-grant Bean check. The local-mock process started at 18:59 and returned HTTP 200 for `/api/health`; the demo employee endpoint returned 401 without a session, as designed. No Oracle or Google key was present in that verification environment. A final Oracle-only IT assertion change compiled at 19:03:49 but did not run. Seed syntax, FK behavior, idempotence, rollback, and migration compatibility remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. The next STEP can build secure account activation/login on these pending records, but must keep synthetic fixtures and Oracle storage distinct and require real Oracle tests before claiming DB readiness.

## Pre-STEP 06 repository audit (2026-11-03 KST)

This section is based on a fresh inspection of checked-in files and new build results. It preserves the historical sections above. The original STEP 01-05 prompt text and `README.md` were not found in the source tree, so prior requirements beyond recorded code and decisions are unconfirmed. No feature, production source, SQL, dependency version, or configuration was changed in this audit.

### Purpose and directory structure

The design documents describe an internal employee directory, one-to-one chat, controlled documents, optional AI answers, account controls, and audit events. Actual HTTP implementation is limited to health and a synthetic employee demonstration. `egovframework.example.EgovBootApplication` is the component-scan root. `bans_chatbot` is the sole Maven module; root `docs/` contains architecture, security, API, database, fixture, test, and Oracle backlog records. Root `.maven-repo/` is an ignored local dependency cache; `.metadata/` is workspace metadata.

| Module path under `src/main/java/egovframework/example` | Verified implementation |
|---|---|
| `auth` | `domain` status/role/session actor, `fixture` pending-account reader, `service` role and exact public-route policies plus interim MVC session interceptor. No auth Controller, repository, or Mapper. |
| `organization` | `domain` employee/department records, `fixture` PSV reader, `repository` read port and in-memory adapter, `service` read service, `controller` local-mock demo endpoint. |
| `chat` | `domain` direct-conversation record and `service` participant policy only. |
| `document` | `domain` scope/segment/grant/record models, `fixture` reader, `repository` grant port and in-memory adapter, `service` read/search/approval policy. |
| `ai` | `client` answer port and offline mock, `service` disclosure policy and question-review port without production adapter. |
| `common` | API response/error records, exception advice, request-ID filter, generic PSV reader. |
| `global` | Profile guard, Oracle DataSource config, MVC interceptor registration, process health Controller. |

There is no `audit` code, UI/template, WebSocket, domain DTO package, Java MyBatis Mapper interface, Mapper XML, `@MapperScan`, `SqlSession` setup, or Oracle-backed repository. `src/test/java/egovframework/example` mirrors the implemented domains and includes three `global.config.*OracleIT` classes. `src/main/resources/fixtures` contains four canonical synthetic PSV files (5 departments, 20 employees, 12 planned accounts, 20 documents). `src/main/resources/db/sampledb.sql` is an unused starter artifact. `db/oracle` holds V001/V002, the checksum manifest, explicit apply/verify scripts, and separate synthetic seed scripts.

### Stack, profiles, and current functionality

`pom.xml` sets Java release 21 and eGov Boot parent 5.0.0; its locally resolved parent inherits Spring Boot 3.5.6. Direct dependencies include eGov MVC/dataaccess/id-generator/property/reactive modules, Spring Boot web/Thymeleaf/Log4j2/validation, Commons DBCP2, Lombok, and test starter/Selenium. The eGov dataaccess POM transitively specifies MyBatis 3.5.19 and mybatis-spring 3.0.5; their availability is not a configured Mapper. The `oracle` Maven profile alone declares `ojdbc17:23.26.4.0.0`. Maven Wrapper uses Maven 3.9.6. No H2 or alternative persistent database is configured.

Default Spring profile is `local-mock`; `local-mock` and `test` exclude JDBC and transaction-manager auto-configuration and use in-memory adapters. `oracle` requires `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, and `ORACLE_DB_PASSWORD`, rejects non-Thin URLs, and checks a real connection at startup; `oracle-it` requires `oracle`. SQL initialization is disabled. `GET /health` and `GET /api/health` are process probes. Only `local-mock` exposes `GET /api/demo/employees` through Controller → Service → in-memory directory; it labels the response `X-Demo-Data: SYNTHETIC_EPHEMERAL` and requires a server session. Common JSON responses use `data` or fixed public error fields plus a server-generated request ID.

No Spring Security dependency or `SecurityFilterChain`, password encoder, activation/login/logout/session issuance, CSRF integration, or current-account/role reload exists. The interim MVC interceptor trusts an existing `HttpSession` `SessionActor`; tests inject that actor directly. `POST /api/auth/activation` and `POST /api/auth/login` are anonymous-route reservations, not implemented endpoints. `SessionActor` uses `AccountStatus` values `ACTIVE/SUSPENDED/DEPARTED`, while Oracle `employee_account.account_status` permits `PENDING/ACTIVE/SUSPENDED/DISABLED` and `employee.employment_status` independently permits `ACTIVE/SUSPENDED/DEPARTED`. STEP 06 must define both lifecycles and their mapping before real authentication. First-admin activation also needs an explicit safe bootstrap decision: all seeded accounts are pending and `account_activation.issued_by_employee_id` is a required employee FK.

### Confirmed STEP 01-05 implementation and limits

| STEP | Confirmed | Still absent or unverified |
|---|---|---|
| 01 | Java 21/eGov Boot bootstrap and process health. | Health does not prove DB readiness. |
| 02 | API envelope/errors/request ID, profiles, mock organization read path, offline AI answer port, Oracle connection gate, separate test suites. | Production domain routes and persistence. |
| 03 | Role/session, direct-chat participant, document ACL/approval, and per-segment AI disclosure policies; interim MVC boundary. | Production authentication/filter chain, status refresh, document/chat routes, real question review and Gemini transport. |
| 04 | Oracle-only 17-table V001, V002 indexes/two triggers/five roles, manual empty-schema runner, static checks and Oracle IT source. | Oracle execution, Mapper, transaction/service SQL behavior. |
| 05 | Canonical synthetic PSV fixture, process-local employee/grant adapters, deterministic Oracle seed and source checks; unusable pending account markers. | Usable credentials, activation/login, persistent adapters, executed seed. |

### New verification and Git state

Windows 11 `java -version` reported Temurin 21.0.3; `mvnw.cmd -version` reported Maven 3.9.6 using Java 21.0.3. From `bans_chatbot`, `MAVEN_HOME` was set to the existing Maven 3.9.6 installation and `-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo` selected the ignored workspace cache.

| Check | 2026-11-03 result | Scope |
|---|---|---|
| `mvnw.cmd -o ... clean test` | **PASS** at 17:15:39 KST; 41 production and 21 test sources compiled; 38 tests, 0 failures/errors/skips. | Unit, Mockito, MockMvc, Spring context, fixture and SQL-source checks. Three Oracle IT classes compiled but did not run. |
| Initial `mvnw.cmd -o ... -DskipTests package` | **FAIL** at 17:16:10; missing Spring Boot repackage plugin dependencies in offline cache. | No source compilation error; full Boot JAR did not finish. |
| Approved cache retrieval then `mvnw.cmd ... -DskipTests package` | **PASS** at 17:18:11; created repackaged `target/bans_chatbot-1.0.0.jar`. | Tests skipped for this command; the prior `clean test` is test evidence. No POM/source change. |
| Repeat `mvnw.cmd -o ... -DskipTests package` | **PASS** at 17:19:09. | Full packaging now succeeds offline with the refreshed ignored cache. |
| Oracle IT, SQLcl application, real Gemini | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** for Oracle; Gemini **SKIPPED**. | No Oracle connection/SQL or real AI request was made. |

Git was clean on `dev` before this audit at `67fc590` (2026-11-02 19:06:56 +0900), preceded by `7cad2da` and `7482547`. This audit leaves only `docs/handoff.md` and `docs/test-status.md` modified; build output/cache are ignored. Recheck `git status` before STEP 06.

### Oracle validation and STEP 06 entry

Before Oracle installation, use the wrapper's DB-free `clean test` and `-DskipTests package`, with source/hash checks kept distinct from SQL execution. After installing Oracle 26ai Free, create a dedicated empty schema, inspect `manifest.sha256`, and explicitly apply `@db/oracle/apply.sql` with SQLcl/SQL*Plus per `docs/database-design.md`. Apply `@db/oracle/seed/apply_seed.sql` only to a separate intended synthetic schema after preflight. Then set the three Oracle environment variables and run `oracleIntegrationTest.cmd`; destructive opt-in IT flags belong only on empty disposable schemas. Verify real DDL/triggers/constraints, seed FK/current-version/idempotence/rollback, account activation and status/role freshness, ACL joins and revocation, concurrency, locking, and performance. All remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED** until execution on Oracle.

STEP 06 can start as DB-free implementation under the existing domain layout and fixed versions, but production authentication is not ready. Resolve the status mapping and first-admin activation design, add the required Spring Security server-session chain and current-state refresh before exposing protected routes, and keep credentials/tokens, secrets, and raw AI transcripts out of Git, responses, logs, and persistent storage. The full original STEP 06 prompt is not in the repository; obtain the user's next instruction before inferring its exact scope.

## STEP 06 handoff (2026-11-03 KST)

The user supplied the STEP 06 scope in this session. The portfolio app remains a synthetic 20-person/5-department direct-messenger and AI-assistance project. Versions remain Java 21, eGovFrame parent 5.0.0 / Boot 3.5.6, MyBatis, and Oracle AI Database 26ai Free as final storage. No Oracle server or real Gemini client is present. The repository was on `dev`; before STEP 06 edits only `docs/handoff.md` and `docs/test-status.md` were modified by the earlier audit. Those edits were preserved. At the end of STEP 06, source, DDL manifest, tests, and the five relevant docs are modified/untracked; see `git status --short` rather than the earlier historical pre-STEP status.

`auth/controller` now has activation, login, logout, current-user, and CSRF-token endpoints. `auth/service` issues admin-authorized 24-hour one-use activation codes and checks BCrypt passwords with five-failure/15-minute account lock. `auth/repository` defines account/activation ports, a synchronized process-local fixture adapter for `test`/`local-mock`, and an `oracle` MyBatis adapter. `auth/mapper` and matching XML implement Oracle account/role lookup, row locking, conditional credential activation, failed-login state, and code invalidation/consumption. `global/security` configures the actual Spring Security session/CSRF chain and per-request account/status/version refresh. The former MVC boundary now reads a trusted request attribute from that chain. V001's **unapplied** employee-account schema gained version/lock fields and the SHA-256 manifest changed. Domain ownership remains intact; `organization`, `chat`, `document`, `ai`, and `common` code was preserved except test adaptation.

Run DB-free verification from `bans_chatbot` in Windows PowerShell after setting `MAVEN_HOME` to the existing Maven 3.9.6 wrapper distribution: `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test`. The 2026-11-03 17:50:43 KST run **PASS**: 59 main / 25 test Java files compiled, 45 selected tests, 0 failures/errors/skips. These include actual security-chain MockMvc, one-use/reissue/expiry, CSRF, fixation, account lock, stale-session rejection, mock concurrency, and offline Mapper XML parsing. The `*OracleIT` cases compile but are excluded from Surefire; they did not connect to Oracle. A later package/startup result, if run, belongs in `docs/test-status.md`.

Local demonstration requires an operator-supplied, non-committed `BANS_LOCAL_ADMIN_PASSWORD` of 12-128 characters at process start to activate synthetic `EMP-001` **in memory**. All other fixture accounts start pending. Login can then be demonstrated through CSRF plus `/api/auth/login`; issuing an employee activation code currently requires calling the internal admin service and a separately designed secure delivery channel. The operator must never put real company data, credentials, or raw activation codes in Git/logs. The memory adapter loses all state on restart and proves no DB transaction or durability.

After Oracle 26ai installation, use a dedicated empty schema, review the changed V001 hash before first application, apply the versioned SQL and synthetic seed explicitly, then configure `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, and `ORACLE_DB_PASSWORD`. The `oracle` profile fails if these are absent and has no mock fallback. Run `.\oracleIntegrationTest.cmd` only against a dedicated test schema; `ORACLE_AUTH_TEST=YES` opts into the compiled auth Mapper/transaction cases after migration and seed. Verify conditional token consumption, concurrent activation, failed-login commit, status/role refresh, reboot persistence, timestamp mapping, and cookie behavior with HTTPS. These are **BLOCKED: ORACLE_26AI_NOT_INSTALLED** today. A secure first-admin bootstrap for the all-pending Oracle seed, code delivery, real Oracle concurrent/restart IT, and WebSocket/STOMP session authorization remain open. STEP 07 may proceed with DB-free work, but DB authentication readiness and production security readiness are not established.

Final STEP 06 gate after the last account-serialization guard: offline `clean test package` **PASS at 18:24:22 KST**, 45 selected tests with zero failures/errors/skips and a repackaged JAR. The separate live `local-mock` HTTP CSRF/login/me smoke **PASS** using a runtime-generated synthetic admin password. No Oracle or real Gemini call occurred. Detailed command/status evidence is in `docs/test-status.md`.

## STEP 07 handoff (2026-11-03 KST)

Implemented the six requested organization read/admin APIs under the existing Spring Security server-session boundary. See `api-contract.md` for exact identifiers, DTOs, limits and examples. Added OrganizationRepository, deterministic in-memory adapter and Oracle MyBatis adapter, domain-owned Mapper/XML, plus minimal personnel audit event/Mapper/XML. Oracle MapperScan now includes organization/audit. Personnel create/change and success audit share an Oracle transaction; employee changes lock the target row. No schema or dependency change was needed in STEP 07; pre-existing STEP 06 schema/POM/source/docs changes remain intact and uncommitted.

The in-memory account adapter reads current organization department/status during session refresh and login. A next request uses the new department for existing document ACL policy; suspension/departure revokes the stale session. Tests exercise real login, CSRF, 401/403, strict mutation JSON, creation/conflict, bounded search and literal injection-shaped values, public six-field output, department ACL before/after movement, departure exclusion and suspension login rejection. No SQL is executed by these tests. New employees do not receive accounts or roles; secure onboarding remains separate. Existing 12 synthetic planned accounts remain available under STEP 06 activation policy. Local mock restarts reset all personnel/audit changes. Legacy demo output remains the static fixture.

Oracle integration source `organization.mapper.OrganizationOracleIT` is gated by `ORACLE_ORGANIZATION_TEST=YES` plus the explicit Failsafe task and dedicated migrated/seeded schema. Real SELECT/UPDATE/sort/paging/lock/audit atomicity, constraints, concurrent account/personnel changes, restart persistence and performance remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Do not interpret XML parsing, compilation or fake tests as Oracle PASS. No real Oracle/Gemini call or dependency download occurred.

Next STEP may proceed after the DB-free gate; it must continue to use refreshed request actors for resource ACL and add separate STOMP/session revalidation if introducing WebSocket. No frontend, work-task assignment CRUD, account/role administration, Gemini transport/model changes or production readiness claim is part of STEP 07. See `test-status.md` for actual final build evidence and `oracle-validation-backlog.md` for remaining integration conditions.


Final STEP 07 `clean test package` **PASS at 18:34:26 KST**: 68 main/29 test sources, 52 selected tests, zero failures/errors/skips, repackaged Boot JAR. One intermediate new-test fixture-ID error was corrected (EMP-003 has no account; EMP-010 does). Optional packaged live localhost smoke was **NOT_RUN** because automatic approval review rejected its command with `blocked by policy`; no STEP 07 live startup result is claimed. Oracle remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

Final source gate after the local-mock synthetic-response label: same offline wrapper `clean test package` **PASS at 2026-11-03 18:36:46 KST**, 68 main / 29 test sources, **52 tests, 0 failures/errors/skips**, repackaged JAR. Ignored log: `step07-label-final.log`. This supersedes the earlier final timestamp for STEP 07. The label itself was not verified by a live HTTP run. Oracle execution remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.
