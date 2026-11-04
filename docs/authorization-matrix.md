# STEP 03 authorization matrix

All decisions below require an active `EMPLOYEE` session unless stated otherwise. An absent account or resource, missing grant, stale metadata, or uncertain safety review is denied.

| Operation | Role and object rule | Denial |
|---|---|---|
| Process/API health | Anonymous exact GET route | Other method/path: 401 |
| Activation/login | Anonymous exact POST route reserved; not implemented | Missing route: 404 |
| Account administration | `ACCOUNT_ADMIN`; target is another employee | 403; self-grant denied |
| Audit access | `AUDITOR`; no document read shortcut | 403 |
| Read/search company document | Approved, or own/explicit `READ` draft | 404 for hidden resource |
| Read/search department document | Approved and same department, or owner/explicit `READ` | 404 |
| Read/search private document | Owner or explicit `READ`; admin status alone is insufficient | 404 |
| Approve document | `DOCUMENT_MANAGER` + read access + explicit `APPROVE` grant | 403/404 at future endpoint |
| Read 1:1 conversation | One of the two participants | 404 at future endpoint |
| External AI disclosure | Readable approved document + matching segment + segment flag and synthetic/non-sensitive labels + approved synthetic question | Deny; no real transport |

`DOCUMENT_EDITOR` can never approve merely because it can edit. The policies do not yet implement editing, document mutation, login, or audit endpoints.

## STEP 13 implemented tool operations

The table above is the historical STEP 03 policy inventory. Current session/login/document/chat implementation is recorded in handoff.md. All tool routes below require the actual active EMPLOYEE session; preview POST additionally requires CSRF, a reviewed synthetic question and acknowledgement. Each invocation reloads current server status/roles/department.

| Operation | Effective rule | Denial |
|---|---|---|
| Tool catalog / HTML demo | Current server session; no client identity claims | 401 |
| Employee / task-owner search | STEP 07 public read plus exact canonical synthetic public-field match; max five records, no task body/salary | Empty filtered output or input denial |
| findDepartment | Actor's current department only, canonical synthetic metadata; supplied departmentId ignored | Empty output / 401 |
| searchDocuments | STEP 11 current published ACL; then separate external/synthetic/non-sensitive flags | Non-exportable hits omitted without their titles/counts |
| getDocumentExcerpt | Exact current IDs + live ACL/approved publication + ai_searchable + all three trusted classification flags | Same hidden 404 |
| SQL/salary/write/role/grant/unknown tool | Never registered, irrespective of model role claims or document instructions | 400 AI_TOOL_INPUT_INVALID |
| Arbitrary question / safety-flag assertion | Only exact server-reviewed ID/text examples plus acknowledgement | 403 AI_QUESTION_NOT_REVIEWED |
| Tool context to real Gemini | Disabled in STEP 13 even if STEP 12 live greetings are configured | 403; zero transport calls |

Outcome audit has fixed actor/outcome/count metadata only. Rechecking does not remove a concurrent revoke after the final check; real Oracle races remain BLOCKED. No free-form AI conversation or privileged audit-query endpoint is added.

## STEP 14 question routes

| Operation | Required authority / resource decision |
|---|---|
| GET /api/ai/questions/examples, GET /ai/questions | Current eligible Spring Security server session; returns reviewed choices/notice only. |
| POST /api/ai/questions | Same current session plus CSRF, shared request/run budget and reviewed-intent policy. Client identity/role/department is discarded. |
| Structured answer | Reuse organization public read service; canonical invented six-field DTO, exactly one unchanged result after two reads. No model fact selection. |
| Document answer | Exactly one internal candidate; externalSource current ACL/grant + approved/current PUBLISHED version + external/synthetic/non-sensitive eligibility before Provider and before response. |
| Source link | Existing document fragment route repeats current authenticated resource authorization, regardless of any prior AI answer. |

No additional document approval/write permission is granted. ACCOUNT_ADMIN does not acquire document rights. Returned answer text is server-built current evidence, not model authority; no evidence means generic UNANSWERABLE without resource existence disclosure. Document Managers retain the existing explicit workflow approval role. Oracle JOIN/races and independent audit commit are still BLOCKED. See ai-question-orchestration.md for the distinct live grounded contract; tool preview remains Mock-only.
