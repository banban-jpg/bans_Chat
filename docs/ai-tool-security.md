# STEP 13 — 읽기 도구, 현재 권한과 외부 전송 경계

검토일: **2026-11-04 KST**. STEP 12의 미커밋 변경을 보존하고 증분 구현했다. Java 21 / 실제 eGovFrame 5.0.0 / Boot 3.5.6 / MyBatis / Spring Security 서버 세션 / 최종 Oracle 26ai Free를 유지한다. 새로운 AI 공급자·SDK 의존성·DB 기술·DDL은 추가하지 않는다.

## 현재 실행 경로

`POST /api/ai/tools/preview` → Spring Security + CSRF → `AiToolService` → `CurrentSessionService` → 고정 registry / 기존 organization·document 서비스 → 최종 재조회 → `MockGeminiProvider.previewTools` → 고정 메타데이터 응답.

`GET /ai/tools`는 로그인 세션을 사용하는 작은 시연 화면이다. 고지와 체크되지 않은 확인란, 서버가 검토한 휴가 예제 실행 버튼만 제공한다. 사용자 질문/모델 응답/문서 본문을 화면에 반사하지 않고 결과 건수만 textContent로 표시한다. `GET /api/ai/tools`는 고지, 검토 예제, 도구 입력 JSON Schema와 제한값을 반환한다. 세 경로 모두 인증이 필요하고 POST에는 CSRF가 필요하다. 별도 익명 경로를 열지 않았다.

**질문 원문도 실제 AI 활성화 시 외부로 전송될 수 있다. 실제 업무자료·개인정보·기밀을 입력하지 않는다.** 서비스는 확인 표시만 믿지 않는다. `ReviewedSyntheticQuestions`가 서버 코드의 질문 ID/본문 쌍과 정확히 일치하는 세 가지 합성 예제만 허용한다. 정규식 마스킹으로 임의 질문을 합성·비민감이라고 인증하지 않는다. 자유 질문, 임의 번역/정규화, 클라이언트의 synthetic/nonSensitive 주장은 허용하지 않는다.

이 단계의 도구 payload는 **Mock 전용**이다. 서비스는 실제 Mock 구현만 받아들이고, 실 `GeminiProvider`의 새 도구 메서드는 기본 거부를 유지한다. 기존 실 Provider는 명시적으로 허가된 STEP 12 인사말 계약만 가진다. 실제 Gemini 기능 호출/모델 도구 루프를 구현하거나 활성화하지 않았다. Mock은 정제된 가상 outbound JSON을 일시적으로 구성하고 고정 응답을 반환하며 payload를 저장하지 않는다.

## 도구와 입력/출력 allowlist

JSON Schema는 `ToolRegistry.definitions()`에서 생성한다. `additionalProperties=false`이며, 아래의 기능 인자만 검증·실행한다. 예약된 `callerId,userId,employeeId,role,roles,departmentId`는 입력의 어느 envelope에 있어도 신원/권한/부서 선택에 사용하지 않고 버린다. Schema는 이 예약 신원 필드를 제거한 기능 인자에 적용되는 계약이다. 그 밖의 미등록 필드는 거절한다.

| 이름 | 기능 인자 | 모델에 제공할 수 있는 결과 |
|---|---|---|
| `searchEmployees` | `name`: 1–80 code points, `limit`: 1–5, 기본 5 | 직원 공개 6필드만: employeeId, displayName, departmentCode, jobTitle, companyEmail, extensionNo |
| `findDepartment` | 빈 객체. 부서 ID 주장을 무시 | 현재 서버 사용자의 부서 code/name 1건 |
| `findTaskOwner` | `query`: 1–80 code points, `limit`: 1–5 | 담당 업무 설명에 대한 기존 STEP 07 검색. 직원 공개 6필드만; 업무 본문·배정 테이블은 전달하지 않음 |
| `searchDocuments` | `query`: 1–80 code points, `limit`: 1–5 | 내부 검색 후보 중 현재 외부 전송까지 허용된 문서 근거만 |
| `getDocumentExcerpt` | documentId/versionId/fragmentId: 양의 Java long 정수 | 정확한 현재 조각의 documentId/versionId/fragmentId/excerpt 4필드만 |

직원·부서 도구도 실제 레코드가 체크인된 합성 fixture의 공개 값과 정확히 일치하는지 확인한다. `.example` 이메일이나 직원 코드만으로 새로운 입력을 합성이라고 판단하지 않는다. 업무 담당자 결과는 canonical 합성 업무 설명에도 같은 query가 일치해야 한다. 인사 수정 후 canonical 값과 달라지면 내부 조회는 가능해도 Mock 전송에서 제외할 수 있다. 모든 직원·부서·업무 조회는 STEP 07 서비스/포트/Oracle Mapper를 사용한다. 전체 테이블·계정·직원 상태·역할·비밀번호·급여·업무 원문은 outbound DTO에 없다.

문서 도구는 STEP 11 검색·정확한 source 조회를 재사용한다. 내부 search/read와 `externalSource`는 별도 판정이다. 현재 actor의 ACL, 현재 버전 포인터, PUBLISHED 버전, APPROVED 또는 이전 게시본을 유지하는 IN_REVIEW 문서, ai_searchable, external_ai_allowed=Y, synthetic_yn=Y, non_sensitive_yn=Y를 확인한다. 조각 조회 row뿐 아니라 다시 읽은 workspace의 현재 버전 분류도 확인한다. OWNER/READ grant/동일 부서/회사 공개 규칙은 기존 서비스와 Oracle ACL JOIN이 소유한다. ACCOUNT_ADMIN이나 모델의 role 주장이 이를 우회하지 못한다.

외부 미허가 search hit는 제목·excerpt·전체 결과 수를 전달하지 않고 제외한다. 직접 excerpt 요청의 존재/ACL/게시/분류 실패는 같은 404로 숨긴다. 문서 제목과 변경 시각도 외부 DTO에는 포함하지 않는다. 조각 excerpt는 최대 200 Unicode code points다. 검색은 내부 후보 최대 5개를 필터하므로 외부 허용 결과가 적거나 비어 있을 수 있다. 더 많은 후보를 얻기 위해 무제한 스캔하지 않는다.

외부 판정의 진입점은 `externalSource`다. 이전 `ManagedDocumentService.externalCandidates`나 STEP 03의 순수 정책 모델만으로 도구의 외부 허가를 결정하지 않는다. 해당 기존 API를 새 모델 전송 경로에 직접 연결해서는 안 된다.

## 신원, 실행 제한과 재검증

각 도구 호출 및 최종 Mock send 직전에 실제 HttpSession의 Spring SecurityContext, 현재 스레드의 AuthenticatedEmployee 신원 일치, 현재 account의 eligibility/EMPLOYEE 역할/credentialsVersion을 확인한다. 이전 ROLE authority나 MVC SessionActor 속성만으로 실행하지 않는다. 계정 상태·역할·부서는 저장소의 현재 값으로 다시 읽는다. 클라이언트가 actor나 권한 객체를 만들 수 있는 API는 없다.

제한값은 입력 8192바이트, JSON nesting 10, 문자열 파서 최대 500, 질문 ID 40, 질문 본문 500, query/name 80 code points, 계획당 1–4개 도구, 도구당 결과 5개, 전체 20개, 세션당 60초에 5회, 세션당 활성 run 1개다. 도구 처리 경과 10초를 읽기 경계마다 확인하고 Mock payload는 최대 24KiB다. 숫자 문자열/실수/음수/overflow, 중복 JSON 키·trailing JSON·제어 문자·미등록 도구/필드는 거절한다. 입력 body는 최대 8193바이트만 읽어 크기를 검사한다. 웹 서버의 느린 요청 수신 자체를 10초에 강제 취소하는 기능은 아니다.

서버는 첫 판정 후 같은 읽기 계획을 send 경계에서 다시 실행한다. payload에는 두 번째 읽기의 결과만 사용한다. 이를 통해 다른 도구 실행 중 회수된 grant/외부 허용/계정 상태와 갱신된 부서·게시 포인터를 다시 확인한다. payload와 행은 불변 목록/복사본으로 보호하며 문서 내용이나 모델 결과의 지시로 새로운 도구를 등록·호출하지 않는다. SQL/UPDATE/DELETE/DDL/권한 변경/급여 조회 도구는 없다.

단, 최종 확인 **이후** 다른 트랜잭션에서 권한이 바뀌는 race를 이 방식만으로 제거하지 못한다. 서로 다른 도구의 결과가 하나의 Oracle snapshot이라고 주장하지 않는다. Oracle의 실제 ACL JOIN·constructor/JDBC mapping·락·REQUIRES_NEW 감사·교차 연결 race는 아직 검증되지 않았다. 실제 외부 도구 전송 경로도 닫혀 있다. 메모리 테스트 성공은 Oracle PASS가 아니다.

## 감사와 비밀 경계

`AiToolEvent`는 서버 actor employee code, SUCCESS/DENIED/ERROR, 계획 호출 수, 반환 건수만 허용한다. query·질문·답·조각·tool arguments·예외·키 필드가 없다. event code/resource type은 고정이고 resource_id는 `calls=n;items=n`이다. Mock 감사는 최대 200개의 메타데이터만 메모리에 보관한다. 세션은 요청 횟수/활성 상태만 보관하며 질문과 payload를 넣지 않는다.

Oracle 감사는 기존 `audit_event`에 바인딩한 메타데이터만 INSERT한다. 모델의 쓰기 도구가 아니라 서버의 독립된 REQUIRES_NEW 감사 경로다. nullable actor는 VARCHAR JDBC 타입을 지정한다. 감사 실패는 고정 503 AI_AUDIT_UNAVAILABLE로 숨기며 원본 예외를 붙이지 않는다. HTTP 인증/CSRF 단계에서 차단돼 서비스까지 도달하지 않은 요청은 이 도구 감사 경로의 대상이 아니다. 일반 보안 감사 파이프라인 전체를 구현한 것은 아니다.

기본 결과 응답은 mock/toolCalls/returnedItems/notice뿐이다. AI 원문이나 정제 payload를 HTTP로 반환하지 않는다. 원문을 로그·DB·파일에 기록하지 않는다. 내부 Plan/Call/Payload 문자열 표현은 원문을 가리며 질문/인자는 일반 Jackson 직렬화에서도 제외한다. 직원 DTO와 문서 DTO는 서버에서 필드별로 새로 작성한다.

## 실행·검증 경계

자동 테스트는 실제 Gemini 호출과 Oracle 연결 0회다. 테스트·local-mock의 저장소/감사는 ephemeral이며 Oracle 어댑터가 아니다. Oracle 프로필은 기존 연결 실패 시 fail-fast를 유지하고 mock DB로 전환하지 않는다. 새로운 mapper와 분류 SELECT는 Oracle 전용이고 기존 V001–V005 DDL/seed/manifest를 변경하지 않았다.

`AiToolOracleIT`는 `@Tag("oracle-integration")` 및 `ORACLE_AI_TOOL_TEST=YES`로 분리돼 있다. 설치된 전용 26ai 스키마의 명시적 migration/seed 이후 `oracleIntegrationTest.cmd`에서만 실행할 준비 코드다. 현재는 **BLOCKED: ORACLE_26AI_NOT_INSTALLED / NOT_RUN**이다. 구체 명령·중간 FAIL·최종 PASS는 `test-status.md`, 미완료 Oracle 시나리오는 `oracle-validation-backlog.md`에 기록한다.

Google 공식 [가격표](https://aistudio.google.com/docs/pricing), [폐기 일정](https://aistudio.google.com/docs/deprecations#gemini-2.5-flash-models), [프로젝트별 한도 안내](https://aistudio.google.com/docs/rate-limits), [한국 지원 지역](https://aistudio.google.com/docs/available-regions)을 재확인했다. STEP 12의 모델 allowlist/무료 티어 수동 확인 정책은 유지한다. 실제 프로젝트 한도·접근 가능성, 최신 SDK 호환성, 실 호출 스모크는 이번 STEP에서 재검증/실행하지 않았다. 자세한 근거와 기존 제약은 `gemini-free-tier.md`를 따른다.

## STEP 14와의 관계

이 문서의 previewTools 계약은 계속 Mock 전용이다. STEP 14는 별도의 `ai.question` 서비스와 `grounded` Provider 메서드를 추가한다. 모델이 도구를 선택하거나 임의 payload를 보내는 루프가 아니다. 검토된 질문·반출 허용 단일 구간만 서버가 구성하며, 기본 Mock / 명시적 live 활성화 조건은 그대로다. 질문과 preview는 동일 세션 5회/분·동시 1회 예산을 공유한다. 질문 응답은 현재 사실/근거를 검증한 DTO이며 preview의 HTTP counts-only 계약을 변경하지 않는다. 자세한 outbound/반환 직전 재검사/감사/한계는 [질문 조정 가이드](ai-question-orchestration.md)를 따른다.
