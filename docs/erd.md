# Oracle 26ai ERD — STEP 18 현재 source

V001의 17개 업무 테이블에 V005의 document_tag를 더해 현재 18개다. 아래는 Oracle 전용 source 설계이며 실제 DB 적용은 BLOCKED: ORACLE_26AI_NOT_INSTALLED다. current_version은 document_version 생성 후 document_id까지 일치시키는 복합 FK로 추가한다. V004는 배정 검토자를 추가하고 V005는 tags/index를 추가한다. DDL/manifest bytes는 STEP 18에서 바꾸지 않았다.

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
    document ||--o{ document_tag : tagged
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
        NUMBER credentials_version
        NUMBER session_version
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
        VARCHAR2 client_request_id
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
        NUMBER review_assignee_id FK
    }
    document_version {
        NUMBER version_id PK
        NUMBER document_id FK
        NUMBER version_no
        CLOB body_text
        VARCHAR2 version_state
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
        CHAR synthetic_yn
        CHAR non_sensitive_yn
    }
    document_tag {
        NUMBER document_id PK,FK
        VARCHAR2 tag_value PK
    }
    audit_event {
        NUMBER event_id PK
        NUMBER actor_employee_id FK
        VARCHAR2 event_code
        VARCHAR2 resource_id
    }
```

`employee_account` separates credentials from the employee directory. `system_role` and `employee_role` separate security roles from job titles and departments. `document_review` records an approver and optional revocation; `document_grant` records a per-employee permission and optional revocation. `document_fragment` holds independent `ai_searchable` and `external_ai_allowed` flags. The database has no AI question, answer, prompt, transcript, or key table. `audit_event` stores metadata codes and IDs only.

V001은 합성 회사 이메일/내선/duty와 stable document_code/category를 포함하고 DISCARDED 상태를 지원한다. 메시지는 room/seq 및 sender/client_request_id의 unique 조건을 사용한다. 여기에는 AI transcript/key 테이블이 없다. [fixture-and-seed.md](fixture-and-seed.md)와 [Oracle 체크리스트](oracle-install-validation.md)를 따른다. 런타임 SQL/제약/락의 실제 수락은 전부 Oracle 설치 후 대기다.
