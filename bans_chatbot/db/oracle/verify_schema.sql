-- Post-apply metadata check. This is not a substitute for negative constraint tests.
DECLARE
    v_tables NUMBER;
    v_roles NUMBER;
    v_triggers NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_tables FROM user_tables WHERE table_name IN (
        'DEPARTMENT', 'EMPLOYEE', 'EMPLOYEE_ACCOUNT', 'ACCOUNT_ACTIVATION',
        'SYSTEM_ROLE', 'EMPLOYEE_ROLE', 'WORK_TASK', 'TASK_ASSIGNMENT',
        'CHAT_ROOM', 'CHAT_MESSAGE', 'CHAT_READ_STATE', 'DOCUMENT',
        'DOCUMENT_VERSION', 'DOCUMENT_REVIEW', 'DOCUMENT_GRANT',
        'DOCUMENT_FRAGMENT', 'AUDIT_EVENT');
    SELECT COUNT(*) INTO v_roles FROM system_role;
    SELECT COUNT(*) INTO v_triggers FROM user_objects
        WHERE object_name IN ('TRG_VERSION_IMMUTABLE', 'TRG_FRAGMENT_IMMUTABLE', 'TRG_CHAT_MESSAGE_LENGTH')
          AND object_type = 'TRIGGER' AND status = 'VALID';
    IF v_tables <> 17 OR v_roles <> 5 OR v_triggers <> 3 THEN
        RAISE_APPLICATION_ERROR(-20051, 'MIGRATION_METADATA_MISMATCH');
    END IF;
END;
/
