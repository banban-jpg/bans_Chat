-- Refuse both an accidental repeated application and a populated target schema.
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_objects;
    IF v_count <> 0 THEN
        RAISE_APPLICATION_ERROR(-20050, 'MIGRATION_REQUIRES_EMPTY_SCHEMA');
    END IF;
END;
/
