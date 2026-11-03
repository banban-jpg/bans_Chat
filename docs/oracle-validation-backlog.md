# Oracle 26ai validation backlog

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
