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
