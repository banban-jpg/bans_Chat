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
