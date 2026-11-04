# STEP 14 — 단일 질문·정형 사실·문서 근거 경계

검토일: 2026-11-04 KST. Java 21 / eGovFrame 5.0.0 / Boot 3.5.6을 유지한다. 기본 Provider는 Mock이고 `test`·`local-mock`에는 DB/실 Gemini가 없다. 기존 STEP 12·13의 미커밋 변경을 보존했다. 최종 영구 DB는 Oracle AI Database 26ai Free다.

## 질문과 응답

인증 세션과 CSRF가 필요한 `POST /api/ai/questions`는 `{ "question": "연차 신청 절차" }`를 받는다. 기존 공통 응답 봉투 `data` 아래 `answer`, `answerType`, `structuredFacts`, `sources`, `requestId`를 반환한다. 요청 ID는 서버 필터의 UUID이며 클라이언트가 정하지 않는다. 인증된 `GET /api/ai/questions/examples`, `GET /ai/questions`는 검토된 질문과 외부 전송 고지 및 작은 시연 화면을 제공한다. 응답은 `Cache-Control: no-store`다.

질문 본문은 2048바이트, 질문 문자열은 200 Unicode code point 이하다. 중복 키·trailing JSON·제어문자·과도한 중첩·임의 도구/모델/history 필드는 거부한다. 알려진 callerId/userId/employeeId/role/roles/departmentId 주장은 버린다. 실제 Spring Security 세션 principal, 자격증명 버전, 현재 활성 계정/EMPLOYEE 역할/부서를 매 읽기와 반환 경계에서 다시 확인한다.

질문 자체에 개인정보가 있을 수 있으므로 임의 질문을 분류 모델에 먼저 보내지 않는다. `QuestionPolicy`의 **정확히 일치하는 서버 검토 예제**만 처리하고 그 외 문장은 그대로 반사하지 않고 UNANSWERABLE로 종료한다. 마스킹·클라이언트 synthetic 주장·체크박스는 반출 허가 근거가 아니다. UI는 자유 입력 대신 예제 선택과 기본 미선택 확인란을 제공한다. POST 계약은 question만 요구하며, 서비스의 실제 허가는 이 고정 검토 목록과 현재 권한/분류 플래그다.

| 유형 | 서버 처리 |
|---|---|
| STRUCTURED | `출장비 담당자`는 검토된 업무 별칭 `정산`으로 STEP 07 서비스 조회. `<정확한 가상 직원명> 연락처`도 지원. canonical 합성 fixture와 일치하는 공개 DTO 여섯 필드만 두 번 조회하고 유일하고 동일한 결과일 때 서버가 답변 작성. 모델 호출 0회. 업무 별칭은 서버 정의이며 모델 추측이 아니다. |
| DOCUMENT | 연차/배포 등 검토된 검색어로 STEP 11 검색. 현재 권한 내 검색 결과가 정확히 하나이고 hasMore=false여야 한다. `externalSource`가 현재 승인/PUBLISHED 버전, ACL/grant, ai_searchable 및 external/synthetic/non-sensitive 플래그를 재검사한다. |
| UNANSWERABLE | 검색 결과 없음·여러 근거·폐기/권한 거부·반출 미허가·사실 변경·모델 형식/인용 불일치·미검토 질문. 답변은 고정 문구, facts/sources는 빈 배열. 숨겨진 자원의 존재를 설명하지 않는다. |

공개 EmployeeView 필드는 employeeId, displayName, departmentCode, jobTitle, companyEmail, extensionNo뿐이다. 급여·계정·권한·DB 자격증명·임의 SQL은 전달하지 않는다. 내부 검색 가능과 외부 전송 가능은 별도 판정이다. 단순 중복 근거도 보수적으로 답변 불가이며 의미 충돌을 모델에게 판정시키지 않는다.

## 문서 Provider 계약

`GroundedPrompt` 생성자는 question 패키지에서만 접근 가능하다. 검토된 DOCUMENT intent, 현재 반출 가능한 단일 fragment의 최대 200 code point, 남은 시간만 담는다. 외부 데이터 JSON은 `{question,evidenceId:"S1",excerpt}`뿐이다. 제목·내부 ID·전체 문서·검색 결과 개수·직원 정보·과거 대화·도구 정의는 보내지 않는다. 실제 API에서는 이 JSON 앞에 고정 서버 지시문을 붙인다. 모델/문서 안의 지시는 권한 정책이 아니다.

Mock은 동일한 계약으로 가상 JSON을 반환한다. 실 Provider도 명시적인 기존 gemini-live 설정/고지/무료 티어 확인/키가 모두 있을 때만 선택 가능하다. test/local-mock/oracle-it에서는 실 Provider 선택이 실패한다. 고정 endpoint/model, 키 헤더, byte 제한, 동시성/RPM/일별 한도, Retry-After cooldown, 자동 재시도 0회 및 유료 전환 금지는 STEP 12를 재사용한다. STEP 13의 `previewTools`는 여전히 Mock 전용이며 실 모델 도구 루프는 없다.

모델 초안은 정확히 `{evidenceId:"S1",quote:<보낸 excerpt 전체와 정확히 같은 문자열>}`만 허용한다. JSON MIME만 신뢰하지 않고 중복/추가 키·잘못된 ID·설명문·수정된 인용을 거부한다. 모델의 자유 생성 문장, URL, 담당자 값은 사실로 채택하지 않는다. 최종 문구와 숫자 ID 기반 출처 URL은 서버가 작성한다. 응답 직전 검색과 externalSource를 반복해 현재 ACL/grant/게시 버전/fragment/인용 내용과 현재 세션을 다시 확인한다. 출처 링크 자체도 기존 문서 API에서 다시 인증·인가한다.

## 실행 상한·감사·실패 격리

- 단일 턴, 최대 8단계 / 읽기 도구 4회 / Provider 1회. 분류도 서버의 고정 분기다. 단계마다 10초 경과를 확인한다.
- 질문과 STEP 13 preview는 같은 HttpSession의 rolling 60초 5회 / 활성 요청 1회 제한을 공유한다. 질문·답변·결과는 세션에 저장하지 않는다. 여러 세션/서버를 합산하는 제한은 아니다.
- 요청당 출력 토큰은 설정값과 256 중 작은 값. 입력은 고정 지시문+검토 질문 200자 이하+구간 200자 이하. 실 HTTP 전체 응답 시간은 남은 10초와 설정 timeout 중 작은 값, 응답 byte cap은 32KiB다.
- 10초는 단계 검사와 HTTP timeout이다. 진행 중인 저장소/JDBC/감사 I/O를 강제 중단하는 전체 요청 watchdog은 아니다. Oracle query/pool/lock timeout과 실 지연은 별도 검증 대기다.
- 감사는 actor/model/answer kind/outcome/tool count/provider count/elapsed만 허용한다. 고정 event/resource type `AI_QUESTION`과 80자 이하 enum/count summary를 기존 audit_event에 INSERT한다. 원문/초안/인용/인자/키/예외 내용 필드는 없다. Oracle REQUIRES_NEW와 메모리 bounded 200건 adapter를 분리했다.
- 감사 실패는 고정 503으로 닫는다. Provider 429/503/504와 조정 한도 429는 공통 안전 오류 계약을 사용한다. AI 경로에만 적용하며 ChatService·REST/STOMP의 설정이나 Provider 의존성을 변경하지 않는다.

요구된 answer는 승인된 현재 사실/근거로 서버가 조립하는 일시적 응답 DTO다. 제출 질문이나 Provider 초안 원문을 HTTP로 반사하지 않는다. AI 대화 이력 DB/로그 저장, 스트리밍, 브라우저 저장소, 재시도 루프, 모델 쓰기 권한은 없다. HTTP 응답 DTO는 이 요구를 위한 명시적인 읽기 응답이며 대화 원문 저장소가 아니다.

## 검증 한계와 다음 작업

MockMvc/Mockito 및 loopback HttpServer는 실제 보안 체인, source 링크, 응답 직전 revoke/withdraw/version/classification 변경, 최소 outbound, 환각 JSON 거부, AI 실패 후 chat REST 사용 가능, 메타데이터 전용 감사와 예산을 검사한다. 전체 회귀에는 기존 실제 localhost STOMP 시나리오도 포함된다. 새 AI 실패와 동시에 진행 중인 STOMP 부하/내구성은 별도 미검증이다. UI는 MockMvc HTML 검사이며 브라우저 시각 검증은 NOT_RUN이다.

Oracle SQL 실행·ACL JOIN·JDBC record/enum mapping·동시 revoke race·독립 감사 commit/rollback·성능은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**다. 마지막 Java 재검사 이후의 변경을 원자적으로 차단한다고 주장하지 않는다. Mock 데이터는 Oracle 조회 성공의 증거가 아니다. 새 `AiQuestionOracleIT`는 명시적인 환경 opt-in에서만 기존 Oracle suite로 실행되며 아직 실행하지 않았다. 실 Gemini 및 Oracle+Gemini E2E는 NOT_RUN이다.

공식 근거: [eGovFrame 5.0 Boot 예제](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/), Google [구조화 출력](https://aistudio.google.com/docs/structured-output), [가격](https://aistudio.google.com/docs/pricing), [폐기 일정](https://aistudio.google.com/docs/deprecations), [지원 지역](https://aistudio.google.com/docs/available-regions), [프로젝트 한도](https://aistudio.google.com/docs/rate-limits)를 재확인했다. JSON 형식 지원은 의미/권한 보장이 아니므로 서버 검사를 유지한다. lite 모델의 실제 계정 접근·무료 티어는 운영자가 별도 확인해야 하며 model/SDK/framework 버전을 변경하지 않았다. 최신 Java SDK 실행 검증은 STEP 12의 해결되지 않은 의존성 제한 상태를 유지한다.
