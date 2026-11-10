# STEP 16 보안 회귀 결과

최종 실행: **2026-11-04 18:05:32 KST clean test package PASS**, selected 279 = **278 PASS + 1 SKIPPED**, 실패·오류 0. 124 main/67 test source, Surefire 51개 report로 확인. STOMP 새 malformed JSON 검사까지 포함하며 전체 case/명령은 test-status.md에 기록했다. 로그는 local-notes/step16-release.log, 실제 Oracle/Gemini 호출은 0회다.

검토일 2026-11-04 KST. 범위는 현재 가상 데이터/오프라인 Mock 및 localhost 서버다. OWASP 인증·전체 침투 테스트 결과가 아니다. 신규 스캐너/의존성을 설치하지 않았다. 저장소 POM/테스트 전략에는 기존 자동 취약점 스캐너가 없었다. Java 21.0.3 / eGovFrame 5.0.0 / Boot 3.5.6 / Oracle 26ai Free 선택을 유지했다. 진입 상태는 깨끗한 dev / 88836b4였다.

P0는 인증/권한 우회로 비밀·타인 데이터 반출 또는 쓰기가 가능한 차단급 위험, P1은 민감 로그 유출 등 높은 위험, P2는 방어·운영 한계로 분류한다. 아래 심각도는 **실패할 경우의 위험**이며 취약점 발견 건수가 아니다. 관찰한 오프라인 미해결 P0는 0건이다. Oracle 의존 P0는 BLOCKED이므로 **최종 보안 검증 완료 판정 금지**다.

| Case | 위험 | 실행 증거 / 실제 상태 |
|---|---|---|
| AUTH-01 인증·CSRF·세션 고정 | P0 | 기존 AuthFlowMockMvcTest/RouteAccessMockMvcTest 및 신규 AttackRegressionMockMvcTest의 실제 세션 로그인/ID 회전/보호 API. PASS. |
| AUTH-02 계정 비활성·자격증명 변경·로그아웃 | P0 | 기존 세션/도구 테스트 및 신규 비활성 후 AI 경로 401. PASS. |
| AUTH-03 자기 역할 승격/위조 caller | P0 | 관리자 role 주입 거부, 현재 세션 사용, 기존 도구/직원 입력 allowlist. PASS. |
| IDOR-01 타인 방 조회·쓰기·읽음 | P0 | 신규 공격 MVC에서 위조 caller/sender/role에도 모두 404, 기존 REST/STOMP outsider 수신 차단. PASS. |
| IDOR-02 문서 revoke/부서 변경/AI 출처 | P0 | 신규 grant 부여→조회→철회→404, D03 이동→조회→D01 이동→404. 기존 반환 직전 revoke/version/classification 회귀도 PASS. 실제 Oracle race 보장은 아님. |
| WS-01 Origin/CONNECT CSRF | P0 | 기존 실제 WebSocket E2E에서 외부 origin/잘못된 CSRF 거절. PASS. |
| WS-02 타인·공용 큐 구독/미등록 SEND | P0 | 신규 실제 raw WebSocket: /topic/private, /queue/chat.messages, /user/EMP-002/queue/chat.messages, /app/not-allowed 각각 SUBSCRIBE/SEND, 8개 공격 모두 ERROR. PASS. |
| WS-03 공격 오류 로그·세션 노출 | P1 | 실제 WS 오류 응답 및 CapturedOutput에서 합성 비밀 sentinel/실제 HTTP 세션 ID/MessageDeliveryException 문자열 누출 없음. PASS. 모든 가능한 framework logger 설정을 검사한 것은 아님. |
| WS-04 잘못된 JSON의 변환 예외 | P1 | 새 catch-all 메시지 handler가 CHAT_REQUEST_INVALID만 반환. 실제 STOMP JSON 파싱 실패 후 코드 응답·본문 sentinel 로그 부재. 수정 전 실행 재현은 NOT_RUN, 소스 검토에 따른 예방적 보강. |
| XSS-01 Markdown/게시 렌더링 | P1 | 기존 document/workbench/client 테스트의 서버 escape, DOM textContent/허용 태그 출력, 브라우저 저장소/키 부재. PASS. 실제 Chrome XSS/클릭은 STEP 15 환경 BLOCKED 유지. |
| SQL-01 공격 문자열 | P0 | 신규 MVC의 직원/문서 SQL 모양 검색은 빈 결과. 기존 Mapper source bind 검사. PASS는 메모리/소스 검사에 한정하며 Oracle SQL injection 반응은 BLOCKED. |
| AI-01 미검토 질문/명령/급여/SQL/PII sentinel | P0 | 신규 4개 질문 입력은 UNANSWERABLE, 실제 Gemini 어댑터의 loopback HTTP 요청 0회, 감사/로그에 원문 없음. PASS. |
| AI-02 fragment external/synthetic/non-sensitive/게시 조건 | P0 | 신규 4개 부정 상태에서 OutboundDisclosurePolicy가 거절하고 실제 HTTP 요청 0회. PASS. |
| AI-03 정책과 실제 전송 바이트 | P0 | 정책이 승인한 현재 구간과 모의 서버 수신 바이트의 excerpt를 정확 비교. 데이터 JSON은 question/evidenceId/excerpt 세 필드, 도구 정의·caller·문서 ID·세션·키 본문 부재. PASS. 키는 테스트용 가짜 헤더만 사용. |
| AI-04 문서 내 ignore all instructions | P0 | 명시적 반출 허용 합성 문서에 공격 지시를 넣어 실제 loopback 전송. 데이터로만 인용하고 wire tools 없음, 추가 호출/권한 변경 없음. PASS. 악성 지시를 regex로 안전 인증하지 않음. |
| AI-05 미등록/쓰기/급여 도구 | P0 | 기존 AiToolSecurityTest/ToolRegistryTest에서 거부, Mock-only preview/live tool 전송 거부. PASS. |
| AI-06 429/403/5xx/timeout/Retry-After | P1 | 기존 GeminiProviderNetworkTest 실제 loopback + 서비스/MockMvc 실패 회귀. 재시도 상한/유료 전환 없음/Chat 분리. PASS. |
| AUDIT-01 원문·키·세션 로그와 감사 | P1 | 신규 MVC/실제 wire/WS CapturedOutput sentinel 검사, 기존 키·예외·audit 필드 allowlist. PASS. Oracle DB 감사 INSERT·내구성·실제 DB 검사 BLOCKED. |
| LIMIT-01 요청 수/단계/툴/byte/token | P1 | 신규 실패 질문 5회 후 429 및 기존 budget/schema/transport 한도. PASS. 분산 제한/장시간 DoS/TPM·실성능 검증은 NOT_RUN. |

## 수정 및 실행

질문 경로의 독립 정책 클래스 `ai.question.OutboundDisclosurePolicy`를 추가해 검토된 intent와 기존 externalSource의 현재 ACL/게시 버전/분류 판정을 실제 전송 경로에서 명시적으로 재사용한다. 기존 AiDisclosurePolicy는 과거 계약으로 남으며 실 전송 권한을 대신하지 않는다. 새 정책은 클라이언트/모델의 safety 플래그를 받지 않는다. STOMP 일반 변환 예외는 원문을 포함할 수 있으므로 예외 자체를 로그/응답에 전달하지 않고 고정 오류로 처리한다. 기존 ApiException 코드는 유지한다.

최초 대상 실행은 새 테스트의 RPM=20 설정이 기존 허용 상한 10을 넘어 **FAIL 17:58:59**(13건 중 9 setup ERROR, STOMP 4건 PASS). 테스트 설정을 10으로 고치고 부분 초기화 cleanup을 보강했다. 제품 한도는 완화하지 않았다. 재실행 **PASS 18:01:42**, 16건/실패·오류·skip 0. 이후 raw WS 공격을 8개 조합으로 확장하고 잘못된 JSON 테스트/고정 오류 처리를 추가했다. 전체 실제 명령·최종 집계는 test-status.md의 STEP 16 항목을 따른다.

## Oracle와 외부 의존성

기존 Maven Failsafe `*OracleIT` + `oracle-integration` tag/전용 runner를 재사용한다. src/oracleIntegrationTest용 별도 소스 플러그인을 새로 도입하지 않으며 일반 test에서 실제 DB 테스트를 이름과 태그로 제외한다. `SecurityAttackOracleIT`는 dedicated seed schema에서 Oracle MERGE/grant 철회, 현재 ACL JOIN, literal SQL 공격 입력을 rollback transaction으로 준비했다. **BLOCKED: ORACLE_26AI_NOT_INSTALLED**이며 컴파일만 했다. 실제 다중 연결 commit/revoke race, lock ordering, DB 감사 내구성, SQL 계획/부하는 예비 코드에 포함되지 않아 NOT_RUN/TODO다. Oracle 의존 P0는 검증 대기다.

실 Gemini·실 DB E2E·외부 DAST/의존성 CVE 스캔은 NOT_RUN. 실제 Google 모델 안전성/무료 quota를 이 Mock 결과로 인증하지 않는다. 문서 조회 이후 네트워크/반환 사이 권한 변경의 원자성은 보장하지 않는다. Chrome renderer/CDP BLOCKED 및 선택적 브라우저 SKIPPED도 유지한다. 로그/임시 메모는 ignored local-notes에만 보관하며 공용 보안 결과 문서는 버전 관리한다.

공식 기준: OWASP [WebSocket Security](https://cheatsheetseries.owasp.org/cheatsheets/WebSocket_Security_Cheat_Sheet.html), [Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html), [Logging](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html), [Prompt Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/LLM_Prompt_Injection_Prevention_Cheat_Sheet.html). eGov Boot 공식 조합과 Google 가격/지원 지역도 재확인했다. 이번 Google 폐기 일정 URL 조회는 도구 fetch 오류로 재확인하지 못했으며, 모델 설정을 바꾸지 않았다. 실제 호출 전 운영자의 최신 모델/무료 티어 검토 조건은 유지한다.
