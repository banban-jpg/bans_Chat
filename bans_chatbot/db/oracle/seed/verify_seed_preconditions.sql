-- Prevent accidentally seeding a partially configured or already activated schema.
DECLARE
    v_tables NUMBER;
    v_roles NUMBER;
    v_active_accounts NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_tables FROM user_tables WHERE table_name IN
        ('DEPARTMENT','EMPLOYEE','EMPLOYEE_ACCOUNT','ACCOUNT_ACTIVATION','SYSTEM_ROLE',
         'EMPLOYEE_ROLE','DOCUMENT','DOCUMENT_VERSION','DOCUMENT_FRAGMENT','DOCUMENT_GRANT','DOCUMENT_REVIEW','DOCUMENT_TAG');
    IF v_tables <> 12 THEN
        RAISE_APPLICATION_ERROR(-20061, 'SYNTHETIC_SEED_REQUIRES_V001_V002_SCHEMA');
    END IF;
    SELECT COUNT(*) INTO v_roles FROM system_role;
    IF v_roles <> 5 THEN
        RAISE_APPLICATION_ERROR(-20062, 'SYNTHETIC_SEED_REQUIRES_FIVE_SYSTEM_ROLES');
    END IF;
    SELECT COUNT(*) INTO v_active_accounts FROM employee_account WHERE account_status <> 'PENDING';
    IF v_active_accounts <> 0 THEN
        RAISE_APPLICATION_ERROR(-20063, 'SYNTHETIC_SEED_REQUIRES_PENDING_ACCOUNTS_ONLY');
    END IF;
END;
/
