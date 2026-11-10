
Current procedure (2026-11-10): Oracle profile on loopback HTTP port 8080. The older dated TLS/8443 verification notes below are historical.

# Oracle UI demonstration

This runbook uses the dedicated synthetic `BANS_CHAT` schema in `FREEPDB1`. The 18 tables and canonical seed must already be present. Work from the repository root. No database password, administrator password, or activation code belongs in a command, SQL file, Git-tracked file, or chat message. The explicitly requested local property file is Git ignored and readable only on this machine.

## Prepare once

1. Check that `bans_chatbot/.env` contains the working `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, and `ORACLE_DB_PASSWORD`. If creating it from scratch, use `bans_chatbot/.env.example` as a template. The actual file is Git ignored.
2. Build the Oracle JAR: `./scripts/oracle-demo.ps1 -Action build`. For an already populated Maven cache, add `-Offline -MavenRepository C:\Users\user\.m2\repository` as appropriate.
## Activate the first administrator

Add `BANS_ORACLE_FIRST_ADMIN_PASSWORD=` with a chosen 12–128 character value to the Git-ignored `bans_chatbot/.env`, then start the Oracle server normally. On the first startup only, the application checks the Oracle product, dedicated `BANS_CHAT` user, PDB, table count, seeded records, pending account state, administrator role, and absence of prior activations. It updates only EMP-001 and adds a metadata-only audit event in one transaction. Failure rolls back. Later startups see that EMP-001 is active and leave its password unchanged; the property may then be removed from the local file. The application never prints the property or inserts it into seed SQL or Git. The earlier interactive `bootstrap` script remains an optional alternative, not a step required for this flow.

If the dedicated demo PDB differs from `FREEPDB1`, set `BANS_ORACLE_BOOTSTRAP_PDB` in the same ignored file before the first startup. The bootstrap refuses a schema owner other than `BANS_CHAT`.

## Start and present

Run `./scripts/oracle-demo.ps1 -Action run` and open `http://127.0.0.1:8080/`. While EMP-001 is PENDING and the local property is absent, the page shows the property-setting instruction and hides the login form. After adding the property, restart the server and refresh the page; sign in as `EMP-001` with the configured password. The Oracle profile uses persistent domain repositories, and the AI provider is explicitly `mock`; The session cookie is scoped to loopback HTTP. This Oracle profile uses the database account state.

From the admin UI, issue one-time activation codes for the seeded PENDING accounts needed in the demonstration, such as EMP-005 and EMP-002. Deliver each code locally to its intended demo user and activate through the UI. The 20 seeded employees are not 20 active login accounts: 12 have pending accounts and the remaining employees have no login account. Chat messages, newly authored documents, and approvals that the presenter demonstrates should be created through the UI so the audience can see those changes persist in Oracle.

## Verification status

On 2026-11-09, the Oracle-profile JAR built, the HTTPS server started on localhost:8443, and `/api/auth/csrf` returned a token over verified TLS using the generated certificate. A later build served `/` with the first-admin setup instruction and the login section hidden for the live PENDING account (HTTP 200). The generated localhost certificate was installed in the Windows CurrentUser root store. The interactive bootstrap reached its hidden-password prompt and was stopped before a password was entered, so EMP-001 remains pending. An actual Oracle login and end-to-end UI flow are **NOT_RUN** until the operator enters the password. Browser automation could not bind to the existing Whale window, so visual/click verification is **BLOCKED** by the local computer-use host. The earlier seed and connection checks were **PASS** on the dedicated schema; a running application is not proof that every Oracle mapper and UI operation passes. The 8443 server was stopped after verification.

After the property-based change, the ordinary Oracle profile started with the admin property empty. The live page displayed the Git-ignored property instruction, hid the login form, and no longer displayed the PowerShell password prompt instruction. The full ordinary suite passed 293 tests with 3 skips and no failures/errors; a later focused build passed 5 related tests. No live password activation was attempted. Port 8443 was closed after verification.

### 2026-11-10 update

The operator's four-character local demo value initially failed the default 12-character check, so the Oracle server stopped after startup. The local file now also contains `BANS_ORACLE_ALLOW_SHORT_DEMO_PASSWORD=YES`. This opt-in works only for loopback HTTPS with Mock AI and the dedicated `BANS_CHAT` schema; ordinary activation continues to require 12–128 characters. The first configured startup activated EMP-001. Verified TLS requests successfully logged in, returned EMP-001 with `ACCOUNT_ADMIN` from `/api/auth/me`, and received a Secure session cookie. A restart with the same property and a second login passed. The updated UI loads versioned JavaScript and reports a bad login as a credential error. Full ordinary tests passed 295 with three skips. Visual browser clicks and the remaining two-user feature flow are still not verified.
