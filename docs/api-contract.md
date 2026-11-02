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
