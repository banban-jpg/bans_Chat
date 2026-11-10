# STEP 15 — 최소 웹 UI와 Oracle DB 시연

2026-11-04 KST. 통합 화면은 `GET /`이다. 기존 Thymeleaf와 네 개의 로컬 CSS/JavaScript 파일만 사용한다. 프론트 패키지 설치·번들러·CDN·무거운 UI 라이브러리·버전 변경은 없다. 기존 Java 21 / eGovFrame 5.0.0 / Boot 3.5.6, Oracle 26ai Free, 서버 세션, MyBatis, Mock 기본 Provider를 유지한다.

## 시작과 계정

모듈에서 Maven Wrapper로 빌드한 후 실행한다. 아래는 **사용자용 실행 절차**이며 명령을 제시했다는 이유로 실행 결과를 주장하지 않는다. 실제 실행한 테스트는 test-status.md를 따른다.

```powershell
# bans_chatbot에서 기존 MAVEN_HOME / Maven 캐시 설정 후
New-Item -ItemType Directory -Path local-notes -Force | Out-Null
.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package *> local-notes/build.log
$securePassword = Read-Host '가상 최초 관리자 비밀번호 (12~128자)' -AsSecureString
$env:BANS_LOCAL_ADMIN_PASSWORD = [Net.NetworkCredential]::new('', $securePassword).Password
try {
    java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=oracle --server.address=127.0.0.1
} finally {
    Remove-Item Env:BANS_LOCAL_ADMIN_PASSWORD -ErrorAction SilentlyContinue
}
```

브라우저에서 `http://127.0.0.1:8080/`을 연다. 가상 관리자 `EMP-001`로 로그인한다. 비밀번호·활성화 코드의 기본값이나 사용 가능한 값은 저장소에 없다. 기존 환경 변수 bootstrap은 로컬 메모리 계정에만 적용한다. 기본 합성 계정 12개는 활성화 대기 상태다. 관리자 화면에서 대기 계정의 코드를 발급하고 별도로 안전하게 전달한 후, 로그아웃 상태의 활성화 화면에서 새 비밀번호를 지정한다. 발급 재시도는 이전 코드를 무효화하며 자기 자신에게 발급할 수 없다.

시연에 필요한 역할은 기존 fixture를 따른다: EMP-001 관리자, EMP-005 문서 작성자, EMP-002 문서 승인자. 둘 이상의 직원을 먼저 활성화하면 1:1 대화를 시연할 수 있다. 두 사용자는 별도 브라우저 프로필/창의 독립 세션을 사용한다. 일반 탭은 쿠키를 공유한다. 신규 직원 등록은 로그인 계정·역할 생성이 아니므로 임의 신규 직원에게 바로 로그인하거나 대화할 수 있다고 가정하지 않는다.

## 여섯 화면

| 화면 | 기능과 서버 경계 |
|---|---|
| 활성화/로그인 | 일회용 코드·새 비밀번호, 로그인/로그아웃, 세션 만료 시 화면 데이터·입력·대화·코드 제거. 서버 세션/CSRF를 사용한다. |
| 직원·담당업무 | 이름/부서/업무 검색, 10건 페이지, 공개 연락처, 담당자 조회, 대화 시작. 개인정보/권한 필드를 추가 조회하지 않는다. |
| 1:1 채팅 | 대화 목록·미읽음·메시지·읽음, 실시간 전송/수신, 재연결/REST 복구, 이전 메시지. AI와 별도 연결이다. |
| 문서 | 검색·근거, TEXT/제한된 Markdown 초안, 초안 수정/새 버전/검토 요청/승인/반려/폐기/READ·APPROVE 부여·철회, 코드로 현재 게시본 조회. 모든 권한은 기존 서비스에서 확인한다. |
| AI | 외부 전송 고지·미선택 확인란·검토된 가상 질문 선택, 답변 유형·정형 사실·현재 권한 출처. 자유 질문·브라우저 API 키·실 API 직접 호출은 없다. |
| 직원 관리 | ACCOUNT_ADMIN의 합성 직원 등록·재직 상태/부서 변경·활성화 코드 발급. 문서 승인 권한을 부여하는 화면이 아니다. |

문서 검토/승인 대기함 API는 기존 계약에 없으므로 임의 전체 목록 API를 만들지 않았다. 알려진 문서 코드와 검토자/허용 직원 코드로 작업한다. 역할별 메뉴 숨김은 편의 기능이며 서버의 현재 역할·소유권·ACL·배정 검토자 판단을 대신하지 않는다. 문서 새 작성/버전은 외부 전송 플래그를 항상 false로 보낸다. 이미 승인된 공개 합성 fixture 외에 UI 입력을 외부 전송 적격으로 승격시키지 않는다.

## 채팅과 자원 한도

`stomp.js`는 현재 서버의 STOMP 1.2 CONNECT/SUBSCRIBE/SEND와 세 고정 사용자 큐만 다루는 작은 클라이언트다. WebSocket 주소는 현재 origin의 `/ws/chat`으로 고정하고, CONNECT에는 세션 CSRF를 넣는다. Spring SimpleBroker가 지원하지 않는 STOMP receipt를 기다리지 않는다. SUBSCRIBE 후 실제 REST 복구를 수행한다. 일반 메시지/읽음 확인은 기존 애플리케이션 ack DTO를 사용한다.

재연결은 최대 5회, 1/2/4/8/15초 대기이며 수동 재연결도 제공한다. 이 broker는 heartbeat를 설정하지 않았으므로 반개방 연결의 즉시 감지는 보장하지 않는다. 백그라운드에서 돌아오거나 재접속하면 최신 REST 페이지를 읽어 `(room,seq)`로 합친다. 화면 메모리는 최근 500건, 페이지는 50건, 한 번의 누락 복구는 최대 10페이지다. 이 한도보다 오래된 기록을 탐색하는 고급 이력 UI는 없다. 서버 이력을 삭제하지 않는다.

WebSocket ack가 4초 내 없으면 같은 clientRequestId로 REST 저장 여부를 한 번 확인한다. 실패 시 동일한 요청을 재확인할 수 있으며, 결과가 불확실할 때 다른 방/본문으로 바꾸지 않는다. 일반 쓰기 요청 전체를 무조건 자동 재시도하지 않는다. 사용자가 실제 대화 화면을 보고 있을 때만 읽음 위치를 갱신한다. 실제 JavaScript 클라이언트와 실제 localhost 서버의 전송/읽음/재연결/REST 복구가 Node 통합 테스트에서 통과했다. 이것은 브라우저 클릭 통과를 의미하지 않는다.

## 출력·세션·오류

모든 동적 결과는 textContent 또는 허용된 DOM 요소로 출력한다. Markdown은 제목/문단 텍스트만 표현하며 링크·이미지·임의 HTML을 실행/요청하지 않는다. 서버 게시 HTML도 기존 renderer의 h1/h2/p/pre만 읽고 엔티티를 텍스트로 복원한다. HTML을 직접 삽입하지 않는다. 공용 shell에는 CSP, no-store, no-referrer를 적용하고 JavaScript/CSS는 같은 origin에서만 로드한다. 허용 경로는 `/`와 네 개의 정확한 리소스뿐이며 `/ui/**` 전체를 익명 공개하지 않는다.

POST/PUT/PATCH/DELETE는 CSRF와 same-origin 쿠키를 사용한다. 비밀번호/코드가 URL 쿼리로 제출되지 않도록 HTML form도 method=post다. JS 요청에는 15초 timeout이 있고, 로그아웃/세션 교체 이전 응답은 버려서 새 사용자 화면에 늦게 표시하지 않는다. 브라우저 저장소·대화 히스토리·콘솔 원문 로그를 추가하지 않는다. 활성화 코드는 일시적인 보안 전달 값이며 표시 지우기/화면 전환/로그아웃 때 제거한다. Google API 키를 취급하는 기능은 브라우저에 없다.

401 세션 만료, 403 권한/CSRF, 404 숨겨진 자원, 409 상태 충돌, 429 한도, 503 서비스 불가, 504 timeout을 고정 한국어로 안내한다. AI 실패가 로그아웃이나 일반 채팅 종료를 일으키지 않는다. skip link, label, 키보드 focus, aria-live, 390px 대응 CSS를 넣었다. 좁은 화면/키보드의 실제 시각·클릭 검증은 아래 제약으로 BLOCKED이며 접근성 인증을 주장하지 않는다.

## 검증의 실제 범위

MockMvc에서 6개 화면 shell과 대응 API의 활성화→로그인→검색→대화/읽음→문서 작성/승인→AI→관리자 변경 흐름을 실행했다. 실제 HTTP/WebSocket 서버에는 화면에 배포되는 JavaScript STOMP 클라이언트를 연결해 전송/읽음/재접속/누락 복구 및 문서/AI 출처를 검증했다. API client의 401/503/stale response, 안전한 Markdown/게시본 텍스트 출력, 403/429 메시지 매핑도 검사했다. 503 브라우저 클릭 UI나 실제 브라우저 XSS 실행을 통과했다고 주장하지 않는다.

Chrome headless/CDP 시나리오를 네 번 실제 시도했으나 GPU 프로세스 종료 및 Page.enable timeout으로 앱 클릭 이전에 실패했다. **BLOCKED: CHROME_RENDERER_CDP_UNAVAILABLE**. 최종 기본 테스트에서는 이 선택적 테스트 1건이 환경 gate로 SKIPPED다. 브라우저가 동작하는 환경에서만 아래 선택적 명령을 사용한다.

```powershell
$env:BANS_UI_BROWSER_TEST = 'YES'
.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' '-Dtest=WorkbenchBrowserTest' test
Remove-Item Env:BANS_UI_BROWSER_TEST
```

이 명령의 성공은 아직 확인하지 않았다. 정상 테스트는 실제 Oracle/Gemini 호출 0회다. Oracle 검증은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, 실제 Gemini/Oracle E2E는 NOT_RUN이다. 정적 자산 4개는 약 25KB이고 shell까지 합쳐 64KiB 미만을 검사했다. 이는 실 DB/다중 사용자 응답 시간 또는 성능 벤치마크가 아니다.

공식 근거: [eGovFrame Boot 예제](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/), [Spring STOMP SimpleBroker](https://docs.spring.io/spring-framework/reference/6.2/web/websocket/stomp/handle-simple-broker.html), [Spring broker 한계](https://docs.spring.io/spring-framework/reference/6.2/web/websocket/stomp/handle-broker-relay.html). Google 가격·지역·모델 폐기·프로젝트 한도도 gemini-free-tier.md의 공식 문서로 재확인했다. 모델 allowlist/SDK/무료 티어 정책은 바꾸지 않았으며 실제 프로젝트의 무료 접근 성공을 확인한 것은 아니다.

2026-11-09 현재 Oracle 프로필에서 HTTPS 로그인 화면과 CSRF 응답은 실제 서버로 확인했다. 기존 local-mock 시연 절차와 구별되는 Oracle 최초 관리자 활성화, 실행 명령, 남은 UI 검증은 [Oracle UI 시연 안내](oracle-ui-demo.md)를 따른다. 위의 `ORACLE_26AI_NOT_INSTALLED` 표기는 과거 STEP 15 당시의 결과다.

이후 최초 관리자 대기 상태의 혼란을 줄이기 위해 Oracle 첫 화면은 초기 설정 안내를 보여주고 로그인 폼을 숨기도록 수정했다. 실제 Oracle HTTPS 응답에서 안내와 숨김 상태를 확인했다. 활성화가 끝난 뒤 새로고침하면 정상 로그인 화면으로 전환된다. 활성화와 로그인 자체는 아직 실행하지 않았다.

최초 관리자 비밀번호 입력 방법은 로컬 프로퍼티로 변경했다. `bans_chatbot/oracle-local.properties`의 `BANS_ORACLE_FIRST_ADMIN_PASSWORD`에 설정하면 첫 Oracle 기동에서만 활성화하고, 이후 기동은 기존 계정을 유지한다. 대기 상태 화면도 PowerShell 명령 대신 이 파일과 재시작 절차를 안내한다.

2026-11-10: 로컬 설정값의 길이 때문에 Oracle 기동이 중단되던 문제를 수정했다. 합성 시연 전용 명시적 허용값과 로컬 HTTPS/Mock AI/전용 스키마 조건에서만 4자 이상의 최초 비밀번호를 허용한다. 실제 Oracle HTTPS에서 EMP-001 로그인과 관리자 세션을 확인했고 재시작 후 재로그인도 성공했다. 로그인 401은 화면에서 자격정보 오류로 표시하며 보호 API의 401만 세션 만료로 처리한다. 전체 브라우저 클릭 검증은 별도다.


## Chat UI redesign (2026-11-10)

2026-11-10 채팅 화면: 대화 목록 검색, 미읽음 배지, 선택 대화 강조, 새 대화 시작, 좌우 말풍선, Enter 전송/Shift+Enter 줄바꿈, 이전 기록을 불러올 때 스크롤 위치 유지 기능을 제공한다. 직원 검색에서 대화하기를 누르거나 채팅 화면의 새 대화 시작을 사용한다. API와 STOMP 목적지는 기존 계약을 사용한다.
