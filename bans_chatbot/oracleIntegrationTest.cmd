@echo off
setlocal
if not defined ORACLE_JDBC_URL (
  echo BLOCKED: ORACLE_CONFIG_MISSING ORACLE_JDBC_URL
  exit /b 2
)
if not defined ORACLE_DB_USER (
  echo BLOCKED: ORACLE_CONFIG_MISSING ORACLE_DB_USER
  exit /b 2
)
if not defined ORACLE_DB_PASSWORD (
  echo BLOCKED: ORACLE_CONFIG_MISSING ORACLE_DB_PASSWORD
  exit /b 2
)
call "%~dp0mvnw.cmd" -Poracle,oracle-it verify %*
exit /b %ERRORLEVEL%
