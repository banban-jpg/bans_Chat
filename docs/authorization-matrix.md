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
