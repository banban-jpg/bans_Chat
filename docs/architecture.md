# Architecture — STEP 02

Java base package: `egovframework.example`. `EgovBootApplication` remains at the scan root. Packages follow the owning domain, never a top-level technical layer.

| Package | Current implementation | Planned responsibility |
|---|---|---|
| `auth` | No code yet | Server session, login, roles, per-request identity |
| `organization` | Synthetic `Employee`, read port, in-memory adapter, service, local-mock-only demo Controller | 20 virtual employees in 5 departments; Oracle mapper and secured production directory API later |
| `chat` | No code yet | 1:1 conversations, STOMP, server authorization |
| `document` | No code yet | Markdown/text, DOCUMENT_MANAGER approval, role and resource ACL |
| `ai` | `AiAnswerPort` with offline `MockGeminiClient` | Explicitly enabled Gemini transport after question and document checks |
| `audit` | No code yet | Safe event metadata without secrets or raw AI text |
| `common` | API response/error records, request ID filter, central exception advice | Shared API behavior only |
| `global` | Process health, Oracle profile DataSource, profile guard | Application-wide configuration and infrastructure |

When a secured domain API is added, its Controller calls its Service; the Service enforces authorization and uses a domain-owned port or Mapper. Oracle Mapper interfaces and XML live under corresponding domain paths and use matching fully qualified namespaces. No Mapper or Oracle SQL exists yet, so no placeholder files or scan configuration were created. Domain services must not use arbitrary SQL or AI model output as authorization policy.

## Profiles and persistence boundary

| Spring profile | Behavior |
|---|---|
| `test` | DataSource and transaction-manager auto configuration excluded; in-memory organization fixture and mock AI adapter; no network client |
| `local-mock` (default) | Same DB-free adapters for local demonstration; 20 invented employees, 5 invented departments; process is nonpersistent |
| `oracle` | Explicit Oracle Thin JDBC URL, user, password required from environment; Oracle-only DataSource opens and checks a connection during startup; no mock adapter |
| `oracle-it` | Must accompany `oracle`; profile guard rejects it alone; reserved for explicit DB integration tests |

The Maven `oracle` profile adds `ojdbc17` only to an explicit Oracle build. The ordinary Maven build has no Oracle JDBC driver dependency and makes no Oracle connection. `oracleIntegrationTest.cmd` checks required environment variables and invokes Maven profiles `oracle,oracle-it` with Failsafe. Ordinary Surefire excludes both `*OracleIT` names and the `oracle-integration` JUnit tag. There is no H2 or fallback database. No SQL initializer runs in `test`, `local-mock`, or `oracle`.

The default `/health` reports process health only. `/api/health` demonstrates the `{ "data": ... }` contract, still without database readiness semantics. `/api/demo/employees` is available only under `local-mock` and demonstrates Controller → Service → in-memory domain port with invented records; it is absent in `oracle`. A Spring Security session, chat/document ACLs, WebSocket/STOMP, real Gemini transport, and Oracle schema remain later-step work. The mock AI adapter has no HTTP client and returns a fixed synthetic answer without forwarding the question.

## External AI boundary

Google's [pricing page](https://ai.google.dev/gemini-api/docs/pricing) currently lists free-tier input/output for `gemini-2.5-flash` and `gemini-2.5-flash-lite`, while also stating that free-tier data may be used to improve Google products. [Available regions](https://ai.google.dev/gemini-api/docs/available-regions) includes South Korea. [Rate limits](https://ai.google.dev/gemini-api/docs/rate-limits) vary by account tier and model and must be checked in AI Studio for the actual project; no fixed free quota is assumed. Real Gemini remains disabled. Future activation must make clear that the user's question text can leave the server, and must permit only synthetic, non-sensitive questions plus reauthorized synthetic document segments with `external_ai_allowed=true`.
