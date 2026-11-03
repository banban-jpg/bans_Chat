-- Exact counts assume the dedicated V001-V005 schema contains only this synthetic seed.
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM department;
    IF v_count <> 5 THEN RAISE_APPLICATION_ERROR(-20071, 'SEED_DEPARTMENT_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM employee;
    IF v_count <> 20 THEN RAISE_APPLICATION_ERROR(-20072, 'SEED_EMPLOYEE_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM employee_account
        WHERE account_status = 'PENDING' AND password_hash = '!PENDING_ACTIVATION!';
    IF v_count <> 12 THEN RAISE_APPLICATION_ERROR(-20073, 'SEED_PENDING_ACCOUNT_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document;
    IF v_count <> 20 THEN RAISE_APPLICATION_ERROR(-20074, 'SEED_DOCUMENT_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document_version;
    IF v_count <> 20 THEN RAISE_APPLICATION_ERROR(-20075, 'SEED_VERSION_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document_fragment;
    IF v_count <> 20 THEN RAISE_APPLICATION_ERROR(-20076, 'SEED_FRAGMENT_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document_tag;
    IF v_count <> 20 THEN RAISE_APPLICATION_ERROR(-20080, 'SEED_TAG_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document_review;
    IF v_count <> 18 THEN RAISE_APPLICATION_ERROR(-20077, 'SEED_REVIEW_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM document_fragment WHERE external_ai_allowed = 'Y'
        AND synthetic_yn = 'Y' AND non_sensitive_yn = 'Y';
    IF v_count <> 3 THEN RAISE_APPLICATION_ERROR(-20078, 'SEED_EXTERNAL_SEGMENT_COUNT'); END IF;
    SELECT COUNT(*) INTO v_count FROM account_activation;
    IF v_count <> 0 THEN RAISE_APPLICATION_ERROR(-20079, 'SEED_MUST_NOT_ISSUE_ACTIVATION'); END IF;
END;
/
