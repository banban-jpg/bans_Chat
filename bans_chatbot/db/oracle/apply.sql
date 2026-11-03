-- Run explicitly with SQLcl or SQL*Plus from the module directory: @db/oracle/apply.sql
-- DDL auto-commits in Oracle. Never run against a populated or shared schema.
SET DEFINE OFF
WHENEVER OSERROR EXIT FAILURE
WHENEVER SQLERROR EXIT SQL.SQLCODE
@@verify_empty.sql
@@V001__initial_schema.sql
@@V002__indexes_guards_roles.sql
@@V003__chat_message_length_guard.sql
@@V004__document_reviewer_assignment.sql
@@V005__document_search_tags_indexes.sql
@@verify_schema.sql
EXIT SUCCESS
