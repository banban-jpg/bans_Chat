# Handoff

## STEP 01

The base server uses eGov 5.0.0 modules, Java 21, and Boot 3.5.6 through the eGov parent. Default profile is `local-mock`. The only endpoint is `/health`. No domain modules, security, repository, SQL, or Gemini integration have been added. Oracle 26ai Free is absent; keep its validation blocked until the server is installed and explicitly configured.

Verification: `clean compile`, direct Java `local-mock` boot, HTTP 200 health, and two manually launched JUnit smoke tests passed offline. Standard Maven `clean test` and `spring-boot:run` failed because required artifacts are absent from the existing local cache; see `test-status.md`. Oracle tests remain blocked. A next STEP can proceed for DB-free features, while the normal Maven test gate needs an approved artifact cache refresh. Do not mark final readiness or Oracle compatibility as verified.

Before each next STEP: inspect tree, Git status, relevant source/config, and these docs. Preserve existing work and perform only the requested STEP. Recheck the offline Maven dependency situation before changing plugin versions. Do not treat `/health` as database readiness.

## Pre-STEP 02 package cleanup (2026-11-02)

The permanent package-by-domain rule is in root `AGENTS.md`. `egovframework.example.EgovBootApplication` remains the scan-root bootstrap. The existing process probe and its test moved to `egovframework.example.global.health`; `/health` and its response remain unchanged. No domain package exists yet because no domain feature has been implemented. Create `auth`, `organization`, `chat`, `document`, `ai`, and `audit` only as code arrives; use `common` only for genuinely shared code. Future MyBatis interfaces and XML follow the owning domain and matching namespace. Eclipse Java settings were aligned with Maven's Java 21 release. No STEP 02 API, security, persistence, or profile implementation was started in this cleanup.

See `docs/test-status.md` for this cleanup's actual build and test results. Oracle validation remains in `docs/oracle-validation-backlog.md`.

The relocated smoke tests passed through a manual cached JUnit launcher, and `local-mock` returned HTTP 200. The normal offline Maven `test` still fails because Launcher 1.12.2 is absent. A clean compile also hits a Java `AccessDeniedException` on cached `reactive-streams-1.0.4.jar`; forked javac prints the exception despite Maven reporting success. Treat clean build and default tests as unresolved gates. Do not change dependency versions or claim Oracle validation to work around these local cache issues.

During verification, the open Eclipse workspace also changed `.project` builder ordering and `.settings/org.eclipse.wst.common.component` generated-source metadata. These files were not intentionally edited for the package move and were left as found; review them separately if preparing a commit.

## STEP 02 handoff (2026-11-02)

Implemented package-by-domain foundation in `organization`, `ai`, `common`, and `global`. `auth`, `chat`, `document`, and `audit` remain uncreated until their first real feature. The organization adapter contains 20 invented employees in 5 invented departments, but there are **no login accounts** yet. Its Controller → Service → port path is exposed only as `/api/demo/employees` under `local-mock`. The AI adapter is a fixed, offline mock, not Gemini integration. `/health` remains compatible; `/api/health` demonstrates the `{data:...}` envelope. Server-generated request IDs, validation handling, public error bodies, profile conflict checks, and Oracle missing-config fail-fast are in place. No DB table, Mapper, SQL, Spring Security session, ACL, WebSocket, document workflow, or real AI request has been implemented.

The ordinary Maven test excludes `*OracleIT` and `oracle-integration`; explicit `oracleIntegrationTest.cmd` invokes Oracle and Failsafe Maven profiles only after required environment variables are present. A preliminary metadata test exists but was not run. The Oracle JDBC artifact is declared only in the explicit Maven `oracle` profile and is not locally cached. Oracle 26ai Free is absent. `docs/oracle-validation-backlog.md` tracks all outstanding DB checks.

DB-free direct Java compilation and 13 JUnit tests passed; final `local-mock` startup and both health HTTP routes passed. Maven `clean test` is **FAIL** in this PC's offline cache: Java zipfs access denied while closing external cached JARs, and subsequent `test` reaches Surefire but cannot resolve JUnit Platform Launcher 1.12.2. See `docs/test-status.md` for commands, times, and scope. Do not call the Maven test gate green. The next STEP may develop DB-free domain features using these boundaries, while restoring the standard Maven gate remains a required verification task. Production security and Oracle compatibility are still unverified.
