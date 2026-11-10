# Architecture — STEP 18 현재 구조

현재는 Java 21/eGovFrame 5.0.0/Boot 3.5.6의 단일 Maven 모듈 `bans_chatbot`이다. 루트 bootstrap은 EgovBootApplication, 업무 코드는 도메인별로 둔다. 공용 shell은 Thymeleaf GET /와 정적 JS/CSS다. 아래 최신 표가 현재 구현이며 이 문서 뒤의 STEP별 설명은 당시 이력이다.

| 도메인 | 현재 경로/책임 |
|---|---|
| auth | Spring Security 서버 세션/CSRF, BCrypt 로그인·활성화/관리자 발급, 매 HTTP/AI 호출의 현재 계정·역할·credentials version. Oracle 최초 관리자 bootstrap은 미구현. |
| organization | 20명/5부서 합성 fixture, 공개 6필드 DTO/검색, 관리자 직원 생성·상태·부서, domain port와 Oracle MyBatis mapper. |
| chat | 두 참여자만 허용하는 REST/순번/idempotency/read, 세 user queue의 STOMP, 현재 session 재검증·code-only 오류, 메모리/Oracle adapter. |
| document | TEXT/제한 Markdown, 작성/버전/검토/명시적 manager 승인, owner/department/grant ACL, 현재 게시 검색/출처, 별도 externalSource 분류. |
| ai | 기본 Mock/명시적 Gemini REST adapter, 고정 endpoint와 호출 제한, 5개 Mock-only 도구, 검토 질문·정형 DTO·정확한 문서 인용, OutboundDisclosurePolicy. |
| audit | 인사·문서·AI 메타데이터, bounded memory와 Oracle mapper/adapter. 대화/키 저장 없음. Oracle 감사 내구성 미검증. |
| common | 응답/오류/requestId/fixture reader와 private data 없는 공용 UI shell. |
| global | profile/DataSource/MyBatis/MVC/Spring Security 구성, health. |

요청 흐름은 세션 필터→현재 actor→Controller→domain service RBAC/ACL→port→선택한 adapter다. 모델/문서/클라이언트 caller는 권한 주체가 아니다. test/local-mock은 DataSource 자동 구성을 제외하고 nonpersistent adapter를 사용한다. oracle은 실제 Thin JDBC와 연결 확인이 필요하고 Mock fallback/자동 DDL/seed는 없다. oracle-it는 oracle과 함께 명시적 Failsafe에서만 사용한다.

Oracle 전용 V001–V005와 manifest는 db/oracle, mapper Java/XML은 각 owning domain에 있다. V005 포함 현재 테이블은 18개다. runtime Oracle guard는 제품명 Oracle 확인까지만 수행하며 정확한 26ai Free banner/기능·락·영속성은 실제 IT/수락 체크리스트가 필요하다. eGov module smoke/SQL source parse는 Oracle 실행 증거가 아니다.

AI 정형 조회는 모델을 쓰지 않는다. 문서 경로는 현재 externalSource+검토 intent→서버가 만든 단일 prompt→Mock 또는 명시적 live provider→exact quote 검사→최종 ACL/version 재검사다. previewTools는 여전히 Mock-only다. 서버는 SQL/쓰기/키를 AI에게 주지 않는다. 실제 인터넷 Gemini는 NOT_RUN이며 무료 계정/모델 확인을 별도 요구한다.

단일 broker/세션 내 한도/500건 UI 등 제한은 [known-limitations.md](known-limitations.md), 설치·운영 분리는 [setup.md](setup.md)를 따른다. 실제 Oracle/Chrome/실 Gemini 포함 운영 검증은 완료되지 않았다.

## 아래는 STEP 02–14 당시의 구조 이력

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

## STEP 12 AI provider addition

The STEP 02–05 sections above are historical, not a current feature inventory; STEP 06–11 implementation is described in `handoff.md`. The new `ai.provider` package owns ChatProvider, keyless Mock, explicitly activated Gemini Developer REST adapter, settings and process-local attempt budgets. It has no DB dependency or HTTP controller. Existing AiAnswerPort remains an offline compatibility API. Live mode requires `gemini-live` and is rejected in test/local-mock/oracle-it. The only current live input type is a server-authored synthetic greeting enum; arbitrary questions and document evidence are not connected. See `gemini-free-tier.md` and the STEP 12 handoff before adding such a path.

## STEP 13 tool flow

`ai.controller` provides the authenticated catalog, Mock preview and minimal same-origin notice/demo page. `ai.tool` validates fixed schemas, enforces bounded runs, composes immutable allowlisted evidence and invokes only Mock; `ai.service.ReviewedSyntheticQuestions` is the conservative example-only question review. `auth.service.CurrentSessionService` reloads the stored SecurityContext/current account before each operation. Organization and document services retain ownership of reads, synthetic/public-field selection and the independent current-fragment external decision. `audit` owns a metadata-only port, bounded memory store and Oracle Mapper/adapter. There is no AI-to-DB technology-layer package or SQL execution tool. Existing Oracle schema/seed/profiles remain unchanged; only existing classification SELECT projections and audit metadata SQL were added. See ai-tool-security.md for the actual boundaries and remaining race/DB validation.

## STEP 14 question use case

`ai.question` owns the separate question policy, single-turn orchestration, bounds, server-minted grounded context and response DTO. `ai.controller.AiQuestionController` exposes authenticated/CSRF-protected question POST, examples and the small protected UI. Organization services supply exact structured facts with no model; document services supply current ACL/publication/external-classified fragments. The Provider gets one reviewed synthetic question/S1/short excerpt, never direct repository access. A strict exact-quote draft is checked before repeating the reads and constructing the final answer/source URL. Shared session budget remains in ai.tool; the new metadata-only audit port stays in audit. No circular repository dependency or top-level technology-layer package is added.

Mock remains default; the new separate grounded method can use live Gemini only behind all existing explicit activation gates. Tool preview remains Mock-only. No arbitrary model tool loop or transcript repository exists. No dependency/profile/migration/seed change. See ai-question-orchestration.md for the source/revalidation/timeout limitations; Oracle execution is BLOCKED and real Gemini E2E NOT_RUN despite offline PASS.
