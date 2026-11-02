# Test strategy — STEP 02

Use `bans_chatbot/mvnw.cmd` on Windows with Java 21. Normal `test` is the DB-free gate: JUnit, Mockito, MockMvc and Spring profile wiring under `src/test/java`. All such tests use the `test` or `local-mock` Spring profile, require no JDBC URL or Google key, and must make zero Oracle/Gemini network calls.

Ordinary Surefire excludes `**/*OracleIT.java` and the `oracle-integration` JUnit tag. The explicit Maven `oracle-it` profile runs Failsafe with that class pattern and tag and fails if no integration test is found. The current preliminary `OracleConnectionOracleIT` is compiled with the other test sources but is never executed by normal `test`. This tag-based separation uses existing Maven infrastructure; it avoids adding a build-helper plugin and an unresolvable offline source-set dependency. Failsafe execution remains blocked until the server and dependencies are available. A configured task is not a passed database test.

Commands from the module directory:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean test
.\oracleIntegrationTest.cmd
```

The Oracle command must be invoked explicitly after Oracle AI Database 26ai Free, `ORACLE_JDBC_URL`, `ORACLE_DB_USER`, `ORACLE_DB_PASSWORD`, Oracle JDBC artifact, and Failsafe artifact are available. It executes `mvnw.cmd -Poracle,oracle-it verify`; no automatic or implicit Oracle validation occurs. There is no schema yet, so the preliminary integration case only checks JDBC metadata and major version 26. Schema, constraints, Mapper SQL, migration, transaction, lock, concurrency, and performance checks are pending.

STEP 03 adds pure policy tests with fake session actors and Mockito grant/question-review ports, plus MockMvc tests for the current MVC boundary and fixed HTTP errors. These prove Java policy behavior only. Once Oracle ACL tables and Mappers exist, explicit Oracle IT must verify grant/revocation joins and status freshness. The required Spring Security session chain and CSRF/STOMP tests are separate pending work; a passing MVC interceptor test does not validate them.

STEP 04 adds `OracleSchemaSourceTest` to the ordinary DB-free Surefire suite. It checks 17 table names, migration version/hash consistency, FK target names, constraint names, prohibited transcript fields, manual runner order, and the narrow JDBC statement splitter. It is a source/splitter check, **not** an Oracle SQL syntax parser. `OracleSchemaOracleIT` is compiled with test sources but excluded from normal `test`; explicit Failsafe can exercise empty-schema application (only with `ORACLE_EMPTY_SCHEMA_MIGRATION_TEST=YES`), metadata/repeat guard, UNIQUE/FK/CHECK rejection, and DML rollback once Oracle is installed. Published-version trigger behavior, authorization joins, and performance need further Oracle tests. The older STEP 02 statement above that there was no schema describes that earlier stage; STEP 04 now has unapplied SQL sources.

This PC's offline cache lacks parent-managed JUnit Platform Launcher 1.12.2 and Failsafe 3.5.4, and javac sometimes raises `AccessDeniedException` while closing cached JARs outside the workspace. The temporary, ignored `target/deps` copy and direct `javac`/JUnit launcher run in `docs/test-status.md` provide independent DB-free verification; they do not turn Maven `clean test` into PASS. Do not change the project's framework or database dialect to mask this environmental failure. Record each command as PASS, FAIL, SKIPPED, BLOCKED, or NOT_RUN with time and reason.

STEP 05 adds canonical PSV fixture tests under the owning `organization`, `auth`, and `document` test packages. They check exact department distribution, unique employee codes/extensions/reserved email addresses, 12 pending account references and roles, 20 document references/states/flags, and representative document ACL cases. `OracleSeedSourceTest` checks byte-for-byte equality between the checked-in Oracle seed and a deterministic renderer plus runner guards. It does not execute or parse Oracle SQL. `OracleSeedOracleIT` is compiled but excluded from Surefire; with a real 26ai server and an explicit `ORACLE_SYNTHETIC_SEED_TEST=YES` on an empty migrated disposable schema, Failsafe must run the seed twice, inspect counts/FKs, then roll back. The older offline-cache failure above is historical: the ignored workspace-local cache allowed STEP 03-05 `clean test` to PASS; see the dated status sections.
