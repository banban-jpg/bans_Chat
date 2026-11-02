# STEP 03 threat model

| Threat | Current control | Remaining verification |
|---|---|---|
| Client supplies another `userId` or title | Authorization methods take a trusted session actor, not request identity; an extra demo query parameter is ignored | Spring Security principal binding and login flow |
| Account admin reads a private document | Document read policy requires owner or explicit `READ` grant | Oracle ACL query and negative HTTP tests on real document API |
| Employee searches another department's material | Search applies the read policy to each result | Oracle filtering, result counts, pagination, and timing |
| Removed employee keeps an old session | Inactive actor is rejected | Reload status on every protected request, session invalidation, Oracle revocation race |
| Grant is revoked during read or AI preparation | Every policy call checks the grant provider | Transaction boundaries and recheck immediately before outbound AI call |
| Prompt injection or sensitive text reaches Gemini | No real client exists; disclosure checks trusted segment flags and server question review | Implement trustworthy review and explicit user notice; do not trust regex or document/model instructions |
| Role self-grant or approval bypass | Role policy rejects self-target; approval needs manager role and document grant | Persisted role changes and approval transition checks |
| Internal errors reveal secrets | Fixed public 401/403/404/429 messages and request ID | Spring Security exception handlers and future transport errors |
| Non-MVC route bypass | No chat/WebSocket or real AI route exposed now | Add Spring Security filter chain, CSRF/session fixation protection, STOMP authorization before exposing them |

The test actors, mocked grants, and synthetic employees are not real accounts or Oracle rows. No production secrets, AI text, or private company data are in fixtures.
