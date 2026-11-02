# STEP 04 Oracle 26ai ERD

All 17 requested candidate tables are retained. No extra application table was added. The relation labeled `current_version` is added **after** `document_version` exists, through a composite FK that also matches `document_id`.

```mermaid
erDiagram
    department ||--o{ employee : employs
    employee ||--o| employee_account : logs_in_with
    employee ||--o{ account_activation : activates
    employee ||--o{ employee_role : has
    system_role ||--o{ employee_role : defines
    employee ||--o{ work_task : creates
    work_task ||--o{ task_assignment : assigned_to
    employee ||--o{ task_assignment : receives
    employee ||--o{ chat_room : lower_participant
    employee ||--o{ chat_room : higher_participant
    chat_room ||--o{ chat_message : contains
    chat_room ||--o{ chat_read_state : tracks
    employee ||--o{ chat_message : sends
    employee ||--o{ chat_read_state : reads
    employee ||--o{ document : owns
    department ||--o{ document : limits
    document ||--o{ document_version : versions
    document_version |o--o| document : current_version
    document_version ||--o{ document_review : reviewed
    document ||--o{ document_grant : grants
    document_version ||--o{ document_fragment : splits_into
    employee ||--o{ audit_event : acts

    department {
        NUMBER department_id PK
        VARCHAR2 department_code UK
    }
    employee {
        NUMBER employee_id PK
        VARCHAR2 employee_code UK
        NUMBER department_id FK
        VARCHAR2 company_email UK
        VARCHAR2 extension_no
        VARCHAR2 duty_summary
        VARCHAR2 employment_status
    }
    employee_account {
        NUMBER employee_id PK,FK
        VARCHAR2 login_id UK
        VARCHAR2 password_hash
        VARCHAR2 account_status
    }
    account_activation {
        NUMBER activation_id PK
        NUMBER employee_id FK
        VARCHAR2 token_hash UK
    }
    system_role {
        VARCHAR2 role_code PK
    }
    employee_role {
        NUMBER employee_id PK,FK
        VARCHAR2 role_code PK,FK
    }
    work_task {
        NUMBER task_id PK
        NUMBER created_by_employee_id FK
        CLOB task_body
    }
    task_assignment {
        NUMBER task_id PK,FK
        NUMBER employee_id PK,FK
    }
    chat_room {
        NUMBER room_id PK
        NUMBER employee_low_id FK
        NUMBER employee_high_id FK
    }
    chat_message {
        NUMBER message_id PK
        NUMBER room_id FK
        NUMBER room_seq
        NUMBER sender_id FK
        CLOB message_text
    }
    chat_read_state {
        NUMBER room_id PK,FK
        NUMBER employee_id PK,FK
        NUMBER last_read_seq
    }
    document {
        NUMBER document_id PK
        VARCHAR2 document_code UK
        VARCHAR2 document_category
        NUMBER owner_employee_id FK
        NUMBER department_id FK
        NUMBER current_version_id FK
        VARCHAR2 document_scope
    }
    document_version {
        NUMBER version_id PK
        NUMBER document_id FK
        NUMBER version_no
        CLOB body_text
    }
    document_review {
        NUMBER review_id PK
        NUMBER document_id FK
        NUMBER version_id FK
        NUMBER reviewer_employee_id FK
        TIMESTAMP revoked_at
    }
    document_grant {
        NUMBER grant_id PK
        NUMBER document_id FK
        NUMBER employee_id FK
        VARCHAR2 permission_code
        TIMESTAMP revoked_at
    }
    document_fragment {
        NUMBER fragment_id PK
        NUMBER version_id FK
        NUMBER fragment_no
        CHAR ai_searchable
        CHAR external_ai_allowed
    }
    audit_event {
        NUMBER event_id PK
        NUMBER actor_employee_id FK
        VARCHAR2 event_code
        VARCHAR2 resource_id
    }
```

`employee_account` separates credentials from the employee directory. `system_role` and `employee_role` separate security roles from job titles and departments. `document_review` records an approver and optional revocation; `document_grant` records a per-employee permission and optional revocation. `document_fragment` holds independent `ai_searchable` and `external_ai_allowed` flags. The database has no AI question, answer, prompt, transcript, or key table. `audit_event` stores metadata codes and IDs only.

STEP 05 refines the still-unapplied V001 schema with synthetic company email/extension/duty columns and stable `document_code`/category columns for rerunnable Oracle seed matching. `document_version.version_state` also accepts `DISCARDED` for withdrawn examples. The 17-table count and dependency graph are unchanged; V001's SHA-256 manifest was updated before any Oracle migration was executed. See `fixture-and-seed.md` for the 5/20/12/20 synthetic rows and the separate seed runner.
