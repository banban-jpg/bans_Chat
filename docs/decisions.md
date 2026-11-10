# Decisions

## STEP 19 — 새 기능 없이 현재 계약·검증 상태 확정

API source 추출을 Spring runtime inventory와 대조하고 실제 모든39 MVC매핑 성공 HTTP 요청을 확인한다. REST36/HTML3/static4/STOMP6을 별도 집계해 UI화면과 API 수를 섞지 않는다. 일반288 testcase(285PASS/3SKIP)와 별도 opt-in2PASS, Oracle13class BLOCKED의 분모도 구분한다. 자동 경로/결과 생성에 표준 Python만 사용하며 SDK/POM/DDL/인증정책 변경은 없다.

최초 관리자 bootstrap, Oracle 실동작, REST fallback 발송 한계, 도메인 양방향 의존·legacy 후보는 [최종 보고서](FINAL_VERIFICATION_REPORT.md)에 근거/우선순위/제안을 남기고 임의 정책변경/삭제하지 않았다. 검증 도구만 txt/py검사 누락 및 smoke evidence 파일명 고정을 수정했다. 로그/캐시/개인메모는 기존 ignored 폴더에 두고 요청한5개 명세·결과·실행 문서는 Git으로 공유한다.

## STEP 18 인수인계 판단 (2026-11-04)

STEP 16/17의 미커밋 변경을 보존한 상태로 README와 현재/설치 후 경로를 분리한다. 기본 Mock을 명령에서도 명시하고 설치/삭제/SQL 적용을 자동 실행하지 않는다. .env.example은 값 없는 변수 이름만 제공한다. 재현 가능한 build/test/run/smoke/source 검사를 scripts에 두고 원시 결과는 local-notes로 제한한다.

Oracle 제품명26ai와 내부23.26 릴리스는 같지 않으므로 예비 IT의 JDBC major=26 단정을 제품 banner/Free edition 증거로 교체했다. 공식 Oracle Upgrade Guide를 근거로 하며 실제 DB 실행 성공은 주장하지 않는다. Oracle 최초 관리자 보안 bootstrap이 없음을 최종 수락의 차단 항목으로 공개한다. dummy hash/일괄 ACTIVE seed/Mock provisioning으로 우회하지 않는다. 정상 source/DDL/버전/모델/의존성은 바꾸지 않았다.

현재 구조/ERD/보안 문서 앞에 최신 내용을 정리하고 과거 STEP 설명은 이력으로 분리한다. Oracle 설치 후 13개 IT를 빈 스키마 migration/rollback seed/committed seed/domain별 gate/커밋 부하 단계로 나누며 모드별 서로 다른 전제를 일괄 활성화하지 않는다. DB 검증 완료는 실제 migration·제약/락·영속 API/ACL·commit 후 STOMP·AI 검색·부하·백업 복구가 모두 수락된 뒤에만 가능하다.

## STEP 17 Mock 관측과 Oracle 실측 분리 (2026-11-04)

dev / 88836b4의 미커밋 STEP 16 변경을 보존했다. Java 21/eGovFrame 5.0.0/Boot 3.5.6, Oracle 26ai Free, 모델/SDK/프로필/운영 SQL은 유지한다. 공식 eGov Boot 예제와 Oracle 26ai Free 자원 제한을 확인했다. 신규 부하 도구/의존성은 도입하지 않고 기존 Maven/JUnit/Node/실제 localhost HTTP·STOMP 및 배포용 JS 클라이언트를 재사용한다.

12개 합성 계정으로 10/20/30개 세션 연결을 각 3회 실행한다. 실제 고유 직원 수와 연결 수를 분리하며 cold-login·동일 PC·고정 순서·작은 표본을 명시한다. 의도한 409/404/401/429와 예상 밖 오류를 분리하고, 복합 operation 표본을 HTTP 처리량/SLA로 바꾸지 않는다. 로그·JSON은 local-notes에만 두고 재현 script/집계 문서는 버전 관리한다.

Oracle의 2 cores/2GB RAM/12GB 사용자 데이터 제한을 바탕으로 트랙 B를 계획하지만 Mock 숫자로 capacity를 추정하지 않는다. 전용 스키마 커밋 쓰기에는 ORACLE_LOAD_TEST와 ORACLE_LOAD_WRITES_ACK 두 gate가 필요하다. DB 설치/중단/복구/삭제는 자동 수행하지 않는다. 전체 Oracle 실험은 BLOCKED다. 이번 병목 후보는 가설로만 기록하고 main 성능 최적화나 보안 비용 완화를 하지 않는다.

## STEP 16 보안 검증 경계 (2026-11-04)

기존 버전/DB/모델을 유지하고 OWASP WebSocket/Session/Logging/Prompt Injection 공식 지침을 기준으로 기존 회귀와 신규 공격 case를 묶는다. 스캐너를 무분별하게 추가하지 않는다. 질문 outbound 정책을 명시적 서버 객체로 분리하고 실제 Gemini 어댑터의 loopback 요청 바이트를 비교한다. 허용되지 않은 fragment/미검토 질문의 전송 시도는 0회여야 한다. 승인된 합성 문서의 공격 지시는 데이터일 뿐 도구/권한 정책이 아니다.

STOMP 변환 예외가 원문을 보유할 수 있으므로 예외 자체를 로그/응답에 넘기지 않는 catch-all을 추가한다. 수정 전 실제 누출 재현은 하지 않았으며 예방적 P1 보강으로 기록한다. 실제 JSON 공격과 로그 검사로 수정 후 경계를 검증한다. Oracle 테스트는 기존 Maven Failsafe/tag/전용 runner를 재사용해 불필요한 별도 소스 플러그인을 추가하지 않는다. 실제 DB 격리는 이름/tag와 환경 opt-in으로 유지한다.

심각도는 공격 실패 시 영향과 발견 여부를 구분한다. offline PASS/미해결 P0 0은 전체 보안 인증이 아니며, Oracle 의존 P0가 BLOCKED인 상태의 최종 완료 판정은 보류한다. 자세한 실행 근거는 security-test-results.md/test-status.md를 따른다.

## STEP 01 — 2026-11-02

- Retain the repository's genuine eGov 5.0.0 parent and runtime modules. Its resolved POM selects Spring Boot 3.5.6. Set Java release to 21; do not choose an unrelated Boot version.
- Retain Maven rather than changing build systems. Pin Wrapper to Maven 3.9.6, the version already installed locally. Pin clean plugin 3.2.0 and Surefire 3.5.2 because parent-selected plugin artifacts were incomplete in the offline cache; revisit when dependency retrieval is authorized.
- Remove the HSQLDB runtime dependency so the default server cannot silently use an embedded database. Preserve the legacy sample SQL file untouched as historical starter material. Oracle 26ai remains the final database.
- Default to `local-mock`; exclude DataSource auto configuration in `local-mock` and `test`. `oracle` requires explicit connection variables. There is no Oracle driver or persistence code in STEP 01.
- The `oracle-it` Maven profile reserves a Failsafe suite; actual `*OracleIT` cases follow when Oracle SQL and schema exist.

## Pre-STEP 02 package decision (2026-11-02)

- Adopt package by domain in root `AGENTS.md`. Keep the Boot application in `egovframework.example` for default descendant component scanning. Put the existing infrastructure-only `/health` endpoint and matching smoke test in `global.health`. No empty domain package or placeholder abstraction is created. There is no Mapper, Mapper XML, or domain Controller/Service to move yet.
- Align checked-in Eclipse JRE/compiler/facet settings to Java 21, already required by the Maven build. No dependency, API contract, database configuration, or SQL change is part of this cleanup.

## STEP 02 — 2026-11-02

- Keep the pre-STEP 02 package-by-domain rules. Implement a meaningful `organization` mock fixture/service and local-mock-only demo Controller plus an `ai` mock adapter; leave `auth`, `chat`, `document`, and `audit` without empty classes. Keep `/health` compatible and add `/api/health` as the response-envelope example.
- Use a server-generated UUID in `X-Request-Id` and error bodies. Central advice maps validation and framework errors to fixed public messages and hides arbitrary exception text. API success is an explicit `ApiResponse<T>` record.
- Select adapters by Spring profile. `test` and `local-mock` exclude JDBC auto configuration and use a nonpersistent fixture/mock AI. `oracle` creates an explicit DataSource and checks connection at startup; no fallback. Prevent mixed DB/mock profiles. Remove optional DevTools because its restart launcher caused a failed Oracle startup to return exit code 0 during local verification; Java 21 and framework versions are unchanged.
- Use a Maven `oracle` profile for the Oracle JDBC runtime artifact so default offline tests need no driver. Oracle's [26ai JDBC guide](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/JDBC-getting-started.html) lists `ojdbc17` for JDK 21; [Maven Central](https://central.sonatype.com/artifact/com.oracle.database.jdbc/ojdbc17/versions) lists `23.26.4.0.0` (published 2026-10-29). The artifact is not in this PC's cache and was not downloaded. Driver selection is not SQL validation.
- Isolate `@Tag("oracle-integration")` and `*OracleIT` with Surefire exclusion and explicit Failsafe inclusion via `oracleIntegrationTest.cmd`. A separate source set would require an additional build plugin not present in the offline cache. Pin the parent-selected Failsafe 3.5.4 in the explicit profile; it is absent locally, so `oracle-it` remains unexecuted.
- The [Google Gemini pricing](https://ai.google.dev/gemini-api/docs/pricing) page lists the two 2.5 Flash candidates in the free tier and says free-tier content may improve Google products. [South Korea is available](https://ai.google.dev/gemini-api/docs/available-regions); [rate limits are account/model dependent](https://ai.google.dev/gemini-api/docs/rate-limits). No fixed quota, live model, billing, or real network client is configured in STEP 02.

## STEP 03 (2026-11-02)

- Preserve the resolved eGov 5.0.0 / Boot 3.5.6 / Java 21 combination. Official [eGov releases](https://github.com/eGovFramework/egovframe-runtime/releases) and [Spring Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html) were rechecked; no version upgrade was made.
- Define security roles separately from department and title. Deny inactive accounts, self-role grants, nonparticipant chat reads, and document access lacking scope/employee grants. `DOCUMENT_MANAGER` needs a resource `APPROVE` grant and read access; administrator roles have no private-document read bypass.
- Treat external AI disclosure as a second decision on a matching, approved synthetic/non-sensitive document segment with `external_ai_allowed=true` and a trusted question review. The question-review port has no production adapter, so real transmission remains impossible.
- An interim MVC interceptor enforces the exact anonymous HTTP allowlist and active session actor for current routes. The local mock employee route now requires a session. It is not the final Spring Security chain; no uncached Spring Security artifact was automatically downloaded. Authentication, current-status reload, CSRF, WebSocket/STOMP, and non-MVC protection are pending.
- The existing Maven cache is readable but javac cannot close JARs there in this sandbox. An ignored, workspace-local copy of the cache permits an offline Java 21 `clean test` without changing dependencies or making network calls. No Oracle or Gemini call was made.

## STEP 04 (2026-11-02)

- Keep all 17 proposed tables, with no migration-history table. Store versioned Oracle-only sources under `bans_chatbot/db/oracle`, plus a SHA-256 manifest and an explicit SQLcl/SQL*Plus `apply.sql` guarded for an empty schema. Never run migrations at Boot startup or during default tests. Oracle DDL auto-commit makes a partial failure a manual inspection task; no DROP/recreate automation was added.
- Use Oracle `NUMBER(19) GENERATED ALWAYS AS IDENTITY`, `VARCHAR2`, `CLOB`, `DATE`, and `TIMESTAMP(6) WITH TIME ZONE` based on the [Oracle 26ai CREATE TABLE reference](https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/CREATE-TABLE.html). Add PK/UK/FK/CHECK constraints, sorted 1:1 chat pair, message idempotency keys, composite read state, current-version composite FK, active-review unique index, and published-version/fragment guards. None has been executed on Oracle yet.
- [Redgate's Flyway Oracle page](https://documentation.red-gate.com/flyway/reference/database-driver-reference/oracle-database) says 26ai is verified and lists an Open Source Oracle module, while the [support matrix](https://documentation.red-gate.com/fd/supported-database-versions-143754067.html?force_isolation=true) describes foundational migration capabilities. The exact artifact/version, license path, and behavior in this repository cannot be validated without a resolved dependency and Oracle server, so STEP 04 does not add Flyway. A later explicit decision may adopt it after verification.
- Add offline source checks and an explicit `*OracleIT` migration/constraint suite. A compiled Oracle test is not a passed Oracle test. Preserve the existing Java 21/eGov 5.0.0/Boot 3.5.6 versions and profiles.

## STEP 05 (2026-11-02)

- Canonical invented data lives in four PSV resources under `src/main/resources/fixtures`; domain-owned Java readers feed the `local-mock` in-memory employee and document-grant adapters and offline tests. The existing `local-mock` employee API explicitly labels ephemeral data in `X-Demo-Data`. No additional HTTP document/account route or real login was created.
- The planned company has five departments and 20 employees with distribution 4/3/6/4/3, 12 pending accounts, and 20 documents. All company mail uses `@bans.example`. Security-themed text is still invented and classified external-transfer prohibited. Role and grant samples remain subject to STEP 03 server policy; fixture flags alone never authorize Gemini transfer.
- Refine the unapplied V001 Oracle schema with unique company email/document business codes, employee extension/duty, document category, and discarded version state. This supports stable, separately rerunnable Oracle `MERGE` seed rows. Recompute the V001 manifest hash. The 17 tables and V002 remain intact; no Oracle connection or SQL execution occurred.
- Keep the Oracle seed in `db/oracle/seed` separate from V001/V002 and render it deterministically from the same PSV resources. Apply it only by an explicit, transactional SQLcl/SQL*Plus command to a dedicated synthetic schema. Accounts stay `PENDING` with an unusable marker in the required hash column; STEP 06 creates real random activation flow and actual password hashes. No fixed credential or token is committed.
- The default Maven test checks fixture counts/references, grant examples, and byte-for-byte seed generation. An `OracleSeedOracleIT` is compiled but only runs under explicit Oracle Failsafe and opt-in on an empty migrated schema. Offline success is not Oracle compatibility evidence.

## STEP 06 (2026-11-03)

- Keep Java 21, eGovFrame parent 5.0.0, and its Spring Boot 3.5.6 combination. Add Boot-managed Spring Security starter and security test support without changing framework versions. The [eGovFrame 5.0.0 release](https://github.com/eGovFramework/egovframe-runtime/releases) lists Boot 3.5.6 and Spring Security 6.5.5; [Spring Security documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html) requires explicit `SecurityContextRepository` saving for a custom authentication endpoint.
- Replace the interim session trust boundary with a Spring Security filter chain. Authenticate JSON employee-code/password requests into server-side `HttpSession`, rotate the session ID, enforce CSRF on mutations, reload account status/roles/credentials version on every authenticated HTTP request, and invalidate stale sessions. Keep the MVC interceptor as a second boundary for current MVC routes. The account lookup is not an ACL or WebSocket security substitute.
- Keep 12 synthetic accounts pending. In `local-mock`, an operator can provide `BANS_LOCAL_ADMIN_PASSWORD` at process startup to activate only the synthetic admin in memory; no usable default credential is committed. The admin-only service issues 256-bit random one-use activation codes, stores SHA-256 hashes, expires them at 24 hours, and invalidates earlier codes when reissued. Raw codes return only to the internal caller for out-of-band delivery; no HTTP issuance route, delivery channel, or response containing a code is implemented.
- Use BCrypt for passwords. After five failed attempts, lock an account for 15 minutes; return fixed 401/409/429 messages. Oracle failure-counter updates commit before the service raises the public error. The memory adapter serializes mutations per account but is ephemeral and cannot prove Oracle concurrency or persistence.
- Add Oracle-only account/activation MyBatis interfaces and XML under `auth`, with `SELECT ... FOR UPDATE`, conditional activation/version update, and conditional unconsumed/unexpired token update in one transaction. Modify the **unapplied** V001 schema to add credentials version, failure count and lock timestamp; update its manifest. No automatic migration or H2 fallback. Oracle runtime compatibility, JDBC type handling, actual lock behavior, and restart durability remain blocked.
- The seed leaves every Oracle account pending. A secure, explicit first-administrator bootstrap and out-of-band activation-code delivery must be designed and validated before Oracle login can be operated; the local mock environment variable does not bootstrap Oracle.
- Gemini was not enabled. The [Google pricing page](https://aistudio.google.com/docs/pricing) currently lists free-tier 2.5 Flash candidates but marks `gemini-2.5-flash` deprecated with a future routing notice; [rate limits](https://aistudio.google.com/docs/rate-limits) depend on account/model, and [South Korea appears in available regions](https://aistudio.google.com/docs/available-regions). Recheck immediately before any explicitly authorized real Gemini integration; no model or paid fallback was activated here.

## STEP 07 directory and personnel administration (2026-11-03)

Preserved all pre-existing STEP 06 modified/untracked work. Added domain-owned organization Controller/DTO/service/repository/Mapper and audit event/Mapper. The service uses only the injected organization repository port. Test/local-mock uses a synchronized deterministic ephemeral store initialized from the existing 20/5 fixture. Oracle uses explicit selected columns, bound parameters, literal `INSTR` predicates, exact department-code filtering, a static MyBatis `<choose>` sort whitelist and Oracle `OFFSET ... FETCH NEXT`. No `${}` SQL identifier interpolation, H2, alternate database or dependency upgrade was introduced. Ordinary tests do not instantiate Oracle DataSource/MyBatis configuration.

Personnel mutations and minimal success audit events are atomic under the Oracle repository transaction; target employee updates acquire a row lock. Audit stores only actor ID, action code, employee code, resource type and outcome, with database timestamp, not payloads or secrets. Missing audit actor insert fails the transaction. Local mock synchronizes the row/event update but demonstrates no Oracle locking or durability. Denied requests do not create a success event; a general denied-security-event pipeline is outside STEP 07. No DDL migration was needed: existing employee/department/audit tables contain the required columns. Existing uncommitted V001/hash changes were preserved.

Employee creation is deliberately distinct from account creation: no credentials, roles, or activation codes are accepted/issued. Unknown mutation fields are rejected. Synthetic email domain is restricted to `bans.example`. Employment lifecycle uses existing ACTIVE/SUSPENDED/DEPARTED values; departure stamps `departed_at`, reactivation clears it, credential state is unchanged. Department moves keep valid sessions and change their trusted department at the next request. CurrentActorFilter already reloads Oracle account joins; the in-memory account adapter now overlays organization status/department for both login and session refresh. Explicit document grants and ownership remain valid under the existing document policy; department membership does not replace these rules. No already-running request cancellation or WebSocket behavior is claimed.

Version evidence rechecked 2026-11-03: project POM retains Java 21 and eGov parent 5.0.0; local resolved runtime remains Boot 3.5.6 and actual eGov modules. Official eGov 5.0 getting-started requires JDK 21 for the development environment: https://www.egovframe.go.kr/docs/5.0/getting-started/ . Official Boot sample uses 3.5.6: https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/ . Oracle 26ai row limiting/order reference: https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/SELECT.html . These support retaining the existing combination and Oracle paging syntax; they do not establish Oracle execution or production readiness. Gemini model/limit/region re-evaluation is NOT_RUN in this organization-only STEP; no Gemini configuration/transport changed and no real call was made.

## STEP 08 REST 1:1 chat (2026-11-03)

The repository was clean on `dev` at STEP entry; no existing user changes were overwritten. REST and later STOMP share `chat.service.ChatService`, which checks the freshly loaded session actor, active recipient account, and existing participant policy before repository operations. A room has exactly two sorted employee codes, a sender-scoped request key, a monotonically allocated room sequence, and per-participant read state. The synchronized local-mock store demonstrates functional concurrency only and resets on restart. The Oracle adapter locks the room row in a transaction before request-key lookup/sequence increment/insert; a unique ordered-pair constraint and `(sender_id,client_request_id)` constraint already exist in V001. Concurrent Oracle outcomes remain untested. No group room, WebSocket, Gemini or paid API was added.

V003 adds an Oracle trigger using `REGEXP_COUNT(CLOB,'.',1,'n')` to reject empty/over-2000-character message content for direct DB writers as well as REST. Java counts Unicode code points. Oracle character equivalence, trigger compilation and surrogate edge cases require real Oracle verification; the checked-in V003 source is not proof of execution. `apply.sql`, metadata check and SHA-256 manifest now include V003. Existing V001/V002 bytes are unchanged. Official [eGovFrame Boot sample](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/) documents parent 5.0.0 and Boot 3.5.6; [Oracle 26ai REGEXP_COUNT](https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/REGEXP_COUNT.html) documents CLOB input and the `n` flag. No version change was made.

## STEP 11 document evidence search decision (2026-11-03)

Preserve the existing Java 21/eGov 5.0.0/Boot 3.5.6, MyBatis and Oracle 26ai target. Search uses a new domain repository port: Oracle MyBatis binds the viewer and literal query, joins the active employee/account/EMPLOYEE role, current published version and fragment, and applies department/owner/live READ-grant ACL before pagination. The service rechecks the candidate's current pointer and ACL; source links repeat the SQL filter and service check. No `external_ai_allowed` predicate is reused for internal search. The old unbounded service search is removed from the active path.

V005 adds `document_tag` and B-tree access paths; the deterministic fixture and generated Oracle seed both assign category tags. User-created documents can add limited metadata tags. The existing fragment is one CLOB segment per version; draft edit updates it, version creation inserts a new one, publication changes the current pointer, and withdrawal/revocation excludes it without modifying published content. `INSTR`/`DBMS_LOB.INSTR` is a bounded, case-sensitive baseline; the B-tree indexes do not establish CLOB substring performance. Oracle Text, Korean analyzer selection, plans and real SQL semantics require official 26ai review and execution on the missing server. No Oracle Text performance claim, dialect substitution, H2 or new dependency was introduced. A read concurrent with ACL revocation can finish after its final check; Oracle transaction/locking behavior remains an integration risk.

## STEP 09 WebSocket security and delivery (2026-11-03)

Kept the pre-existing uncommitted STEP 08 changes intact. The eGovFrame parent 5.0.0 / Boot 3.5.6 combination remains fixed. The offline dependency cache lacked `spring-boot-starter-websocket`; used its already cached Spring 6.2.11 `spring-websocket` and `spring-messaging` modules under the same Boot-managed dependency versions. No version upgrade, database swap, Oracle server or Gemini transport was added. Spring Security's [WebSocket security guide](https://docs.spring.io/spring-security/reference/6.5/servlet/integrations/websocket.html) confirms CONNECT CSRF and same-origin requirements; Spring's [user destination guide](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/user-destination.html) supports per-user queues. The official [eGovFrame Boot sample](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/) documents the retained Boot 3.5.6 combination.

Handshake accepts only an authenticated current session; the STOMP inbound interceptor compares the stored session security principal/version, reloads the account on each frame, checks CONNECT CSRF and whitelists SEND/SUBSCRIBE destinations. A client outbound interceptor drops broker messages to invalidated or ineligible sessions. Only the user's three queues are exposed; no public topic. The STOMP controller calls ChatService, then publishes to the other user's message queue and the sender's ack queue. Since the Oracle repository method is transactional and returns after its own commit when called without an outer transaction, this is designed as a post-commit emission; actual Oracle commit/event ordering and failure recovery remain unverified. The SimpleBroker is single-server, in-process and non-durable; reconnection uses the REST cursor and client deduplication. Duplicate retries may emit the same saved event again, with identical room sequence. No WebSocket message content, secrets or AI transcripts are logged by the new code.

## STEP 10 document workflow and disclosure boundary (2026-11-03)

Preserved pre-existing uncommitted STEP 08–09 work. Added domain-owned document Controller, DTO, common workflow Service, workspace repository port, synchronized ephemeral adapter, Oracle MyBatis adapter/XML, and an ordered Oracle-only V004 reviewer-assignment column/FK. The old document scope/status vocabulary remains unchanged in V001; V004 merely records the assigned reviewer needed for a transactional decision. V001–V003 bytes were not changed. Manual `apply.sql` and the manifest now include V004; no automatic migration or H2 was added. Existing published-version/fragment immutability triggers are retained.

The repository callback holds a document-level monitor in local-mock and a `SELECT ... FOR UPDATE OF d.document_status` transaction in Oracle. Business transitions and ACL checks are shared in `ManagedDocumentService`; the Oracle adapter writes the chosen transition and minimal `audit_event` row in the same transaction. Mock events record actor, code and action only, never document body. Published versions are never updated; new drafts can coexist with the prior current published version, and rejection retains that pointer. Review is assigned to one active DOCUMENT_MANAGER with current READ+APPROVE grant, and self-approval is forbidden. ACCOUNT_ADMIN grants no document authority. User-authored content can be searchable internally after publication, but is never marked external-transfer eligible. A future trusted synthetic ingestion path must establish and review segment-level flags separately before Gemini disclosure. The renderer escapes all HTML and supports only heading lines; images/links remain literal. Oracle lock, FK, trigger, CLOB mapping, grant concurrency and rollback are **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

Version evidence remains the repository's Java 21/eGov parent 5.0.0/Boot 3.5.6 combination, matching the [official eGovFrame Boot sample](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/). No dependency version or Gemini configuration changed. Oracle-only SQL was written against the existing Oracle schema, not a substitute dialect.

## STEP 12 provider and outbound-call control (2026-11-04 KST)

Entry state was clean `dev` at `1c8fd0a`. Kept Java 21, eGov parent/runtime 5.0.0 and Boot 3.5.6; checked the resolved parent POM and the same official eGov Boot sample again. No Oracle SQL/schema/Mapper or framework dependency version changed. Provider code belongs entirely to `ai.provider`; no business data is loaded by this adapter.

Evaluated Google's official Java SDK first: the official repository and tagged POM identify `com.google.genai:google-genai:1.80.0`. Actual wrapper dependency resolution failed offline (missing artifact) and online (`Permission denied: getsockopt`). Its Java 21 runtime/transitive compatibility is NOT_RUN, not PASS. The cached old 0.2.0 was inspected but not adopted. Use the official Developer REST contract through Java 21 HttpClient and existing Jackson for this STEP; no new SDK dependency or speculative version upgrade was added. See `gemini-free-tier.md` for primary sources and the pending SDK re-evaluation.

All normal profiles select Mock by default; ambient GOOGLE_API_KEY never enables a call. Live selection requires `bans.ai.provider=gemini`, the extra `gemini-live` Spring profile, explicit transfer/free-tier acknowledgements, an environment key and the reviewed exact model. Live selection is rejected with `test`, `local-mock` or `oracle-it`. Exclude `gemini-2.5-flash` because Google's current deprecation notice announces automatic routing; only `gemini-2.5-flash-lite` is allowlisted, with operator review of actual account eligibility and Free tier. No model/account tier can be inferred from the ID alone.

The public Java contract accepts an active server actor and one of two server-authored synthetic greetings, not arbitrary text/history/documents. This deliberately leaves user-question safety review and current-document ACL/disclosure integration for a later STEP. The old AiAnswerPort/MockGeminiClient string API remains offline and cannot route to the live provider. No new HTTP endpoint or AI transcript storage is introduced.

Endpoint, request shape and key header are fixed server-side. Bound connect/whole-body timeout, response bytes, candidate shape and output token count. Reject redirects, cookie/proxy/header forwarding and wire-debug mode. Concurrency, rolling-minute and Pacific-day budgets count attempted requests including failures in one process. Automatic retries are zero; 429/5xx Retry-After creates a bounded shared cooldown. Return only fixed public failures without upstream causes/body/headers. Internal result text is transient, redacted in toString and excluded from Jackson output. Restart/multi-process/TPM/Google billing enforcement are explicitly outside this memory-only limiter.

Ordinary Surefire now excludes both Oracle and real-Gemini smoke by name/tag. The optional `gemini-smoke` Maven profile additionally requires permission/acknowledgement environment gates before exactly one greeting; it does not start Spring or Oracle. It was not used to make a real call. Local HttpServer/Mockito tests verify the transport without a real key. Detailed executed results, including resolved intermediate failure, are in `test-status.md`.

## STEP 13 read-only tools and disclosure (2026-11-04 KST)

Preserved all uncommitted STEP 12 changes on dev at 1c8fd0a. Rechecked the resolved eGov 5.0.0 parent/Boot 3.5.6, Java 21 runtime and the official eGov Boot sample; no dependency/framework/database change. Rechecked Google pricing/deprecation/regions/rate-limit documents listed in gemini-free-tier.md; existing model restrictions remain and actual project quota/real API execution is NOT_RUN. SDK resolution was not retried in this STEP.

Register exactly five named read operations with bounded schemas and a switch, not reflection or model-selected SQL. The protected preview API and minimal HTML demo reuse Spring Security sessions and CSRF. CurrentSessionService reloads actual stored session identity, credential version, account status/EMPLOYEE role and department for each tool and before send. Known identity claims in untrusted JSON are dropped; unknown functional fields and all unregistered tools are denied. Approved example question ID/text must match the server catalog exactly plus acknowledgement; no regex masking or caller-provided safety flag authorizes arbitrary text.

Organization tools reuse STEP 07 ports/Oracle Mapper and allow only the six public fields matching canonical invented fixture values; .example email alone is insufficient. Department lookup follows the current actor only. Document tools reuse STEP 11 source/search/ACL queries. Added classification columns to SELECT projections and in-memory/current-version carriers; internal search keeps its original predicates. New externalSource separately requires current ACL/publication and external/synthetic/non-sensitive flags in both query row and refreshed workspace. User-authored versions default false and cannot set those flags by JSON. No DDL, seed or manifest bytes changed.

The service runs a first read and a final read pass, exporting only final results to a concrete Mock provider. The live provider inherits denial of tool payloads and the service rejects non-Mock providers before data lookup. Payload projections are immutable and bounded; questions/args are excluded from default JSON/string representations. Document/model content is data, never a policy or registration source. The direct excerpt denial hides existence; search omits internally readable but non-exportable candidates. No title, internal result count, credentials, salary, task body or complete table is sent.

Audit adds an independent domain port/ephemeral adapter and Oracle Mapper for fixed metadata in the existing audit_event table, with nullable actor VARCHAR binding and REQUIRES_NEW intended durability. This is server audit, not a model write tool. No question/answer/argument/document/exception/key field exists. Actual Oracle mapping/transaction/races remain BLOCKED: ORACLE_26AI_NOT_INSTALLED. Full tests PASS does not certify SQL. See ai-tool-security.md and handoff for limits, complete file inventory and actual commands/results.

## STEP 14 question orchestration decision (2026-11-04 KST)

Preserved the uncommitted STEP 12/13 work on dev at 1c8fd0a. Retained Java 21, eGov parent/modules 5.0.0, Boot 3.5.6, profiles, SDK decision and Oracle 26ai target. Rechecked the official eGov Boot example and Google pricing/deprecation/regions/rate-limit/structured-output documents linked in ai-question-orchestration.md. No dependency/POM/config/model upgrade, migration/seed change, Oracle installation or actual API request was performed in STEP 14.

Add a server-classified, single-turn question use case. Exact reviewed synthetic intents are the safety boundary; unknown input is UNANSWERABLE without model classification or reflection. Expense-owner and canonical employee-contact facts come only from the existing organization public DTO and are re-read before return. Document search and externalSource reuse STEP 11/13 current ACL/publication/classification services. Multiple candidates are conservatively unanswerable instead of allowing a model to resolve conflict or invent a rule.

The new package-minted GroundedPrompt is the only additional live input contract: one reviewed synthetic question and one current external-allowed synthetic/non-sensitive excerpt of at most 200 code points, with a local S1 evidence label. This supersedes the STEP 12 greeting-only statement for this distinct contract. STEP 13 previewTools remains Mock-only. Never pass source titles/IDs/full tables/credentials/history/tool definitions. A model may only return the exact complete excerpt in a strict two-field JSON object. The server writes the final answer/source URL and rechecks identity, ACL/grant, publication/version/fragment/content immediately before response. Model prose or document instructions cannot select tools, facts or policy.

Bound eight stages, four reads, one provider call, ten-second stage checks and remaining-time HTTP timeout; share the existing session five-per-minute/one-active budget with tool preview. Output tokens are capped by min(configuration,256). Fixed safe failures and metadata-only audit are isolated to AI. The required answer DTO is an approved transient read response, not raw question/model transcript reflection or storage. Oracle audit adds only an existing-table metadata INSERT through a separate REQUIRES_NEW port. Normal tests/local-mock stay DB-free and Mock by default. Oracle mapping/transactions/final-check races remain BLOCKED and real Gemini remains NOT_RUN. See the new orchestration guide and handoff for limits and actual PASS evidence.

## Regression and local artifact ownership (2026-11-04)

Keep reusable API/security tests and shared design/handoff/validation documents under version control. Ignore disposable developer notes only through explicit local locations (notes/local, docs/local, scratch/.scratch) or *.local.md/*.local.txt, together with backups and JVM diagnostic files. Existing build logs already match *.log. Avoid blanket document ignores or index removals. Narrow secret-name rules to actual secret files/directories because the old credential substring rule had hidden the necessary Java CredentialStatus enum; its source must be included in a future commit.

Add a runtime-mapping inventory and actual-security MockMvc regression test for all 37 MVC method/path mappings, plus missing document HTTP transitions. Rerun all offline tests and the packaged loopback anonymous-route smoke. No production API, framework, model, SQL or final Oracle choice changes were necessary. Results are in test-status.md; database and actual model verification stay separately blocked/not run.

## STEP 15 최소 UI 결정 (2026-11-04)

진입 시 dev / 9f1cfad, Git 작업 트리가 깨끗함을 확인했다. 기존 Java 21/eGovFrame 5.0.0/Boot 3.5.6과 실제 모듈·프로필·Oracle 26ai·Gemini 설정을 유지한다. eGov Boot 공식 예제와 Google 가격/지역/폐기/프로젝트 한도를 재확인했다. 모델·SDK·의존성 버전을 올리지 않고 실제 API는 호출하지 않았다.

이미 있는 Thymeleaf로 공용 GET / shell을 렌더링하고 정적 ES module/CSS 네 개를 같은 origin에서 제공한다. common.web은 도메인 데이터를 담지 않는 공용 shell만 맡는다. 기존 도메인 API/인가를 재사용하며 문서 승인 대기함·전체 테이블 API를 임의로 추가하지 않는다. 계정 활성화 시연에 필요한 관리자 전용 코드 발급만 auth 내부 서비스를 기존 현재 세션/RBAC/CSRF와 연결했다. 기본 비밀번호·활성화 코드·API 키를 추가하지 않는다.

브라우저 메뉴 숨김은 인가 수단이 아니다. 출처·문서·관리자 쓰기는 서버 권한을 다시 검사한다. 모든 출력은 textContent/허용 DOM 요소로 처리하며 서버 게시 renderer의 제한 태그도 텍스트로 복원한다. CSP/no-store, POST form, same-origin 쿠키/CSRF, 세션 교체 전 비동기 응답/쓰기를 폐기하는 경계를 적용했다. AI는 기존 검토 질문 선택만 제공하고 외부 전송 고지를 유지한다.

작은 STOMP 클라이언트는 SimpleBroker가 지원하지 않는 receipt를 기다리지 않고, 세 고정 큐와 기존 애플리케이션 ack를 사용한다. 재접속·REST 복구·안정적인 요청 ID·제한된 메모리/페이지 수를 사용한다. Chrome renderer/CDP가 이 환경에서 실패하여 클릭/시각 E2E는 BLOCKED로 남겼다. 대신 실제 HTTP/WebSocket 서버에 배포용 JS 클라이언트를 연결한 테스트와 6개 화면에 대응하는 MockMvc 흐름을 실행한다. 이 검증을 실제 브라우저 클릭 PASS로 바꾸어 기록하지 않는다. 상세 범위·실행법은 web-ui.md와 test-status.md에 기록한다.

# 2026-11-09 Oracle local connection configuration

For the explicit `oracle` profile, load `bans_chatbot/oracle-local.properties` from the module working directory. The file is Git-ignored and carries `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, and `ORACLE_DB_PASSWORD`; the committed `.example` has no usable credentials. `oracleIntegrationTest.cmd` accepts that file instead of requiring PowerShell environment-variable input and still supports the prior environment fallback. Startup retains the Oracle Thin URL and real connection guards; no schema or seed initialization is added.

## 2026-11-09 Oracle first-admin demo decision

The canonical seed keeps all accounts PENDING and contains no usable credential. To make the persistent Oracle UI demo possible, add a separate opt-in `oracle-bootstrap` application profile that activates only EMP-001 after an interactive operator password prompt. It checks the connected Oracle user/PDB, exact schema and seed shape, pending/sentinel state, and ACCOUNT_ADMIN role, then commits the password hash and metadata audit together. Repeated or mismatched runs fail closed. The ordinary Oracle profile does not invoke this bootstrap, and local-mock's environment password remains unrelated. A local-only HTTPS connector preserves the secure session cookie; the demo uses Mock AI until live-provider authorization is separately established. Details and outstanding validation are in oracle-ui-demo.md.

The Oracle shell uses a read-only account check on page load to distinguish the pending first-admin state from normal sign-in. It renders a setup instruction and hides the login form until refresh after activation. This avoids a public account-creation or activation endpoint and keeps the setup password out of the rendered page and Git. The local-mock shell remains unchanged.

The operator requested a Git-ignored local property instead of entering the first-admin password in PowerShell. On ordinary `oracle` startup, `OracleFirstAdminBootstrap` now reads `BANS_ORACLE_FIRST_ADMIN_PASSWORD` only when EMP-001 is still the fresh seeded PENDING account; the guarded transaction activates it once. Later startups ignore the property after the account is active. `oracle-it` excludes the runner, and the interactive profile remains an optional fallback. The property never appears in the committed example, SQL, HTTP response, or application logs. The initial setup page now explains the property and restart workflow.

For the dedicated localhost synthetic demo only, an explicitly opted-in short initial password is permitted when the Oracle server binds to 127.0.0.1, HTTPS is enabled, Mock AI is selected, and the configured schema user is `BANS_CHAT`. This does not alter normal account activation's 12-character minimum. The user-supplied secret remains only in the Git-ignored local property and its hash in Oracle. A failed login now displays a credential error without clearing an anonymous page as an expired session. Versioned UI script URLs force the corrected client to load.

## 2026-11-10 local demo account passwords

The operator requested predictable passwords for all 12 existing synthetic Oracle login accounts. Provisioning was kept in the Git-ignored `bans_chatbot/demo-mock-data/` folder, separate from the canonical seed. The target is `employee_account.password_hash` (there is no profile password column). The running authentication service already uses salted BCrypt cost 10, so the local SQL uses matching BCrypt hashes rather than a bare SHA-256 digest. It activates only existing accounts in the dedicated `BANS_CHAT`/`FREEPDB1` synthetic schema, bumps credential/session versions on changes, clears failed-login locks, and writes metadata-only audit events. The checked-in seed still has no usable password; the normal activation policy is unchanged. These weak credentials are limited to the local demonstration.

The password for each account is its own employee code without the hyphen (for example, `EMP-005` uses the corresponding `EMP005` demo password). The first assignment incorrectly used the account's ordinal among the 12 accounts; the local SQL and database were corrected, rotating the 10 affected hashes and session versions.

The operator subsequently chose the three-digit suffix alone as the final local demo password (for example, `EMP-014` uses `014`). All 12 local hashes were rotated again. This later decision supersedes the prefixed-password mapping above; the checked-in seed still contains no usable password.

The login failure reported for `emp001` was an identifier-format mismatch: the Oracle mapper searches the canonical `EMP-001`, while the login form allowed only uppercase codes with a hyphen. Login now normalizes case and an optional hyphen in the authentication service, and the form accepts the same formats. Password hashes and the account table do not change for this fix.

## 2026-11-10 Oracle-only demo entry point
Default application profile is now `oracle`; `scripts/local.ps1 -Action build` includes `-Poracle`, and `-Action run` delegates to the Oracle HTTP demo. The previous 8080 local-mock process was stopped. Test fixture adapters remain available only for the offline test harness and explicit `local-mock` test profile; normal tests do not connect to Oracle. The current demo URL is http://127.0.0.1:8080/.


## Oracle-only 8080 demo (2026-11-10)

2026-11-10: Removed the local-mock runtime profile. Oracle is the default and only demonstration persistence path. Offline tests use test and fixture-test profiles; their fixtures do not run in the Oracle application. Oracle HTTP binds to loopback port 8080; the TLS demo configuration is no longer imported.

## 2026-11-10 saved local Gemini settings
Use Git-ignored `bans_chatbot/.env` for the Gemini key and activation flags. Oracle configuration imports it from the module working directory; the run script selects `gemini-live` only when the saved provider is `gemini`. Keep the default Mock mode and fail-closed activation gates.

## Consolidated local settings (2026-11-10)

2026-11-10: Consolidated local Oracle and AI settings into Git-ignored `bans_chatbot/.env`. The Oracle profile imports only this file from the module working directory. The old Oracle local properties file was removed after exact key comparison and successful Oracle login.


## Chat UI redesign (2026-11-10)

2026-11-10: Chat UI was reorganized into a workplace messenger layout without changing the chat API, persistence, or authorization path. Message content is still inserted with textContent; conversation filtering uses the already loaded room list instead of a request per keystroke.
