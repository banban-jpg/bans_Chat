# STEP 19 실행 가이드

현재 시연은 **Oracle DB + Mock AI**로 `http://127.0.0.1:8080/`에서 실행한다. 일반 테스트만 메모리 fixture를 사용한다.

## 필요한 프로그램과 확인한 버전

- Windows 11 x64, JDK21.0.3(빌드/런타임), Git, Windows PowerShell5.
- 프로젝트 Maven Wrapper3.9.6. eGovFrame5.0.0 parent→Boot3.5.6; 버전을 바꾸지 않는다.
- Node22.17.0은 배포 JS HTTP/STOMP 테스트/Mock 부하 실행에 사용한다. 현재 테스트는 `C:/Program Files/nodejs/node.exe`를 확인한다. 앱 실행에는 Node 빌드가 필요 없다.
- Python3.12는 명세/결과 생성 도구용(표준 라이브러리만). 앱 런타임 필수 아님.
- 실제 Chrome 시연은 별도 opt-in. 이번 기본 테스트에서 제외했다. 환경 실패 이력을 현재 PASS로 쓰지 않는다.
- Oracle26ai Free가 현재 로컬 전용 합성 스키마에 설치돼 있다.

## 현재 PC에서 빌드·테스트

저장소 루트의 PowerShell에서 실제 실행한 환경이다. 다른 PC의 캐시/경로는 그 PC 환경에 맞춰 검증해야 한다.

```powershell
$env:MAVEN_HOME='C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cache='C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
java -version
.\bans_chatbot\mvnw.cmd -version
$env:BANS_MOCK_LOAD_TEST='NO'
$env:BANS_UI_BROWSER_TEST='NO'
.\scripts\local.ps1 -Action build -Offline -MavenRepository $cache
```

build는 wrapper **clean test package**다. 테스트만 하려면 Action=test(clean test). 둘을 연속 실행할 필요는 없다. 오프라인 캐시가 없는 PC는 `-Offline -MavenRepository $cache` 없이 허용된 저장소에서 먼저 의존성을 준비해야 하며, 그 PC의 다운로드 성공은 이번에 검증하지 않았다. 설정 누락/다운로드 오류 때문에 버전이나 DB를 바꾸지 않는다.

최종 결과는 [TEST_RESULTS.md](TEST_RESULTS.md)에 실제 testcase별로 있다. 로그·테스트 자격증명·raw metric은 Git 제외 `bans_chatbot/local-notes/`에서만 관리한다.

## 백엔드·프론트엔드 실행

```powershell
.\scripts\smoke-local.ps1 -EvidencePrefix step19-smoke
.\scripts\local.ps1 -Action run -PromptAdminPassword
```

smoke는 실제 JAR를 임시 포트/127.0.0.1/Mock으로 기동해 화면·health·정적JS·CSRF·보호API401을 확인하고 자기 프로세스만 종료한다. STEP19에서 재실행했다. run 명령은 코드 확인된 wrapper이며 수동 대화형 password 입력 실행 자체는 이번 STEP19에 별도로 수행하지 않았다(사용자 입력 필요). 자격증명은 Read-Host SecureString으로 받아 자식 프로세스 환경에 일시 전달하고 종료 후 기존 값을 복원한다.

실행 후 기본 주소는 `http://127.0.0.1:8080/`. 프론트는 Thymeleaf template과 같은 서버의 static/ui JS/CSS이며 별도 npm dev server/CDN이 없다. 화면의 **가상 데이터 / 영구 저장 아님** 표시를 확인한다. Ctrl+C로 종료하며 재시작하면 Mock 변경 데이터는 사라진다.

초기 관리자 EMP-001 비밀번호는 직접 지정한다. 기본 비밀번호는 없다. 이를 지정하지 않으면 서버는 기동하나 pending 계정으로 로그인할 수 없다. 관리자 UI에서 EMP-005(작성자)/EMP-002(승인자) 등 기존 pending 계정에 일회용 코드를 발급하고 안전하게 별도 전달한다. 각 사용자는 활성화 화면에서 자기 비밀번호를 지정한다. 두 독립 브라우저 프로필로 대화한다. 토큰/코드/비밀번호를 공용 문서·콘솔·스크린샷에 남기지 않는다.

직원 검색→1:1 대화→문서 작성→관리자와 별개인 DOCUMENT_MANAGER에게 READ+APPROVE 명시적 grant→검토 요청→담당자 승인→현재 문서 조회/검색→AI 검토된 합성 질문을 시연한다. DOCUMENT_EDITOR 소유자만 작성·수정·grant, 지정 DOCUMENT_MANAGER만 승인한다. ACCOUNT_ADMIN은 문서 우회 권한이 아니다. 상세 시연은 [web-ui.md](web-ui.md).

## API 명세·결과 재생성

```powershell
# 루트에서: 실제 전체 HTTP 검사는 normal suite에 포함
python scripts/generate-api-docs.py
python scripts/summarize-verification.py
.\scripts\verify-source.ps1
```

명세 생성기는 현재 source annotation과 테스트가 기록한 실제 Spring 매핑을 비교해 다르면 실패한다. 수동 검토 metadata가 없는 새 매핑도 실패한다. API_ENDPOINTS.txt는 UTF-8 BOM으로 저장한다. HTTP 테스트의 raw body/토큰은 evidence에 없다.

결과 생성기는 ignored `step19-baseline-reports`와 선택적 `step19-load-reports`의 **실제 XML**을 읽는다. normal build 후 기본 report를 다음처럼 보존하고, 추가 부하 실행 뒤 두 report만 별도 폴더에 보존해야 한다. 이전 결과를 최신 실행으로 착각하지 않도록 이번 실행 전 snapshot 폴더 유무/시각을 확인한다.

```powershell
# 기존 snapshot이 있으면 별도 이름으로 수동 보관한 뒤 새 실행 결과를 저장한다.
if(Test-Path bans_chatbot/local-notes/step19-baseline-reports){throw '이전 baseline snapshot을 먼저 별도 보관하세요.'}
New-Item -ItemType Directory bans_chatbot/local-notes/step19-baseline-reports
Copy-Item bans_chatbot/target/surefire-reports/TEST-*.xml bans_chatbot/local-notes/step19-baseline-reports
# 선택적 Mock 부하. 실제 DB/외부 모델 호출 없음
.\performance\scripts\track-a.ps1 -o ('-Dmaven.repo.local='+$cache)
New-Item -ItemType Directory bans_chatbot/local-notes/step19-load-reports -Force
Copy-Item bans_chatbot/target/surefire-reports/TEST-egovframework.example.common.web.MockLoadExperimentTest.xml,bans_chatbot/target/surefire-reports/TEST-egovframework.example.ai.provider.ProviderFailureExperimentTest.xml bans_chatbot/local-notes/step19-load-reports -Force
python scripts/summarize-verification.py
```

테스트 증거 없이 생성기를 실행해 PASS를 만드는 기능은 없다. 전체 브라우저 UI 검사는 `BANS_UI_BROWSER_TEST=YES` + 선택된 WorkbenchBrowserTest로 별도 실행할 수 있지만 이번 STEP19에는 미실행/검증 필요다. 실제 브라우저 클릭과 Node HTTP/STOMP 검증을 구분한다.

## 환경 변수·실 Gemini

`.env.example`은 이름만 제공하며 Spring이 자동 로드하지 않는다. 기본 자동 테스트와 Mock에는 GOOGLE_API_KEY가 필요 없다. 키/DB 비밀번호는 환경 또는 Git 제외 로컬 설정만 사용한다. 실제 개인정보·업무자료·기밀 입력은 금지다. 질문 원문도 Google로 전송될 수 있다.

이번 STEP19에서 실 Gemini 사용을 활성화하지 않았다. 나중에 명시적 별도 허가·무료 티어/계정 모델 접근/결제 상태 확인이 있을 때만 [setup.md](setup.md)의 단일 합성 인사말 smoke를 사용한다. 유료/다른 모델 자동 전환·무한 재시도는 없다. Vertex/OpenAI/Ollama는 구현 경로가 아니다.

## Oracle 설치 후 — 현재 BLOCKED/검증 필요

[ORACLE_VERIFICATION_CHECKLIST.md](ORACLE_VERIFICATION_CHECKLIST.md)와 [단계별 상세 절차](oracle-install-validation.md)를 따른다. JDBC driver 포함 빌드, 전용 빈 schema, V001~V005, seed rollback 검증 후 승인된 commit, Failsafe13개 class의 단계별 gate, TLS oracle 앱 순서다.

Oracle 최초 관리자 보안 bootstrap은 아직 없다. dummy hash나 ACTIVE SQL로 우회하지 않는다. 설치 자체만으로 로그인 UI가 완성되지 않는다. oracle 프로필은 DB가 없으면 실패하며 Mock으로 바뀌지 않는다. 백업/복원·제약·락·SQL 권한·영속성·실성능까지 증명하기 전 DB 완료로 표시하지 않는다.

## 자주 발생할 수 있는 오류

| 증상 | 확인/해결 |
|---|---|
| Maven wrapper 다운로드/의존성 해석 실패 | JDK21/MAVEN_HOME/허용된 저장소·기존 cache 확인. 오프라인 캐시 누락은 다운로드 필요, 버전 우회 금지. |
| PowerShell native stderr가 실패로 해석됨 | local/track-a runner는 종료코드를 검사하도록 처리. Mockito Java agent 경고만으로 실패로 판정하지 않음. |
| 한글 문구 검사 실패 | Windows PowerShell5의 PS1은 UTF-8 BOM 유지. API_ENDPOINTS도 BOM 확인. |
| 8080 사용 중 | 기존 프로세스를 임의 종료하지 않음. 사용 주체 확인; smoke는 임시 포트 사용. |
| 401/로그인 불가 | 관리자 비밀번호 지정 여부, pending 활성화, 재직 상태/자격증명 버전/세션 만료 확인. 쿠키 값을 로그에 찍지 않음. |
| 403 | CSRF token 새로 취득, 현재 역할/문서 grant 확인. 클라이언트 role/userId 조작으로 해결하지 않음. |
| 문서404 | 타 부서/미허가·미게시·폐기/옛버전도404일 수 있음. 현재 권한·게시 버전 확인. |
| AI429/503/504 | 제한/서비스 상태 확인. 다른 모델로 우회하지 않음. 일반 채팅 독립 동작 확인. |
| 실시간 연결 실패 | same-origin·세션·CONNECT CSRF 확인, 재연결 버튼/REST cursor 복구. REST 저장 경로는 STOMP broadcast를 하지 않음. |
| ORACLE_CONFIG_MISSING/DRIVER_MISSING/UNAVAILABLE | Oracle 전용 설정/driver 포함 빌드/실제 서버 준비. Mock fallback 금지. |
| Oracle HTTP 로그인 쿠키 유지 안 됨 | Secure=true 설정 유지, TLS를 구성해 검증. HTTP 데모용 보안 완화 금지. |

위 해결법 중 Oracle/TLS/다른 PC 설치 절차는 실행 안내이며 현재 환경에서 성공을 검증한 결과가 아니다.
