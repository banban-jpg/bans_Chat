-- Oracle AI Database 26ai Free only. Apply once after V001.
-- B-tree indexes complement PK/UNIQUE indexes; verify plans on the real database.

CREATE INDEX ix_employee_department ON employee (department_id, employment_status);
CREATE INDEX ix_activation_employee ON account_activation (employee_id, consumed_at);
CREATE INDEX ix_emp_role_code ON employee_role (role_code, revoked_at);
CREATE INDEX ix_task_creator ON work_task (created_by_employee_id, created_at);
CREATE INDEX ix_assignment_employee ON task_assignment (employee_id, assignment_status);
CREATE INDEX ix_chat_high ON chat_room (employee_high_id, employee_low_id);
CREATE INDEX ix_message_sender_time ON chat_message (sender_id, created_at);
CREATE INDEX ix_read_employee ON chat_read_state (employee_id);
CREATE INDEX ix_doc_owner ON document (owner_employee_id, document_status);
CREATE INDEX ix_doc_department ON document (department_id, document_scope, document_status);
CREATE INDEX ix_doc_current_version ON document (document_id, current_version_id);
CREATE INDEX ix_review_doc_version ON document_review (document_id, version_id);
CREATE INDEX ix_review_reviewer ON document_review (reviewer_employee_id, decided_at);
CREATE INDEX ix_grant_employee ON document_grant (employee_id, permission_code, revoked_at);
CREATE INDEX ix_fragment_search ON document_fragment (ai_searchable, version_id);
CREATE INDEX ix_audit_actor_time ON audit_event (actor_employee_id, occurred_at);
CREATE INDEX ix_audit_request ON audit_event (request_id);

-- At most one non-revoked approval per version. Oracle unique indexes ignore all-NULL keys.
CREATE UNIQUE INDEX ux_review_active_approval ON document_review
    (CASE WHEN decision_code = 'APPROVED' AND revoked_at IS NULL THEN version_id END);

-- Publish transition is allowed; any later UPDATE or DELETE of a published row is rejected.
CREATE OR REPLACE TRIGGER trg_version_immutable
BEFORE UPDATE OR DELETE ON document_version
FOR EACH ROW
BEGIN
    IF :OLD.version_state = 'PUBLISHED' THEN
        RAISE_APPLICATION_ERROR(-20041, 'PUBLISHED_VERSION_IMMUTABLE');
    END IF;
END;
/

-- A published version's fragments must not be inserted, changed, moved, or deleted.
-- Lock the parent version so publication and fragment mutation serialize.
CREATE OR REPLACE TRIGGER trg_fragment_immutable
BEFORE INSERT OR UPDATE OR DELETE ON document_fragment
FOR EACH ROW
DECLARE
    v_state document_version.version_state%TYPE;
BEGIN
    IF INSERTING OR UPDATING THEN
        SELECT version_state INTO v_state FROM document_version
         WHERE version_id = :NEW.version_id FOR UPDATE;
        IF v_state = 'PUBLISHED' THEN
            RAISE_APPLICATION_ERROR(-20042, 'PUBLISHED_FRAGMENT_IMMUTABLE');
        END IF;
    END IF;
    IF UPDATING OR DELETING THEN
        SELECT version_state INTO v_state FROM document_version
         WHERE version_id = :OLD.version_id FOR UPDATE;
        IF v_state = 'PUBLISHED' THEN
            RAISE_APPLICATION_ERROR(-20042, 'PUBLISHED_FRAGMENT_IMMUTABLE');
        END IF;
    END IF;
END;
/

-- Synthetic system metadata only. Employees/accounts are seeded in a later STEP.
INSERT INTO system_role (role_code, role_name) VALUES ('EMPLOYEE', 'Employee');
INSERT INTO system_role (role_code, role_name) VALUES ('ACCOUNT_ADMIN', 'Account administrator');
INSERT INTO system_role (role_code, role_name) VALUES ('DOCUMENT_EDITOR', 'Document editor');
INSERT INTO system_role (role_code, role_name) VALUES ('DOCUMENT_MANAGER', 'Document manager');
INSERT INTO system_role (role_code, role_name) VALUES ('AUDITOR', 'Auditor');
COMMIT;
