# Project coding rules

These rules apply to STEP 02–18 and subsequent maintenance in this repository.

## Package by domain

- Use `egovframework.example` as the Java base package. Keep `EgovBootApplication` at its root so Spring scans all domain packages. The root contains bootstrap code only.
- Place each new feature under its owning top-level domain: `auth`, `organization`, `chat`, `document`, `ai`, or `audit`. Put its Controller, Service, repository/port, Mapper, DTO, and domain types inside that domain, adding subpackages only when useful.
- Put genuinely shared code in `common`; put application-wide configuration, security setup, and infrastructure health checks in `global`. Do not move domain-specific behavior into either package.
- Never create top-level technology-layer packages such as `controller`, `service`, or `repository`. Avoid circular domain dependencies and unnecessary interfaces, empty classes, or empty directories.
- Keep MyBatis Java Mapper interfaces under their owning domain. Keep XML under the corresponding domain path in `src/main/resources`, with a namespace matching the fully qualified Java Mapper interface. Align MapperScan and XML location when these are introduced.
- Mirror production domain packages in `src/test/java`. Keep Oracle-only tests in the explicitly selected Oracle integration suite, outside ordinary tests.

## Fixed technical and safety boundaries

- Keep Java 21, the resolved eGovFrame 5.0 modules, and the repository's verified Spring Boot 3.5.x combination. Use the Maven wrapper on Windows. Do not upgrade versions without checking repository state and official compatibility evidence.
- Oracle AI Database 26ai Free is the final persistent database. Write Oracle-specific schema, SQL, and MyBatis mapping for Oracle; never change their dialect or add H2 to make tests pass. `local-mock` is nonpersistent and cannot prove Oracle SQL behavior.
- Keep normal tests free of real Oracle connections and Gemini calls. Run Oracle integration tests only through the explicit integration profile and report unavailable Oracle validation as `BLOCKED: ORACLE_26AI_NOT_INSTALLED`.
- Keep secrets and raw AI conversation transcripts out of Git, HTTP responses, logs, and persistent storage. Real Gemini calls require explicit activation and the project's server-side authorization and synthetic-data restrictions.
- Preserve existing work. Update `docs/decisions.md`, `docs/api-contract.md`, `docs/handoff.md`, `docs/test-status.md`, and `docs/oracle-validation-backlog.md` as relevant to each STEP, distinguishing PASS, FAIL, SKIPPED, BLOCKED, and NOT_RUN.

## Current layout

Implemented packages now include `organization` (synthetic fixture and read service), `ai` (offline mock port/adapter), `common` (API contract), and `global` (health/profile configuration). `auth`, `chat`, `document`, and `audit` are created when their first real feature is implemented. `src/test/java` mirrors the production packages; Oracle-only `*OracleIT` cases carry `@Tag("oracle-integration")` and run only through the explicit Failsafe command.
