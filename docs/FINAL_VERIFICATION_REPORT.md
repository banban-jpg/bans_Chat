# STEP 19 프로젝트 최종 종합 점검

기준: 2026-11-04 KST / dev / HEAD 88836b4. 기존 STEP16~18 미커밋 변경을 보존하고 **현재 source·설정·실제 실행 결과로 재점검**했다. 과거 대화의 PASS를 이번 실행으로 가져오지 않았다. 새 업무 기능/API/인증 정책/DDL/의존성 변경은 없다.

**판정: 오프라인 기능 구현 및 회귀 검증 가능. Oracle 포함 최종 기능·보안·성능 검증은 보류.** 완성도를 추측성 퍼센트로 표시하지 않는다.

## 1. 개요와 기술 스택

가상20명/5부서/12pending 계정의 학습·포트폴리오용 사내 메신저다. 1:1 채팅, 합성 직원/담당업무 조회, 텍스트/Markdown 문서 승인·검색·ACL, 검토된 합성 질문의 AI 근거 답변을 제공한다. 그룹/첨부/자유 AI 대화/유료 전환은 범위 밖이다.

실제 최종 실행 JAR의 BOOT-INF/lib와 POM을 확인했다.

| 구성 | 현재 적용/근거 |
|---|---|
| 빌드/런타임 | Temurin Java21.0.3, Maven Wrapper3.9.6, Windows11 x64 |
| 전자정부/Boot | eGovFrame parent·실제 rte 모듈5.0.0 / Boot3.5.6 |
| Spring | Framework6.2.11 / Security6.5.5 |
| MyBatis | MyBatis3.5.19 / mybatis-spring3.0.5 |
| UI | Thymeleaf3.1.3.RELEASE + 자체 ES module JS/CSS, native WebSocket/STOMP |
| JDBC pool | commons-dbcp2 2.13.0 |
| 최종 영구 DB | Oracle AI Database26ai Free; oracle Maven profile만 ojdbc17:23.26.4.0.0 추가. 이번 JAR/기본 test는 DB driver/서버 불필요 |
| AI | Java HttpClient Gemini Developer REST adapter, 기본 MockGeminiProvider. SDK/실 계정 모델 접근·무료 quota 검증은 미완료 |

버전 변경/취약점 DB 조회/CVE 정밀 스캔은 수행하지 않았다. JAR에 라이브러리가 있다는 사실과 해당 기능이 업무 흐름에 사용된다는 사실은 구별한다.

## 2. 구조·구현 현황

생산 Java124개, 테스트 Java73개를 inventory에 포함했다. 전체 Java 텍스트의 package/import/annotation/참조와9개 Mapper XML의 namespace·메서드명55개 statement·설정·DDL·fixture·UI를 조사하고, 핵심 보안/서비스/저장 흐름은 해당 구현을 읽어 대조했다. 전 파일의 모든 의미/가능한 실행 경로를 증명한 정적 분석은 아니다. 소스 해시 목록은 verify-source의 ignored inventory에 보존한다.

| 도메인 | Java 수 | 실제 구성/경계 |
|---|---:|---|
| auth | 25 | Auth/ActivationAdmin Controller, BCrypt/활성화/잠금/세션, Account/Activation port+Oracle/Mock adapter+Mapper |
| organization | 14 | 직원·담당자·부서/Admin Controller, OrganizationService/DTO/Mapper; 초기 fixture용 별도 demo directory |
| chat | 12 | REST Controller/Service/port/Oracle·Mock/Mapper + STOMP3클래스; room seq/요청키/읽음 |
| document | 22 | 문서 lifecycle/검색/현재 출처/ACL/버전/소유자·지정 승인자, workspace/search port와 Oracle·Mock/Mapper |
| ai | 24 | 질문/도구 Controller, 고정 policy/registry/실행 budget, ChatProvider/Mock/REST Gemini, outbound 정책 |
| audit | 12 | 직원변경·AI 도구/질문 metadata event/port/Oracle·Mock/Mapper; 감사 조회 UI/API는 없음 |
| common | 7 | ApiResponse/ApiError/예외 handler/서버 requestId/fixture loader/workbench shell |
| global | 7 | Spring Security/session actor 갱신, profile guard, Oracle fail-fast/MyBatis wiring, health |
| bootstrap | 1 | 루트 EgovBootApplication만 |

HTTP Controller10개+STOMP Controller1개, @Service9개, @Repository18개다. top-level controller/service/repository 계층 패키지는 없다. Mapper 인터페이스는 도메인 내부이며 XML은 `src/main/resources/egovframework/example/<domain>/mapper`다.9개 namespace가 실제 인터페이스/statement 메서드와 일치했다. `${}` 텍스트 SQL 치환은 없고 `#{}` bind와 고정 sort 분기를 사용한다. 이것은 **SQL 소스/파싱 검사**이며 실제 Oracle 반응은 미검증이다.

DB 원본은 `bans_chatbot/db/oracle`: V001~V005,18테이블, 인덱스/제약/trigger와 별도 seed/manifest. canonical 데이터는 `src/main/resources/fixtures/*.psv`다. normal test의 schema/seed/Mapper source checks는 PASS이나 Oracle에서 실행하지 않았다. 프로필은 test/local-mock의 비영속 어댑터와 oracle의 실제 DB 어댑터로 분리되며 자동 SQL 초기화는 never다.

## 3. API 집계와 자동 명세

| 범위 | 전체 | GET | POST | PUT | PATCH | DELETE |
|---|---:|---:|---:|---:|---:|---:|
| 선언된 Controller HTTP 매핑(local-mock) | **39** | **18** | **16** | **2** | **2** | **1** |
| 그중 JSON REST API | **36** | **15** | **16** | **2** | **2** | **1** |
| HTML 화면 | 3 | 3 | 0 | 0 | 0 | 0 |

HTML은 `/`, `/ai/questions`, `/ai/tools`다. health2개는 JSON REST에 포함한다. 조건부 `/api/demo/employees`는 local-mock만 존재하므로 oracle은 MVC38/JSON35다. 정적4파일, Boot framework `/error`, 자동 HEAD/OPTIONS, WS handshake는 REST 수에 포함하지 않는다. server.servlet.context-path/API prefix는 설정돼 있지 않다.

**WebSocket/STOMP 구체적 경로6개:** 연결 `/ws/chat`1개 + SEND `/app/chat.send`, `/app/chat.read`2개 + SUBSCRIBE `/user/queue/chat.messages`, `/user/queue/chat.ack`, `/user/queue/chat.errors`3개. prefix 자체를 추가 API로 세지 않았다.

[API_ENDPOINTS.txt](API_ENDPOINTS.txt)는 실제 source annotation을 추출하고 Spring RequestMappingHandlerMapping runtime 목록과 정확히 대조해 생성한다. method별/도메인/고유번호/DTO·검증·권한·CSRF·관측상태·Controller 파일/줄을 담고 UTF-8 BOM으로 저장했다. 경로 추출/집계는 자동, 서비스 의미/권한/DTO 세부 metadata는 현재 source를 읽고 검토한 고정 catalog다. 알 수 없는/새 mapping은 추측하지 않고 생성 실패한다.

## 4. 실제 실행 증거

모든 Maven 명령은 Windows wrapper/기존 MAVEN_HOME/로컬 `.maven-repo`를 사용했다. 전체 환경·명령은 [RUNBOOK.md](RUNBOOK.md), 개별 testcase는 [TEST_RESULTS.md](TEST_RESULTS.md)다.

| 실제 실행 | 결과/시각(KST) | 근거 |
|---|---|---|
| Java/Maven -version | PASS /19:09 | Java21.0.3/Maven3.9.6 |
| 최초 clean test package | PASS /19:10:08 | step19-build.log; 신규 HTTP test 추가 전 기준, 최종 집계와 중복 합산 안 함 |
| 신규 FinalApiHttpVerificationTest focused 실행 | PASS1건 /19:12:33 | step19-http-test.log |
| 최종 `scripts/local.ps1 -Action build -Offline -MavenRepository $cache` | **PASS /19:17:23**, 124 main/73 test compile, **288선택=285 PASS+3 SKIPPED, FAIL/ERROR0**, executable JAR | step19-build-final.log, step19-baseline-reports/56개 XML |
| 기본 suite의 실제 local HTTP | PASS /19:17:06 | FinalApiHttpVerificationTest,65요청. MVC39각각 성공+static4+negative. step19-http-evidence.json에는 method/template/status만, body·쿠키·코드 없음 |
| 명시적 track-a.ps1 | **PASS2건 /19:19:25** | step19-load.log, step19-load-reports. 기본 skip2개와 동일 testcase의 별도 실행 |
| JAR smoke-local.ps1 | **PASS**,6 route 검사+Mock banner, 임시 포트/localhost/기동 후 종료 | step19-smoke-final.log; 최종 EvidencePrefix로 이전 단계 로그 덮어쓰기 방지 |
| API 명세 생성 | PASS | source/runtime39일치, JSON36/HTML3, 각 매핑 성공 HTTP evidence 존재 |
| 실제 JUnit 결과 문서 생성 | PASS | XML testcase별 PASS/FAIL/SKIP 추출; 실행하지 않은 case 성공 생성 없음 |
| 제한 시크릿·문서 링크·예제 빈 값 / PS AST·JS/Python 구문 / diff | PASS(최종 점검 기록 참조) | step19-source-check.log, step19-static-check.log |

최종 source 검사(19:32 KST)는295파일/텍스트281개/위반0이며, PS AST5개·JS구문6개·Python AST2개와 diff 검사가 통과했다. API_ENDPOINTS의 UTF-8 BOM(EFBBBF)을 확인했다. 이는 제한 패턴 검사이며 모든 비밀의 부재를 보증하지 않는다. smoke의 자기 프로세스 PID23728 종료도 확인했다.

기본 SKIPPED3개는 Chrome1+Mock 부하/장애2다. 부하2건은 별도 실행해 PASS했지만 baseline skip을 덮지 않았다. **이번 실행된 JUnit 실패0건**이다. 루프백 장애 테스트의 의도한429/503/timeout/JSON오류는 기대대로 처리돼 PASS한 시험이며, 테스트 FAIL과 구별한다. 실제 Oracle13 class는 BLOCKED, 실 Gemini는 SKIPPED(실행 시도 없음/NOT_RUN), Chrome opt-in은 이번 STEP19 재실행하지 않았다. 이 숫자들은 서로 다른 분모이므로 임의 합산하지 않는다.

서버 boot/health PASS는 DB readiness가 아니다. actual HTTP65요청은 모든 권한 조합/모든 실패 분기/실제 브라우저 DOM·화면 paint를 검증한 것이 아니다. Node JS/실제 HTTP/STOMP 시나리오는 normal suite에서 실행됐으며 실제 Chrome 클릭과 구분했다.

## 5. Mock 부하·장애 관측

**Mock 환경 결과: DB 포함 안 됨.** 19:19:11~19:19:25 KST, Ryzen7 7800X3D/16 logical CPU/RAM33444917248bytes/Windows10.0.26200/Node22.17.0, 동일 PC loopback. 연결10/20/30×3 burst, 고유 직원 최대12명. warmup 없이 고정 증가 순서다. 실제 Oracle/Gemini0회.

HTTP2763회/의도한 오류응답45(1.629%)/예상 밖 HTTP오류0, 최대STOMP연결30개. latency는 짧은 합성 요청 관측이며 SLA/Oracle 동시 사용자 수가 아니다.

| 작업 | n | p50 ms | p95 ms |
|---|---:|---:|---:|
| 로그인 | 180 | 76.735 | 190.479 |
| 직원 검색 | 180 | 5.565 | 13.729 |
| 문서 검색 | 180 | 6.146 | 14.375 |
| STOMP connect | 180 | 32.904 | 117.433 |
| STOMP ack | 180 | 38.351 | 61.834 |
| STOMP 상대 수신 | 180 | 4.190 | 28.204 |
| 정형 AI | 180 | 6.721 | 26.788 |
| 재연결 전체 | 9 | 1034.095 | 1043.734 |

Provider loopback429/503/잘못된JSON/timeout 각3회에서 요청각1회/자동재시도0을 검증했다. timeout p50/p95=163.605/167.229ms(모의150ms budget), 동시20시도에서 wire1/AI_BUSY19. 이 작은 표본으로 실제 외부 장애 복구 성능을 주장하지 않는다. 원본은 ignored step19-track-a.json/step19-failures.json이며 기존 STEP17 runner의 출력명을 복사해 보존했다.

## 6. 프론트·백엔드 연동 전수 대조

`src/main/resources/static/ui/app.js`, `api.js`, `stomp.js`, `templates/workbench.html`, AiQuestionController/AiToolController의 보조 HTML fetch를 조사했다. 다음31개 JSON 경로-method는 main app의 모든 정적·동적 호출이다. 보조 도구 화면의2개 추가 API도 확인했다.

| 호출 코드/범위 | method·실제 path | 계약 점검 |
|---|---|---|
| api.js / auth form | GET csrf, GET me, POST login/activate/logout (`/api/auth/*`) | data envelope, MeView roles, password/code 필드, same-origin cookie/CSRF header 일치 |
| directory/담당업무 | GET `/api/employees`, `/api/departments`, `/api/tasks/owners` | 검색 query/form name/departmentId/task/page/size, Page.items/total와 Department 코드/이름 일치 |
| chat | GET/POST `/api/chat/rooms`, GET/POST `/api/chat/rooms/{id}/messages`, PUT `/api/chat/rooms/{id}/read-state` | conversationId→숫자 roomId, Message/Room/cursor/read DTO 일치. clientRequestId 동일키 재확인 |
| documents | POST `/api/documents`, POST `/{code}/versions`, PUT `/{code}/versions/draft`, POST `/{code}/review-request`, POST `/{code}/approve`, POST `/{code}/reject`, POST `/{code}/withdraw`, POST/DELETE `/{code}/grants`, GET `/{code}/published`, GET `/search`, GET `/{id}/versions/{versionId}/fragments/{fragmentId}` | 앞에 `/api/documents`; workflow action allowlist·JSON fields·READ/APPROVE·q·Published/Search/Source DTO 일치. externalAiAllowed=false 고정 |
| ai | GET `/api/ai/questions/examples`, POST `/api/ai/questions` | notice/questions, question body, Answer answerType/facts/sources/requestId 일치. source.url은 정규식으로 내부 양수ID 경로만 허용 |
| admin | POST `/api/admin/employees`, PATCH `/api/admin/employees/{id}/status`, PATCH `/{id}/department`, POST `/api/admin/activations` | 뒤 PATCH도 /api/admin/employees prefix. 직원 필드·부분수정·Issued.code 일치; 계정/역할 생성 아님 |
| 보조 화면 | GET `/api/ai/tools`, POST `/api/ai/tools/preview`; questions/examples/csrf도 재사용 | catalog 예제/도구calls/Preview count 일치. 보조 HTML은 main shell과 별개 |
| stomp.js | /ws/chat, SEND2/SUBSCRIBE3 | 서버 고정 destination/payload와 일치 |

**선언 경로·method·body/parameter·응답 필드의 명백한 불일치는 발견하지 않았다.** health/demo는 main UI 호출이 없지만 구현된 API다. demo는 초기 fixture이므로 현재 admin 수정 결과 조회에 사용하면 안 된다. 실제클릭/레이아웃/키보드·좁은 화면/브라우저CORS·쿠키 전체 동작은 별도 검증 보류다. REST 전송 fallback의 실시간 알림 한계는 아래 위험표에 별도 기록했다.

## 7. 보안 점검

| 항목 | 실제 근거 / 현재 판단 |
|---|---|
| 비인증/CSRF/세션고정/만료·비활성 | SecurityConfiguration/CurrentActorFilter/AuthController + AuthFlowMockMvcTest/SessionBoundaryMockMvcTest/AttackRegressionMockMvcTest 및 신규 live HTTP401/403/logout. 오프라인 PASS |
| IDOR/자기권한승격/문서철회 | Chat/ManagedDocument 서비스 현재 사용자/참여/소유·명시grant; ChatMockMvcTest/ManagedDocumentMockMvcTest/AiToolSecurityTest/AttackRegressionMockMvcTest; 신규 live타인방404/타인문서404/revoke 직후published·fragment404. 오프라인 PASS |
| STOMP | handshake 세션principal/same-origin/CONNECT CSRF/고정 destination; inbound/outbound 현재 계정/자격증명 재검사. ChatStompE2ETest 실제 루프백 PASS. DB commit race는 BLOCKED |
| SQL injection |9 XML에 `${}` 없음/고정sort/bind, 소스 계약 테스트 및 literal 공격 입력 PASS. Oracle DB에서의 해석·락·실제 ACL JOIN은 BLOCKED |
| XSS/Markdown | 서버 escape/제한적heading renderer, UI textContent/내부 source path 제한, root CSP; MockMvc·신규HTTP `<script>` escape/Node script tests PASS. 실제 DOM 전부 검증 아님 |
| AI prompt injection/반출 | QuestionPolicy exact synthetic allowlist/ToolRegistry schema+identity무시/현재ACL·version·external·synthetic·nonSensitive/응답전 재검사. OutboundSecurityRegressionTest의 loopback 수신bytes/금지payload미포함·무전송·키/원문 sentinel검사 PASS |
| 비밀·원문/예외 | 환경 key/고정endpoint/원인 예외 보관 안 함/고정ApiError·STOMP error/metadata audit. 현재 HEAD/index/worktree 제한패턴 검사. 로그전체·전체과거Git·CVE/DAST 인증 아님 |
| 권한/백업/운영 | 관리자 역할은 문서 ACL 우회 불가. Oracle 첫admin/TLS/내부망/실DB 감사 내구성·백업복구는 미완료 |

로컬 합성 fixture와 loopback 모의 서버에만 시험했다. 실제 외부 운영 서비스 공격/실 Gemini 호출/실 Oracle 연결은 없다. **오프라인 회귀에서 확인된 Critical 결함0건**이나 Oracle 의존 항목을 포함한 최종 보안 인증은 할 수 없다.

## 8. 발견한 문제·미사용 후보·우선순위

새 업무 계약/정책/스키마 변경이 필요한 항목은 수정 제안만 남겼다. 아래 줄 번호는 현재 source 기준이며 추측성 취약점 확정이 아니다.

위험표의 `auth/...`, `organization/...`, `chat/...`, `ai/...`, `common/...`, `global/...` 경로는 **bans_chatbot/src/main/java/egovframework/example/** 기준이다. `src/main/resources/...`, `db/oracle/...`, `pom.xml`은 **bans_chatbot/** 기준, `AGENTS.md`는 저장소 루트 기준이다.

| ID/우선순위 | 근거 파일·실제 상태 | 영향 / 후속 조치 |
|---|---|---|
| R01 High | `auth/repository/OracleAccountRepository.java`, `auth/service/AccountAuthenticationService.java` issueActivation, `db/oracle/seed/S001__synthetic_company.sql:133` | pending 첫 관리자에 대해 자기 코드 발급 거부, Oracle에는 안전한 bootstrap 경로 없음. 앱 로그인 인수 차단. 신원·일회성·감사·재실행 거부를 갖춘 별도 bootstrap 설계·구현 승인 필요 |
| R02 High | Oracle IT13개, `global/config/OracleDataSourceConfiguration.java`, `db/oracle` | 실제 SQL/제약/락/영속성/부하/복원 미검증. 설치 후 체크리스트 순서로 수락; Mock PASS로 대체 금지 |
| R03 Medium | `chat/controller/ChatController.java:22` vs `chat/websocket/ChatStompController.java` send | REST 저장 경로에 STOMP publish 없음. WS 실패/ack 확인 fallback으로 새 저장하면 상대 즉시 수신 보장 없음. 중복 알림/commit 이후 보장을 고려한 공통 발송 설계를 별도 제안; 이번 계약 변경 없음 |
| R04 Medium | `auth/repository/InMemoryAccountRepository.java:8,24,63` ↔ `organization/service/OrganizationService.java:5` | auth→organization 조회와 organization→auth 타입 의존으로 소스 도메인 양방향 참조 존재. 실제 bean boot 순환 오류는 재현되지 않음. fixture/현재 직원 조회 계약의 의존 방향을 후속 설계에서 단순화 |
| R05 Medium | `auth/controller/AuthController.java:94-96` | login employeeCode/password 및 activation employeeCode/code에 DTO 최대 길이 제한 없음. 전체 요청크기 제한도 명시돼 있지 않음. 기존 CSRF/로그인 잠금과 별도로 payload/입력 상한 설계·호환성 검토 필요; DoS 취약점 실증을 뜻하지 않음 |
| R06 Medium | `ai/question/AiQuestionService.java` uniqueSource/provider/재조회, `document/service/DocumentSearchService.java` externalSource | 현 시점 재검사 있으나 조회→전송/응답의 동시 revoke 원자성은 미증명. 실제 두 연결 Oracle race 검사 필요 |
| R07 Medium | `chat/websocket/ChatWebSocketConfiguration.java` SimpleBroker, `static/ui/stomp.js` heartbeat/retry | 비내구singlebroker/heartbeat없음, 장시간·반개방·다중서버·프로세스복구 보장 없음. 운영확장 전 계측/설계 필요 |
| R08 Low | `ai/client/AiAnswerPort.java`, `MockGeminiClient.java`, `auth/service/RolePolicy.java`, `auth/domain/EmployeeRole.java` | legacy port는 구현체/테스트 wiring만, RolePolicy·EmployeeRole의 생산 호출/참조 없음(단순 이름 참조 조사). 자동삭제하지 않음. reflection외부사용 가능성·테스트 목적 확인 후 후속 정리 후보 |
| R09 Low | `organization/controller/SyntheticEmployeeController.java`, `organization/repository/InMemoryEmployeeDirectory.java` | demo는 초기 불변 fixture, 새 OrganizationRepository와 별도. 실제 관리 변경과 값 다를 수 있음. main UI는 demo 호출 안 함; 명세에 초기 데모 전용으로 표시 |
| R10 Low | `src/main/resources/db/sampledb.sql:1` | 템플릿 HSQL memory/sample SQL 잔존. 현재 spring.sql.init.mode=never/사용 경로 참조 없음. Oracle migration 아니며 실행 금지. 불필요 샘플 제거는 별도 정리 제안; SQL dialect를 수정하지 않음 |
| R11 Low | `pom.xml:66,70,74,118`, `application.properties` open-in-view | idgnr/property/reactive/Lombok의 생산 직접 API/annotation 사용은 확인 못함; starter 전이 의존성/예제 목적일 수 있어 제거 후보일 뿐. JPA 사용 없음에도 open-in-view 설정 잔존. 의존성 변경 없이 후속 영향 분석 |
| R12 Low | `global/security/SecurityConfiguration.java`와 `auth/service/PublicEndpointPolicy.java` | 공개 경로 목록 중복 유지. 현재 대조/테스트상 불일치 없음. 단일 정의 또는 일치 회귀 강화 제안 |
| R13 Low | `common/web/WorkbenchController.java` vs AiQuestionController/AiToolController page | 엄격 CSP는 root shell에서만 설정, 보조 HTML은 inline JS 사용. 현재 textContent/서버 source라 XSS 결함을 실증한 것은 아님. 공통 CSP/스크립트 분리 후 정책 적용 제안 |
| R14 Low | `AGENTS.md` Current layout, 기존 단계별 docs 하단 | audit 미생성/임시 세션 interceptor라는 과거 설명은 현재 코드와 다름. 현재는 audit12클래스와 Spring Security chain+actor bridge가 있음. 지침 자체를 임의 변경하지 않고 최신 보고서/문서 절을 우선 참조 |

로그 설정의 jdbc.sqltiming category도 남아 있으나 현재 JDBC logging proxy를 쓰지 않는다. 추후 SQL bind/wire logging 도입 시 원문/비밀 누출 검토가 필요하다. 발견되지 않은 결함이 없다는 보장은 하지 않는다.

## 9. 이번 STEP에서 실제 수정한 범위

- 신규 `FinalApiHttpVerificationTest.java`: 실제 loopback HTTP65요청/전 매핑 성공/negative와 runtime inventory. 업무 기능 변경 없음.
- 신규 `scripts/generate-api-docs.py`, `summarize-verification.py`: 실제 source/runtime/XML 근거 문서 생성 도구. 새 라이브러리 없음.
- `scripts/verify-source.ps1`: 새 산출물 `.txt`와 생성 도구 `.py`도 검사하도록 누락 확장자 보완.
- `scripts/smoke-local.ps1`: 단계18 로그명이 고정돼 재실행 시 과거 로그를 덮던 도구 문제 수정. 검증된 EvidencePrefix로 실행별 stdout/stderr 구분, 같은 Mock smoke 재실행 PASS. 폴더 경계를 넘는 prefix는 거부.
- `.gitignore`: Python 캐시만 추가, 한글 주석. 공용 API/검증 문서는 Git 대상, 실제 로그/메모는 기존 ignored local-notes.
- 요청한5문서와 README/기존 decisions/api-contract/handoff/test-status/Oracle backlog를 현재 결과로 연결.

컴파일/import 오류나 경로·DTO 불일치에 대한 생산 코드 수정은 **0건**이다. 위 R01~R14는 임의 기능/정책 변경 없이 해결 제안·검증 보류로 남겼다. 재현을 위한 shell 조사 중 잘못된 glob/기본 CP949 JSON 읽기 등 도구 호출 오류는 올바른 경로/UTF-8로 재확인했으며 앱 테스트 FAIL로 계산하지 않았다.

## 10. 최종 인수 상태와 추가 작업

| 대상 | 구현 상태 | 이번 검증 |
|---|---|---|
| 인증·직원·채팅·문서·AI Mock·최소 UI | 구현 완료(문서화된 MVP 범위) | 단위/MockMvc/실 JS loopback/전 MVC path 정상 HTTP 검증 완료 |
| Oracle port/Mapper/DDL/seed | 코드 구현, 실제 동작 보류 | 소스 검사 완료 / 실제 SQL BLOCKED |
| Oracle 첫 관리자 | 미구현 | 로그인 전체 인수 차단 |
| Gemini REST adapter/반출 통제 | 구현 | 모의 HTTP/실패/금지반출 회귀 완료; 실 Gemini/SDK/계정 무료 설정 보류 |
| UI 실제 브라우저 전체/모바일·키보드 | 코드 구현, 실제 동작 부분 검증 | MockMvc/Node 완료, 실제 Chrome 전체 클릭 이번 SKIPPED |
| DB 포함 보안·성능·백업/복구·TLS 운영 | 부분 준비/실환경 구성 미완료 | 검증 보류 |

인수자는 [RUNBOOK.md](RUNBOOK.md)→[API_ENDPOINTS.txt](API_ENDPOINTS.txt)→[TEST_RESULTS.md](TEST_RESULTS.md)를 사용한다. 우선 R01 최초 관리자 초기화의 안전한 설계를 확정하고, 설치/변경 별도 승인 아래 [Oracle 체크리스트](ORACLE_VERIFICATION_CHECKLIST.md)의 migration/seed/제약·락/API영속성/ACL/STOMP commit/AI검색/부하·복구를 실제26ai에서 수행한다. 그 전 최종 DB/운영 준비 완료 선언은 보류한다.

현재 모든 변경은 미커밋이다. 설치/DB변경/배포/실 API 사용/실 운영 공격은 수행하지 않았다.
