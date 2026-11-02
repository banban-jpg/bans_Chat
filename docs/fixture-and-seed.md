# STEP 05 synthetic company fixture and Oracle seed

## One logical data set, two separate adapters

`bans_chatbot/src/main/resources/fixtures/*.psv` is the canonical, invented data set. Small readers under the owning `organization`, `auth`, and `document` packages load it for offline tests and `local-mock`. The in-memory employee directory and document grant provider are process-local; restart may reset them. Neither adapter is an Oracle repository or evidence that SQL ran. The local-mock employee API labels its response `X-Demo-Data: SYNTHETIC_EPHEMERAL`. It still requires the interim server session; STEP 06 must implement actual activation/login before interactive access.

| Resource | Contents |
|---|---|
| `departments.psv` | 5 invented departments: HR 4, finance 3, development 6, sales 4, management support 3. |
| `employees.psv` | 20 invented employee codes, names, titles, extensions, duty summaries and reserved `@bans.example` email addresses. |
| `accounts.psv` | 12 planned login IDs and roles. All Oracle rows begin `PENDING`; no password or activation token is in Git. |
| `documents.psv` | 20 text/Markdown documents: 16 published/approved, 2 rejected submissions, 2 discarded/archived; company/department/private scopes, named employee READ/APPROVE grants, one fragment each. Categories cover vacation, expenses, deployment, equipment, contracts, onboarding and security. |

Security examples DOC-007 and DOC-014 are wholly invented but classified `nonSensitive=N` and `externalAiAllowed=N`. Only three published fragments are marked `externalAiAllowed=Y`; this is metadata for later safety checks, **not** permission for a real Gemini request. Approval requires the STEP 03 `DOCUMENT_MANAGER` role and a resource grant. Accounts remain unusable until STEP 06 issues a random activation code and stores a hash. No fixed administrator password, code, or real company contact is present.

## Oracle-only seed execution after installation

`bans_chatbot/src/test/java/egovframework/example/global/config/SyntheticOracleSeedRenderer.java` deterministically renders `db/oracle/seed/S001__synthetic_company.sql` from the PSV resources. The checked-in SQL is compared byte-for-byte in the ordinary test. The SQL is Oracle-only `MERGE` plus guarded updates for publication/current version. It is deliberately separate from V001/V002 migrations and never runs during Boot startup or normal tests.

1. Install Oracle AI Database 26ai Free and create a dedicated, disposable **synthetic** application schema. Use a schema owner with only its own DDL/DML privileges. Do not use `SYS`/`SYSTEM` or a schema containing real or shared data. Confirm V001/V002 are the intended, not-yet-applied revision from `manifest.sha256`; then run `@db/oracle/apply.sql` interactively in SQLcl/SQL*Plus as described in `database-design.md`.
2. Before seed execution, inspect the SQL, verify that the schema has the 17 expected tables/five roles and that any existing accounts are only `PENDING`. Connect interactively without putting the password in Git, a command line, or a saved SQL file. From `bans_chatbot`, run `@db/oracle/seed/apply_seed.sql`.
3. The runner checks the schema, applies the idempotent `MERGE` statements, verifies exact counts, and commits once. A SQL error exits with rollback. Rerun is intended only while accounts are still pending and fixture rows have not been edited independently. It does not repair conflicting existing data; inspect and resolve a conflict manually in the dedicated schema. There is no DROP, TRUNCATE, reset, or automatic retry.
4. Record the V001/V002 hash, seed source revision, Oracle version/service, time and result in a local deployment log without credentials. Run the explicit Oracle integration suite only against a dedicated test schema. `ORACLE_SYNTHETIC_SEED_TEST=YES` opts the seed IT into DML and rollback on an **empty migrated** schema; it must not be set for populated application data.

The Oracle seed has **not** been run. Offline parsing and byte equality do not prove Oracle syntax, FK behavior, trigger behavior, idempotence, or rollback. Those remain `BLOCKED: ORACLE_26AI_NOT_INSTALLED` in the backlog. No H2 conversion exists.
