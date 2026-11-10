# 실행 환경과 두 가지 경로

현재 수락 범위는 **오프라인 구현·테스트·Mock 시연**이다. **Oracle 포함 최종 기능·보안·성능 검증은 미완료**다. 최초 실행자는 README부터 읽고, 날짜별 과거 결과를 현재 실행 결과로 바꾸어 해석하지 않는다.

## A — Oracle 없이 지금 실행

Java 21과 Git, Maven Wrapper가 필요하다. eGov 개발 PC는 Java 21, resolved parent는 eGovFrame 5.0.0/Boot 3.5.6을 사용한다. [공식 eGov Boot 예제](https://www.egovframe.go.kr/docs/5.0/egovframe-development/deployment-tool/build-tool/gradle/boot-to-gradle/)의 Boot 조합을 유지했다. JAR/module smoke가 실제 eGov 모듈 적용을 검사한다. Maven distribution은 wrapper에 3.9.6으로 고정돼 있다. 버전을 임의로 바꾸지 않는다.

이 PC의 오프라인 명령(저장소 루트):

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cache = 'C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
java -version
.\bans_chatbot\mvnw.cmd -version
.\scripts\verify-source.ps1
.\scripts\local.ps1 -Action test -Offline -MavenRepository $cache
.\scripts\local.ps1 -Action build -Offline -MavenRepository $cache
.\scripts\smoke-local.ps1
.\scripts\local.ps1 -Action run
```

test는 clean test, build는 clean test package다. 일상 검증에서는 build만 실행하면 테스트를 중복 실행할 필요가 없다. 다른 PC에는 위 캐시/설치 경로가 없을 수 있다. JDK 21을 PATH에 설정한 후 기본 wrapper build를 온라인으로 먼저 준비한다. 의존성 부재/다운로드 실패를 버전 변경이나 SQL 변경으로 우회하지 않는다. offline은 모든 필요한 artifact가 있을 때만 성공한다. Node 22는 `WorkbenchClientIntegrationTest` 및 별도 부하 실험에 필요하며 이 PC의 테스트는 `C:/Program Files/nodejs/node.exe`를 사용한다. 미설치 시 Node 의존 case는 명시적으로 SKIPPED한다.

run은 Oracle 프로필과 Mock AI Provider로 루프백 HTTP 8080에서 실행한다. `bans_chatbot/.env`가 필요하다. 8080에는 Oracle 서버만 실행한다. smoke-local은 배포 시연이 아닌 오프라인 테스트 도구다.

기본 health는 HTTP 프로세스 상태일 뿐 DB readiness가 아니다. `/`에 가상/영구 저장 아님 라벨이 있어야 한다. 일반 테스트에서 실제 Oracle/Gemini 0회이며 localhost HTTP/WebSocket은 실제 네트워크 검증이지만 외부 Gemini 호출이 아니다. `BANS_MOCK_LOAD_TEST`와 `BANS_UI_BROWSER_TEST`는 기본 테스트에서 NO 또는 미설정으로 둔다. Chrome 환경 문제는 [한계](known-limitations.md)를 따른다.

## 역할별 시연

1. EMP-001 관리자 로그인→직원 관리에서 EMP-005/EMP-002 등 기존 pending 계정 코드 발급.
2. 안전하게 별도 전달 후 활성화 화면에서 본인이 비밀번호 지정→로그인. 재발급은 이전 코드 무효, 코드/표시는 사용 후 제거.
3. 직원/담당자 검색→독립 브라우저 프로필의 다른 활성 직원과 1:1 채팅→재접속/읽음 확인.
4. EMP-005 문서 작성→EMP-002에 READ와 APPROVE 부여→검토 요청→EMP-002 승인. 소유자 자기 승인은 금지.
5. 문서 검색/근거→READ 철회 후 기존 링크 거부 확인→AI 검토 예제/출처 확인.

20명 모두 로그인 계정이 있는 것은 아니다. 계정 fixture 12개는 기본 pending이다. 신규 직원 등록은 계정/역할을 자동 발급하지 않는다. 관리자와 AUDITOR는 문서 ACL 우회 권한이 아니다. 동일 브라우저 일반 탭은 세션 쿠키를 공유한다. 실제 UI 클릭 전체 검증은 이번 환경에서 BLOCKED이며 이 절차의 성공을 가정하지 않는다. 자동 MockMvc/Node 시나리오 증빙은 test-status.md에 있다.

## B — Oracle 설치 후

Oracle 접속값을 PowerShell에 입력하지 않으려면 `.env.example`을 같은 폴더의 `.env`로 복사한 뒤 URL(호스트·포트·서비스명), 계정, 비밀번호를 채운다. 실제 파일은 `.gitignore`에서 제외된다. `bans_chatbot` 폴더를 작업 디렉터리로 하여 `oracle` Spring 프로필로 실행하면 해당 파일을 읽는다. 파일이 없으면 시작 스크립트가 실패한다. Oracle JDBC 드라이버가 포함된 Maven `oracle` 프로필 역시 필요하다.

[Oracle 설치·검증 체크리스트](oracle-install-validation.md)의 단계를 별도로 따른다. `.env.example`은 변수 이름만 제공하며 자동 로드하지 않는다. Oracle 프로필은 JDBC 드라이버 포함 Maven 빌드와 실제 URL/계정/비밀번호를 요구한다. DB가 없으면 fail-fast하며 Mock으로 전환하지 않는다.

현재 Oracle 시연은 127.0.0.1:8080 루프백 HTTP와 Secure=false 쿠키를 사용한다. 외부 네트워크 공개는 별도 TLS 및 보안 검증 전까지 하지 않는다.

Oracle admin pending seed를 처리하려면 Git에서 제외된 `bans_chatbot/.env`의 `BANS_ORACLE_FIRST_ADMIN_PASSWORD`에 12자 이상의 비밀번호를 설정하고 Oracle 서버를 시작한다. 최초 한 번만 EMP-001을 활성화하며 이후에는 기존 계정을 유지한다. 로컬 HTTP 실행 절차는 [Oracle UI 시연 안내](oracle-ui-demo.md)에 정리했다. 실제 활성화와 Oracle 로그인 시연이 수행되기 전까지는 NOT_RUN이다. 테스트의 provisionForTest나 rollback 안의 dummy hash를 운영 초기화에 재사용하지 않는다.

2026-11-10 현재 전용 로컬 합성 시연 스키마에서는 `BANS_ORACLE_ALLOW_SHORT_DEMO_PASSWORD=YES`와 루프백 HTTP·Mock AI 조건을 함께 설정한 경우에만 4자 이상의 최초 관리자 비밀번호를 허용한다. 실제 EMP-001 활성화와 Oracle HTTP 로그인/재시작 후 재로그인이 PASS했다. 일반 계정 활성화는 기존 12자 이상 규칙을 유지한다. 공유 환경에서는 이 예외를 사용하지 않는다.

## 실 Gemini는 기본 경로에서 제외

기본값은 Mock이다. [Gemini 안내](gemini-free-tier.md)를 읽고 실제 계정의 한국 접근·모델 이용 가능·무료 티어·결제 설정·프로젝트 quota를 다시 확인해야 한다. Flash 후보는 폐기/자동 라우팅 때문에 allowlist에 없다. 현재 lite 설정도 계정 접근을 보증하지 않는다. 실패해도 유료/다른 모델로 바꾸지 않는다.

이번 인수인계는 실 호출을 승인하거나 실행하지 않는다. 나중에 별도 명시적 허가가 있을 때만 backend 단일 합성 인사말 smoke를 사용한다(문서/직원 데이터/대화 이력 없음):

```powershell
# 모듈에서, GOOGLE_API_KEY를 안전하게 환경에 입력한 후 별도 허가를 받은 경우만
$env:BANS_GEMINI_SMOKE = 'YES'
$env:BANS_GEMINI_FREE_TIER_CONFIRMED = 'YES'
$env:BANS_GEMINI_TRANSFER_ACKNOWLEDGED = 'YES'
.\mvnw.cmd -Pgemini-smoke '-Dtest=GeminiLiveSmokeIT' test
# 끝나면 이 세 gate와 임시 GOOGLE_API_KEY를 제거/기존 환경으로 복원한다.
```

Maven 프로필이 system property gate를 설정한다. 오류가 나면 종료하며 자동 재시도 0회다. 일반 웹 UI를 gemini-live로 켜면 인사말만이 아니라 검토된 문서 질문 경로도 연결되므로, 단일 greeting 검증에 웹 live 모드를 사용하지 않는다. 실제 키/결과 원문을 출력하거나 Git/브라우저/로그/DB에 넣지 않는다.

## 2026-11-10 Oracle-only demo entry point
Default application profile is now `oracle`; `scripts/local.ps1 -Action build` includes `-Poracle`, and `-Action run` delegates to the Oracle HTTP demo. The previous in-memory demo process was stopped. Test fixture adapters remain available only for the offline `test` and `fixture-test` profiles; normal tests do not connect to Oracle. The current demo URL is http://127.0.0.1:8080/.

## 저장형 Gemini 설정 (2026-11-10)

실제 키는 Git 제외 파일 `bans_chatbot/.env`의 `GOOGLE_API_KEY=` 뒤에 저장한다. 이 파일은 Oracle 프로필이 모듈 작업 디렉터리에서 자동 읽는다. `.env.example`에는 실제 키를 쓰지 않는다. 기본 `bans.ai.provider=mock`에서는 키가 있어도 외부 호출이 없다.

실제 Gemini를 켜려면 본인의 프로젝트 무료 티어, 모델 접근과 외부 전송 조건을 확인한 뒤 같은 `.env`에서 `bans.ai.provider=gemini`, `bans.ai.model=gemini-2.5-flash-lite`, `bans.ai.external-transfer-acknowledged=true`, `bans.ai.free-tier-confirmed=true`로 지정한다. 기존 서버를 종료하고 `./scripts/local.ps1 -Action run`으로 다시 시작한다. 스크립트는 `gemini` 설정일 때 `oracle,gemini-live` 프로필을 선택한다. 키와 조건이 없거나 잘못되면 시작에 실패한다. 자유 입력 AI 채팅은 구현되어 있지 않고 검토된 가상 질문만 처리한다.