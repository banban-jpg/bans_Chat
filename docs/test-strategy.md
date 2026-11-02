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

This PC's offline cache lacks parent-managed JUnit Platform Launcher 1.12.2 and Failsafe 3.5.4, and javac sometimes raises `AccessDeniedException` while closing cached JARs outside the workspace. The temporary, ignored `target/deps` copy and direct `javac`/JUnit launcher run in `docs/test-status.md` provide independent DB-free verification; they do not turn Maven `clean test` into PASS. Do not change the project's framework or database dialect to mask this environmental failure. Record each command as PASS, FAIL, SKIPPED, BLOCKED, or NOT_RUN with time and reason.
