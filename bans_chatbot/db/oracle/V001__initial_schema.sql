-- Oracle AI Database 26ai Free only. Apply once to a dedicated empty schema.
-- No DROP, IF NOT EXISTS, or cross-database compatibility substitutions.

CREATE TABLE department (
    department_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    department_code VARCHAR2(20 CHAR) NOT NULL,
    department_name VARCHAR2(100 CHAR) NOT NULL,
    active_yn CHAR(1 CHAR) DEFAULT 'Y' NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_department PRIMARY KEY (department_id),
    CONSTRAINT uq_department_code UNIQUE (department_code),
    CONSTRAINT ck_department_active CHECK (active_yn IN ('Y', 'N'))
);

CREATE TABLE employee (
    employee_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    employee_code VARCHAR2(30 CHAR) NOT NULL,
    display_name VARCHAR2(100 CHAR) NOT NULL,
    department_id NUMBER(19) NOT NULL,
    job_title VARCHAR2(100 CHAR),
    company_email VARCHAR2(200 CHAR) NOT NULL,
    extension_no VARCHAR2(10 CHAR),
    duty_summary VARCHAR2(300 CHAR) NOT NULL,
    employment_status VARCHAR2(20 CHAR) DEFAULT 'ACTIVE' NOT NULL,
    hired_on DATE,
    departed_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (employee_id),
    CONSTRAINT uq_employee_code UNIQUE (employee_code),
    CONSTRAINT uq_employee_email UNIQUE (company_email),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (department_id),
    CONSTRAINT ck_employee_status CHECK (employment_status IN ('ACTIVE', 'SUSPENDED', 'DEPARTED')),
    CONSTRAINT ck_employee_departed CHECK (employment_status <> 'DEPARTED' OR departed_at IS NOT NULL)
);

CREATE TABLE employee_account (
    employee_id NUMBER(19) NOT NULL,
    login_id VARCHAR2(80 CHAR) NOT NULL,
    password_hash VARCHAR2(255 CHAR) NOT NULL,
    account_status VARCHAR2(20 CHAR) DEFAULT 'PENDING' NOT NULL,
    session_version NUMBER(10) DEFAULT 0 NOT NULL,
    credentials_version NUMBER(10) DEFAULT 0 NOT NULL,
    failed_login_count NUMBER(5) DEFAULT 0 NOT NULL,
    locked_until TIMESTAMP(6) WITH TIME ZONE,
    activated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_employee_account PRIMARY KEY (employee_id),
    CONSTRAINT uq_account_login UNIQUE (login_id),
    CONSTRAINT fk_account_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_account_status CHECK (account_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DISABLED')),
    CONSTRAINT ck_account_session_ver CHECK (session_version >= 0),
    CONSTRAINT ck_account_credentials_ver CHECK (credentials_version >= 0),
    CONSTRAINT ck_account_failed_count CHECK (failed_login_count >= 0)
);

CREATE TABLE account_activation (
    activation_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    employee_id NUMBER(19) NOT NULL,
    token_hash VARCHAR2(128 CHAR) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP(6) WITH TIME ZONE,
    issued_by_employee_id NUMBER(19) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_account_activation PRIMARY KEY (activation_id),
    CONSTRAINT uq_activation_token UNIQUE (token_hash),
    CONSTRAINT fk_activation_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_activation_issuer FOREIGN KEY (issued_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_activation_expiry CHECK (expires_at > created_at)
);

CREATE TABLE system_role (
    role_code VARCHAR2(40 CHAR) NOT NULL,
    role_name VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_system_role PRIMARY KEY (role_code)
);

CREATE TABLE employee_role (
    employee_id NUMBER(19) NOT NULL,
    role_code VARCHAR2(40 CHAR) NOT NULL,
    assigned_by_employee_id NUMBER(19) NOT NULL,
    assigned_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    revoked_by_employee_id NUMBER(19),
    CONSTRAINT pk_employee_role PRIMARY KEY (employee_id, role_code),
    CONSTRAINT fk_emp_role_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_emp_role_role FOREIGN KEY (role_code) REFERENCES system_role (role_code),
    CONSTRAINT fk_emp_role_assigner FOREIGN KEY (assigned_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_emp_role_revoker FOREIGN KEY (revoked_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_emp_role_revoke CHECK ((revoked_at IS NULL AND revoked_by_employee_id IS NULL)
                                   OR (revoked_at IS NOT NULL AND revoked_by_employee_id IS NOT NULL))
);

CREATE TABLE work_task (
    task_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    task_title VARCHAR2(200 CHAR) NOT NULL,
    task_body CLOB,
    task_status VARCHAR2(20 CHAR) DEFAULT 'OPEN' NOT NULL,
    priority_code VARCHAR2(10 CHAR) DEFAULT 'NORMAL' NOT NULL,
    created_by_employee_id NUMBER(19) NOT NULL,
    due_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_work_task PRIMARY KEY (task_id),
    CONSTRAINT fk_task_creator FOREIGN KEY (created_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_task_status CHECK (task_status IN ('OPEN', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
    CONSTRAINT ck_task_priority CHECK (priority_code IN ('LOW', 'NORMAL', 'HIGH'))
);

CREATE TABLE task_assignment (
    task_id NUMBER(19) NOT NULL,
    employee_id NUMBER(19) NOT NULL,
    assigned_by_employee_id NUMBER(19) NOT NULL,
    assignment_status VARCHAR2(20 CHAR) DEFAULT 'ASSIGNED' NOT NULL,
    assigned_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    completed_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_task_assignment PRIMARY KEY (task_id, employee_id),
    CONSTRAINT fk_assignment_task FOREIGN KEY (task_id) REFERENCES work_task (task_id),
    CONSTRAINT fk_assignment_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_assignment_assigner FOREIGN KEY (assigned_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_assignment_status CHECK (assignment_status IN ('ASSIGNED', 'DONE'))
);

CREATE TABLE chat_room (
    room_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    employee_low_id NUMBER(19) NOT NULL,
    employee_high_id NUMBER(19) NOT NULL,
    last_room_seq NUMBER(19) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_chat_room PRIMARY KEY (room_id),
    CONSTRAINT uq_chat_pair UNIQUE (employee_low_id, employee_high_id),
    CONSTRAINT fk_chat_low FOREIGN KEY (employee_low_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_chat_high FOREIGN KEY (employee_high_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_chat_order CHECK (employee_low_id < employee_high_id),
    CONSTRAINT ck_chat_last_seq CHECK (last_room_seq >= 0)
);

CREATE TABLE chat_message (
    message_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    room_id NUMBER(19) NOT NULL,
    room_seq NUMBER(19) NOT NULL,
    sender_id NUMBER(19) NOT NULL,
    client_request_id VARCHAR2(80 CHAR) NOT NULL,
    message_text CLOB NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    edited_at TIMESTAMP(6) WITH TIME ZONE,
    deleted_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_chat_message PRIMARY KEY (message_id),
    CONSTRAINT uq_message_room_seq UNIQUE (room_id, room_seq),
    CONSTRAINT uq_message_client UNIQUE (sender_id, client_request_id),
    CONSTRAINT fk_message_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_message_seq CHECK (room_seq > 0)
);

CREATE TABLE chat_read_state (
    room_id NUMBER(19) NOT NULL,
    employee_id NUMBER(19) NOT NULL,
    last_read_seq NUMBER(19) DEFAULT 0 NOT NULL,
    read_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_chat_read_state PRIMARY KEY (room_id, employee_id),
    CONSTRAINT fk_read_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id),
    CONSTRAINT fk_read_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_read_seq CHECK (last_read_seq >= 0)
);

CREATE TABLE document (
    document_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    document_code VARCHAR2(40 CHAR) NOT NULL,
    document_title VARCHAR2(200 CHAR) NOT NULL,
    document_category VARCHAR2(20 CHAR) NOT NULL,
    owner_employee_id NUMBER(19) NOT NULL,
    department_id NUMBER(19),
    document_scope VARCHAR2(20 CHAR) NOT NULL,
    document_status VARCHAR2(20 CHAR) DEFAULT 'DRAFT' NOT NULL,
    current_version_id NUMBER(19),
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_document PRIMARY KEY (document_id),
    CONSTRAINT uq_document_code UNIQUE (document_code),
    CONSTRAINT fk_doc_owner FOREIGN KEY (owner_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_doc_department FOREIGN KEY (department_id) REFERENCES department (department_id),
    CONSTRAINT ck_doc_scope CHECK (document_scope IN ('COMPANY', 'DEPARTMENT', 'PRIVATE')),
    CONSTRAINT ck_doc_category CHECK (document_category IN
        ('VACATION', 'EXPENSE', 'DEPLOYMENT', 'EQUIPMENT', 'CONTRACT', 'ONBOARDING', 'SECURITY')),
    CONSTRAINT ck_doc_department CHECK ((document_scope = 'DEPARTMENT' AND department_id IS NOT NULL)
                                 OR (document_scope <> 'DEPARTMENT' AND department_id IS NULL)),
    CONSTRAINT ck_doc_status CHECK (document_status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'ARCHIVED')),
    CONSTRAINT ck_doc_current CHECK (document_status <> 'APPROVED' OR current_version_id IS NOT NULL)
);

CREATE TABLE document_version (
    version_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    document_id NUMBER(19) NOT NULL,
    version_no NUMBER(10) NOT NULL,
    content_format VARCHAR2(10 CHAR) NOT NULL,
    body_text CLOB NOT NULL,
    version_state VARCHAR2(20 CHAR) DEFAULT 'DRAFT' NOT NULL,
    created_by_employee_id NUMBER(19) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    published_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_document_version PRIMARY KEY (version_id),
    CONSTRAINT uq_doc_version_no UNIQUE (document_id, version_no),
    CONSTRAINT uq_doc_version_pair UNIQUE (document_id, version_id),
    CONSTRAINT fk_version_document FOREIGN KEY (document_id) REFERENCES document (document_id),
    CONSTRAINT fk_version_creator FOREIGN KEY (created_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_version_no CHECK (version_no > 0),
    CONSTRAINT ck_version_format CHECK (content_format IN ('TEXT', 'MARKDOWN')),
    CONSTRAINT ck_version_state CHECK (version_state IN ('DRAFT', 'SUBMITTED', 'PUBLISHED', 'DISCARDED')),
    CONSTRAINT ck_version_published CHECK ((version_state = 'PUBLISHED' AND published_at IS NOT NULL)
                                     OR (version_state <> 'PUBLISHED' AND published_at IS NULL))
);

-- Added only after document_version exists; composite FK prevents a foreign document's version.
ALTER TABLE document ADD CONSTRAINT fk_doc_current_version
    FOREIGN KEY (document_id, current_version_id)
    REFERENCES document_version (document_id, version_id);

CREATE TABLE document_review (
    review_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    document_id NUMBER(19) NOT NULL,
    version_id NUMBER(19) NOT NULL,
    reviewer_employee_id NUMBER(19) NOT NULL,
    decision_code VARCHAR2(20 CHAR) NOT NULL,
    decided_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    revoked_by_employee_id NUMBER(19),
    CONSTRAINT pk_document_review PRIMARY KEY (review_id),
    CONSTRAINT fk_review_version FOREIGN KEY (document_id, version_id)
        REFERENCES document_version (document_id, version_id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_review_revoker FOREIGN KEY (revoked_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_review_decision CHECK (decision_code IN ('APPROVED', 'REJECTED')),
    CONSTRAINT ck_review_revoke CHECK ((revoked_at IS NULL AND revoked_by_employee_id IS NULL)
                                  OR (revoked_at IS NOT NULL AND revoked_by_employee_id IS NOT NULL))
);

CREATE TABLE document_grant (
    grant_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    document_id NUMBER(19) NOT NULL,
    employee_id NUMBER(19) NOT NULL,
    permission_code VARCHAR2(20 CHAR) NOT NULL,
    granted_by_employee_id NUMBER(19) NOT NULL,
    granted_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    revoked_by_employee_id NUMBER(19),
    CONSTRAINT pk_document_grant PRIMARY KEY (grant_id),
    CONSTRAINT uq_doc_grant UNIQUE (document_id, employee_id, permission_code),
    CONSTRAINT fk_grant_document FOREIGN KEY (document_id) REFERENCES document (document_id),
    CONSTRAINT fk_grant_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_grant_granter FOREIGN KEY (granted_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT fk_grant_revoker FOREIGN KEY (revoked_by_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_grant_permission CHECK (permission_code IN ('READ', 'APPROVE')),
    CONSTRAINT ck_grant_revoke CHECK ((revoked_at IS NULL AND revoked_by_employee_id IS NULL)
                                 OR (revoked_at IS NOT NULL AND revoked_by_employee_id IS NOT NULL))
);

CREATE TABLE document_fragment (
    fragment_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    version_id NUMBER(19) NOT NULL,
    fragment_no NUMBER(10) NOT NULL,
    body_text CLOB NOT NULL,
    ai_searchable CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    external_ai_allowed CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    synthetic_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    non_sensitive_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    CONSTRAINT pk_document_fragment PRIMARY KEY (fragment_id),
    CONSTRAINT uq_fragment_order UNIQUE (version_id, fragment_no),
    CONSTRAINT fk_fragment_version FOREIGN KEY (version_id) REFERENCES document_version (version_id),
    CONSTRAINT ck_fragment_no CHECK (fragment_no > 0),
    CONSTRAINT ck_fragment_flags CHECK (ai_searchable IN ('Y', 'N')
        AND external_ai_allowed IN ('Y', 'N')
        AND synthetic_yn IN ('Y', 'N')
        AND non_sensitive_yn IN ('Y', 'N')),
    CONSTRAINT ck_fragment_external CHECK (external_ai_allowed = 'N'
        OR (synthetic_yn = 'Y' AND non_sensitive_yn = 'Y'))
);

CREATE TABLE audit_event (
    event_id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    actor_employee_id NUMBER(19),
    event_code VARCHAR2(60 CHAR) NOT NULL,
    resource_type VARCHAR2(40 CHAR),
    resource_id VARCHAR2(80 CHAR),
    outcome_code VARCHAR2(20 CHAR) NOT NULL,
    request_id VARCHAR2(36 CHAR),
    occurred_at TIMESTAMP(6) WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_audit_event PRIMARY KEY (event_id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_employee_id) REFERENCES employee (employee_id),
    CONSTRAINT ck_audit_outcome CHECK (outcome_code IN ('SUCCESS', 'DENIED', 'ERROR'))
);
