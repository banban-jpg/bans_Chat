# Oracle 26ai validation backlog

Status on 2026-11-02 KST: **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No Oracle SQL or connection was executed.

| Future check | Required condition | Current status |
|---|---|---|
| `oracle` profile startup and JDBC connection | Oracle 26ai Free installed, JDBC driver declared, valid local credentials | BLOCKED: ORACLE_26AI_NOT_INSTALLED |
| Oracle DDL, constraints, indexes, migrations, MyBatis SQL | Schema/mapper code from later STEP and Oracle server | BLOCKED: ORACLE_26AI_NOT_INSTALLED; code NOT_RUN / not yet written |
| Transactions, locking, concurrency, performance | Oracle server and domain implementation | BLOCKED: ORACLE_26AI_NOT_INSTALLED; code NOT_RUN / not yet written |

Future command (do not run until the above conditions hold): `bans_chatbot\mvnw.cmd -Poracle-it verify` with the Oracle environment variables. `*OracleIT.java` tests run only in this explicit Failsafe profile. No test class exists yet; a zero-test invocation is not validation.

STEP 01 DB-free evidence is tracked in `docs/test-status.md`; its HTTP and JUnit smoke passes do not change any Oracle status above.

Pre-STEP 02 package cleanup (2026-11-02 KST): `global.health` relocation, Java 21 IDE alignment, and `AGENTS.md` rules made no SQL, Mapper, schema, or Oracle test changes. All Oracle items above remain **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. The Maven `oracle-it` profile remains reserved; it was not invoked. The separate offline Maven build failures and DB-free smoke results are recorded in `docs/test-status.md`.

## STEP 02 update (2026-11-02 KST)

`global.config.OracleDataSourceConfiguration` now requires an Oracle Thin JDBC URL and credentials and checks a connection at `oracle` startup. With the variables removed, it exited 1 with `ORACLE_CONFIG_MISSING`; this is a **PASS for the fail-fast guard**, not a database connection test. `oracleIntegrationTest.cmd` checks prerequisites and then runs `mvnw.cmd -Poracle,oracle-it verify`. Its missing-variable check exited 2 as intended; the database suite was **BLOCKED: ORACLE_26AI_NOT_INSTALLED** and did not run. `OracleConnectionOracleIT` is tagged `oracle-integration`, excluded from normal Surefire, and intended to assert Oracle JDBC metadata major version 26. The default test compiled that class only in an independent direct compilation; compilation does not validate connectivity.

| Pending Oracle check | Status / condition |
|---|---|
| Driver resolution and Oracle 26ai Free connection | BLOCKED: ORACLE_26AI_NOT_INSTALLED; explicit Maven `oracle` profile declares `ojdbc17:23.26.4.0.0`, absent from the offline cache |
| `OracleConnectionOracleIT` execution via Failsafe | BLOCKED: ORACLE_26AI_NOT_INSTALLED; Failsafe 3.5.4 also absent offline; neither artifact was downloaded |
| Domain Oracle Mapper/XML, schema DDL, constraints, indexes, migrations | NOT_RUN: no domain persistence code or schema in STEP 02; requires later implementation and Oracle server |
| Transaction, lock, concurrency, migration compatibility, performance | BLOCKED: ORACLE_26AI_NOT_INSTALLED; tests to be added with the relevant domain implementations |

Future explicit Windows command from `bans_chatbot`: `.\oracleIntegrationTest.cmd`, after installing Oracle 26ai Free, setting the three Oracle environment variables, and making the driver/Failsafe artifacts available. Until then, Oracle behavior and final readiness remain unverified. No H2 or alternative database was added.
