# Oracle 26ai validation backlog

## STEP 19 재확인 — Oracle 결과는 승격하지 않음

2026-11-04 19:09KST: Oracle서비스/PATH도구/1521리스너/설치registry/접속env값 존재를 조사했으나 사용 가능한 설치·접속증거 없음. 실제 JDBC/SQL접속시도0. Oracle13개IT 전부 **BLOCKED: ORACLE_26AI_NOT_INSTALLED** 유지다. 신규 [ORACLE_VERIFICATION_CHECKLIST.md](ORACLE_VERIFICATION_CHECKLIST.md)에 각 class·gate·데이터 전제/실행여부/수락 결과 기록란을 추가했다. 기존 [상세 명령](oracle-install-validation.md)과 함께 사용한다.

현재39MVC경로의 Mock실HTTP성공/285단위·Mock PASS/별도부하2PASS는 Oracle SQL·ACL JOIN·commit/락·성능 증거가 아니다. 첫관리자 secure bootstrap 미구현과 TLS/backup·restore/DB중단회복도 최종 수락 차단으로 유지한다. Oracle설치/SQL방언/스키마/마이그레이션/seed 내용은 변경하지 않았다.

## STEP 18 최종 DB 수락 대기

상태는 계속 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**다. [oracle-install-validation.md](oracle-install-validation.md)가 최종 설치 후 단계별 실행 매뉴얼이며, [known-limitations.md](known-limitations.md)의 미구현 항목도 함께 수락해야 한다. 모든 Oracle 명령은 현재 미실행이다.

| 항목 | 수락 기준 / 현재 상태 |
|---|---|
| 설치·계정·제품 증거 | 공식 Windows 전제·백업·승인·전용 PDB/schema·Oracle26ai Free banner/RU. JDBC major26 단정 제거. banner unit4건 PASS는 실제 연결 증거 아님. BLOCKED. |
| migration | V001–V005/manifest/18 tables/5 roles/3 VALID triggers/제약 negative rollback/재실행거부. empty gate와 수동 apply 중 하나만. BLOCKED. |
| seed | migrated empty state에서 rollback 2회 적용 IT 후 명시적 commit seed, 5부서20직원12pending계정20문서/관계. active 계정 이후 재적용 금지. BLOCKED. |
| 최초 관리자 | **보안 bootstrap 미구현**, Oracle 인증/전체 UI 수락의 차단 사항. pending 기본값을 버리거나 dummy test hash를 적용하지 말고 별도 승인 구현 필요. |
| 기능·보안·영속성 | 13개 Oracle IT를 각 gate/데이터 전제별로 실행, 실제 HTTP/HTTPS API·재시작 상태·현재 ACL·부서/grant revoke·버전. BLOCKED. |
| 메시지/승인 경쟁 | room_seq/idempotency/read, FK/락/승인/revoke commit race, 실패시 rollback. 일부 코드 준비, 추가 다중 연결 case TODO. BLOCKED. |
| STOMP/AI | 실제 commit 후 전달/실패시 미발송/중복·누락 복구; Oracle 검색+Mock/반출0회/감사 내구성. BLOCKED. |
| 부하/중단/복구 | track-b 두 gate의 커밋 부하, 계획/리소스, 승인된 DB stop/restart, 백업 분리복원/RPO·RTO 실측. NOT_RUN/BLOCKED. |
| 배포 경계 | oracle Secure 쿠키와 TLS/동일 origin WS/프록시·내부망 통제/비밀·로그/백업 보관 검증. NOT_RUN. |

명령은 체크리스트에 class/환경변수/SQL client 경로까지 기록했다. 기존 `oracleIntegrationTest.cmd`/Maven oracle+oracle-it/tag 격리 유지, 정상 test의 실제 연결은 0회다. seed rollback IT를 committed seed 뒤 실행하거나 모든 opt-in을 동시에 켜지 않는다. ChatConcurrencyOracleIT는 실제 커밋을 남기므로 전용 폐기 가능 스키마와 두 gate가 필수다. 자동 설치/삭제/DDL/seed/복구/유료 전환은 없다.

STEP18 offline build PASS나 driver 다운로드/Mapper parse로 위 항목을 PASS로 올리지 않는다. 최종 DB 포함 검증·운영 준비 완료 판정은 보류한다.

## STEP 17 트랙 B (2026-11-04)

모든 Oracle 실험은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**다. Mock 30연결/부하 9회/HTTP2763회 결과와 기본 suite 280 PASS는 Oracle 실측 증거가 아니다. SQL dialect/최종 DB/기존 DDL/seed/Mapper는 변경하지 않았다. 기존 태그+Failsafe 격리 유지, Oracle IT 13개가 일반 test에서 제외된다.

준비한 `ChatConcurrencyOracleIT`는 별도 worker transaction 20개가 10개 requestId를 각 2회 보내고 정확히 10개 연속 room_seq와 read monotonic을 검사한다. 실제 커밋 쓰기이며 테스트가 자동 rollback하지 않는다. 전용 폐기 가능한 migrated/seeded schema, 외부 writer 없음, ORACLE_LOAD_TEST=YES와 ORACLE_LOAD_WRITES_ACK=YES 두 opt-in에서만 실행한다. **코드 컴파일만 PASS**, 실제 SQL/lock/commit 결과는 BLOCKED다.

미실행 절차: 기존 Oracle 환경 변수와 Maven 설정 후 루트에서 `performance/scripts/track-b.ps1` 실행. runner가 기존 oracleIntegrationTest.cmd/Failsafe의 ChatConcurrencyOracleIT를 선택한다. 설치/migration/seed/중단/데이터 삭제를 자동 수행하지 않는다. 자원 관측은 read-only oracle-observe.sql을 운영자가 별도 수행하며 v$ 권한이 없으면 DBA가 수행한다. AI/앱의 권한을 확장하지 않는다.

Oracle Free 공식 제한(2 cores/2GB RAM/12GB user data)을 기준으로 계획한다. 상세 공식 링크·관측 SQL 한계·표는 performance-and-failure.md에 있다. 남은 항목은 문서 승인/revoke/부서 이동 다중 연결 commit 경쟁, FK/trigger/실행계획/검색/CLOB/pool wait, DB 중단·복구/복구 후 동일 키 재전송, CPU·메모리·디스크 시간축 관측이다. 이 중 신규 concurrency IT 외 추가 fault injection/plan 자동화는 TODO/NOT_RUN이다.

운영 DB/성능 검증 완료 판정은 보류한다. Oracle 설치 후 트랙 B를 명시적으로 실행해야 한다. 실제 Gemini/Oracle E2E도 NOT_RUN이며 oracle-it에서는 Mock만 사용한다.

## STEP 16 공격 시나리오 Oracle 대기 (2026-11-04)

전체 offline **PASS 18:05:32**, 278 PASS/1 SKIPPED/실패·오류 0. 모든 실제 DB 항목은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**이며 offline 입력 거부/Mapper source 검사를 SQL 안전성 증명으로 취급하지 않는다. migration/seed/production SQL/DB 선택은 변경하지 않았다.

`document.mapper.SecurityAttackOracleIT`를 추가했다. 기존 `src/test`의 `*OracleIT` + `@Tag("oracle-integration")` + 명시적 Failsafe를 사용하므로 별도 src/oracleIntegrationTest 플러그인을 새로 만들지 않았다. 일반 test에서는 이름/tag로 제외한다. 총 Oracle IT는 12개다.

- 준비 코드: 전용 seeded schema의 EMP-002를 테스트 transaction에서 활성화, 실제 Oracle literal injection query, Oracle MERGE로 READ grant 부여, source 조회, SYSTIMESTAMP revoke, 동일 transaction 내 거절 확인 후 rollback. **컴파일만 했으며 실행 BLOCKED**.
- 별도 TODO/NOT_RUN: 두 연결에서 commit된 revoke/부서 이동/출판 경쟁, lock ordering/동시 승인, audit REQUIRES_NEW 내구성, Oracle 실제 injection/constraint/JDBC/실성능·부하. Oracle 의존 P0는 계속 차단급 검증 대기다.
- 실제 Gemini/Oracle E2E: NOT_RUN. oracle-it에서 Mock Provider를 유지한다.

Oracle 설치 및 전용 합성 스키마의 기존 migration/seed/환경 변수 준비 이후에만 실행할 **미실행 절차**:

```powershell
$env:ORACLE_SECURITY_TEST = 'YES'
.\oracleIntegrationTest.cmd
```

기존 domain별 opt-in과 파괴적 migration/seed 제한은 유지한다. 이번 결과로 어떤 DB backlog도 PASS로 변경하지 않는다. 최종 보안 검증 완료 판정은 보류한다.

Status on 2026-11-02 KST: **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No Oracle SQL or connection was executed.

| Future check | Required condition | Current status |
|---|---|---|
| `oracle` profile startup and JDBC connection | Oracle 26ai Free installed, JDBC driver declared, valid local credentials | BLOCKED: ORACLE_26AI_NOT_INSTALLED |
| Oracle DDL, constraints, indexes, migrations, MyBatis SQL | Schema/mapper code from later STEP and Oracle server | BLOCKED: ORACLE_26AI_NOT_INSTALLED; code NOT_RUN / not yet written |
| Transactions, locking, concurrency, performance | Oracle server and domain implementation | BLOCKED: ORACLE_26AI_NOT_INSTALLED; code NOT_RUN / not yet written |

Future command (do not run until the above conditions hold): `bans_chatbot\mvnw.cmd -Poracle-it verify` with the Oracle environment variables. `*OracleIT.java` tests run only in this explicit Failsafe profile. No test class exists yet; a zero-test invocation is not validation.

STEP 01 DB-free evidence is tracked in `docs/test-status.md`; its HTTP and JUnit smoke passes do not change any Oracle status above.

Pre-STEP 02 package cleanup (2026-11-02 KST): `global.health` relocation, Java 21 IDE alignment, and `AGENTS.md` rules made no SQL, Mapper, schema, or Oracle test changes. All Oracle items above remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. The Maven `oracle-it` profile remains reserved; it was not invoked. The separate offline Maven build failures and DB-free smoke results are recorded in `docs/test-status.md`.

## STEP 02 update (2026-11-02 KST)

`global.config.OracleDataSourceConfiguration` now requires an Oracle Thin JDBC URL and credentials and checks a connection at `oracle` startup. With the variables removed, it exited 1 with `ORACLE_CONFIG_MISSING`; this is a **PASS for the fail-fast guard**, not a database connection test. `oracleIntegrationTest.cmd` checks prerequisites and then runs `mvnw.cmd -Poracle,oracle-it verify`. Its missing-variable check exited 2 as intended; the database suite was **BLOCKED: ORACLE_26AI_NOT_INSTALLED** and did not run. `OracleConnectionOracleIT` is tagged `oracle-integration`, excluded from normal Surefire, and intended to assert Oracle JDBC metadata major version 26. The default test compiled that class only in an independent direct compilation; compilation does not validate connectivity.

| Pending Oracle check | Status / condition |
|---|---|
| Driver resolution and Oracle 26ai Free connection | BLOCKED: ORACLE_26AI_NOT_INSTALLED; explicit Maven `oracle` profile declares `ojdbc17:23.26.4.0.0`, absent from the offline cache |
| `OracleConnectionOracleIT` execution via Failsafe | BLOCKED: ORACLE_26AI_NOT_INSTALLED; Failsafe 3.5.4 also absent offline; neither artifact was downloaded |
| Domain Oracle Mapper/XML, schema DDL, constraints, indexes, migrations | NOT_RUN: no domain persistence code or schema in STEP 02; requires later implementation and Oracle server |
| Transaction, lock, concurrency, migration compatibility, performance | BLOCKED: ORACLE_26AI_NOT_INSTALLED; tests to be added with the relevant domain implementations |

Future explicit Windows command from `bans_chatbot`: `.\oracleIntegrationTest.cmd`, after installing Oracle 26ai Free, setting the three Oracle environment variables, and making the driver/Failsafe artifacts available. Until then, Oracle behavior and final readiness remain unverified. No H2 or alternative database was added.

## STEP 03 ACL validation additions (2026-11-02 KST)

The Java `EmployeeRole`, `DocumentGrant`, `DocumentRecord`, and `DocumentSegment` models plus Mockito-backed policies contain **no Oracle DDL or SQL**. All following database checks are **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; the corresponding Oracle test code is **NOT_RUN / not yet written**. The 26 passing offline tests are not evidence of these checks.

| Planned Oracle integration case | Required assertion |
|---|---|
| Employee role and status lookup | `employee_role` rows map only assigned roles; a suspension/departure immediately blocks a stale session on the next protected request. |
| Department and explicit employee grants | Same-department approved document is readable; cross-department/private document is hidden unless a current `READ` grant exists; admin roles do not override this. |
| Approval grant and role | Only an active `DOCUMENT_MANAGER` with read access and current `APPROVE` grant can approve; `DOCUMENT_EDITOR` alone cannot. |
| Search ACL parity | Oracle result filtering matches direct-read policy, including pagination/count behavior and hidden resources. |
| Grant revocation and transaction boundary | A revoked grant is rejected on the next read and before external AI disclosure; inspect concurrency and rollback behavior. |
| Segment external-transfer metadata | Only matching synthetic, non-sensitive segments with explicit `external_ai_allowed` are eligible; no whole-document shortcut. |
| Direct conversation authorization | Only stored participants can read the 1:1 conversation; revoked/inactive accounts are denied. |

Future explicit command from `bans_chatbot`: `.\oracleIntegrationTest.cmd` after installing Oracle AI Database 26ai Free, setting `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, and `ORACLE_DB_PASSWORD`, and implementing STEP 04 Oracle schema/Mapper tests. The current `OracleConnectionOracleIT` only checks JDBC metadata when eventually run; it cannot validate ACL queries.

## STEP 04 schema source and executable Oracle backlog (2026-11-02 KST)

The 17-table Oracle-only DDL, V002 indexes/triggers/role seeds, SHA-256 manifest, and manual empty-schema runner now exist under `bans_chatbot/db/oracle`. `OracleSchemaSourceTest` passed offline, but **no Oracle SQL was executed**. All database results below remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. The existing STEP 01-03 history above is retained for traceability; references there to missing schema describe those earlier stages.

| Oracle-only case | Code and explicit condition | Status |
|---|---|---|
| Initial migration on a dedicated empty schema | `apply.sql`; optional `OracleSchemaOracleIT` migration method only when `ORACLE_EMPTY_SCHEMA_MIGRATION_TEST=YES` | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| Second application refused | `verify_empty.sql` and `OracleSchemaOracleIT.migratedSchemaExistsAndRepeatPreflightRejectsIt` expect ORA-20050 | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| 17 tables, five roles, two valid triggers | `verify_schema.sql` and `OracleSchemaOracleIT` metadata assertion | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| UNIQUE, FK, CHECK negative cases and DML rollback | `OracleSchemaOracleIT.uniqueForeignKeyAndCheckFailuresRollBack` uses synthetic rows and rollback | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| Published version/fragment immutability; document current-version integrity | V002 triggers and V001 composite FK; add negative Oracle IT cases after first migration | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |
| Chat pair/order, message idempotency/read-state constraints | V001 constraints; add negative Oracle IT cases with synthetic employees | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |
| RBAC/ACL/revocation joins, concurrency, index plans and performance | Domain Mappers/services and Oracle fixture needed in later STEP | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |

Explicit application after installation: connect interactively as an empty dedicated app schema in SQLcl/SQL*Plus from `bans_chatbot` and run `@db/oracle/apply.sql`. Then set the three Oracle environment variables and run `.\oracleIntegrationTest.cmd`. For an isolated disposable **empty** integration schema, set `ORACLE_EMPTY_SCHEMA_MIGRATION_TEST=YES` before the command to let the IT create schema objects; do not use that flag against populated data. No DROP/reset or automatic startup migration is provided. A successful offline Maven `test` never changes this BLOCKED status.

## STEP 05 synthetic seed validation additions (2026-11-02 KST)

V001 was refined **before any Oracle application** with business codes and synthetic directory columns, and its SHA-256 manifest was updated. The 20/5/12/20 PSV fixture and generated `db/oracle/seed/S001__synthetic_company.sql` pass offline byte-equality/count/reference checks. `OracleSeedOracleIT` is compiled but excluded from ordinary Surefire. Its explicit opt-in `ORACLE_SYNTHETIC_SEED_TEST=YES` requires an empty, migrated, disposable schema; it applies the seed twice in one transaction, checks row counts, then rolls back. None of this IT ran in STEP 05.

| Oracle-only case | Planned command / condition | Current status |
|---|---|---|
| Current V001/V002 migration compatibility after STEP 05 refinement | `@db/oracle/apply.sql` against a dedicated empty Oracle 26ai schema; verify manifest and 17 tables | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; NOT_RUN |
| Synthetic seed FK/constraint and trigger behavior | `@db/oracle/seed/apply_seed.sql` after migration; inspect exact counts, document current versions, approval/grant references | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; NOT_RUN |
| Seed idempotence and DML rollback | Explicit `.\oracleIntegrationTest.cmd` with `ORACLE_SYNTHETIC_SEED_TEST=YES` on a separate empty migrated test schema; `OracleSeedOracleIT` | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; compiled but NOT_RUN |
| Activation after pending seed and account/role/grant freshness | Later STEP 06/ACL Mapper IT with real Oracle and server sessions | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; implementation NOT_RUN |
| Locking, concurrency, index plans, real performance | Real Oracle 26ai Free plus domain Mapper/services and representative synthetic load | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; NOT_RUN |

For manual seed application, connect interactively as the dedicated synthetic schema owner without exposing credentials and run `@db/oracle/seed/apply_seed.sql` from `bans_chatbot`. The runner preflights the schema, rolls back DML on error, verifies exact counts, and commits once. Rerun only while all accounts are still pending and rows have not diverged. See `docs/fixture-and-seed.md`. This command is an **execution instruction**, not a result. The test/seed status is kept aligned with `docs/test-status.md`.

## STEP 06 auth persistence boundary (2026-11-03 KST)

V001, still unapplied on Oracle, now adds `credentials_version`, `failed_login_count`, and `locked_until` to `employee_account`; `manifest.sha256` was updated. Domain-owned `AccountMapper` / `ActivationMapper` XML uses Oracle `SELECT ... FOR UPDATE`, conditional pending-account activation and credential-version update, role lookup, activation-code hash insertion/invalidation, and conditional unconsumed/unexpired consumption. Offline Mapper XML parsing and the 45-test DB-free suite **PASS**; this is no evidence of Oracle SQL execution. The `oracle` adapter uses one account-row transaction for issuance/consumption/login failure state. Every check below remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED** and has **NOT_RUN** as an Oracle test.

| Oracle-only check | Available code / execution condition | Status and remaining work |
|---|---|---|
| V001/V002 migration with revised account columns and manifest | Manually inspect and apply `@db/oracle/apply.sql` on a dedicated empty 26ai schema; then synthetic seed. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; verify DDL, checks, indexes and seed compatibility on the engine. |
| Mapper account/role mapping and JDBC time-zone types | `AuthOracleIT.mapperLoadsAccountAndCredentialVersionFromMigratedSeed`; set `ORACLE_AUTH_TEST=YES` only on a dedicated migrated and seeded synthetic schema. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; compiled, **NOT_RUN**. Verify `TIMESTAMP WITH TIME ZONE` to `OffsetDateTime` mapping. |
| Activation code hash consumption, account credential update, transaction rollback | `AuthOracleIT.activationConsumesHashAndChangesCredentialsAtomicallyWithRollback`, same opt-in; Spring test transaction rolls back. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; compiled, **NOT_RUN**. Inspect row-lock and conditional-update behavior on Oracle. |
| Reissue invalidates previous code and concurrent activation permits one winner | Oracle server, isolated synthetic rows, multi-connection integration test. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; Oracle concurrency test code **NOT_RUN / not yet implemented**. In-memory concurrency PASS is not a substitute. |
| Failed-login counter/lock survives public 401/429 and restart | Oracle server, repeated wrong-password requests, separate committed transaction, application restart. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; Oracle restart/commit test code **NOT_RUN / not yet implemented**. |
| Credential-version and status/role revocation on next request | Oracle-backed HTTP session plus direct committed DB changes in another transaction. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; Oracle-backed MockMvc/HTTP test **NOT_RUN / not yet implemented**. |
| First-admin bootstrap, delivery, cookie over HTTPS, performance | Secure operator procedure and dedicated Oracle environment; all seed accounts begin pending. | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** for DB behavior; bootstrap/delivery design **NOT_RUN / not yet implemented**. No usable administrator credential is checked in. |

After Oracle 26ai Free installation and dedicated empty-schema setup, run the existing explicit migration and seed procedures; set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, and (for the auth suite) `ORACLE_AUTH_TEST=YES`, then run `.\oracleIntegrationTest.cmd` from `bans_chatbot`. Do not set destructive migration/seed opt-in flags against populated schemas. The command was **not executed** in STEP 06. Until the items above pass against real Oracle, account persistence, simultaneous activation and restart durability have no PASS claim. The status matches `docs/test-status.md`.

## STEP 07 organization persistence (2026-11-03 KST)

All Oracle execution remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. `OrganizationMapperSourceTest` parses organization/audit XML and checks bound SQL generation offline; its PASS is not an Oracle WHERE, sort, paging, lock, constraint or transaction result.

| Check | Executable source / remaining work | Status |
|---|---|---|
| SELECT filters, literal injection-shaped strings, page boundaries, each permitted sort | `organization.mapper.OrganizationOracleIT.filtersLiteralSearchAndPagesOnOracle` on the unchanged 20/5 migrated/seeded synthetic schema. Further engine-specific Korean/supplementary-character collation and equal-name tie-order assertions required. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| Actual employee row lock, department/status UPDATE, departed exclusion, create and audit insertion | `OrganizationOracleIT.rowLockMutationAuditAndCreateParticipateInRollback`; Spring test transaction rolls back all changes. Post-rollback external visibility still needs a separate case. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |
| Duplicate email/code, FK/active department rejection, audit-failure rollback, failed write leaves no audit, cross-connection commit/rollback visibility | Additional Oracle IT cases required. HTTP/fake conflict tests do not establish constraint behavior. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |
| Concurrent personnel updates, account/status mutation races, lock timeout/deadlock handling | Multi-connection Oracle IT required; target row locking exists but is unverified. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |
| Oracle-backed HTTP session department refresh and document ACL, suspension/departure, restart persistence | HTTP/Oracle IT required. Existing MockMvc ACL and session tests prove fake behavior only. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; test code NOT_RUN / incomplete |
| Real sort/index execution plans, substring-search performance and representative load | Oracle plans/load needed; no speculative tuning/index migration added. Separate count/list statements may observe concurrent changes under READ COMMITTED. | BLOCKED: ORACLE_26AI_NOT_INSTALLED; NOT_RUN |

After installation, review/apply the existing migration and synthetic seed procedures to a dedicated empty Oracle 26ai schema. Set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD` and `ORACLE_ORGANIZATION_TEST=YES` only for that isolated migrated/seeded schema; from `bans_chatbot`, run `.\oracleIntegrationTest.cmd`. This explicit Failsafe command also selects other Oracle ITs with their own opt-ins. Do not set empty-schema migration/seed flags against a populated schema. STEP 07 did not execute this command or any JDBC SQL. Default Surefire excludes every `*OracleIT` and the `oracle-integration` tag.

## STEP 08 chat Oracle backlog (2026-11-03 KST)

All cases below are **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. XML parsing, fake two-thread runs, migration hashes and JVM package are not SQL execution.

| Oracle check | Status / next evidence |
|---|---|
| V003 trigger compiles and rejects empty, 2001 code points, accepts 2000 Unicode supplementary code points; CLOB binding and newline | BLOCKED; manual migration/negative Oracle IT needed. `REGEXP_COUNT` semantics must be compared with Java code points. |
| Ordered pair UNIQUE reuse and initial two read-state rows | BLOCKED; `ChatOracleIT` has preliminary rollback case, two-connection race IT still NOT_RUN/incomplete. |
| Room-row `FOR UPDATE`, atomic `last_room_seq`, sender/request uniqueness and differing-content conflict | BLOCKED; preliminary `ChatOracleIT` serial/rollback case exists; simultaneous two-connection stress IT NOT_RUN/incomplete. |
| Descending cursor SQL with chronological response, latest-room ordering, unread count, monotonic read position | BLOCKED; preliminary `ChatOracleIT` covers part of this, further Oracle boundary/timestamp cases NOT_RUN/incomplete. |
| Transaction rollback/commit, cross-room request reuse, restart durability, query plans/performance | BLOCKED; dedicated Oracle IT and restart process NOT_RUN/incomplete. |

After installing Oracle 26ai Free, use a dedicated empty schema, inspect V001–V003 hashes and explicitly run `@db/oracle/apply.sql`; apply the synthetic seed separately. Set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, `ORACLE_CHAT_TEST=YES` only on that isolated migrated/seeded schema. From `bans_chatbot`, run `.\oracleIntegrationTest.cmd`. Never run destructive migration opt-ins against populated data. No Oracle command was run in STEP 08.

## STEP 09 WebSocket/Oracle boundary (2026-11-03 KST)

**BLOCKED: ORACLE_26AI_NOT_INSTALLED.** The real embedded-server STOMP E2E uses `test` profile and in-memory chat/account stores only. It proves no Oracle connection, commit, locking, queue durability or failover.

| Planned Oracle check | Status |
|---|---|
| `ChatOracleIT` (existing explicit `ORACLE_CHAT_TEST=YES`) plus STOMP sender/recipient test against a dedicated migrated and seeded 26ai schema | BLOCKED: ORACLE_26AI_NOT_INSTALLED; STOMP+Oracle test code NOT_RUN/incomplete. |
| Message commit succeeds before user queue event, rollback emits none, broker failure after commit recovered by REST cursor | BLOCKED: ORACLE_26AI_NOT_INSTALLED; fault-injection IT NOT_RUN/incomplete. |
| Two-connection duplicate request, room sequence locking, read monotonicity and reconnect after process restart | BLOCKED: ORACLE_26AI_NOT_INSTALLED; multi-connection/restart IT NOT_RUN/incomplete. |
| Session invalidation/account suspension against Oracle during active socket and attempted SEND/SUBSCRIBE/outbound delivery | BLOCKED: ORACLE_26AI_NOT_INSTALLED; Oracle-backed HTTP/STOMP IT NOT_RUN/incomplete. |

After Oracle installation, review/apply the versioned Oracle migration and synthetic seed to a dedicated empty test schema; set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, and `ORACLE_CHAT_TEST=YES`, then run `.\oracleIntegrationTest.cmd` from `bans_chatbot`. This is an instruction, not a STEP 09 execution result. Multi-instance broker behavior needs separate architecture work; the current SimpleBroker is deliberately local-only.

## STEP 10 document workflow (2026-11-03 KST)

Every real DB result below remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Offline XML parsing, migration hashes, Mockito, MockMvc and synchronized fake concurrency are not Oracle execution.

| Oracle-only check | State / executable condition |
|---|---|
| V004 reviewer column/FK and compatibility with V001–V003 migration/seed, metadata and existing immutable-published triggers | BLOCKED; apply only to a dedicated empty schema and inspect actual 26ai compile/constraints. |
| Mapper create/CLOB/fragment/assignee/grant/revoke/audit mapping | `ManagedDocumentOracleIT.createsAndLocksDraftWithRollback` is compiled and opt-in with `ORACLE_DOCUMENT_TEST=YES`; BLOCKED, NOT_RUN. |
| Two concurrent approvals for the same submitted version, self approval, grant revoked during review, row lock ordering, transaction rollback and audit atomicity | BLOCKED; multi-connection Oracle IT code NOT_RUN/incomplete. Fake two-thread PASS cannot substitute. |
| Existing current published version while a new draft is submitted/rejected, immutable published body/fragment UPDATE rejection, FK and scope checks | BLOCKED; negative Oracle IT code NOT_RUN/incomplete. |
| Fresh ACL read/search after status, department and grant changes, search pagination/plan, large CLOB performance, restart durability | BLOCKED; HTTP/Oracle IT and representative load NOT_RUN/incomplete. |

After installing Oracle 26ai Free, review the V001–V004 SHA-256 manifest and explicitly apply `@db/oracle/apply.sql` to a dedicated empty schema; apply synthetic seed intentionally. Set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, and `ORACLE_DOCUMENT_TEST=YES` only for that migrated/seeded test schema. Run `.\oracleIntegrationTest.cmd` from `bans_chatbot`. Do not run the empty-schema opt-in against populated data. STEP 10 did not execute this command or any JDBC SQL.

## STEP 11 Oracle search backlog (2026-11-03 KST)

All entries are **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; no real JDBC or SQL was executed. Offline XML parsing/manifest and local-mock search are not Oracle PASS.

| Required Oracle check | Current implementation / next execution |
|---|---|
| V005 tag table/FK/indexes, V001–V005 ordered apply, 18-table metadata, generated 20-tag seed | Source and manifest tests PASS offline; real migration/seed BLOCKED. Review hashes, apply explicitly to a dedicated empty 26ai schema, then seed intentionally. |
| Mapper current published version, active account/EMPLOYEE role, department/owner/live READ grant joins, `ai_searchable` and bound query | `DocumentSearchOracleIT` compiled and opt-in via `ORACLE_DOCUMENT_SEARCH_TEST=YES`; actual SELECT/source/revoke BLOCKED. Verify source ID belongs to current version and stale link becomes 404. |
| Fragment CLOB substring/tag/title matching, Korean case/spacing, literal `%`/`_`/quote injection input, excerpt and page ordering | SQL-source parser and fake PASS only; SQL results and linguistic accuracy BLOCKED. Add representative 26ai data and compare plans/results. |
| Tag/current-document B-tree indexes, large CLOB cost, Oracle Text/Korean analyzer availability | Execution plan and performance BLOCKED; Oracle Text choice intentionally NOT_RUN until official 26ai support review and measured analyzer experiment. B-tree does not prove CLOB substring acceleration. |
| Concurrent grant revoke, department change, withdrawal/version publish versus search/source response; snapshot/lock consistency | Multi-connection Oracle IT and HTTP/Oracle tests NOT_RUN/incomplete; real race semantics BLOCKED. Current service rechecks before response but cannot cancel a response after that check. |

After Oracle installation, use a disposable dedicated schema, review the V001–V005 manifest and run `@db/oracle/apply.sql`; separately run `@db/oracle/seed/apply_seed.sql`. Set `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, `ORACLE_DOCUMENT_SEARCH_TEST=YES` and run `.\oracleIntegrationTest.cmd` from `bans_chatbot`. This command is an execution instruction only, **NOT_RUN** in STEP 11. No H2 or substitute search engine is authorized.

## STEP 12 DB-independent provider boundary (2026-11-04 KST)

No table, migration, MyBatis statement or DB access was added or changed. Oracle AI Database 26ai Free remains the final persistent database. All existing V001–V005/seed/Mapper/ACL/lock/transaction/restart/performance cases above remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. STEP 12's passing loopback HTTP and Java policy tests do not change any Oracle result. Existing eight `*OracleIT` classes remain compiled and excluded from normal Surefire; no extra artificial DB test was added for this DB-independent contract.

| Pending check | Execution condition / status |
|---|---|
| Existing connection/schema/seed/auth/organization/chat/document/search Oracle IT | Dedicated installed 26ai schema, intentional migration/seed, existing per-suite opt-ins; `oracleIntegrationTest.cmd`. **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, NOT_RUN. |
| Oracle profile still fails without DB and never substitutes a mock DB | Existing DataSource/profile guards retained. No live Oracle startup/connection attempted in STEP 12. Oracle execution remains BLOCKED. Provider selection in an isolated AI-only context is not an Oracle context boot. |
| Future document-to-provider RBAC+ACL, current version, revoked grants and fragment external permission | Must be implemented through current authorization/read services before accepting document input. DB-backed cross-transaction revoke/read/outbound tests are **NOT_RUN / not implemented**, and DB execution is BLOCKED. No document text was sent in STEP 12. |
| Absence of AI conversation/key persistence | STEP 12 contains no repository or SQL and writes no AI transcript; source plus mock tests only. Actual database inspection remains BLOCKED; existing schema has no AI transcript table. |

After Oracle installation and the previously documented dedicated-schema preparation, run from `bans_chatbot`:

```powershell
.\oracleIntegrationTest.cmd
```

This remains an **unexecuted procedure**, not a PASS result. Leave `bans.ai.provider=mock`; `oracle-it` explicitly rejects live Gemini selection. `gemini-smoke` is a separate opt-in task and must not be combined with Oracle verification. Normal STEP 12 tests made zero Oracle connections and zero real Gemini requests.

## STEP 13 tool ACL/classification/audit Oracle boundary (2026-11-04 KST)

Every real DB item below remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Java/MockMvc/Mockito/XML tests PASS, but no JDBC SQL/transaction was executed. Existing V001–V005, seed SQL and manifest are unchanged. New audit XML uses the existing audit_event table; document search/workspace XML now SELECT existing external_ai_allowed/synthetic_yn/non_sensitive_yn columns. No new DDL or substitute database exists.

| Required Oracle check | Available code / remaining work |
|---|---|
| Current actor account/EMPLOYEE role, department and credential-version refresh for each tool | Existing auth/organization joins reused. Oracle-backed HTTP/session/tool integration is **BLOCKED / NOT_RUN**, including concurrent role/status/department changes. |
| Current approved/PUBLISHED version, live ACL JOIN, exact fragment and classification mappings | New `ai.tool.AiToolOracleIT` is tagged and compiled. With ORACLE_AI_TOOL_TEST=YES it reads seeded external-allowed vacation evidence and denies internally readable external=N evidence through the domain service. Actual 26ai constructor/JDBC/flag mapping is **BLOCKED / NOT_RUN**. |
| Metadata audit INSERT/counts, nullable actor VARCHAR binding and rollback | Same IT exercises the new mapper inside a test transaction and rolls back. Actual INSERT/null binding is **BLOCKED / NOT_RUN**; add null-actor/constraint negative cases. |
| Independent audit REQUIRES_NEW commit, audit database failure and denied-read outcome | Production adapter exists; separate cross-connection and fault-injection IT is **NOT_RUN / not implemented**, DB execution BLOCKED. Current IT's outer rollback is not a REQUIRES_NEW verification. |
| Revoke/withdraw/new publication/classification/account status during multi-tool final read and outbound handoff | Multi-connection Oracle/HTTP IT **NOT_RUN / not implemented**. Deterministic memory tests cover changes before rechecks only. A change after the last recheck remains a race; reads are not claimed to share an Oracle snapshot. |
| CLOB/version-load cost, bounded search/filter underfill, plans, concurrent reads, pool/lock timeouts | Representative Oracle load/plan tests **BLOCKED / NOT_RUN**. No measured performance or durability claim. |
| Prior migration/seed/constraint/trigger/chat/auth/directory/document suites | Remain blocked under their existing execution conditions above. STEP 13 adds no new migration. |

Unexecuted command after Oracle 26ai Free installation, intentional dedicated-schema V001–V005 migration/seed and secure Oracle environment configuration:

```powershell
# From bans_chatbot, only on an isolated migrated/seeded synthetic Oracle test schema.
$env:ORACLE_AI_TOOL_TEST = 'YES'
.\oracleIntegrationTest.cmd
```

The existing runner selects Oracle Failsafe tests; other domain tests retain their own explicit opt-ins. Do not enable empty-schema migration/seed flags against populated data. Keep AI provider Mock and do not combine gemini-live/gemini-smoke with Oracle IT. Normal test/local-mock still have no DataSource/MyBatis auto-start and no mock DB fallback in oracle. Nine Oracle IT classes are now compiled/excluded from ordinary tests. The new server audit is not a model-accessible write tool, and no question/answer/document/tool argument/key is persisted.

## STEP 14 question/evidence Oracle backlog (2026-11-04 KST)

All real Oracle checks remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Final offline build PASS: 196 selected tests, 2026-11-04 16:22:38 KST; this proves no Oracle SQL execution. No DDL/migration/seed/manifest or DB technology changed. Organization/document reads reuse STEP 07/11/13 ports/Mappers; the only additional SQL is the fixed metadata INSERT into existing audit_event in AiQuestionAuditMapper.xml. No AI transcript table is added.

| Pending Oracle check | Code / execution condition / limitation |
|---|---|
| Seeded vacation DOCUMENT and expense-owner STRUCTURED through Oracle services + Mock Provider | New `ai.question.AiQuestionOracleIT`, tagged oracle-integration and ORACLE_AI_QUESTION_TEST=YES. It activates EMP-002 only inside a rollback test transaction, queries existing services, invokes Mock and tests direct audit mapper insert. Compiled, **BLOCKED / NOT_RUN**. |
| Exact expected facts/source IDs, non-exportable source and absent/ambiguous/retired/cross-department rows | Existing service assertions cover memory behavior. Broader Oracle seed/negative/HTTP assertions are **NOT_RUN / not implemented**, DB execution BLOCKED. Current IT only asserts non-UNANSWERABLE for its two baseline queries. |
| Actual organization/document ACL JOIN, current version/classification constructor mapping, CLOB and Oracle predicates | Reused Mapper XML requires actual Oracle execution. **BLOCKED / NOT_RUN**; XML bind/source tests do not establish compatibility. |
| Question audit enum/count-summary INSERT, nullable VARCHAR actor, constraints and no transcript storage | Baseline direct mapper insert code exists; actual INSERT, DB inspection/null-actor/negative cases **BLOCKED / NOT_RUN**. No raw text field exists in new Java event or INSERT. |
| Production REQUIRES_NEW audit durability / audit failure / rollback independence | Separate two-connection fault-injection IT **NOT_RUN / not implemented**. Baseline test deliberately calls direct mapper within outer rollback and does not validate production independent commit. |
| Grant revoke/account-role-department change/publication switch between search, outbound and final response | Multi-connection Oracle + HTTP timing tests **NOT_RUN / not implemented**, execution BLOCKED. Memory/Mockito deterministic rechecks passed; no atomic authorization snapshot or no-race guarantee after final check. |
| Bounded read/query/pool/lock/audit latency, CLOB load, plans and concurrent STOMP behavior | Representative Oracle performance/failure suite **BLOCKED / NOT_RUN**. Ten-second Java stage/HTTP limits cannot interrupt an already blocked JDBC/audit operation. |
| Oracle + actual Gemini E2E | **NOT_RUN**, independent explicit API authorization/Free-tier/model review required; never enable Gemini in oracle-it. Do not combine real AI with this database suite. |

Unexecuted procedure, only after Oracle 26ai Free installation and explicit setup of an isolated dedicated synthetic schema with existing V001–V005 and seed, secure JDBC environment and the established wrapper/cache configuration:

```powershell
# From bans_chatbot. The runner requires ORACLE_JDBC_URL / ORACLE_DB_USER / ORACLE_DB_PASSWORD.
$env:ORACLE_AI_QUESTION_TEST = 'YES'
.\oracleIntegrationTest.cmd
```

Other IT classes retain independent environment opt-ins. Do not set destructive empty-schema migration/seed switches for an existing populated schema. `oracleIntegrationTest.cmd` selects the existing Maven oracle,oracle-it profiles and Failsafe; the new test cannot run from normal test. Ten Oracle IT classes are now compiled/excluded from ordinary tests. Default/test/local-mock remain DB-free, oracle remains explicit and never silently falls back. This planned command was not run in STEP 14.

## Full regression follow-up (2026-11-04 KST)

Offline clean test package **PASS 16:48:29**, 256 selected tests including 60 additional HTTP route/security checks. Packaged local-mock loopback anonymous access checks PASS for all 37 MVC method/path pairs. These results use ephemeral stores and do not resolve any Oracle backlog item. Ten Oracle IT classes remain excluded from normal Surefire. Oracle tests/SQL were not attempted; all actual database compatibility/ACL JOIN/migration/transaction/locking/race/performance checks remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Real Gemini and Oracle+Gemini E2E remain NOT_RUN. No SQL/schema/seed/framework/database change or new integration opt-in was introduced by this regression/Git-ignore maintenance.

## STEP 15 UI Oracle 검증 대기 (2026-11-04)

이번 UI는 기존 도메인 Port/Mapper를 사용하며 SQL/DDL/seed/트랜잭션 설정을 변경하지 않았다. 모든 Oracle 실행 항목은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**다. HTML/MockMvc/메모리·Mock Provider/실제 localhost JS 통합 검사는 실제 Oracle 데이터 검증이 아니다.

| 항목 | 코드/상태 |
|---|---|
| Oracle 프로필의 공용 shell/정적 파일/인증 경계 | `common.web.WorkbenchOracleIT` 추가·컴파일. ORACLE_UI_TEST=YES일 때만 실행. 현재 BLOCKED/NOT_RUN. Oracle banner와 비로그인 API 차단만 예비 검사하며 DB-backed 로그인/UI E2E는 아님. |
| 관리자 코드 발급→활성화→로그인→권한 갱신 | 기존 Oracle auth Mapper 재사용. 실제 잠금·일회성 소비·재발급/경합·만료·세션 무효화는 BLOCKED. 기존 AuthOracleIT와 별도로 HTTP/UI 연속 시나리오 보강 필요. |
| 검색·대화 목록/전송/읽음·문서 workflow·AI 출처 | 기존 Oracle 조직/채팅/문서/AI IT를 Mock Provider로 재실행해야 함. JDBC/CLOB/ACL JOIN/현재 버전/grant race/commit 후 STOMP와 실제 풀 지연은 전부 BLOCKED. |
| 브라우저+Oracle 전체 경로와 실성능 | 테스트 미구현/NOT_RUN, 실제 실행 BLOCKED. Chrome renderer/CDP도 현재 환경에서 BLOCKED이므로 별도 해결 필요. |
| 실 Gemini와 Oracle E2E | NOT_RUN. oracle-it에 실 Gemini를 켜지 않으며 별도 명시적 허가/무료 티어 검토 필요. |

Oracle 설치 및 전용 합성 스키마의 기존 V001–V005/seed 준비 이후에만 실행할 **미실행 명령**:

```powershell
# bans_chatbot, 기존 ORACLE_JDBC_URL / ORACLE_DB_USER / ORACLE_DB_PASSWORD 설정 후
$env:ORACLE_UI_TEST = 'YES'
.\oracleIntegrationTest.cmd
```

기존 domain별 opt-in은 각각 유지한다. Oracle IT 클래스는 총 11개이며 일반 test에서 이름/tag로 제외한다. Oracle 프로필은 DB가 없을 때 Mock으로 전환하지 않는다. 기존 migration/seed destructive opt-in을 운영/기존 데이터 스키마에 적용하지 않는다. 최종 DB와 영속성 판정은 여전히 Oracle 26ai Free 기준으로 대기한다.

STEP 15 최종 offline gate는 17:38:32 KST selected 265 / PASS 264 / SKIPPED 1 / FAIL·ERROR 0이다. 최종 JAR의 39개 localhost 경로 검사도 PASS했다. 이 결과로 Oracle backlog를 완료 처리하지 않는다. 이번 단계 실제 Oracle 연결과 Gemini 호출은 각각 0회다.

## 2026-11-09 live Oracle seed progress

On the dedicated `FREEPDB1` demo schema, `OracleConnectionOracleIT` passed and the canonical `S001__synthetic_company.sql` executed once through JDBC (216 statements, one committed transaction). A fresh connection verified the expected seed counts and document current-version relationship. This changes the live seed application/count gate to **PASS** for that schema. `apply_seed.sql` via SQL*Plus, a second idempotence pass, injected-failure rollback, negative FK/UK/CHECK/trigger tests, concurrency, restart persistence, and Oracle UI authentication remain **NOT_RUN** or pending; earlier STEP-specific blocked entries below are historical.

## 2026-11-09 Oracle UI conversion

The Oracle-profile JAR and HTTPS login page/CSRF endpoint now run against the configured database. A guarded one-time EMP-001 bootstrap is implemented and reached its password prompt, but no password was entered and no account update was committed. Thus actual Oracle authentication, secondary account activation, document/chat workflows, persistence after restart, and full browser UI remain **NOT_RUN**. See [oracle-ui-demo.md](oracle-ui-demo.md) for the operator sequence; earlier no-Oracle BLOCKED entries are historical for this machine.

The first-admin landing-page correction was verified against the live Oracle profile: `GET /` now shows setup guidance and hides the login form while EMP-001 is PENDING. This is **PASS** for the read-only onboarding state only; activation and post-activation UI remain **NOT_RUN**.

The operator-selected local property path is implemented and its no-property Oracle startup was verified. `BANS_ORACLE_FIRST_ADMIN_PASSWORD` remains empty in the Git-ignored file, so live activation, persisted password hash, and sign-in are **NOT_RUN**. On first configured startup, the existing transaction checks the dedicated schema/seed and writes one account update plus metadata audit; later startups skip activation.

## 2026-11-10 administrator authentication result

The operator's local four-character demo password was accepted only under the explicit loopback HTTPS/Mock AI/`BANS_CHAT` short-password opt-in. The first Oracle startup completed the guarded activation; HTTPS login and `/api/auth/me` returned EMP-001 with `ACCOUNT_ADMIN` and a Secure session cookie. A restart with the unchanged property and a repeat login also passed. Thus the first-admin activation/login gate is **PASS** for this dedicated local schema. Secondary account activation, full browser click flow, chat/document/AI Oracle UI operations, and broader Oracle integration cases remain **NOT_RUN**.

## 2026-11-10 synthetic demo password update

All 12 existing seeded accounts are now ACTIVE in the dedicated local Oracle schema, and a direct JDBC BCrypt comparison passed for each. This is **PASS** for database provisioning and local hash verification. The local generated SQL is guarded and Git-ignored. A rerun caused zero new audit rows. Secondary-account HTTP sign-in, normal activation-code workflow, full browser UI, and Oracle chat/document/AI operations remain **NOT_RUN** because the operator asked that the server stay stopped. Earlier PENDING-account backlog entries describe historical pre-provisioning state.

The password mapping was corrected after operator feedback: each password now matches its employee-code number, rather than the account's position in the 12-row list. Ten hashes were rotated in the live Oracle schema; all 12 matches passed and a repeated application added zero audit rows. HTTP sign-in remains **NOT_RUN** while the server is stopped.

The operator's final mapping uses the three-digit suffix alone. All 12 hashes were rotated and verified against the final suffix passwords; the prior prefixed passwords no longer match. Database provisioning is **PASS**. HTTP sign-in remains **NOT_RUN** while the server is stopped.

The `emp001` sign-in report revealed a login identifier mismatch, now corrected in the service and form. A no-listener MockMvc suite passed for four login-code variants; direct Oracle JDBC password verification passed; the Oracle-profile JAR was rebuilt. Live HTTP sign-in with that new JAR remains **NOT_RUN** until the operator starts the server.

2026-11-10 PASS: Oracle demo login and employee directory smoke check against the dedicated schema. Other domain Oracle integration checks retain their prior status; this smoke check does not establish them as PASS.


## Oracle-only 8080 demo (2026-11-10)

2026-11-10 PASS: Oracle-backed login and employee directory on HTTP loopback port 8080. Remaining Oracle integration cases retain their previous status.


## Consolidated local settings (2026-11-10)

2026-11-10 PASS: Oracle local connection sourced from Git-ignored `.env`; login and employee directory smoke checks passed on port 8080. Remaining integration checks unchanged.
