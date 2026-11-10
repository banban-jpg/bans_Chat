# Handoff

## STEP 19 최종 종합 점검 (2026-11-04 KST)

현재 코드를 다시 조사하고 신규 업무기능/API정책/DDL/의존성 변경 없이 최종 검증했다. [RUNBOOK.md](RUNBOOK.md)→[API_ENDPOINTS.txt](API_ENDPOINTS.txt)→[TEST_RESULTS.md](TEST_RESULTS.md)→[FINAL_VERIFICATION_REPORT.md](FINAL_VERIFICATION_REPORT.md)를 신규 인수 경로로 사용한다. Oracle은 [별도 체크리스트](ORACLE_VERIFICATION_CHECKLIST.md)를 따른다.

기본 wrapper clean test package **PASS19:17:23, 288선택=285PASS/3SKIP/실패0**, 별도 Mock부하·장애2PASS19:19:25. Controller HTTP39(JSON36+HTML3)를 source/runtime로 대조하고65실HTTP요청으로 전 매핑 정상응답과 일부negative를 확인했다. source확장자검사·로그증거이름 같은 검증 도구 누락만 수정했고 생산Java/DDL/POM 수정0이다. 신규 HTTP test/표준Python 생성기/공용5문서와 기존 문서 갱신이 STEP19 변경이다.

남은 High: Oracle 첫관리자 보안초기화 미구현, 실DB검증 전부 BLOCKED. REST채팅fallback STOMP미발송·소스도메인 양방향 참조·payload상한/운영TLS/실Chrome/실Gemini 등은 보고서에 제안과 미검증으로 남겼다. 완성 퍼센트나 최종운영 준비완료를 선언하지 않는다. 기존 STEP16~18 미커밋 작업 보존, 커밋/배포/설치/SQL적용/원격모델호출0. 로그·raw evidence·개인메모는 ignored local-notes에만 둔다.

## STEP 18 최종 인수인계 — 오프라인 버전 (2026-11-04)

**인수 가능: 코드·오프라인 회귀·local-mock 시연 산출물. 인수 미완료: Oracle 포함 최종 기능·보안·성능·운영 준비.** dev / 88836b4에서 미커밋 STEP 16/17을 보존한 채 정리했다. Java 21/eGovFrame 5.0.0/Boot 3.5.6·모델·SDK·POM·운영 API/DDL/seed/Mapper는 STEP 18에서 바꾸지 않았다. 실제 Oracle/Gemini 호출은 0회다.

신규 인수자는 README의 A 경로를 먼저 따른다. 현재 구조는 architecture/erd/security-policy 앞의 최신 절이 우선하며 이후 STEP 설명은 당시 이력이다. [setup.md](setup.md)에 Windows wrapper/기존 캐시/기동/계정 활성화/역할별 시연, [oracle-install-validation.md](oracle-install-validation.md)에 설치 전 승인·백업·전용 스키마·정확한 단계별 명령/gate/재실행 위험과 수락 조건, [known-limitations.md](known-limitations.md)에 차단/미구현을 모았다.

STEP 18 수정 목록:

- 루트 README.md, .env.example(값 없는 변수 이름만).
- scripts/local.ps1(build/test/run), smoke-local.ps1(임시 포트 Mock/health/protected API/종료), verify-source.ps1(HEAD/index/worktree 제한 시크릿 패턴·문서 링크·source hash inventory).
- performance/scripts/track-a.ps1, track-b.ps1의 **UTF-8 BOM 인코딩만** 보정해 Windows PowerShell 5의 한글 오독 방지. 부하/DB 동작 변경 없음.
- test global/config/OracleConnectionOracleIT.java: JDBC major=26 가정을 Oracle26ai Free banner 확인으로 수정. 신규 OracleReleaseEvidenceTest.java 4 case. actual DB 실행 아님.
- docs/setup.md, oracle-install-validation.md, known-limitations.md 신규; architecture/erd/security-policy/api-contract/decisions/gemini-free-tier/handoff/test-status/oracle-validation-backlog 현재화.

수락 증거: scripts/local.ps1 build로 wrapper clean test package **PASS 18:46:50**, selected287 = **284 PASS/3 SKIPPED**, 실패·오류0, 실행 JAR. 핵심 MVC+실제 JS HTTP/STOMP 5건 **PASS 18:51:10**. Mock startup/health/root/static/protected401 smoke도 PASS 후 프로세스 종료. 선택적 Chrome·부하2 skip은 숨기지 않으며 부하 실험은 STEP 17에 별도 PASS로 남아 있다. 상세 실제 명령/초기 smoke FAIL/시크릿 검사 범위는 test-status.md를 따른다.

이번에 확인한 중요 차단: **Oracle 최초 관리자 보안 bootstrap 미구현**. pending EMP-001 seed를 앱에서 자기 활성화할 수 없다. Oracle UI 전체 수락 전에 신원 확인/최소 권한/일회성/감사를 갖춘 초기화 구현·검증이 필요하다. dummy hash·provisionForTest·일괄 ACTIVE 우회는 금지한다. 26ai 제품명은 JDBC major26을 뜻하지 않으며 현재23.26 릴리스/Free edition을 실제 환경에서 증명해야 한다.

Oracle 설치 후 migration/seed·제약/락·API 영속성·ACL·commit 후 STOMP·AI Oracle 검색(Mock)·부하·백업 복구가 실제26ai에서 모두 통과해야 DB 검증 완료로 바꾼다. 현재는 BLOCKED: ORACLE_26AI_NOT_INSTALLED. Chrome 클릭/모바일/키보드 paint는 환경 BLOCKED, 실 Gemini/SDK 및 배포/TLS/복구 drill은 NOT_RUN/대기다. 운영 확장 전 내부망/방화벽/프록시/비밀·로그 접근/백업 정책을 별도 검증한다.

01~18 단계 산출물 인수인계는 이 상태로 마무리한다. 남은 작업을 삭제하거나 최종 운영 준비 완료로 선언하지 않는다. 변경은 미커밋이며 STEP16/17/18 코드·공용 문서를 함께 검토할 수 있다. 개인 메모·로그·metric·source inventory는 계속 ignored local-notes에만 둔다. 커밋/배포/Oracle 설치·변경/실 API 사용은 이번에 수행하지 않았다.

## STEP 17 인수인계 (2026-11-04 KST)

시작 시 STEP 16 파일이 미커밋 상태였으며 모두 보존했다. STEP 17은 performance/scripts와 테스트/문서만 추가·수정했고 운영 Java/SQL/DDL/프레임워크/프로필/모델을 바꾸지 않았다. 실 Oracle/Gemini 호출은 0회다. 실행 로그·raw metric·개인 메모는 기존 ignored local-notes에 모았다.

STEP 17 파일 목록:

- `performance/scripts/track-a.ps1`, `track-a.mjs`: 명시적 Mock 부하 runner/실제 HTTP·배포 JS STOMP·9 burst·metric 집계.
- `performance/scripts/track-b.ps1`, `oracle-observe.sql`: 전용 Oracle 커밋 실험 gate/수동 read-only 자원 관측 SQL. 설치/중단/삭제 자동화 없음.
- `bans_chatbot/src/test/java/egovframework/example/common/web/MockLoadExperimentTest.java`: ephemeral 12계정과 random password를 가진 명시적 테스트 서버/Node 실행.
- `ai/provider/ProviderFailureExperimentTest.java`: 429/503/잘못된 JSON/timeout 3회씩·20시도 busy burst.
- `ai/provider/LoopbackProviderFactory.java`: STEP 16의 기존 helper에 test timeout overload만 추가. 운영 endpoint 변경 아님.
- `chat/service/MockConcurrencyRecoveryTest.java`: 30 worker duplicate/seq/read와 새 메모리 객체의 기록 손실을 명시하는 2개 기본 case.
- `chat/mapper/ChatConcurrencyOracleIT.java`: 20 worker 독립 transaction의 10개 요청 키 경쟁/읽음. 실제 실행 BLOCKED.
- 루트 docs/performance-and-failure.md와 decisions/api-contract/handoff/test-status/oracle-validation-backlog 갱신.

최종 트랙 A runner **PASS 18:28:25**, 10/20/30 연결×3회, 최대 고유 직원 12명/연결 30개. HTTP 2763회, 의도한 오류 응답 45회(1.629%), 예상 밖 오류 0. 상대 실제 수신 180건과 REST 재접속 복구도 확인했다. 최종 표는 performance-and-failure.md를 따른다. 첫 PS runner는 Mockito stderr를 Stop 오류로 취급해 FAIL했으며 native stderr와 종료코드를 구분하도록 수정 후 실제 재실행했다.

전체 기본 clean test package **PASS 18:30:41**, 124 main/71 test source, selected 283 = **280 PASS / 3 SKIPPED**, 실패·오류 0. skip은 Chrome opt-in 1과 별도 실행한 부하 실험 2다. 부하 2건의 PASS는 별도 실행 결과이며 정상 suite의 skip을 숨기지 않는다. Oracle IT 13개와 live smoke는 일반 suite 선택에서 제외한다.

당시 다음 STEP 18은 DB 없는 기능/관측 범위에서 진행 가능했다. 최종 DB 통합·성능 검증은 완료가 아니다. Oracle race/승인/FK/lock/계획/중단·복구/리소스 실측, 장시간 포화/진짜 브라우저 paint/운영 SLA는 남는다. Oracle 설치 후 폐기 가능한 dedicated seed schema와 두 gate를 확인하고 track-b.ps1을 사용한다. 이 테스트는 커밋하며 자동 rollback/삭제가 아니다. 최신 STEP 18 결과는 이 문서 맨 위 절을 따른다.

## STEP 16 인수인계 (2026-11-04 KST)

진입은 깨끗한 dev / 88836b4. Java/프레임워크/Oracle/모델/SDK/POM/프로필/SQL/DDL/seed 변경 없음. 질문 경로에 명시적 OutboundDisclosurePolicy를 연결해 기존 externalSource의 현재 RBAC/ACL/게시 버전/분류 검사를 재사용한다. 미검토 intent는 전송하지 않는다. STOMP 변환 예외는 고정 CHAT_REQUEST_INVALID만 내보내도록 예방적으로 보강했다. 기존 ApiException 업무 코드는 유지한다.

수정 목록(모듈의 egovframework/example 기준):

- production: 신규 `ai/question/OutboundDisclosurePolicy.java`, 변경 `ai/question/AiQuestionService.java`, `chat/websocket/ChatStompController.java`.
- test: 신규 `ai/provider/LoopbackProviderFactory.java`, `ai/question/OutboundSecurityRegressionTest.java`, `global/security/AttackRegressionMockMvcTest.java`, `document/mapper/SecurityAttackOracleIT.java`; 변경 `chat/websocket/ChatStompE2ETest.java`.
- root docs: 신규 security-test-results.md; decisions/api-contract/handoff/test-status/oracle-validation-backlog 갱신.

최종 wrapper clean test package **PASS 18:05:32**, selected 279 = **278 PASS / 1 SKIPPED**, 실패·오류 0, 124 main/67 test source와 실행 JAR. 추가 14 case가 실제 wire bytes/허용 여부/전송 0회, STOMP 불법 SEND/SUBSCRIBE/JSON 로그, MVC IDOR/revoke/부서 이동/비활성/세션 회전/입력/한도를 검사한다. 전체 51개 report로 확인. 처음 대상 테스트 setup FAIL은 test-status에 원인/수정/재실행을 보존했다. 스캔 인증이나 Oracle SQL 성공이 아니다.

신규 Oracle IT는 기존 tagged JUnit/Failsafe 격리를 재사용한다. ORACLE_SECURITY_TEST=YES와 명시적 oracleIntegrationTest.cmd에서만 선택하며 총 Oracle IT 12개다. 실제 DB 미설치로 BLOCKED: ORACLE_26AI_NOT_INSTALLED. 다중 연결 revoke race/lock/commit·감사 내구성/실성능은 별도 TODO다. Chrome 클릭 환경 BLOCKED와 선택적 SKIPPED도 유지한다. 실제 Gemini/최신 SDK 실행/외부 scanner는 NOT_RUN이다.

오프라인 관찰 범위에서 미해결 P0 0건이며 DB 없는 다음 STEP으로 진행 가능하다. Oracle 의존 P0가 미검증이므로 최종 보안 검증 완료/운영 준비 완료는 금지한다. STEP 17/18 구현은 시작하지 않았다. 실행 로그/개인 메모는 local-notes, 공용 보안 결과와 소스는 버전 관리한다. 변경은 미커밋 상태다.

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

## STEP 08 handoff (2026-11-03 KST)

Added five chat REST routes, common ChatService entry point for later STOMP, domain repository port, synchronized local-mock adapter, Oracle MyBatis adapter and XML. V003 adds a CLOB code-point guard and was added to the ordered manual migration runner, manifest, metadata check and offline source tests. Default test/local-mock remains DB-free; oracle requires configured real Oracle and never falls back to memory. The prior chat participant policy is reused. All Oracle lock, uniqueness, trigger, CLOB mapping, cross-connection concurrency and restart claims remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. REST does not provide real-time push; a later STEP must bind STOMP authentication and reuse ChatService.

The next STEP may proceed after the DB-free test gate. Preserve room ID/sequence and sender request-key contract. Oracle installation must be followed by deliberate migration application to a dedicated empty schema and explicit `oracleIntegrationTest.cmd`; `ORACLE_CHAT_TEST=YES` enables the preliminary chat IT. See test-status and Oracle backlog for precise results and missing cases.

Final STEP 08 source verification after V003 parser assertion: offline wrapper `test` **PASS at 19:04:11 KST**, 56 selected tests, zero failures/errors/skips. Earlier `clean test package` **PASS at 19:02:40 KST** created the JAR before this test-only assertion. No Oracle SQL or Gemini call occurred.

## STEP 09 handoff (2026-11-03 KST)

Added Spring WebSocket/STOMP endpoint `/ws/chat`, inbound handshake/session/account/CSRF/destination checks, outbound session/account checks, three user queues, `chat.send`/`chat.read` handlers that reuse STEP 08 ChatService, and a 3-person real embedded-server STOMP test. Existing STEP 08 uncommitted files and Oracle SQL were preserved. The local-mock/test store remains ephemeral; no real Gemini or Oracle call occurred.

The next STEP can add a simple browser UI using the documented CSRF CONNECT, sender ack and REST cursor recovery contract. It must deduplicate by room/sequence and treat the current process SimpleBroker as single-server only. Oracle persistence, commit-to-event guarantee under database faults, multi-instance user routing and durable fanout remain blocked or later work. The exact test results and status distinctions are in `test-status.md`; Oracle follow-up is in `oracle-validation-backlog.md`.

Final STEP 09 offline `clean test package` **PASS at 19:22:46 KST**: 78 main/34 test sources, 59 selected tests, 0 failures/errors/skips, Boot JAR. Embedded Tomcat/WebSocket E2E covered three accounts, CSRF/Origin, private delivery/ack, outsider rejection, duplicate request, reconnect REST recovery, logout and suspension outbound blocking. The user should still treat Oracle integration and broker fault recovery as BLOCKED/NOT_RUN per `oracle-validation-backlog.md`.

## STEP 10 handoff (2026-11-03 KST)

The document workflow now has create, new/edit draft, assigned review request, approve/reject, withdraw, grant/revoke, current published read and ACL-filtered internal search. `ManagedDocumentService` is the single authorization path for read/search and the future AI candidate lookup. It treats all user-authored content as untrusted for external AI, so `externalAiAllowed=true` is rejected and no Gemini transport was called. The `local-mock` workspace is process-local and synchronized; its assertions do not verify Oracle SQL. The Oracle adapter and V004 source are checked in but **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Earlier STEP 08–09 uncommitted changes were preserved.

Next STEP can consume the published/read/search service contract, but must not bypass fresh ACL/approval or treat a user-supplied synthetic flag as external-transfer authority. Actual Oracle V004 application, locking, review races, FK/trigger compatibility, audit atomicity, search plans and persistence need a dedicated 26ai schema and explicit Failsafe execution before any DB readiness claim. See `test-status.md` and `oracle-validation-backlog.md`.

STEP 10's full gate exposed and fixed a STEP 09 outbound revocation flaw: broker messages without a STOMP command were bypassing the old check. `ChatStompSecurity` now also checks Spring `SimpMessageType.MESSAGE`; the suspended-account real E2E test passed after the fix. This security change is part of the STEP 10 verification scope and does not alter chat REST/SQL. Final full gate status is in `test-status.md`.

Final STEP 10 offline `clean test package` **PASS at 19:54:44 KST**: 86 main / 38 test sources, 64 selected tests, zero failures/errors/skips, Boot JAR. The prior STOMP security issue and its resolved intermediate FAIL are recorded in `test-status.md`; Oracle remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

## STEP 11 handoff (2026-11-03 KST)

Prior uncommitted STEP 10 and older work was preserved. STEP 11 replaces HTTP search with bounded `q/page/size` evidence hits and exact numeric source-fragment links. `DocumentSearchService` uses a domain search port; Oracle Mapper SQL filters active viewer account/role, current approved version, `ai_searchable` fragment and live department/owner/READ grant before pagination. The service reloads the document and repeats current-version and ACL checks for each hit and source link. The former unbounded `ManagedDocumentService.search` path was removed. `external_ai_allowed` remains independent; no Gemini transport or real request was added.

V005 adds `document_tag` and B-tree indexes without changing V001–V004 bytes. The synthetic seed renderer now emits category tags; local-mock loads the canonical 20 invented documents at process start and remains ephemeral. Document creation accepts bounded tags. One fragment per version is the current editor model; draft edit changes it, new version inserts another, approval changes the pointer, and withdraw/revoke removes access without changing published rows. SQL uses bound literal `INSTR`/`DBMS_LOB.INSTR`; Oracle Text, Korean analysis, actual execution plans and SQL behavior are **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

DB-free service/MockMvc/SQL-source checks and final full wrapper `clean test package` **PASS 20:30:16 KST** with 68 tests; exact intermediate FAIL and all status categories are in `test-status.md`. Explicit `DocumentSearchOracleIT` compiles and requires `ORACLE_DOCUMENT_SEARCH_TEST=YES` on a dedicated migrated/seeded 26ai test schema; it was not executed. STEP 12 may consume evidence IDs and source text only through this service/REST path with fresh ACL, must treat returned text as data, and must still apply independent synthetic/non-sensitive/external permission checks before any Gemini call. Oracle search races, migration/seed compatibility and performance are not validated, so production readiness remains withheld.

## STEP 12 handoff (2026-11-04 KST)

Entry was clean `dev` at `1c8fd0a`. Implemented DB-independent `ai.provider.ChatProvider`, keyless `MockGeminiProvider`, explicitly activated `GeminiProvider`, settings, fixed safe failures and per-process concurrency/minute/day budgets. Java 21/eGov 5.0.0/Boot 3.5.6, Oracle 26ai final storage, domain layout and existing routes remain intact. No SQL/table/Mapper was added or changed.

Provider input is intentionally limited to two server-authored synthetic greeting enum values plus an active EMPLOYEE actor. No free-form question, history or document body can enter this live contract. Legacy AiAnswerPort/MockGeminiClient remains offline. There is no production AI HTTP/UI endpoint. The current actor passed by a future HTTP service must still come from fresh server-session account/RBAC checks. Document transfer later requires STEP 11 current source/ACL plus trusted segment external permission and independent question safety review; the old policy classes alone are not a connected production RAG service.

Default `bans.ai.provider=mock` starts without a key, including when a key exists in the environment. Live activation needs provider=gemini, additional `gemini-live` Spring profile, explicit transfer/free-tier acknowledgements, exact reviewed `gemini-2.5-flash-lite` and GOOGLE_API_KEY. The test/local-mock/oracle-it profiles reject a live provider. All Oracle seed accounts remain subject to the prior pending/bootstrap restrictions. Do not supply a key through command arguments or commit a usable local secret. See `gemini-free-tier.md` for official sources, current model access limits, acknowledgement text and all settings.

Evaluated official `com.google.genai:google-genai:1.80.0` first. Offline artifact retrieval failed for missing cache; an actual online Maven retrieval also failed with `Permission denied: getsockopt`. Latest SDK runtime/transitive compatibility with Java 21 is NOT_RUN, and SDK dependency resolution remains BLOCKED. No old cached SDK was promoted or framework version changed. The implemented/tested fallback uses Java 21 HttpClient and existing Jackson against Google's official Developer REST API. This is a REST adapter PASS, not an SDK integration PASS.

Live endpoint/request/key header are fixed. There is one HTTP attempt per call and no automatic retry, redirect, cookie/proxy forwarding, function tool, SQL or paid/model fallback. Bounded request/body completion, 32KiB response cap and sanitized parsing prevent unsafe upstream errors reaching callers. Failure attempts consume the limiter; Retry-After establishes shared cooldown. Internal text is transient, redacted in toString and excluded from JSON. Counters reset on restart, are not shared between processes and do not enforce Google project billing/TPM.

Final wrapper offline `clean test package` **PASS at 2026-11-04 15:11:01 KST**: 98 main / 47 test Java sources, **107 tests, 0 failures/errors/skips**, Boot JAR. New coverage consists of 32 loopback network cases, four configuration tests, two budget tests and one common-error MockMvc test. The suite also preserved prior auth/directory/chat STOMP/document checks. Initial URI construction failed and was corrected; exact commands and intermediate results are in `test-status.md`. `gemini-smoke` gate check with BANS_GEMINI_SMOKE=NO selected one test and intentionally SKIPPED its body; no Google call occurred. Real Gemini smoke is **NOT_RUN**. Oracle execution remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Ordinary tests made zero Oracle connections and zero real Gemini requests.

STEP 12 inventory (paths under `bans_chatbot` unless prefixed otherwise):

| Files | Purpose |
|---|---|
| `src/main/java/egovframework/example/ai/provider/{ChatProvider,MockGeminiProvider,GeminiProvider,GeminiSettings,GeminiProviderConfiguration,CallBudget,ProviderFailure}.java` | Provider contract, mock/live REST, configuration, budgets, sanitized failures. |
| `src/main/java/egovframework/example/ai/client/MockGeminiClient.java` | Reuses mock response; legacy string API stays offline. |
| `src/main/resources/application.properties`, root `.env.example` | Explicit safe defaults and environment-key instructions. |
| `pom.xml` | Ordinary Oracle/live-smoke exclusions and separately gated `gemini-smoke` task. No dependency version changed. |
| `src/test/java/egovframework/example/ai/provider/{ProviderConfigurationTest,CallBudgetTest,GeminiProviderNetworkTest,ProviderErrorContractTest,GeminiLiveSmokeIT}.java` | DB-free tests and explicit single-greeting real-smoke source. |
| root `docs/{gemini-free-tier,decisions,api-contract,architecture,handoff,test-status,oracle-validation-backlog}.md` | Official evidence, internal contract, results, limits and next work. |

Next STEP may build DB-free features after this gate. Before any arbitrary question/document transfer, add a trustworthy question review and exact current external-allowed fragment loading with refreshed RBAC+ACL at the final send boundary; do not widen SyntheticPrompt to a raw string or pass model/document instructions as policy. No paid fallback should be added. Optional real greeting requires separate user authorization, secure environment key, current Free-tier/model review and the documented task; it has not happened. After Oracle installation, follow the existing dedicated-schema migration/seed and explicit `oracleIntegrationTest.cmd` procedures with Mock AI. DB/SDK/live quota and production readiness are not certified.

## STEP 13 handoff (2026-11-04 KST)

Entry was dev at 1c8fd0a with the uncommitted STEP 12 files listed above, all preserved. STEP 13 did not change POM dependency/framework versions, profiles, Oracle V001–V005/seed/manifest or the STEP 12 key/default activation settings. Rechecked Java 21.0.3, resolved eGov 5.0.0 parent/Boot 3.5.6 and official eGov/Google compatibility/free-tier sources. No dependency download, Oracle connection or real Gemini call was made in this STEP.

Implemented the fixed five-tool registry with bounded JSON schemas, exact reviewed synthetic questions, actual-session/current-account reload per invocation, strict employee/evidence projections, final read pass and metadata-only audit. POST /api/ai/tools/preview is authenticated, CSRF-protected and Mock-only; GET /api/ai/tools returns schemas/limits/notice and GET /ai/tools provides the minimal protected demo. HTTP preview returns counts and notice only, never submitted question, AI answer, source body or outbound payload. The UI warns that actual AI can transmit question text and rejects unreviewed input rather than relying on masking. User identity/role/department claims are discarded.

Organization reads reuse STEP 07 service/ports/Mappers, filtering public values against canonical invented fixtures before export. Document reads reuse STEP 11 source/search/ACL, with classification added to SELECT/current-version carriers. externalSource requires current published/approved access and external/synthetic/non-sensitive flags in both the source row and refreshed current version. Internal search is not restricted by external eligibility. User-authored drafts keep classification false. Read/search pages may remain internally available when export is denied. The current editor still creates one fragment per version.

The Mock payload uses immutable copies and bounded question/evidence/results. A final re-read narrows revoke/status/department/version races, but a concurrent change after a final check remains possible; no Oracle snapshot or locking guarantee is claimed. Live Gemini refuses the new tool contract even if STEP 12 greetings are explicitly enabled. No free-form conversational generation, model function-call loop, write/SQL/salary/credential tool or paid fallback was added.

Audit stores fixed actor/outcome/count metadata only. Its mock store is bounded and ephemeral. Oracle uses the existing audit_event table with an independent REQUIRES_NEW adapter, nullable VARCHAR actor binding and no question/answer/body columns. It is server audit, not AI write authority. Actual SQL/JDBC constructor mapping, transaction durability, audit failure behavior and cross-connection ACL races remain BLOCKED: ORACLE_26AI_NOT_INSTALLED. AiToolOracleIT is compiled/tagged and requires explicit ORACLE_AI_TOOL_TEST=YES plus the existing Oracle suite on a dedicated installed/migrated/seeded schema; it was not run.

Final offline wrapper `clean test package` **PASS at 2026-11-04 15:44:44 KST**: 110 main / 52 test sources, **152 tests, 0 failures/errors/skips**, repackaged Boot JAR. This is 45 additional selected cases over STEP 12, including actual-security MockMvc, mocked revocations during runs, prompt/identity/schema/projection/budget checks, Oracle XML parsing and a live-provider no-tool-transport guard. Nine Oracle IT classes and the real Gemini smoke remain excluded from ordinary tests. Real Gemini is NOT_RUN; Oracle execution is BLOCKED. Intermediate test syntax/assertion FAILs and commands are preserved in test-status.md.

STEP 13 file inventory (relative to bans_chatbot unless stated root):

| Added / changed | Files / role |
|---|---|
| New AI runtime | `ai/controller/AiToolController.java`; `ai/service/ReviewedSyntheticQuestions.java`; `ai/tool/{ToolRegistry,ToolPayload,ToolRunBudget,AiToolService}.java` under src/main/java/egovframework/example. |
| Existing AI increment | `ai/provider/{ChatProvider,MockGeminiProvider}.java` add Mock-only tool contract; `ai/service/QuestionSafetyReview.java` updates review scope. Existing STEP 12 files remain uncommitted. |
| New session reader | `auth/service/CurrentSessionService.java`. |
| New audit runtime | `audit/domain/AiToolEvent.java`, `audit/repository/{AiToolAudit,InMemoryAiToolAudit,OracleAiToolAudit}.java`, `audit/mapper/AiToolAuditMapper.java` and matching resource XML. |
| Existing organization | `organization/service/OrganizationService.java` adds canonical synthetic-public projections through existing read port. |
| Existing document runtime | `document/domain/ManagedDocument.java`; `mapper/ManagedDocumentMapper.java`; `repository/{DocumentSearchRepository,InMemoryDocumentSearchRepository,InMemoryDocumentWorkspace,OracleDocumentWorkspace}.java`; `service/{DocumentSearchService,ManagedDocumentService}.java`. Current classification and independent externalSource checks. |
| Existing Oracle XML | Resource document `DocumentSearchMapper.xml` and `ManagedDocumentMapper.xml` select existing flags; internal ACL predicates and dialect retained. No DDL/seed migration. |
| New tests | `ai/controller/AiToolMockMvcTest.java`, `ai/tool/{AiToolSecurityTest,ToolRegistryTest,AiToolOracleIT}.java`, `audit/mapper/AiToolAuditMapperSourceTest.java` under src/test/java/egovframework/example. |
| Existing tests | `document/mapper/DocumentSearchMapperSourceTest.java` distinguishes projection from internal predicates; `ai/provider/GeminiProviderNetworkTest.java` proves live tool contract makes no HTTP call. |
| Root docs | New `docs/ai-tool-security.md`; updates to decisions/api-contract/handoff/test-status/oracle-validation-backlog/architecture/security-policy/authorization-matrix/gemini-free-tier. |

Next STEP may continue DB-free development after this PASS gate. Keep actual identity/current permission checks in the server; do not pass client or model authority into services. The only question review currently implemented is exact server-approved examples. A real model tool loop or arbitrary question support needs a separate explicit implementation/authorization decision, trustworthy safety review and fresh source/ACL/classification checks at the real network boundary. Recheck Google project Free-tier/model access before any separately authorized greeting smoke. After Oracle installation run the dedicated migration/seed and explicit oracleIntegrationTest.cmd with ORACLE_AI_TOOL_TEST=YES and Mock AI; add multi-connection revoke/departure/publish/audit fault tests. No final DB/production readiness is asserted.

## STEP 14 handoff (2026-11-04 KST)

STEP 14 is implemented as bounded single-turn questions with current facts/evidence and explicit unanswerable outcomes. Entry dev at 1c8fd0a already contained all uncommitted STEP 12/13 files; preserved them. Framework/build/profile/model/SDK/Oracle choices and migrations/seed remain unchanged. Oracle is still the final persistent database. Official eGov/Google sources were rechecked; no Oracle connection or real Gemini invocation occurred.

`POST /api/ai/questions {question}` returns answer, answerType, structuredFacts, sources, requestId under the established data envelope. Authenticated examples and `/ai/questions` provide the transfer warning and fixed-choice UI. Exact reviewed synthetic intents only; unknown text is UNANSWERABLE without sending it to a classifier. Structured contact/expense values are current canonical public service DTOs, not model strings. Documents require exactly one current internally readable candidate plus independent external eligibility. Only one <=200-code-point synthetic external-allowed excerpt and reviewed question reach Provider. Strict exact-quote/S1 JSON validation and repeated identity/search/source/version/grant checks precede the server-created answer/source URL. Source API reauthorizes on click. The new live grounded contract requires all STEP 12 gates; STEP 13 previewTools remains Mock-only.

Execution limits: eight stages, four reads, one provider call, 10-second stage checks/remaining HTTP timeout and min(configured,256) output tokens. Questions and preview share session five-per-minute/one-active limits. No arbitrary NLU, model-selected tool loop, streaming/history/conversation storage, paid fallback or writes. Current synthetic fixture answers are demonstrations, not Oracle-queried facts. Audit is fixed metadata only, with ephemeral and Oracle REQUIRES_NEW adapters; no new table or transcript column. AI errors retain sanitized status/codes and chat REST/STOMP configuration remains independent.

Actual final wrapper `clean test package` **PASS 2026-11-04 16:22:38 KST**, **121 main / 58 test sources, 196 tests, 0 failures/errors/skips**, executable repackaged JAR. Forty-five fresh Surefire XML files confirm totals. Targeted 77 tests passed at 16:19:41. Initial compile FAIL from a new timeout/catch variable collision was corrected and recorded in test-status.md. Ten Oracle IT classes and real Gemini smoke are excluded from ordinary tests; Oracle execution BLOCKED: ORACLE_26AI_NOT_INSTALLED; live Gemini and Oracle+Gemini E2E NOT_RUN. UI is MockMvc-verified, not visually/browser tested. Existing STOMP regression passed; simultaneous AI-failure/STOMP load is not a new tested guarantee.

STEP 14 file inventory (relative to bans_chatbot/src unless root docs):

| Added / changed | Files / role |
|---|---|
| New question runtime | `main/java/egovframework/example/ai/question/{QuestionPolicy,QuestionModels,QuestionExecution,GroundedPrompt,AiQuestionService}.java`; strict request/review, output models, bounds, server-minted context, orchestration. |
| New controller | `main/java/egovframework/example/ai/controller/AiQuestionController.java`; question POST, examples and protected HTML. |
| Existing AI increment | `main/java/egovframework/example/ai/provider/{ChatProvider,MockGeminiProvider,GeminiProvider}.java`; separate grounded contract and bounded JSON transport. `ai/tool/ToolRunBudget.java` exposes same existing session budget for sharing. Existing STEP 12/13 code retained. |
| New metadata audit | `main/java/egovframework/example/audit/domain/AiQuestionEvent.java`; `repository/{AiQuestionAudit,InMemoryAiQuestionAudit,OracleAiQuestionAudit}.java`; `mapper/AiQuestionAuditMapper.java`; matching `main/resources/egovframework/example/audit/mapper/AiQuestionAuditMapper.xml`. |
| New tests/support | `test/java/egovframework/example/ai/question/{QuestionTestSupport,AiQuestionServiceTest,QuestionPolicyTest,AiQuestionOracleIT}.java`; `ai/controller/AiQuestionMockMvcTest.java`; `audit/mapper/AiQuestionAuditMapperSourceTest.java`. |
| Existing network tests | `test/java/egovframework/example/ai/provider/GeminiProviderNetworkTest.java`; four grounded wire/failure/timeout cases on loopback, no Google traffic. |
| Root docs | New `docs/ai-question-orchestration.md`; updates decisions/api-contract/handoff/test-status/oracle-validation-backlog/architecture/security-policy/authorization-matrix/ai-tool-security/gemini-free-tier. |

Next STEP may proceed with DB-free development after this PASS. Do not widen reviewed intents into arbitrary raw text or accept model facts/URLs/permissions. A full procedure summarizer/general conversation or new trusted synthetic ingestion path requires separately designed review, output evidence and disclosure controls. Multiple candidates intentionally yield no answer. Counters are session/process-local; blocking repository/audit calls are not forcibly interrupted. The final Java check leaves a concurrent-change window and actual Oracle ACL/mapping/transaction/latency remains unverified.

After Oracle installation and dedicated-schema setup, run the existing `oracleIntegrationTest.cmd` with ORACLE_AI_QUESTION_TEST=YES and Mock only; see oracle-validation-backlog.md for the compiled baseline and missing negative/multi-connection/audit durability tests. Real model access/free-tier/quota/latest SDK compatibility remain separate pending items. Actual live greeting or document E2E requires separate explicit authorization and current Google account/project review. DB/real API/operational readiness is not complete. Changes are left uncommitted for review.

## Full regression and Git hygiene handoff (2026-11-04 KST)

At the user's request, reran the entire offline feature suite and added `src/test/java/egovframework/example/global/security/RouteAccessMockMvcTest.java`. It enumerates all 37 production MVC method/path mappings, requires an explicit authentication probe for each, checks all 20 mutations without CSRF, covers authenticated GETs/404/405 and adds missing HTTP-level draft/reject/version/withdraw workflow coverage. Existing actual localhost STOMP and provider loopback tests also ran. Final wrapper clean test package **PASS 16:48:29 KST**, 256/0/0/0 tests, executable JAR. Actual packaged local-mock localhost smoke also passed all 37 anonymous route status checks; process stopped afterward. Details/commands and the corrected disposable-script FAIL are in test-status.md.

Updated root .gitignore for local-only notes, troubleshooting output, editor backups and JVM dumps. Shared handoff/contracts/decisions/test status remain tracked. Put personal next-step notes in notes/local or docs/local, or use *.local.md/*.local.txt. Raw diagnostics remain ignored logs/target/.scratch. No existing documents were deleted or untracked. Do not blanket-ignore *.md or *.txt.

Important commit preparation: the old broad credential ignore had hidden the existing required `bans_chatbot/src/main/java/egovframework/example/auth/domain/CredentialStatus.java`. Narrowed the rule so it now appears as untracked. Include it alongside all existing feature changes when preparing a future commit; this task did not stage or commit anything. No production Java/API/SQL/dependency changes were needed for the passing route checks. Git hygiene tests confirm required source/doc/example paths are not excluded and local secret artifacts remain excluded.

Oracle remains BLOCKED: ORACLE_26AI_NOT_INSTALLED; real Gemini/Oracle E2E and SDK validation retain NOT_RUN/pending status. No STEP 15 feature or paid/live API activation was performed. DB-free work can continue, but final DB/operational readiness is still pending.

## 로컬 메모·진단 자료 정리 (2026-11-04)

사용자 요청에 따라 모듈 및 루트에 흩어져 있던 로그 55개, javac 진단 파일 1개, 로컬 확인 스크립트 1개를 `bans_chatbot/local-notes/`로 이동했다. 앞으로 개인용 다음 단계 메모와 문제 해결 자료도 이 한 폴더에서 관리한다. 폴더 전체를 Git에서 제외하고 .gitignore 주석을 한글로 바꿨다. 공용 docs는 기존 위치와 버전 관리를 유지한다.

과거 문서에 파일명만 기록된 step/regression 로그의 현재 위치는 `bans_chatbot/local-notes/<파일명>`이다. 과거 `.scratch/route-smoke.ps1`은 `bans_chatbot/local-notes/route-smoke.ps1`로 이동했으며 모듈·출력 경로를 수정했다. 이동 목록과 당시 해시는 무시되는 폴더의 moved-files.json에 남겼다. 기능/SQL 변경이나 테스트 재실행은 없으며 이전 256건 PASS를 이번 정리 작업의 신규 실행 결과로 간주하지 않는다.

## STEP 15 인수인계 (2026-11-04)

진입 상태는 깨끗한 dev / 9f1cfad였다. 프레임워크·SDK·모델·Oracle·프로필·DDL/Mapper/seed 버전을 바꾸지 않았다. 공용 GET /에 로그인/활성화, 직원/담당업무 검색, 1:1 채팅, 문서 검색/작성/작업/승인, AI/출처, 관리자 직원 관리 화면을 구현했다. 실행/역할별 시연 절차는 web-ui.md, 실제 명령/결과는 test-status.md를 따른다. 개인 메모/진단은 계속 bans_chatbot/local-notes에 둔다.

변경 파일 목록(모듈 기준):

| 파일 | 역할 |
|---|---|
| `src/main/java/egovframework/example/common/web/WorkbenchController.java` | 공개 shell, profile 안내, CSP/no-store. |
| `src/main/java/egovframework/example/auth/controller/ActivationAdminController.java` | 현재 관리자 세션으로 기존 일회용 코드 발급 서비스 연결. |
| `src/main/java/egovframework/example/auth/service/PublicEndpointPolicy.java`, `global/security/SecurityConfiguration.java` | shell과 정확한 정적 파일 GET만 공개. |
| `src/main/resources/templates/workbench.html` | 여섯 화면, 역할별 표시, 안내·접근성·POST forms. |
| `src/main/resources/static/ui/{app.css,api.js,stomp.js,app.js}` | 가벼운 같은-origin UI/API/오류/안전 출력/세션/채팅 복구. |
| `src/test/java/egovframework/example/common/web/{WorkbenchMockMvcTest,WorkbenchClientIntegrationTest,WorkbenchBrowserTest,WorkbenchOracleIT}.java` | 6개 화면 대응 유스케이스, 실제 JS+HTTP/STOMP, 선택적 Chrome, Oracle shell 예비 검증. |
| `src/test/ui/{client-integration,workbench-browser}.mjs` | 추가 npm 의존성 없이 Node 통합 및 opt-in CDP 시나리오. |
| `src/test/java/egovframework/example/global/security/RouteAccessMockMvcTest.java` | MVC 등록 목록 39개, 신규 발급 API/CSRF와 공개 shell 검사. |
| 루트 `docs/{web-ui,decisions,api-contract,handoff,test-status,oracle-validation-backlog,gemini-free-tier}.md` | 설계·시연·현재 검증 범위와 다음 작업. |

Spring SimpleBroker에는 STOMP receipt 지원이 없으므로 기다리지 않는다. 단일 서버/비내구성 broker, heartbeat 미설정, 세션/프로세스 내 한도라는 기존 제약이 남는다. 메시지 화면은 500건, 재접속 복구는 최대 10×50건, 코드로 처리하는 문서 workflow이며 승인 대기함 목록은 없다. 관리자 활성화 코드는 현재 화면에서 일시 전달하고 지우며 기본 계정 비밀번호를 체크인하지 않는다. local-mock은 12개 대기 합성 계정과 20명/5부서 fixture를 사용하고 재시작 시 모두 사라진다.

Chrome 실제 클릭 테스트는 GPU/CDP 환경 문제로 BLOCKED다. 정상 기본 suite에서는 선택적 브라우저 테스트 1건을 SKIPPED하며, 실제 JS와 HTTP/WebSocket 검사·MockMvc를 브라우저 PASS로 혼동하지 않는다. Node 미설치 환경은 JS 통합 테스트가 명시적으로 SKIPPED된다. 브라우저가 동작하는 환경에서 BANS_UI_BROWSER_TEST=YES로 시각/키보드/모바일·클릭 전체 흐름을 검증해야 한다. Oracle shell 예비 IT는 ORACLE_UI_TEST=YES로 기존 명시적 Oracle suite만 사용한다. DB 부재는 BLOCKED: ORACLE_26AI_NOT_INSTALLED이며 실제 Gemini/Oracle E2E도 미검증이다.

다음 STEP은 이 오프라인 검증 범위에서 진행할 수 있다. 실제 클릭 UX·반개방 소켓·장시간/500건 초과 이력·다중 사용자 실성능·Oracle 권한/트랜잭션·실 Gemini E2E는 완료 조건으로 남긴다. 최종 완성/운영 준비 완료를 선언하지 않는다. 새 서버 API와 UI 모두 existing RBAC/ACL/current-source 경계를 유지해야 한다.

최종 STEP 15 gate: **2026-11-04 17:38:32 KST clean test package PASS**, 123 main/63 test source, selected 265 = **264 PASS + 1 SKIPPED**, 실패·오류 0. fresh Surefire 49개 report와 일치한다. 최종 JAR localhost 39개 경로 smoke **PASS 17:40:51**, 프로세스 종료 확인. 브라우저의 앞선 4회 FAIL과 현재 환경 BLOCKED를 정상 suite의 SKIPPED와 별도로 보존했다. 로그는 모두 local-notes, 수정 파일은 미커밋이다.

# 2026-11-09 Oracle local properties handoff

The Oracle profile now imports optional `bans_chatbot/oracle-local.properties` from the module working directory; `.gitignore` excludes the actual file. A credential-free example is present, and `oracleIntegrationTest.cmd` now accepts the file with an environment-variable fallback. The local file was created with blank account/password fields. The operator must enter their own DB values. No real Oracle connection or seed execution was attempted for this change.

## 2026-11-09 Oracle demo seed handoff

After the operator configured the dedicated `FREEPDB1` schema, `OracleConnectionOracleIT` passed. The canonical 216-statement synthetic seed was then applied once through a local ignored JDBC runner with preflight, a single transaction, pre-commit validation, and post-commit verification. SQL*Plus did not successfully run the wrapper due local client connection errors; no rows changed in those attempts. The committed seed has 20 employees, 12 PENDING accounts and 20 documents with the expected related rows. The first-administrator activation gap remains, and no live Gemini call was made.

## 2026-11-09 Oracle UI demo handoff

The first-administrator gap is addressed in code by an opt-in, interactive `oracle-bootstrap` profile with strict schema/PDB/seed checks and a one-transaction activation/audit operation. A local PowerShell runbook builds the Oracle JAR, generates a Git-ignored localhost TLS certificate and settings, and starts the Oracle UI with Mock AI. The HTTPS login page and CSRF endpoint were checked against the live configured Oracle profile, but the operator has not entered an administrator password, so EMP-001 is still PENDING and Oracle login/full UI are **NOT_RUN**. Follow [oracle-ui-demo.md](oracle-ui-demo.md); never copy the local-mock password into the Oracle seed.

The public Oracle landing page now detects the pending first administrator and shows the one-time setup instruction while hiding the login form. The change was verified by live HTTPS `GET /`; full `-Poracle test package` passed 290 tests with 3 skipped and no failures/errors. No administrator password was set. The demo server was stopped after verification, following the operator's earlier request.

The first-admin setup instruction now points to Git-ignored `bans_chatbot/oracle-local.properties` rather than a PowerShell password prompt. An ordinary Oracle startup activates the fresh seeded EMP-001 only when `BANS_ORACLE_FIRST_ADMIN_PASSWORD` is configured; a later startup leaves the active account alone. The property key is present but empty in the local file, awaiting an operator-selected value. The 2026-11-09 full ordinary suite passed 293 tests with 3 skipped and no failures/errors; the live pending-account landing page returned HTTP 200 with property guidance and a hidden login form. No activation/login was performed, and port 8443 was closed after the check.

## 2026-11-10 first-admin login repair

The operator entered a four-character value in the Git-ignored local property. Oracle startup initially failed the 12-character check; the UI mislabeled a login 401 as session expiration. A local-demo-only short-password opt-in now requires loopback HTTPS, Mock AI, and the `BANS_CHAT` schema. General account activation still requires at least 12 characters. The first configured Oracle startup activated EMP-001, and HTTPS login plus `/api/auth/me` returned the admin role and Secure cookie; login passed again after restart. The UI client now treats login 401 as credential failure and versioned asset URLs prevent stale script use. Full ordinary tests passed 295 with 3 skipped; focused repackage passed after stopping a running JAR lock. No second-user or complete browser UI flow was run.

## 2026-11-10 demo account handoff

The 12 existing Oracle accounts now use the three-digit suffix of their employee codes as local demo passwords (for example, `EMP-014` uses `014`). The ignored `bans_chatbot/demo-mock-data/README.txt` contains the exact login mapping, `activate_demo_accounts.sql` contains the guarded BCrypt updates, and `verify_demo_accounts.sql` contains read-only checks. A local JDBC helper applied the final update to `BANS_CHAT` in `FREEPDB1`; all 12 accounts are ACTIVE and all 12 final password checks passed. The earlier ordinal and prefixed mappings have been superseded. No web server was started. The prior `BANS_ORACLE_FIRST_ADMIN_PASSWORD` setting is no longer needed for an already ACTIVE EMP-001; its one-time bootstrap skips the account. For SQLcl/SQL*Plus use `@demo-mock-data/activate_demo_accounts.sql` from the module directory after connecting to the dedicated schema. In DBeaver execute the entire file as a script on that connection. The canonical seed verification that expects PENDING accounts is a pre-activation check; use the local demo verification file after provisioning.

The reported `emp001`/`001` sign-in failure was caused by the login form and account lookup requiring canonical `EMP-001`. The login service and form now accept `emp001`, `EMP001`, or `EMP-001`; the service looks up `EMP-001`. A focused MockMvc suite passed and the executable Oracle-profile JAR was rebuilt without starting it. The operator must start that new JAR under the Oracle profile on their chosen port; port 8080 under `local-mock` uses separate ephemeral accounts and does not use these Oracle passwords. Live HTTP sign-in with the rebuilt JAR remains untested because the operator requested that the assistant never start the server.

## 2026-11-10 Oracle-only demo entry point
Default application profile is now `oracle`; `scripts/local.ps1 -Action build` includes `-Poracle`, and `-Action run` delegates to the Oracle HTTP demo. The previous 8080 local-mock process was stopped. Test fixture adapters remain available only for the offline test harness and explicit `local-mock` test profile; normal tests do not connect to Oracle. The current demo URL is http://127.0.0.1:8080/.


## Oracle-only 8080 demo (2026-11-10)

2026-11-10 current runtime: http://127.0.0.1:8080/ serves the Oracle-backed application. The previous 8443 server is stopped. The local-mock profile was removed from application code and tests; test and fixture-test remain for ordinary offline tests. The pre-existing oracle-demo-ssl.properties file is ignored by the application.

## 저장형 Gemini 설정 (2026-11-10)

실제 키는 Git 제외 파일 `bans_chatbot/.env`의 `GOOGLE_API_KEY=` 뒤에 저장한다. 이 파일은 Oracle 프로필이 모듈 작업 디렉터리에서 자동 읽는다. `.env.example`에는 실제 키를 쓰지 않는다. 기본 `bans.ai.provider=mock`에서는 키가 있어도 외부 호출이 없다.

실제 Gemini를 켜려면 본인의 프로젝트 무료 티어, 모델 접근과 외부 전송 조건을 확인한 뒤 같은 `.env`에서 `bans.ai.provider=gemini`, `bans.ai.model=gemini-2.5-flash-lite`, `bans.ai.external-transfer-acknowledged=true`, `bans.ai.free-tier-confirmed=true`로 지정한다. 기존 서버를 종료하고 `./scripts/local.ps1 -Action run`으로 다시 시작한다. 스크립트는 `gemini` 설정일 때 `oracle,gemini-live` 프로필을 선택한다. 키와 조건이 없거나 잘못되면 시작에 실패한다. 자유 입력 AI 채팅은 구현되어 있지 않고 검토된 가상 질문만 처리한다.

## Consolidated local settings (2026-11-10)

2026-11-10: `bans_chatbot/.env` now contains the migrated Oracle connection settings and the AI settings. It is Git ignored. The legacy `oracle-local.properties` was removed after live HTTP 8080 login and employee query passed from the consolidated file. The Gemini key remains blank and real Gemini is NOT_RUN.


## Chat UI redesign (2026-11-10)

2026-11-10: Chat screen redesign is deployed in the Oracle JAR on loopback port 8080. IDs used by existing API/STOMP handlers are preserved. Desktop and narrow-screen CSS are included. Browser screenshot inspection was blocked by an unavailable browser access host; live HTTP and focused tests passed.
