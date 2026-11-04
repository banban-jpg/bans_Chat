# API contract

## STEP 01 compatibility

`GET /health` returns HTTP 200 and `{"status":"UP"}`. It is a process health probe only.

## STEP 02 common contract

`GET /api/health` returns HTTP 200 and `{"data":{"status":"UP"}}`. Like `/health`, it reports only that the process can answer HTTP; it does not certify Oracle readiness. `/health` keeps its STEP 01 body for existing local checks.

Future JSON API success bodies use `{"data":...}`. JSON API failures use `{"code":"...","message":"...","requestId":"..."}`. The server generates a new UUID for each request and returns it in `X-Request-Id`; a client-supplied `X-Request-Id` is ignored. Validation failures return HTTP 400 / `VALIDATION_FAILED`; missing resources return 404 / `NOT_FOUND`; unsupported methods return 405 / `METHOD_NOT_ALLOWED`; unexpected errors return 500 / `INTERNAL_ERROR`. Error messages are fixed public text and do not include exception messages, rejected values, SQL, secrets, or AI text. A domain `ApiException` may use an explicitly reviewed public message and status.

`GET /api/demo/employees` is available only in `local-mock` and returns `{"data":[{"employeeId":"EMP-001","displayName":"가상직원01","departmentCode":"D01"}, ...]}` for 20 invented people across five invented departments. It is a development fixture, never a persistent or Oracle-backed directory. No login account is created by this fixture.

No production auth, organization, chat, document, or AI HTTP endpoint is exposed in STEP 02. Their later Controllers must use these response records and central handling. Security error integration with Spring Security is still pending because session authentication is not yet implemented.

## STEP 03 authorization contract

Current HTTP MVC requests are anonymous only for exact `GET /health`, `GET /api/health`, `POST /api/auth/activation`, and `POST /api/auth/login`; the two auth routes are reserved and not implemented. Other routes require an active server-side session actor. In particular, `GET /api/demo/employees` is no longer an anonymous demo route; without a session it returns 401.

401 uses `AUTH_REQUIRED`; 403 uses `FORBIDDEN`; a hidden document or conversation uses 404 `NOT_FOUND`; 429 uses `RATE_LIMITED` when a future limiter rejects a request. Each uses `{"code":"...","message":"...","requestId":"..."}` and the server-generated `X-Request-Id`. These public messages are fixed and omit internal reasons. The MVC session check is interim; the Spring Security filter chain and login flow remain unimplemented. There are no document, chat, role-management, or external AI production routes yet.

## STEP 04

No HTTP route or response shape changed. Oracle DDL and manual migration sources are not exposed through an API, and no DB-backed Controller or Mapper was introduced.

## STEP 05 synthetic data

`GET /api/demo/employees` now returns 20 employees distributed 4/3/6/4/3 across five invented departments. Each record also includes `jobTitle`, `extensionNo`, `companyEmail` (reserved `.example` domain), and `dutySummary`. The response header `X-Demo-Data: SYNTHETIC_EPHEMERAL` warns that the data is synthetic and process-local. The STEP 03 session requirement still applies; no login route is available yet. Twelve planned accounts are pending fixture/seed records and cannot authenticate. No document or account API was added. The earlier STEP 02 example describes the historical response before these STEP 05 fields and accounts.

## STEP 06 session authentication

This section supersedes the historical STEP 03 reserved `/api/auth/activation` path and the STEP 05 no-login statement above. The actual route is `/api/auth/activate`. Only exact `GET /health`, `GET /api/health`, `GET /api/auth/csrf`, `POST /api/auth/activate`, and `POST /api/auth/login` allow anonymous sessions. All other HTTP requests require an authenticated server session; the `/api/demo/employees` route still requires one. No admin code-issuance HTTP route exists.

1. `GET /api/auth/csrf` returns `200 {"data":{"headerName":"X-CSRF-TOKEN","token":"..."}}` and establishes the session carrying that token. The browser must retain the session cookie and send the token in the named header on **every POST**, including anonymous activation and login. A missing or incorrect token returns `403 FORBIDDEN` in the common error envelope. Never log or persist this token.
2. `POST /api/auth/activate` accepts `{"employeeCode":"EMP-002","code":"<one-use code>","newPassword":"<12-128 characters>"}`. A valid pending, active employee and unexpired code yield `200 {"data":{"activated":true}}`. An invalid, expired, reused, superseded, or ineligible activation yields `409 ACTIVATION_CONFLICT` with fixed public text. Invalid JSON/field length yields `400 VALIDATION_FAILED`. The response never contains the activation code or password hash.
3. `POST /api/auth/login` accepts `{"employeeCode":"EMP-002","password":"..."}` and returns `200 {"data":{"employeeCode":"EMP-002","departmentCode":"D01","roles":["EMPLOYEE"]}}` for a valid active account. The server rotates the session ID, saves the Spring Security context, and sets the configured idle timeout (default 1800 seconds). Wrong credentials, unknown code, or inactive credentials yield `401 AUTH_REQUIRED`; the fifth wrong attempt and requests during the 15-minute lock yield `429 RATE_LIMITED`. Public errors do not identify which condition applied.
4. `GET /api/auth/me` returns the same current identity view under `data` for a valid session. Missing, disabled, or credential-version-stale sessions return `401 AUTH_REQUIRED`; the stale session is invalidated. Roles are loaded from the account repository at request time, not trusted from the old session.
5. `POST /api/auth/logout` requires a valid session and CSRF token. It invalidates that session and returns `200 {"data":{"loggedOut":true}}`. A following `/api/auth/me` returns 401.

All errors retain the server-generated `X-Request-Id` and `{"code","message","requestId"}` contract. `403 FORBIDDEN` also applies when a protected operation is denied or CSRF fails; `409` is reserved here for activation conflicts. Session cookies are HttpOnly and SameSite=Lax. The `oracle` profile sets Secure=true and therefore requires HTTPS at the browser boundary; local HTTP `test`/`local-mock` set Secure=false for development. Activation issuance is an internal administrator service operation only, with out-of-band delivery pending. No reset-password, account-management, or real AI endpoint is included in STEP 06.

## STEP 07 organization API (2026-11-03)

All six routes require a real Spring Security server session and a freshly reloaded active employee. Mutation routes additionally require `ACCOUNT_ADMIN` in the service and a CSRF token. Responses retain the existing `{data: ...}` envelope. Department identifiers are business codes (`D01`–`D05` in the fixture), and employee path IDs are employee business codes (`EMP-001`). They are never internal Oracle identity values.

| Method / path | Input / response |
|---|---|
| `GET /api/employees` | `name`, `departmentId`, `task` default empty; `page=0`, `size=20`, `sort=employeeId`. Returns `{items,total,page,size}`. |
| `GET /api/departments` | Active departments as `{departmentCode,departmentName}`. |
| `GET /api/tasks/owners?query=` | Literal substring search of employee `duty_summary`, not `work_task` assignments. Returns the same page DTO, page 0, size 100; `total` signals truncation. Use employee search `task` plus paging to retrieve more. |
| `POST /api/admin/employees` | Strict JSON fields: `employeeId,displayName,departmentId,jobTitle,companyEmail,extensionNo,task`; 201 `{data:true}`. No login account/role is created. |
| `PATCH /api/admin/employees/{id}/status` | Strict JSON `{status: ACTIVE|SUSPENDED|DEPARTED}`; 200 `{data:true}`. This is employment status, not credential/account status. |
| `PATCH /api/admin/employees/{id}/department` | Strict JSON `{departmentId: D02}`; 200 `{data:true}`. |

Each employee item has exactly six fields: `employeeId,displayName,departmentCode,jobTitle,companyEmail,extensionNo`. No duty text, status, salary, personal contact, bank details, credentials or roles are returned. Search text is literal, case-sensitive, at most 100 characters; SQL metacharacters are data. `departmentId` is empty or `D` plus two digits. Page is 0–10000; size 1–100. Sort accepts only `employeeId`, `name`, `departmentId`, ascending, with employee code as tie breaker. Unknown sort expressions and malformed numeric pagination return 400. Departed employees are excluded; suspended employees remain directory-visible but cannot authenticate.

Create limits: code `EMP-` plus 3–10 digits, name/title at most 100, task at most 300, reserved synthetic `@bans.example` email at most 200, extension digits/hyphens at most 10. All fields are required. Unknown mutation fields (including `roles`, `passwordHash`, `accountStatus`) return 400. No role assignment route or privilege promotion is provided. Duplicate code/email returns 409, nonexistent target 404, unknown/inactive destination department 400, anonymous 401, non-admin 403. These routes use the existing generic sanitized error body.

A committed department change is reflected by current-account reload on the next HTTP request; resource ACL must use that refreshed actor. Suspension/departure invalidates an existing session on its next protected request and rejects new login. No authorization decision cache is introduced. Already-running requests are not cancelled. Role changes, WebSocket/STOMP revalidation and transactional ACL for future document operations remain later work.

Example (authenticated session, with CSRF header for writes): `GET /api/employees?departmentId=D01&page=0&size=2&sort=employeeId`; `GET /api/tasks/owners?query=지원`; `PATCH /api/admin/employees/EMP-002/department` with `{"departmentId":"D02"}`. The legacy local-mock demo route remains a static fixture demonstration; use the new routes for mutable directory behavior.

Under `local-mock`, the six organization routes label successful responses `X-Demo-Data: SYNTHETIC_EPHEMERAL`. This header does not imply database persistence.

## STEP 08 chat REST contract

All paths use a real server session; writes require CSRF. `POST /api/chat/rooms` accepts `{recipientEmployeeId}` and returns `{data:{conversationId,firstEmployeeId,secondEmployeeId}}`, reusing an existing sorted pair. Recipient must have an active account; self-chat and malformed IDs return 400. `GET /api/chat/rooms` returns recent activity first, with `{roomId,otherEmployeeId,lastRoomSeq,lastReadSeq,unreadCount}`. `GET /api/chat/rooms/{id}/messages?beforeSeq=0&size=20` returns `{items:[{roomSeq,senderEmployeeId,content}],nextBeforeSeq}`; `beforeSeq=0` means latest, otherwise strictly older; response is chronological, page size 1–100. The next cursor is the first returned sequence, or zero for empty. `POST /api/chat/rooms/{id}/messages` accepts `{clientRequestId,content}` and returns the saved message. Request ID is 1–80 ASCII letters/digits/`.`/`_`/`:`/`-`; content is nonblank, at most 2000 Unicode code points, with NUL rejected. Retrying the same sender ID/request ID/content in the same room returns the original message; changed content or room returns 409. `PUT /api/chat/rooms/{id}/read-state` accepts `{lastReadSeq}` and returns the effective monotonic position; beyond room head returns 400. Unread count counts only the other participant's messages after that position. Outsiders receive 404 for room-specific routes and no room-list entry. `local-mock` responses carry `X-Demo-Data: SYNTHETIC_EPHEMERAL`; they are not persistent. Message content is chat data, never AI transcript storage; do not send real personal/company data in this synthetic portfolio.

## STEP 09 STOMP chat contract (2026-11-03)

A same-origin authenticated HTTP session may upgrade `GET /ws/chat` to WebSocket. STOMP `CONNECT` must include the existing server-session CSRF token in the native `X-CSRF-TOKEN` header; obtain it from `GET /api/auth/csrf`. The server derives employee identity from the stored HTTP SecurityContext and reloads status, roles and credentials version on CONNECT, SEND and SUBSCRIBE. A client-supplied sender ID is ignored. The initial single-server broker accepts only `/app/chat.send`, `/app/chat.read` for SEND and `/user/queue/chat.messages`, `/user/queue/chat.ack`, `/user/queue/chat.errors` for SUBSCRIBE. No public `/topic/**` destination is configured or accepted.

`SEND /app/chat.send` JSON: `{roomId,clientRequestId,content}`. It calls the STEP 08 `ChatService`; after the repository returns, the other participant receives `{roomId,roomSeq,senderEmployeeId,clientRequestId,content}` on `/user/queue/chat.messages` and the sender receives `{roomId,roomSeq,clientRequestId,lastReadSeq:-1}` on `/user/queue/chat.ack`. Duplicate retries retain the same `roomSeq`; the client deduplicates by `(roomId,roomSeq)` and its own `clientRequestId`. A changed-content retry produces a sanitized code on `/user/queue/chat.errors` with no payload reflection. `SEND /app/chat.read` JSON: `{roomId,lastReadSeq}`; the actor receives `{roomId,roomSeq:-1,clientRequestId:null,lastReadSeq:<effective>}` on ack. Room membership and request bounds use the same service as REST.

On reconnect, the client obtains missing messages with `GET /api/chat/rooms/{id}/messages?beforeSeq=0&size=...`, then continues paging backward using `nextBeforeSeq`; deduplicate by room/sequence before showing messages. The broker is process-local and does not replay messages. HTTP logout or an ineligible account prevents subsequent inbound frames and outbound chat messages on an existing socket. Socket cleanup can be asynchronous. Errors from rejected CONNECT or forbidden STOMP destinations are protocol errors; business validation errors use `/user/queue/chat.errors`. This STEP supplies an embedded-server test client, not a browser UI.

## STEP 10 document workflow (2026-11-03)

All paths require the STEP 06 active server session. Mutations require CSRF. Success uses `{data:...}` and sanitized errors retain the common envelope. Document code is `DOC-` plus 1–32 ASCII letters/digits/hyphens. `scope` is `INTERNAL`, `DEPARTMENT`, or `SELECTED`; the Oracle storage mapping is respectively existing `COMPANY`, `DEPARTMENT`, `PRIVATE`. For DEPARTMENT, `departmentId` must equal the editor's current department; other scopes omit it. The first version and each added version use `TEXT` or `MARKDOWN`, nonblank body up to 20,000 UTF-16 units, `aiSearchable`, and `externalAiAllowed`. Client-authored `externalAiAllowed=true` is rejected; the server does not certify user input as synthetic. The only supported Markdown rendering is escaped text with `#`/`##` heading lines; links, raw HTML and remote images remain literal text, never fetched or emitted as elements.

| Method / path | Contract |
|---|---|
| `POST /api/documents` | Editor creates `{documentCode,title,category,scope,departmentId,format,body,aiSearchable,externalAiAllowed}`; returns code. Categories follow existing Oracle CHECK values. No published content yet. |
| `POST /api/documents/{code}/versions` | Owner/editor adds a new draft version; one pending version at a time. Prior published version remains current. |
| `PUT /api/documents/{code}/versions/draft` | Owner/editor edits only a DRAFT version. Published versions and submitted reviews cannot be edited. |
| `POST /api/documents/{code}/review-request` | Owner/editor specifies `{reviewerEmployeeId}`. Reviewer must be a current active DOCUMENT_MANAGER with explicit READ and APPROVE grants; self-review is rejected. |
| `POST /api/documents/{code}/approve` and `/reject` | Only assigned DOCUMENT_MANAGER with current READ+APPROVE grants, never the owner. Approval publishes the submitted version and changes the current pointer; rejection discards the submitted version and retains an earlier published pointer. |
| `POST /api/documents/{code}/withdraw` | Owner/editor archives a document without modifying immutable published versions. Current read/search becomes hidden. |
| `POST` / `DELETE /api/documents/{code}/grants` | Owner/editor adds or revokes `{employeeId,permission: READ|APPROVE}`. Revocation takes effect at the next service read/approval; it is not cached. |
| `GET /api/documents/{code}/published` | Returns current published `{documentCode,title,versionNo,format,renderedHtml,aiSearchable}` only after fresh resource ACL. Hidden/withdrawn documents return 404. |
| `GET /api/documents/search?q=&page=&size=` | STEP 11 contract below replaces the old `query` response. An internal `externalCandidates` path independently requires the external flag and fresh ACL; no Gemini transport exists. |

INTERNAL is readable by active employees, DEPARTMENT by the current department, SELECTED only by owner or explicit READ grant. ACCOUNT_ADMIN has no document override. Owner/editor can manage grants but cannot approve their own document. Current-account/department refresh follows STEP 06/07. `local-mock` responses label `X-Demo-Data: SYNTHETIC_EPHEMERAL` and lose changes on restart.

## STEP 11 document evidence search (2026-11-03)

`GET /api/documents/search?q=휴가&page=0&size=20` requires an active employee session. `q` is required, nonblank, 1–100 Unicode code points, treated as a literal case-sensitive substring; `%`, `_`, quote and SQL-looking text are data. `page` is 0–100, `size` is 1–50; default page 0 and size 20. The response is `{data:{items:[{documentId,versionId,fragmentId,title,excerpt,updatedAt}],page,size,hasMore}}`. Each excerpt is at most 240 Unicode code points, plain text. Results are ordered by latest document update then stable IDs in Oracle; the deterministic fake orders by synthetic version ID. The service rechecks each candidate against the current published pointer, `aiSearchable`, active actor and document ACL before returning it. A grant revoked or a document withdrawn before this recheck removes the hit. A concurrent change after the recheck is a remaining race until Oracle transaction validation. Empty searches and oversized/invalid pagination return 400. This response never includes `externalAiAllowed` and does not authorize Gemini transfer.

`GET /api/documents/{id}/versions/{versionId}/fragments/{fragmentId}` returns `{data:{documentId,versionId,fragmentId,title,content,updatedAt}}` only if that exact fragment is still in the current published, internally searchable version and the actor can currently read it. Otherwise it returns 404, including stale version IDs, withdrawn documents and revoked grants; anonymous requests return 401. `content` is source text in JSON, not trusted HTML. Clients must display it as text, or use the STEP 10 sanitized rendering path, and must never insert it via `innerHTML`. A search hit's IDs form the link; the client cannot choose an employee ID to override the session.

Document creation accepts optional `tags` (0–8 distinct strings of 1–32 letters, digits, underscore or hyphen). Tags belong to document metadata, not individual versions; the synthetic seed assigns each document its category as a tag. Title, category/tag and current fragment body are searched. One fragment per version is currently produced by the STEP 10 editor and fixture. Draft edits update its fragment; a newly approved version changes the current pointer. During review of a new draft, the prior published pointer remains searchable. Withdrawal and grant revocation are excluded by both search and link queries. `local-mock` search is process-local and does not prove Oracle `DBMS_LOB.INSTR`, index use or Korean analysis. Oracle Text is deferred pending official 26ai capability review and actual analyzer/plan experiments.

## STEP 12 internal provider contract (2026-11-04)

No production HTTP endpoint was added. Existing auth/chat/document routes and SQL are unchanged. `ai.provider.ChatProvider.chat(SessionActor, SyntheticPrompt)` is an internal DB-independent single-turn contract. It accepts only `HELLO_KO` or `HELLO_EN` and requires an active EMPLOYEE actor. Result text is transient, omitted by Jackson and redacted in toString; it is not an HTTP conversation DTO. A future controller must not accept client-supplied actor/safety/ACL flags or use the legacy string port to reach a live provider.

`MockGeminiProvider` returns the existing fixed synthetic response. The default provider is always Mock; a key alone has no effect. Live `GeminiProvider` requires explicit configuration, `gemini-live` and transfer/free-tier review. It sends only a fixed synthetic greeting, no employee identifiers/history/documents/tools, to the fixed Developer endpoint using the `x-goog-api-key` header and bounded generation configuration. Arbitrary headers, request URLs and models cannot be chosen by an HTTP client. See `gemini-free-tier.md` for operator settings, the exact model review, SDK decision and the separately gated one-call smoke procedure.

Public provider failures use the existing `{code,message,requestId}` advice. Codes are `AI_TRANSFER_DENIED` (403), `AI_BUSY` / `AI_QUOTA_EXHAUSTED` / `AI_COOLDOWN` / `AI_UPSTREAM_RATE_LIMITED` (429), `AI_UPSTREAM_FORBIDDEN` / `AI_UPSTREAM_UNAVAILABLE` (503), `AI_TIMEOUT` (504), and `AI_INVALID_RESPONSE` (502). The message is fixed `AI request unavailable.`; no upstream response, key, cause or Retry-After header is forwarded. A bounded numeric retry delay is available only internally. No REST route exposes these provider operations in STEP 12. The test-only error probe is under src/test and is absent from the packaged server.

**Future user notice:** questions themselves can leave the server when real AI is enabled. Only synthetic, non-sensitive input and separately authorized, freshly rechecked external-allowed synthetic document segments may be transferred. STEP 12 does not authorize arbitrary questions or document transfer and does not add a production question-review adapter.

## STEP 13 read-only Mock tool preview (2026-11-04)

The STEP 12 no-HTTP statement describes that earlier step. STEP 13 adds the three protected routes below. Live Gemini still cannot receive a tool payload. All identities come from the actual Spring Security session plus current account reload, never a body/query caller ID, role, department or old request attribute. Existing error/data/request-ID and CSRF contracts remain in force.

| Method / path | Contract |
|---|---|
| `GET /api/ai/tools` | Active session. Returns `{notice,mockOnly:true,examples:[{id,text}],tools:[{name,inputSchema}],limits:{maxInputBytes:8192,maxCalls:4,maxRowsPerTool:5,maxRowsTotal:20,requestsPerMinute:5,maxRunSeconds:10}}` under data. Examples are fixed server-reviewed synthetic text, not user conversation history. |
| `POST /api/ai/tools/preview` | Active session + CSRF + application/json. Accepts the bounded plan below. Returns only `{mock:true,toolCalls,returnedItems,notice}` under data; no question/answer, employee results or document source text is reflected in HTTP. |
| `GET /ai/tools` | Active session. Small same-origin HTML demo: transfer notice, unchecked acknowledgement, fixed vacation example and Mock result count. No arbitrary question box or raw result rendering. |

Request example, using an already authenticated session and CSRF token:

```json
{"questionId":"VACATION_HELP","question":"가상 회사의 휴가 신청 안내를 찾아주세요.","externalTransferAcknowledged":true,"calls":[{"name":"searchDocuments","arguments":{"query":"휴가","limit":3}}]}
```

`questionId` and question must exactly match a server-reviewed example. A checkbox, `synthetic` claim or masking regex cannot certify arbitrary input. Missing acknowledgement or a changed question yields 403 `AI_QUESTION_NOT_REVIEWED` with a fixed transfer notice. The service notice says questions themselves may leave the server if real AI is later enabled; real business/personal/confidential data is forbidden. No real tool transfer is enabled here.

Tools are exactly `searchEmployees` (`name`, optional limit), `findDepartment` (no functional arguments, current department only), `findTaskOwner` (`query`, optional limit), `searchDocuments` (`query`, optional limit), `getDocumentExcerpt` (positive integral documentId/versionId/fragmentId). Query/name length is 1–80 Unicode code points and no control characters; limit is an integer 1–5 (default 5). Schema and runtime reject unknown functional fields, mutation/SQL/salary tools, invalid numeric types, duplicate/trailing JSON, excessive nesting and bodies above 8192 bytes. Known `callerId,userId,employeeId,role,roles,departmentId` claims are discarded before validating functional inputs, not treated as authority or advertised as useful arguments. Plans contain 1–4 calls, at most 5 items per tool / 20 total and a 24KiB Mock payload.

Employee output to Mock has exactly the six STEP 07 public fields and must match canonical invented records; current department output has code/name only. Documents reuse STEP 11 ACL/current source reads but additionally require current PUBLISHED, approved/current-review document state, ai_searchable and trusted external/synthetic/non-sensitive flags. Mock evidence has only documentId/versionId/fragmentId/excerpt, excerpt at most 200 code points, no title or internal hit counts. Search filters non-exportable hits silently; a direct source denied for any such reason returns 404 `NOT_FOUND`. Internal readability does not imply exportability.

POST attempts are limited to five per rolling minute and one active run per HttpSession. Service reads are checked against a 10-second execution budget (not a socket body-read timeout). Each tool reloads current session/account state; reads are repeated at the final Mock boundary. Results are ephemeral immutable copies. Revoke/status/version tests prove the Java/mock behavior only; a concurrent change after the final recheck remains a race pending Oracle validation.

Additional safe error codes: 400 `AI_TOOL_INPUT_INVALID`, 429 `AI_TOOL_LIMIT`, 403 `AI_TOOL_LIVE_DISABLED`, 503 `AI_AUDIT_UNAVAILABLE`. Authentication remains 401; malformed/missing CSRF is 403. No rejected text/upstream cause/key is returned. Audit records only actor, fixed event/type/outcome and bounded call/item counts. See `ai-tool-security.md` for schema/projection, execution, audit and database limits.

## STEP 14 question answer API (2026-11-04)

| Method / path | Contract |
|---|---|
| `POST /api/ai/questions` | Active current session + CSRF + application/json; `{question}`. Returns the data envelope below, no-store. |
| `GET /api/ai/questions/examples` | Active session; `{notice,questions:[string]}` under data, fixed reviewed synthetic examples, no-store. |
| `GET /ai/questions` | Active session; small example selector, unchecked notice acknowledgement, textContent rendering and current source links. No browser conversation storage. |

```json
{
  "data": {
    "answer": "확인 가능한 권한 내 근거가 없어 답변할 수 없습니다.",
    "answerType": "UNANSWERABLE",
    "structuredFacts": [],
    "sources": [],
    "requestId": "<server UUID matching X-Request-Id>"
  }
}
```

answerType is STRUCTURED, DOCUMENT or UNANSWERABLE. STRUCTURED contains only canonical synthetic EmployeeView values: employeeId, displayName, departmentCode, jobTitle, companyEmail, extensionNo. The server constructs exact facts; no Provider call is made. DOCUMENT contains one Source `{documentId,versionId,fragmentId,title,url}` with server-created same-origin `/api/documents/{documentId}/versions/{versionId}/fragments/{fragmentId}` URL; that existing route reauthorizes every access. Answer text is a server prefix plus a validated current excerpt, never arbitrary model prose. UNANSWERABLE has empty arrays and a fixed non-disclosing message. Unknown/unreviewed questions, absent/multiple/retired/unauthorized/non-exportable evidence, changed grants/version/content and invalid model citations do not produce facts or sources.

At most 2048 input bytes and 200 question Unicode code points; no blank/control-character text, duplicate keys/trailing JSON or unknown functional fields. Known identity claims are ignored, never authoritative. Only exact reviewed examples plus canonical `<synthetic employee name> 연락처` are processed; `출장비 담당자` uses the server-reviewed 정산 duty mapping. Arbitrary question input is not sent to any classifier. The notice explicitly warns that a question itself may be sent to Google and prohibits actual business/personal/confidential input. The UI confirmation is an informational step; the service uses the reviewed-intent policy and current ACL/classification for authorization. No extra consent field is part of this POST contract.

Questions share five requests/rolling minute and one active run per session with previewTools. Maximum eight stages/four reads/one Provider call, ten-second stage checks, at most 200 excerpt code points and min(configured,256) generated tokens. Source and identity are rechecked after generation. Live generation requires all existing explicit gemini-live gates and is forbidden in test/local-mock/oracle-it. previewTools stays Mock-only. No history, streaming, query/answer persistence or model write tools.

Additional safe errors are 400 `AI_QUESTION_INPUT_INVALID` and 429 `AI_ORCHESTRATION_LIMIT`; shared `AI_TOOL_LIMIT`, `AI_AUDIT_UNAVAILABLE` and STEP 12 sanitized Provider errors remain. In particular upstream 429 becomes 429, unavailable/forbidden becomes 503 and timeout becomes 504, without upstream body/header/key/cause. These errors do not alter chat REST/STOMP contracts. Audit contains only fixed actor/model/type/outcome/count/latency metadata. See ai-question-orchestration.md for the exact outbound and race boundary. The transient approved answer DTO is the explicit question API output, not raw conversation echo.

## Full route verification (2026-11-04)

No API contract changed during the maintenance check. The explicit RouteAccessMockMvcTest inventory matches all 37 production MVC method/path mappings in local-mock (17 GET, 20 mutation). Anonymous health/CSRF GETs succeed, login/activation empty requests return 400, and all 32 protected routes return 401 when CSRF is valid. All 20 mutation paths return 403 without CSRF even with a valid authenticated session. Authorized reads and documented document transitions pass; authenticated unknown API returns 404 and POST to GET-only /api/health returns 405. These are expected access-control outcomes, not promises that anonymous callers can access every API.

The repackaged local-mock server was also checked over actual loopback HTTP for the same 37 anonymous statuses. Authenticated business/ACL scenarios use the full MockMvc chain; STOMP uses the existing embedded-server E2E tests. Oracle-backed access, deployed-environment routing and real Gemini remain separate pending validation. See test-status.md for commands/results and exclusion details.
