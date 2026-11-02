# Architecture — STEP 02

Java base package: `egovframework.example`. `EgovBootApplication` remains at the scan root. Packages follow the owning domain, never a top-level technical layer.

| Package | Current implementation | Planned responsibility |
|---|---|---|
| `auth` | Roles, account status, trusted actor model, role policy, anonymous allowlist, interim MVC session boundary | Spring Security server session, login, current-status refresh |
| `organization` | Synthetic `Employee`, read port, in-memory adapter, service, local-mock-only demo Controller | 20 virtual employees in 5 departments; Oracle mapper and secured production directory API later |
| `chat` | 1:1 participant access policy | Conversation service, STOMP, server authorization |
| `document` | Scope/segment/grant models, grant lookup port, read/search/approval policy | Markdown/text, DOCUMENT_MANAGER approval, Oracle ACL persistence |
| `ai` | Offline mock answer port plus separate disclosure policy and question-review port | Explicitly enabled Gemini transport after question and segment checks |
| `audit` | No code yet | Safe event metadata without secrets or raw AI text |
| `common` | API response/error records, request ID filter, central exception advice | Shared API behavior only |
| `global` | Process health, Oracle profile DataSource, profile guard, MVC security boundary registration | Application-wide configuration and infrastructure |

When a secured domain API is added, its Controller calls its Service; the Service enforces authorization and uses a domain-owned port or Mapper. Oracle Mapper interfaces and XML live under corresponding domain paths and use matching fully qualified namespaces. No Mapper or Oracle SQL exists yet, so no placeholder files or scan configuration were created. Domain services must not use arbitrary SQL or AI model output as authorization policy.

## Profiles and persistence boundary

| Spring profile | Behavior |
|---|---|
| `test` | DataSource and transaction-manager auto configuration excluded; in-memory organization fixture and mock AI adapter; no network client |
| `local-mock` (default) | Same DB-free adapters for local demonstration; 20 invented employees, 5 invented departments; process is nonpersistent |
| `oracle` | Explicit Oracle Thin JDBC URL, user, password required from environment; Oracle-only DataSource opens and checks a connection during startup; no mock adapter |
| `oracle-it` | Must accompany `oracle`; profile guard rejects it alone; reserved for explicit DB integration tests |

The Maven `oracle` profile adds `ojdbc17` only to an explicit Oracle build. The ordinary Maven build has no Oracle JDBC driver dependency and makes no Oracle connection. `oracleIntegrationTest.cmd` checks required environment variables and invokes Maven profiles `oracle,oracle-it` with Failsafe. Ordinary Surefire excludes both `*OracleIT` names and the `oracle-integration` JUnit tag. There is no H2 or fallback database. No SQL initializer runs in `test`, `local-mock`, or `oracle`.

The default `/health` reports process health only. `/api/health` demonstrates the `{ "data": ... }` contract, still without database readiness semantics. `/api/demo/employees` is available only under `local-mock`, now requires an active server session, and demonstrates Controller to Service to in-memory domain port with invented records; it is absent in `oracle`. A Spring Security filter chain, persisted session/login, WebSocket/STOMP, real Gemini transport, and Oracle schema remain later-step work. The mock AI adapter has no HTTP client and returns a fixed synthetic answer without forwarding the question.

## External AI boundary

Google's [pricing page](https://ai.google.dev/gemini-api/docs/pricing) currently lists free-tier input/output for `gemini-2.5-flash` and `gemini-2.5-flash-lite`, while also stating that free-tier data may be used to improve Google products. [Available regions](https://ai.google.dev/gemini-api/docs/available-regions) includes South Korea. [Rate limits](https://ai.google.dev/gemini-api/docs/rate-limits) vary by account tier and model and must be checked in AI Studio for the actual project; no fixed free quota is assumed. Real Gemini remains disabled. Future activation must make clear that the user's question text can leave the server, and must permit only synthetic, non-sensitive questions plus reauthorized synthetic document segments with `external_ai_allowed=true`.

## STEP 04 Oracle source boundary

The 17-table Oracle schema lives in versioned `bans_chatbot/db/oracle/V*.sql` files, separate from application runtime resources. `apply.sql` is an explicit SQLcl/SQL*Plus operation for a dedicated empty Oracle schema. Neither `test` nor `local-mock` executes it; `oracle` still has no automatic initializer. Offline `OracleSchemaSourceTest` checks source structure and SHA-256 hashes. `OracleSchemaOracleIT` is excluded from Surefire and reserved for explicit Failsafe with a real Oracle 26ai server. See `docs/erd.md` and `docs/database-design.md`.

## STEP 03 policy modules

`auth` now owns security roles, account status, the trusted session actor model, role policy, exact anonymous-route policy, and an interim MVC session interceptor. `chat` owns a direct-conversation participant policy. `document` owns scope/segment/grant models, a grant lookup port, read/search/approval policy, and resource-hidden 404 behavior. `ai` owns a separate disclosure policy and question-review port. The review port has no production adapter. `global.security` registers the current MVC interceptor; this does not substitute for the required Spring Security session filter chain. No document, chat, or role mutation HTTP route exists. See `security-policy.md` and `authorization-matrix.md`.

## STEP 05 fixture boundary

The four canonical PSV resources in `src/main/resources/fixtures` are read by `organization.fixture`, `auth.fixture`, and `document.fixture`; the generic line reader alone is in `common.fixture`. `organization.repository.InMemoryEmployeeDirectory` and `document.repository.InMemoryDocumentGrantProvider` are available only in `test`/`local-mock` and hold process-local synthetic records/grants. Planned account records are pending metadata, not an authentication adapter. The Oracle-only, generated seed is a separate manual operation under `db/oracle/seed` and has no runtime initializer. This separation keeps default tests at zero Oracle connections and zero Gemini network calls. See `fixture-and-seed.md`.
