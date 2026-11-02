-- Explicit SQLcl/SQL*Plus run only, after db/oracle/apply.sql succeeded in a dedicated synthetic schema.
-- No DROP or reset; all DML runs in one transaction and rolls back on a SQL error.
SET DEFINE OFF
WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
@@verify_seed_preconditions.sql
@@S001__synthetic_company.sql
@@verify_seed.sql
COMMIT;
EXIT SUCCESS
