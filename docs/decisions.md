# Decisions

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
