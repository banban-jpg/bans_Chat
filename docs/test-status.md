# Test status

## STEP 19 현재 실행 결과 (2026-11-04 KST)

최종 `scripts/local.ps1 -Action build -Offline -MavenRepository $cache` 내부 wrapper clean test package **PASS19:17:23**, Java21.0.3/Maven3.9.6/124main·73test source. **288testcase=285PASS/3SKIPPED/FAIL·ERROR0**,56개 baseline XML. 별도 track-a runner는 **2PASS19:19:25**이며 기본 skip2와 같은 testcase의 별도 실행이다. Chrome1은 이번 opt-in 미실행 SKIPPED. Oracle13class는 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, 실Gemini는 SKIPPED(실행시도없음/NOT_RUN).

신규 FinalApiHttpVerificationTest(기본 suite 포함): 실제 local-mock 서버65HTTP요청,39개 Controller mapping 각각 정상응답 + static4 + 비인증/CSRF/IDOR/revoke/logout negative. JAR smoke도 별도기동/6route/Mock라벨 PASS 후 종료했다. 현재 source/runtime API 추출과 실제 XML 결과 생성·시크릿/링크/구문 검사 결과는 [개별 결과](TEST_RESULTS.md)와 [최종 보고서](FINAL_VERIFICATION_REPORT.md)를 따른다. 실제 DB/외부AI 호출각0이며 과거 단계 결과는 아래 이력이다.

## STEP 18 최종 재현·인수인계 (2026-11-04 KST)

환경: Windows PowerShell 5 / Java21.0.3 / wrapper Maven3.9.6 / eGovFrame5.0.0 / Boot3.5.6 / Node22.17.0. 진입은 dev/88836b4와 기존 미커밋 STEP16/17이며 보존했다. .env.example 값을 비웠고 운영 API/DDL/seed/POM/모델을 바꾸지 않았다. Oracle 제품명26ai와 내부23.26 릴리스 근거를 구분하도록 예비 IT만 수정했다. 실 Oracle/Gemini 호출 각각0회.

기존 MAVEN_HOME과 이 PC의 `.maven-repo` 절대 경로를 사용했다. exact 환경과 명령은 setup.md/README에 있다.

| 실제 실행 | 결과/증거 |
|---|---|
| `scripts/local.ps1 -Action build -Offline -MavenRepository <기존 캐시>` | **PASS 18:46:50**, 내부 wrapper **clean test package**, 124 main/72 test source, selected287 = **284 PASS + 3 SKIPPED**, 실패·오류0. local-notes/step18-build.log. 실행 JAR 생성, package의 중복 Surefire 실행 없음. |
| OracleReleaseEvidenceTest | **PASS 4건**, 공식 제품 banner26ai Free/23.26 예시 허용, 다른 제품/edition 거부. 실제 DB 연결/SELECT 실행은 아님. |
| `mvnw.cmd -o <cacheArg> '-Dtest=WorkbenchClientIntegrationTest,WorkbenchMockMvcTest' test` | **PASS 18:51:10**, 핵심 시나리오 5건. MVC 활성화/검색/채팅·문서/AI/관리자 및 실제 JS+localhost HTTP/STOMP/출처/재접속. step18-scenarios.log와 CLIENT_PASS. |
| `scripts/verify-source.ps1` | **PASS**, 최초 및 최종 문서 재검사(19:00 KST). HEAD/index/worktree 후보 287파일/텍스트273개, 제한 키·private-key 패턴/.env.example 빈 값/로컬 문서 링크 위반0. 원문 매치/비밀값 출력 없음. step18-source-check.log 및 step18-source-inventory.json. |
| `scripts/smoke-local.ps1` 최초 | **FAIL**, 도구 출력 MOCK_BANNER_MISSING. Windows PowerShell5가 BOM 없는 UTF-8 한글 literal을 오독. 서버 자체는 정상 부팅. scripts와 performance PS1을 UTF-8 BOM으로 보정. |
| 같은 smoke 재실행 | **PASS**, /·health2개·csrf·app.js 200, 보호 chat API401, Mock banner. step18-smoke-final.log. 임시포트 localhost/Mock만 실행, finally에서 자기 프로세스 종료. |
| 기본 suite 선택적 테스트 | **SKIPPED 3**: Chrome1(기존 renderer/CDP BLOCKED), 부하실험2(별도 STEP17 PASS). full55개 Surefire report 287/0/0/3과 일치. |
| Oracle-only 13개 class / live greeting smoke | 일반 test에서 이름/tag로 **SKIPPED by selection**. 실제 Oracle 검증 모두 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. live Gemini **NOT_RUN**. |
| Oracle 설치·변경·seed·TLS·backup/restore·실 DB E2E | **NOT_RUN / BLOCKED**, 수행하지 않음. Oracle 최초 관리자 bootstrap **미구현/수락 차단**. |
| 실제 Chrome 클릭·raw run 사용자 입력/실 greeting | **NOT_RUN**, 기존 브라우저 환경 BLOCKED 유지. 안내한 수동 명령을 실행 결과로 표시하지 않음. |

예비 IT는 JDBC major=26만 요구하던 오류를 v$version banner/Free 확인으로 바꿨다. 이 Oracle SELECT는 아직 실행하지 않았다. .env.example에는 값 없는 이름만 있고 자동 로드하지 않는다. 현재 source 검사 범위는 제한 패턴 및 링크/manifest 성격이며 전체 Git 과거 이력·모든 비밀·CVE/DAST 인증이 아니다. 소스/명령/로그/제약/미구현 항목을 README/setup/known-limitations/Oracle checklist로 동기화했다.

## STEP 17 실험/전체 테스트 (2026-11-04 KST)

Windows 11 / Java 21.0.3 / Maven Wrapper 3.9.6 / eGovFrame 5.0.0 / Boot 3.5.6 / Node 22.17.0. 시작은 dev / 88836b4와 미커밋 STEP 16 변경이며 보존했다. 환경/하드웨어·실험 표본·p50/p95·오류율의 정의는 performance-and-failure.md를 따른다. 실제 Oracle/Gemini 호출 0회.

모듈에서 기존 MAVEN_HOME과 `$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`를 사용했다.

| 실제 실행 | 상태/근거 |
|---|---|
| CIM CPU/메모리 조회 | **FAIL**, 권한 거부. Node os API 수집은 PASS. 개인정보/비밀값을 조사하거나 권한을 올리지 않음. |
| BANS_MOCK_LOAD_TEST=YES, `.\mvnw.cmd -o $cacheArg '-Dtest=MockLoadExperimentTest,ProviderFailureExperimentTest' test` | **PASS 18:21:02**, 2건, 실패·오류·skip 0, 초기 9 burst와 장애 실험. step17-track-a-build.log. |
| 같은 명령, HTTP 오류 분리/Mock 문서 AI·429·JS 제어 추가 후 | **PASS 18:23:26**, 2건. step17-track-a-final.log. |
| BANS_MOCK_LOAD_TEST=NO, `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 18:25:35**, 당시 selected 281/실패0/오류0/skip3. step17-full.log. |
| `& ..\performance\scripts\track-a.ps1 -o $cacheArg` 최초 | **FAIL**, Windows PowerShell ErrorActionPreference=Stop이 Mockito native stderr 경고를 종료 오류로 처리. underlying JSON은 생성됐으나 runner 성공으로 기록하지 않음. step17-script-run.log. |
| 같은 실제 runner 명령, native stderr/종료 코드 처리 수정 후 | **PASS 18:28:25**, 두 실험 PASS. 최종 수신 assertion/9 burst/장애/UI fake timer 포함. step17-script-final.log와 TRACK_A_PASS. |
| 최종 BANS_MOCK_LOAD_TEST=NO, `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 18:30:41**, **124 main/71 test source, selected 283 = 280 PASS + 3 SKIPPED**, 실패·오류 0, 실행 JAR 생성. step17-final.log. |
| 최종 Surefire XML 집계 | **PASS**, 54개 report의 283/0/0/3. skipped 3은 Chrome 선택적 테스트 1 + 별도 실행한 Mock 부하/장애 실험 2. |
| 10/20/30 실제 localhost 연결, 각 3회 | **PASS**, 최대 12개 고유 합성 계정, 연결 30개, HTTP 2763회/기대 거부45/예상 밖 오류0. 혼합 표본3312는 HTTP 개수와 다름. 상대 JS 수신180/중복송신/읽음/승인경쟁/권한변경/재연결 확인. |
| 429/503/JSON/timeout·busy burst | **PASS**, 각각 3회+20 동시 시도, 외부 호출0. 인위적 실패율100%는 정상 성공률 저하와 구분. 원격 API가 아닌 localhost mock. |
| UI 동작 제어/재시도 상한 | **PASS**, production API JS/fake fetch의 오류 전파, STOMP fake timer 1/2/4/8/15초 5회 상한. 실제 브라우저 paint/클릭은 **BLOCKED: CHROME_RENDERER_CDP_UNAVAILABLE**, STEP 17 재시도 NOT_RUN. |
| 새 Mock 저장소 객체 | **PASS**, 데이터가 사라짐을 확인. OS/process kill·restart 복구는 **NOT_RUN**이며 객체 교체와 동치 취급 안 함. |
| Oracle-only 13개 클래스/live smoke | 일반 suite에서 **SKIPPED by selection**. Oracle 실제 실행 전체 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. |
| Oracle 트랙 B·실 Gemini·실 DB E2E/성능/중단·복구 | **NOT_RUN / BLOCKED**, 설치·중단·유료 전환·DB SQL 실행 없음. |

스크립트 PS AST와 JS 구문 검사 PASS, git diff --check PASS, 운영 POM/db/oracle diff 없음. 새 read-only Oracle 관측 SQL은 performance/scripts의 미실행 계획이다. 로그/JSON은 ignored local-notes, 공용 재현 코드·집계 문서는 Git 대상이다. Mock 숫자로 DB 성능/SLA를 약속하지 않는다. 당시 STEP 18은 미수행이었으며 최신 결과는 이 문서 맨 위 STEP 18 절을 따른다.

최종 확인 **18:35:10 KST PASS**: raw JSON/log Git 제외, 재현 script/공용 문서 Git 대상, 9회 최종 metric/예상 밖 HTTP 오류0, git diff --check exit0, 두 실행 프로세스 종료코드0. 이후 운영 코드/테스트 변경 없음. STEP 16 미커밋 변경과 STEP 17 변경 모두 미커밋 상태로 남겼다.

## STEP 16 최종 보안 회귀 (2026-11-04 KST)

깨끗한 dev / 88836b4에서 시작. Windows 11, Java 21.0.3, Maven Wrapper 3.9.6, eGovFrame 5.0.0/Boot 3.5.6 유지. 상세 case/심각도/한계는 security-test-results.md를 따른다. 실제 Oracle 연결/실 Gemini 호출은 각각 0회다. 로그는 ignored bans_chatbot/local-notes에 있다.

모듈에서 기존 MAVEN_HOME을 지정하고 `$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`로 실행했다.

| 실행 명령 | 결과 |
|---|---|
| `.\mvnw.cmd -o $cacheArg '-Dtest=ChatStompE2ETest,OutboundSecurityRegressionTest' test` | **FAIL 17:58:59**, 13건 중 9 setup ERROR. 새 테스트의 RPM 20이 제품 상한 10을 초과. 테스트 설정만 수정하고 null-safe cleanup 보강. 제품 한도 변경 없음. step16-targeted.log. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=ChatStompE2ETest,OutboundSecurityRegressionTest,AttackRegressionMockMvcTest' test` | **PASS 18:01:42**, 16건, 실패·오류·skip 0. step16-targeted2.log. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 18:03:41**, STOMP 8개 공격 조합 확장 후. step16-final.log. |
| 같은 전체 명령, malformed JSON 오류/원문 로그 방어 추가 후 | **PASS 18:05:32**, **279 selected = 278 PASS + 1 SKIPPED**, 실패·오류 0. 124 main/67 test source, 실행 JAR 생성. step16-release.log. Surefire 중복 실행 없음. |
| Fresh Surefire XML | **PASS**, 51개 report의 279/0/0/1과 일치. 기존 265 selected 대비 14개 case 추가. |
| 신규 보안 case | **PASS**, outbound wire 9 + 공격 MVC 3 + 기존 WS E2E에 추가한 2 = 14. 실제 raw WS 8개 SEND/SUBSCRIBE 공격은 한 case 안에서 모두 실행. |
| 정책과 HTTP bytes/전송 0회 | **PASS**, localhost 모의 서버가 승인된 합성 구간만 수신. 4개 fragment 부정 조건·4개 미검토 공격 질문은 HTTP 호출 0. 로그/감사 sentinel 부재. |
| Chrome 전체 클릭 | 정상 suite에서 **SKIPPED 1**, STEP 15의 CHROME_RENDERER_CDP_UNAVAILABLE 환경 BLOCKED 유지. STEP 16에서 브라우저 클릭 재시도 NOT_RUN. |
| Oracle IT 12개 클래스·실 Gemini smoke | 정상 test에서 이름/tag로 **SKIPPED by suite selection**. Oracle 실제 검증 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, 호출 0. |
| 실 모델·DB E2E·외부 DAST/의존성 CVE 스캔 | **NOT_RUN**. 기존 scanner 설정 없음, 새 도구 설치 안 함. |

18:06 KST git diff --check PASS(exit 0), POM/db/oracle diff 없음. 정상 소스·테스트·공용 보안 보고서는 Git 대상, 임시 로그는 local-notes 제외를 유지한다. 관찰된 오프라인 미해결 P0는 0이나 Oracle 의존 P0가 BLOCKED이므로 최종 보안 검증 완료가 아니다. STEP 17/18은 수행하지 않았다.

## STEP 01 baseline

Environment: Windows 11, Asia/Seoul, 2026-11-02; Temurin Java 21.0.3; Maven 3.9.6 via `bans_chatbot/mvnw.cmd` with `MAVEN_HOME` set to the locally installed distribution and `-Dmaven.repo.local=C:\Users\user\.m2\repository`. All Maven invocations below used `-o` (offline). No Oracle connection, SQL execution, or Gemini request occurred.

| Check / command | Status | Evidence and cause |
|---|---|---|
| `java -version` | PASS | Temurin 21.0.3, 2026-11-02 16:36 KST |
| `mvn -version` | FAIL | `mvn` absent from PATH; the existing local Maven 3.9.6 `mvn.cmd -version` passed at 16:37 |
| `mvnw.cmd -o ... clean compile` | PASS | 16:49, two sources compiled with `release 21`; `javap` reports major version 65 |
| `mvnw.cmd -o ... clean test` | FAIL | 16:44: parent-managed JUnit Platform Launcher 1.12.2 absent from local cache. Earlier attempts exposed an incorrect test import (fixed), incomplete clean 3.4.1 and Surefire 3.5.4 cache entries (pinned cached 3.2.0/3.5.2). Current source compiles; Maven test lifecycle still cannot complete offline. No dependency download was performed. |
| `mvnw.cmd -o ... dependency:build-classpath` | PASS | 16:49, generated ignored `target/classpath.txt` from cached project dependencies |
| Direct `java -cp target/classes;<generated classpath> egovframework.example.EgovBootApplication` | PASS | 16:50, Boot 3.5.6 / Java 21.0.3 / default `local-mock`; Tomcat started port 8080 without DB configuration |
| `Invoke-WebRequest http://127.0.0.1:8080/health` | PASS | 16:50, HTTP 200 `{"status":"UP"}`; process stopped afterward |
| Manual JUnit Platform launch of `BootSmokeTest` | PASS | 16:53, 2 found / 2 successful / 0 failed in `test` profile. Used cached launcher 1.12.1 with project-managed JUnit engine because 1.12.2 launcher is absent; this does **not** count as Maven `clean test` success. Runner was compiled under ignored `target`. |
| `spring-boot:run` Maven goal | FAIL | 16:49, Boot Maven plugin transitive artifacts missing from offline cache; direct Java boot succeeded |
| `-Poracle-it verify` / `oracle` startup / JDBC | BLOCKED: ORACLE_26AI_NOT_INSTALLED | Oracle server absent; not invoked |
| Gemini network test | NOT_RUN | No Gemini integration in STEP 01 and no key; real call count 0 |
| Oracle SQL, migrations, transactions, lock and performance checks | BLOCKED: ORACLE_26AI_NOT_INSTALLED | No Oracle server or schema yet; SQL execution count 0 |

Normal `test` includes no Oracle or external API. `*OracleIT.java` cases are reserved for an explicit `-Poracle-it verify` run; none exist yet. `local-mock` currently means a DB-free server only; in-memory repositories and Gemini mock are future work. The two passing smoke tests verify HTTP routing and a genuine eGov runtime JAR on the classpath. They do not verify Oracle or Gemini behavior.

Follow-up to obtain a standard `clean test` PASS: make the parent-managed JUnit Platform Launcher 1.12.2 and missing plugin artifacts available in an approved local Maven cache, then rerun the wrapper command. Do not claim this gate passed until executed.

## Pre-STEP 02 package cleanup verification (2026-11-02, 17:01–17:05 KST)

Environment: same Windows/Java 21.0.3 and Maven wrapper 3.9.6 as STEP 01; Maven `-o`, existing `C:/Users/user/.m2/repository`, no network retrieval. Paths below are relative to `bans_chatbot`; `MAVEN_HOME` was set to the existing Maven 3.9.6 distribution. No Oracle connection or Gemini call occurred. The production change was a package move of `/health`, its test, and Java 21 IDE alignment; no STEP 02 feature was implemented.

| Check / actual command | Status | Evidence and cause |
|---|---|
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean test` | FAIL | 17:01: javac reported `AccessDeniedException` while closing cached `reactive-streams-1.0.4.jar`; clean build did not complete. An earlier unquoted Windows-path invocation resolved incorrectly to `C:\.m2\repository` and failed before build. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' compile` | PASS (incremental only) | 17:01: compiled classes were already present; Maven said nothing to compile. This does not establish a clean build pass. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' test` | FAIL | 17:02: test class compiled, but javac again reported access denied on the cached JAR. A repeat with `-e` reached Surefire and failed because `junit-platform-launcher:1.12.2` is absent offline. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean compile` | FAIL | 17:04: reproduced Java `AccessDeniedException` on `reactive-streams-1.0.4.jar`; `-X` stack trace identifies `jdk.zipfs.ZipFileSystemProvider.removeFileSystem` / `toRealPath`. |
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' '-Dmaven.compiler.fork=true' clean compile` | FAIL (misleading Maven exit 0) | 17:05: Maven printed `BUILD SUCCESS`, but forked javac printed the same uncaught `AccessDeniedException`. This is not accepted as a valid clean build. |
| `mvnw.cmd -o ... org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath` | PASS | 17:02: wrote ignored `target/classpath.txt` from cached dependencies. The unversioned plugin goal failed offline because its selected 3.8.1 is absent; no version was changed in the POM. |
| Manual JUnit Platform launch of `egovframework.example.global.health.BootSmokeTest` | PASS | 17:03: cached Launcher 1.12.1 with compiled test and project classpath, 2 found / 2 passed / 0 failed, active `test` profile. Runner was temporary in ignored `target`. This does not count as Maven `test` PASS. |
| Direct Java `EgovBootApplication --spring.profiles.active=local-mock --server.port=18080`; `Invoke-WebRequest http://127.0.0.1:18080/health` | PASS | 17:03: Boot 3.5.6 / Java 21.0.3 started; HTTP 200 `{"status":"UP"}`. Process then stopped. |
| Oracle integration and SQL | BLOCKED: ORACLE_26AI_NOT_INSTALLED | No Oracle server; no connection, schema, or SQL was executed. |
| Gemini real API | NOT_RUN | No integration or key; real call count 0. |

The package move preserved component scanning and the existing HTTP contract. Clean Maven build and normal test remain failing environment gates; resolve cached-JAR access and the missing launcher before treating the default test gate as green. No Oracle claim follows from these DB-free checks.

## STEP 02 verification (2026-11-02, 17:16–17:35 KST)

Environment: Windows 11 / Temurin Java 21.0.3 / Maven wrapper 3.9.6. All Maven calls used `-o` and `-Dmaven.repo.local=C:/Users/user/.m2/repository`, with `MAVEN_HOME` set to the existing local Maven distribution. Direct verification copied 144 cached project JARs and cached JUnit Platform Launcher 1.12.1 into ignored `bans_chatbot/target/deps`; it did not download dependencies. The final JUnit run removed Oracle and Gemini variables from its process environment. Oracle connection count 0; real Gemini call count 0.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' clean test` | FAIL | 17:28: javac `AccessDeniedException` while closing cached `reactive-streams-1.0.4.jar` outside the workspace; lifecycle stopped before tests. A 17:16 attempt showed the same error. Removing the unused reactive module temporarily caused the same error on `mybatis-3.5.19.jar`; it was restored. This is a cache/filesystem issue, not a Java source diagnostic. |
| `mvnw.cmd -o ... test` | FAIL | 17:29: test compilation hit the same JAR access error. Repeating after classes existed reached Surefire at 17:30, then failed because `junit-platform-launcher:1.12.2` is absent from the offline cache. Normal Maven `test` is **not** PASS. |
| `mvnw.cmd -o ... org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath` | PASS | 17:28: generated ignored `target/classpath.txt`; no network download. |
| Direct `javac -proc:none -encoding UTF-8 --release 21 -cp 'target/deps/*'` and test compilation with `target/direct-classes` | PASS | 17:29 and 17:34: final 16 main Java sources and 8 test Java sources compiled into ignored `target/direct-*`. `OracleConnectionOracleIT` compiled but was not executed. This is a separate source compilation, not Maven lifecycle success. |
| Direct JUnit Platform launch selecting seven DB-free test classes | PASS | 17:34: 13 found / 13 successful / 0 failed, including MockMvc response/validation/sanitization, Mockito service port, local-mock Controller → Service → port, test/local-mock Bean wiring, profile guard and eGov smoke. OracleIT was deliberately absent from discovery. Cached launcher 1.12.1 was used only for this offline verification. |
| Direct Java `--spring.profiles.active=local-mock --server.port=18082/18083`; HTTP `/health`, `/api/health`, `/api/demo/employees` | PASS | 17:31 and final 17:35: Boot 3.5.6 started without DB/Google key. Both health routes returned HTTP 200; `/api/health` returned `{"data":{"status":"UP"}}` and a server-generated request ID different from the client header. The mock-only employee route returned 20 invented records across 5 departments. Processes stopped afterward. |
| Direct Java `--spring.profiles.active=oracle` with Oracle environment variables removed | PASS (expected failure) | 17:30: startup exited 1 with `ORACLE_CONFIG_MISSING: ORACLE_JDBC_URL`; it did not fall back to mock or open a connection. This verifies the missing-config guard only. |
| Direct Java `--spring.profiles.active=oracle-it` alone | PASS (expected failure) | 17:30: exited 1 with `PROFILE_CONFLICT: oracle-it requires oracle`. |
| `oracleIntegrationTest.cmd` with Oracle variables removed | PASS (precondition only) | 17:30: exited 2 with `BLOCKED: ORACLE_CONFIG_MISSING ORACLE_JDBC_URL`; Maven/Failsafe and database test were not invoked. |
| `mvnw.cmd -o ... -Poracle-it validate` | FAIL | 17:19: Failsafe 3.5.4 is absent from offline cache. This is not an Oracle test result. |
| Real Oracle 26ai connection, SQL, migrations, transactions, locks, performance | BLOCKED: ORACLE_26AI_NOT_INSTALLED | Oracle server absent; no JDBC connection or SQL execution attempted. The preliminary `OracleConnectionOracleIT` remains unrun. |
| Real Gemini transport/network test | NOT_RUN | No real transport was implemented in STEP 02. The offline mock adapter wiring passed; no external request was made. |
| Real Gemini API invocation | SKIPPED | Intentionally disabled; no key or paid fallback configured. |

The independent DB-free tests are executable and passed, but the default Maven `clean test` gate remains FAIL. Restore the missing artifacts through an authorized dependency-cache refresh and resolve the JAR-access issue, then rerun the Maven wrapper before claiming that gate is green. These passes do not verify Oracle 26ai schema or production security.

## STEP 02 gate update from user run (2026-11-02 18:03:49 KST)

The user ran `cd bans_chatbot; .\mvnw.cmd -U clean test` in ordinary PowerShell. The supplied Maven output showed **PASS: 13 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS** on Java 21.0.3 / Boot 3.5.6. This supersedes the earlier offline-cache failure for that user environment; it is not an Oracle integration result. The assistant did not initiate that network-enabled run.

## STEP 03 verification (2026-11-02 18:10-18:16 KST)

Environment: Windows / Java 21.0.3 / Maven wrapper 3.9.6 / eGov parent 5.0.0 / Boot 3.5.6. Before edits, Git was clean on `dev`. Maven was run **offline** (`-o`), with a copy of the already present Maven cache under ignored workspace `/.maven-repo/`; no dependency download, Oracle connection, or real Gemini call was initiated. The copy exists only to avoid this execution sandbox's access failure when javac closes JARs outside the writable workspace. The POM versions and SQL were not changed.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `cd bans_chatbot; .\mvnw.cmd -o clean test` | FAIL, environment | 18:10: Maven tried `C:\.m2\repository` and could not create it; no tests ran. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/Users/user/.m2/repository' test` | FAIL, environment | 18:10: javac failed while closing cached `reactive-streams-1.0.4.jar` outside the writable workspace. |
| Copy existing cache to ignored `/.maven-repo/`; run `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | FAIL, resolved code issue | 18:11: 24 tests attempted, five WebMvc slice context errors because the new interceptor's `PublicEndpointPolicy` was not imported. The test slice was fixed. |
| Same offline `clean test` | PASS, intermediate | 18:12: 24 tests, zero failures/errors/skips. This preceded per-segment AI metadata and final path adjustment. |
| Same offline `clean test` after per-segment change | FAIL, resolved code issue | 18:16: 26 tests, two health failures. MockMvc provided an empty servlet path; the interceptor switched to URI minus context path. |
| Same offline `clean test` after path fix | **PASS** | 18:16:46: 26 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main source files and 10 test source files compiled. Covers role/self-grant, chat participant, document scope/grant/approval, inactive actors, per-segment external AI decision, anonymous allowlist, 401/403/404/429 contract, profile wiring, and prior smoke tests. |
| Same offline `clean test` after placing tests under their owning domain packages and extending safety cases | **PASS (final)** | 18:22:06: 26 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main and 13 test source files compiled. Tests additionally check approve-only grants, non-synthetic/sensitive segments, and draft rejection. This is the final STEP 03 source state. |
| `*OracleIT` execution under ordinary Surefire | SKIPPED, intentionally excluded | Surefire POM excludes the name/tag; final run contained only DB-free tests. The preliminary Oracle case was compiled, not run. |
| Oracle 26ai connection, ACL joins, grant revocation, SQL, transaction/locking | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; explicit `oracleIntegrationTest.cmd` was not invoked in STEP 03. |
| Spring Security filter chain, persisted session/login, CSRF, STOMP ACL | NOT_RUN | No Spring Security artifact was downloaded or filter chain implemented in this policy-only STEP. Current MVC interceptor tests do not establish those guarantees. |
| Real Gemini API or external question transfer | SKIPPED | No production review adapter or real transport; network call count 0. |

The final PASS covers Java/MVC policy behavior with fake session actors and Mockito ports. It does not prove Oracle permission joins, instant account/grant revocation, or production Spring Security wiring.

## STEP 04 verification (2026-11-02 18:33-18:42 KST)

Before editing, Git showed the uncommitted STEP 03 source/docs work listed in `git status --short`; those files were preserved. Environment: Windows PowerShell, Java 21.0.3, Maven wrapper 3.9.6, eGov 5.0.0 parent, Boot 3.5.6. Commands ran from `bans_chatbot` with `-o` and the ignored workspace-local cache `C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo`. No dependency download, Oracle connection, SQL execution, or real Gemini call occurred.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' '-Dtest=OracleSchemaSourceTest' test` | **PASS**, then **FAIL**, then **PASS** | 18:35:25: five source checks passed. A new parser-order assertion failed at 18:40:54 because it incorrectly expected `ALTER TABLE` to be the last V001 statement; the SQL correctly contains later tables. After fixing that test assertion, six checks passed at 18:41:25. These are source/parser checks, not Oracle execution. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | **PASS (final)** | 18:42:13: 32 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 35 main and 15 test Java sources compiled. No DB URL or Google key required. Earlier full runs passed at 18:36:09 and 18:39:03 before the extra parser check. |
| `git diff --check` | **PASS** | 18:43 KST: exit 0; Git warned about future LF-to-CRLF conversion on tracked files but found no whitespace errors. |
| `OracleConnectionOracleIT` and `OracleSchemaOracleIT` in ordinary Surefire | **SKIPPED** | Excluded by `*OracleIT` name and `oracle-integration` tag. Compilation is not execution. |
| `@db/oracle/apply.sql` on empty Oracle 26ai | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | SQLcl/SQL*Plus and server were not invoked; Oracle DDL execution count 0. |
| Explicit `.\oracleIntegrationTest.cmd` / Oracle schema, UNIQUE/FK/CHECK, rollback, repeat guard, published-version triggers | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | Oracle server absent; preliminary `OracleSchemaOracleIT` exists but was not run. Actual Oracle constraint behavior remains unknown. |
| Flyway implementation/validation | **NOT_RUN** | Official support was reviewed; no verified artifact/runtime combination or Flyway integration was installed. Versioned manual SQL was selected. |
| Real Gemini API | **SKIPPED** | No real transport or key; network call count 0. |

One intermediate STEP 04 source-check test failed because its assertion assumed the wrong statement position; it was corrected and rerun successfully. The final static PASS must not be reported as an installed Oracle schema, migration success, or validated concurrency/performance.

## STEP 05 verification (2026-11-02 18:53-19:04 KST)

Before STEP 05 edits, Git showed the uncommitted STEP 03/04 source/docs work; it was preserved. Environment: Windows PowerShell, Temurin Java 21.0.3, Maven wrapper 3.9.6, eGov parent 5.0.0, Boot 3.5.6. All Maven commands ran from `bans_chatbot` with `-o` and the ignored workspace-local cache `C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo`. The real local-mock run had no `ORACLE_JDBC_URL`, `GOOGLE_API_KEY`, or `GEMINI_API_KEY` environment variables. No network dependency retrieval, Oracle connection, SQL execution, or real Gemini request occurred.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' test-compile` | **PASS** | 18:53:30: 41 main and 16 then-current test sources compiled with Java release 21; no tests executed by this command. |
| `java -cp 'target/classes;target/test-classes' egovframework.example.global.config.SyntheticOracleSeedRenderer db/oracle/seed/S001__synthetic_company.sql` | **PASS, generation only** | 18:53: generated 91,836-byte Oracle-only seed from the same PSV files; no SQL was executed. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' clean test` | **PASS** | 18:56:23: 38 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS`; 41 main and 21 test sources compiled. Includes 6 new fixture/seed-source checks and prior DB-free tests. Source equality does not establish Oracle SQL validity. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:/eGovFrameDev-5.0.2-Windows-64bit/workspace-egov/bans_Chat/.maven-repo' org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath '-Dmdep.outputFile=target/classpath.txt'` | **PASS** | 18:58:49: wrote ignored classpath file from the existing offline cache; no external download. |
| Direct `java -cp <target/classes + generated classpath> egovframework.example.EgovBootApplication --spring.profiles.active=local-mock --server.port=18085` and `Invoke-WebRequest` | **PASS** | 18:59:27 startup; 18:59:50 `/api/health` HTTP 200 `{"data":{"status":"UP"}}`; unauthenticated `/api/demo/employees` HTTP 401. The process was stopped afterward. This is process/API startup only, not a database or login result. |
| Fixture employee, account, document, grant and seed-source checks | **PASS** | Count/distribution, reserved mail, unique IDs, valid references, published/rejected/discarded cases, external-transfer flags, ACL examples, and deterministic seed equality passed inside the 38-test run. |
| `OracleSeedOracleIT`, `OracleSchemaOracleIT`, `OracleConnectionOracleIT` in ordinary Surefire | **SKIPPED, intentionally excluded** | `*OracleIT.java` and `oracle-integration` tag are excluded from default test execution; these sources compile only. |
| `@db/oracle/apply.sql`, `@db/oracle/seed/apply_seed.sql`, `.\oracleIntegrationTest.cmd` | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; none was invoked. Oracle seed idempotence, FK and transaction results have no PASS evidence. |
| Real Gemini API call | **SKIPPED** | No real transport enabled or key; external call count 0. |
| Production account activation/login and Spring Security chain | **NOT_RUN** | STEP 06 work, deliberately absent from STEP 05. |

The V001 SHA-256 manifest was recomputed after the still-unapplied schema refinement and passed the offline source test. Oracle 26ai parser/constraint/trigger checks, DB authorization joins, concurrency and performance remain pending; no final DB readiness conclusion follows from these results.

Final STEP 05 check: after adding the in-memory document-grant Bean assertion, the same offline `clean test` command passed again at **19:03:10 KST**, with **38 tests, 0 failures, 0 errors, 0 skipped** and 41 main/21 test sources compiled. A final addition of extra orphan/current-version assertions to the Oracle-only IT was then compiled with the same offline Maven arguments and `test-compile` at **19:03:49 KST**: **PASS**, 21 test sources compiled; the Oracle IT was **NOT_RUN**. `git diff --check` exited 0 at 19:02 KST (only Git's LF/CRLF conversion warnings). No Oracle/Gemini call was made during any of these checks.

## Pre-STEP 06 recheck (2026-11-03 KST)

Environment: Windows 11, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov Boot parent 5.0.0 / inherited Spring Boot 3.5.6. `MAVEN_HOME` pointed to the existing Maven distribution. Commands ran from `bans_chatbot` with `-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo`, the ignored workspace cache. No production source or dependency version changed.

| Check | Status | Evidence / limit |
|---|---|---|
| `java -version`; `mvnw.cmd -version` | **PASS** | Java 21.0.3 and Maven 3.9.6, same Java runtime. |
| `mvnw.cmd -o ... clean test` | **PASS** | 17:15:39 KST: 41 main and 21 test Java sources compiled; 38 tests, 0 failures, 0 errors, 0 skips; `BUILD SUCCESS`. Unit, Mockito, MockMvc, Spring profile, fixture and Oracle-source tests ran. Compilation also confirms current package imports. |
| Three `*OracleIT` classes in ordinary Surefire | **SKIPPED** by configured suite selection; **NOT_RUN** as Oracle tests | Sources compiled, but Surefire excludes their name and tag. No Oracle connection or SQL execution. The `38/0/0/0` Surefire summary describes selected tests only. |
| First `mvnw.cmd -o ... -DskipTests package` | **FAIL**, environment | 17:16:10 KST: Boot `repackage` could not resolve uncached plugin dependencies offline. Plain JAR goal ran, but full package failed. |
| Approved dependency-cache refresh and `mvnw.cmd ... -DskipTests package` | **PASS** | 17:18:11 KST: full Boot repackage created `target/bans_chatbot-1.0.0.jar`; tests were intentionally skipped in this packaging command. |
| Repeated `mvnw.cmd -o ... -DskipTests package` | **PASS** | 17:19:09 KST: full Boot package succeeded offline after cache refresh. |
| SQLcl migration/seed and `oracleIntegrationTest.cmd` | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | Not invoked; Oracle SQL behavior, constraints, triggers, ACL, transactions, locks, and performance remain unverified. |
| Real Gemini transport | **SKIPPED** | No production adapter or real call. |

The initial packaging failure was resolved by fetching missing plugin artifacts into the ignored cache after approval; no POM change was needed. The package PASS and 38-test PASS are separate checks. Oracle readiness and production authentication remain unverified; see `docs/handoff.md` and `docs/oracle-validation-backlog.md`.

## STEP 06 verification (2026-11-03 17:20-18:24 KST)

Environment: Windows PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGovFrame parent 5.0.0 / Spring Boot 3.5.6. Maven ran from `bans_chatbot`; `MAVEN_HOME` selected the existing wrapper distribution and `-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo` selected the ignored cache. No Oracle URL, real Gemini key, Oracle server, or real Gemini client was used. Security starter artifacts were fetched into the ignored cache after the offline `test-compile` initially failed to resolve them; the subsequent compile passed. No framework version upgrade or H2 dependency was added.

| Actual command/check | Status | Evidence and limit |
|---|---|---|
| `.\mvnw.cmd -o ... test-compile` before Security artifacts were cached | **FAIL**, resolved | Offline dependency resolution lacked the newly required Security artifacts; this was an environment/cache failure, not an Oracle failure. An approved online `test-compile` then **PASS** after retrieval. |
| Intermediate `.\mvnw.cmd -o ... clean test` during implementation | **FAIL**, resolved | 38 tests selected; 10 failures while legacy MockMvc tests still asserted the interim fake-session boundary and a concurrent edit changed the schema manifest. Tests were updated to exercise the real security chain and the final manifest was regenerated. |
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test` | **PASS** at 17:50:43 KST | 59 main / 25 test Java files compiled; 45 tests, 0 failures, 0 errors, 0 skips; `BUILD SUCCESS`. Includes security-chain MockMvc activation/login/logout/me, CSRF, fixation, failure lock, inactive/version revocation, in-memory concurrent activation, existing policy tests, Oracle DDL manifest and Mapper XML parsing. This is not SQL execution. |
| Same offline Maven arguments with `-DskipTests package` | **PASS** at 17:52:43 KST | Repackaged `target/bans_chatbot-1.0.0.jar`; tests intentionally **SKIPPED** in this separate packaging command. The preceding `clean test` is the test evidence. |
| Direct `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.port=18086` plus live HTTP | **PASS** around 17:55 KST | A random, non-committed `BANS_LOCAL_ADMIN_PASSWORD` was supplied only to the process environment. `/api/auth/csrf`, CSRF-protected admin `/api/auth/login`, and `/api/auth/me` returned the expected `EMP-001` identity; process was stopped. This demonstrates local ephemeral login only. |
| `git diff --check` | **PASS** after STEP 06 edits | Exit 0; Git may warn about future line-ending normalization. |
| `AuthOracleIT` and other `*OracleIT` in ordinary Surefire | **SKIPPED** by suite selection; as Oracle tests **NOT_RUN** | Test source compiled, but no Failsafe Oracle test ran. |
| `@db/oracle/apply.sql`, seed, `.\oracleIntegrationTest.cmd`, real DB auth/lock/restart | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle JDBC connection, schema application, or SQL execution. `AuthOracleIT` has an additional `ORACLE_AUTH_TEST=YES` opt-in for a dedicated migrated/seeded schema. |
| Real Gemini Developer API | **SKIPPED** | No real transport or network request; 0 real calls. |

The selected 45/0/0/0 Surefire result excludes Oracle-only cases. Offline XML parsing and mock concurrency do not establish Oracle transaction atomicity, JDBC timestamp mapping, SQL validity, or persistence after restart. Actual Oracle concurrency/reboot cases are still **NOT_RUN** because their executable test code has not been completed; see `docs/oracle-validation-backlog.md`.

Final STEP 06 rerun after redacting the internal account object's string/JSON representation: `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package` **PASS at 18:24:22 KST**. The command compiled 59 main and 25 test sources, ran 45 selected tests with 0 failures/errors/skips, and created the Boot JAR. Surefire did not rerun the same tests during the `package` phase. This is the final source/test/package gate; Oracle IT remains **BLOCKED**, not passed.

## STEP 07 verification (2026-11-03 18:27–18:35 KST)

Environment rechecked: Windows 11 PowerShell, Temurin Java **21.0.3** (build/runtime), Maven Wrapper **3.9.6**, eGov parent **5.0.0**, Boot **3.5.6**. Existing 33 modified/untracked status entries on `dev` were preserved; STEP 07 introduced no dependency/version/schema change. No Oracle connection, SQL execution, Gemini request or dependency download was made. Maven used the ignored existing workspace cache.

Commands from repository root used `.\bans_chatbot\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' -f bans_chatbot/pom.xml` plus the goals below. The equivalent from `bans_chatbot` is `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package`. `MAVEN_HOME` was `C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6-bin\439sdfsg2nbdob9ciift5h5nse\apache-maven-3.9.6`.

| Actual check / goals | Status | Evidence / limits |
|---|---|---|
| `java -version`, wrapper `-version` | **PASS** | Java 21.0.3, wrapper Maven 3.9.6 running on the same JDK. |
| `test-compile` | **PASS** 18:29:24 | 68 main / then 25 test sources compiled; no tests executed by this command. Ignored log `step07-build.log`. |
| `test` | **PASS** 18:30:44 | 51 tests, 0 failures/errors/skips. Ignored log was under target and was removed by the subsequent clean. |
| First `clean test package` after extra suspension test | **FAIL**, resolved, 18:32:11 | 52 tests, 0 assertion failures, 1 error. The new test tried provisioning EMP-003, which is one of 20 employees but not one of the 12 fixture accounts. Corrected the test to use existing account EMP-010; no fixture expansion or production workaround. Ignored `step07-final.log`. |
| Final `clean test package` | **PASS** 18:34:26 | 68 main / 29 test source files compiled with release 21; **52 tests, 0 failures, 0 errors, 0 skips**, BUILD SUCCESS; repackaged Boot JAR. `package` did not rerun identical Surefire tests. Ignored `step07-final-rerun.log` and `bans_chatbot/target/surefire-reports/`. |
| Organization MockMvc / Mockito / XML source checks | **PASS** within final suite | Five real security-chain MockMvc cases plus service-port validation and Mapper XML/bound-SQL test. Department filter, count/page limit, exact six public fields, departments, literal injection-shaped search, sort whitelist, numeric errors, CSRF, 401/403, create/conflict, role-field rejection, department/ACL refresh, departure exclusion, suspension/session/login denial and minimal success audit checked. These are DB-free tests. |
| First `git diff --check` | **FAIL**, resolved | Four appended docs had an extra blank line at EOF. Trimmed only trailing whitespace; no existing content discarded. |
| Optional packaged local-mock process + live localhost HTTP | **NOT_RUN** | Automatic approval review rejected the proposed process/HTTP command with `blocked by policy` before execution. No startup/live endpoint PASS is claimed for STEP 07. Existing STEP 06 live evidence remains historical. MockMvc login and API demonstration did run. |
| All `*OracleIT` / `oracle-integration` in ordinary Surefire | **SKIPPED** by suite selection; Oracle execution **NOT_RUN** | New OrganizationOracleIT compiled; no Failsafe suite invoked. The selected suite's zero skips does not mean Oracle ran. |
| Migration/seed, real organization Mapper SELECT/UPDATE/row locks/audit transaction/sort/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED** | No Oracle server; `oracleIntegrationTest.cmd` not executed. Separate backlog lists executable preliminary IT and missing concurrency/restart/constraint cases. |
| Real Gemini API / Mock Gemini network tests | Real API **SKIPPED**; network tests **NOT_RUN** | Organization-only STEP added no Gemini transport or network test; real call count 0. Existing offline AI policy/mock tests remained in the passing suite. |

Final whitespace check result is recorded below after document synchronization. Passing fake search or XML parsing proves no Oracle predicate, collation, paging, lock, transaction, constraint or durability. STEP 07's DB-free API contract gate is met; final persistent DB/production readiness remains pending.

STEP 07 file inventory (relative to repository root; separate from preserved STEP 06 changes):

- Added production: `bans_chatbot/src/main/java/egovframework/example/organization/controller/OrganizationController.java`, `organization/dto/DirectoryModels.java`, `organization/service/OrganizationService.java`, `organization/repository/OrganizationRepository.java`, `organization/repository/InMemoryOrganizationRepository.java`, `organization/repository/OracleOrganizationRepository.java`, `organization/mapper/OrganizationMapper.java`, `audit/domain/PersonnelEvent.java`, `audit/mapper/PersonnelAuditMapper.java` (short paths share the same Java base directory).
- Added XML: `bans_chatbot/src/main/resources/egovframework/example/organization/mapper/OrganizationMapper.xml`, `bans_chatbot/src/main/resources/egovframework/example/audit/mapper/PersonnelAuditMapper.xml`.
- Incrementally updated production: `auth/repository/InMemoryAccountRepository.java`, `global/config/OracleMyBatisConfiguration.java`, `common/exception/ApiExceptionHandler.java` under the same Java base directory.
- Added tests: `organization/controller/OrganizationMockMvcTest.java`, `organization/service/OrganizationServiceContractTest.java`, `organization/mapper/OrganizationMapperSourceTest.java`, `organization/mapper/OrganizationOracleIT.java` under `bans_chatbot/src/test/java/egovframework/example`; updated existing `auth/service/ActivationConcurrencyTest.java` for the injected organization port.
- Updated docs: `docs/api-contract.md`, `docs/decisions.md`, `docs/handoff.md`, `docs/test-status.md`, `docs/oracle-validation-backlog.md`.

Final synchronized `git diff --check`: **PASS**, exit 0 (only existing LF/CRLF normalization warnings). The organization Controller also labels local-mock responses `X-Demo-Data: SYNTHETIC_EPHEMERAL`; a final gate is rerun after this label addition.

Final source gate after the local-mock synthetic-response label: same offline wrapper `clean test package` **PASS at 2026-11-03 18:36:46 KST**, 68 main / 29 test sources, **52 tests, 0 failures/errors/skips**, repackaged JAR. Ignored log: `step07-label-final.log`. This supersedes the earlier final timestamp for STEP 07. The label itself was not verified by a live HTTP run. Oracle execution remains **BLOCKED: ORACLE_26AI_NOT_INSTALLED**.

## STEP 08 verification (2026-11-03 KST)

Windows PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6; offline workspace cache. Before changes `git status --short` was empty on `dev`. No Oracle connection, SQL execution, Gemini call, dependency download or live network test occurred.

From `bans_chatbot`, with `MAVEN_HOME` set to the existing Maven 3.9.6 wrapper distribution: `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' clean test package`.

| Check | Status / evidence |
|---|---|
| Initial `clean test` before new chat tests were created | **PASS**, 18:59:50 KST, 52 existing tests. A Python test-file creation command failed because it used a root-relative path while running in the module directory; no test was created by that command. Log `bans_chatbot/target-step08-test.log` is ignored. |
| First `clean test package` with new tests | **FAIL**, 19:01:15 KST, 56 selected tests, one assertion failure. Test expected numeric HTTP status in `ApiException` message, while the exception exposes a public code. Production code did not cause the failure. |
| Immediate rerun while test-file correction was made | **FAIL**, 19:01:56 KST, 56 tests, one same assertion failure because Maven had already compiled the old test before the edit. |
| Final independent `clean test package` after test correction | **PASS**, 19:02:40 KST, 75 main/33 test sources, **56 tests, zero failures/errors/skips**, repackaged Boot JAR. Log `bans_chatbot/step08-verified.log` is ignored. Includes real security-chain MockMvc, service cursor/idempotency/read/ACL tests, fake two-thread race, Oracle XML/source/manifest checks. It does not execute Oracle. |
| Default `*OracleIT` | **SKIPPED** by Surefire selection; Oracle execution **NOT_RUN**. New ChatOracleIT compiled only. |
| Oracle V003, Mapper SELECT/INSERT/UPDATE, locking/uniqueness/concurrency/CLOB semantics | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No actual SQL execution; see Oracle backlog. |
| Real Gemini API | **SKIPPED**; 0 calls. Mock Gemini network test **NOT_RUN**, outside chat STEP. |
| Live local-mock HTTP process | **NOT_RUN**. REST exercised through MockMvc, not a packaged process. |

STEP 08 files: new `chat/controller/ChatController.java`, `chat/dto/ChatModels.java`, `chat/service/ChatService.java`, `chat/repository/{ChatRepository,InMemoryChatRepository,OracleChatRepository}.java`, `chat/mapper/ChatMapper.java` under `bans_chatbot/src/main/java/egovframework/example`; matching chat Mapper XML in resources; new V003 under `bans_chatbot/db/oracle`; tests `ChatMockMvcTest`, `ChatServiceTest`, `ChatMapperSourceTest`, `ChatOracleIT`. Updated MyBatis MapperScan, migration apply/verify/manifest, schema source/Oracle IT and five relevant docs plus database-design.md.

After adding an offline V003 single-statement parser assertion, the same wrapper/cache command with `test` (without `clean package`) **PASS at 19:04:11 KST**: 56 tests, 0 failures/errors/skips, BUILD SUCCESS. This is the final test source state; the prior `clean test package` is the package evidence. Oracle validation is unchanged.

## STEP 09 verification (2026-11-03 KST)

Environment: Windows 11 PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6. At STEP entry `dev` contained uncommitted STEP 08 migration/chat/source/docs changes, all preserved. Maven commands ran in `bans_chatbot` with `MAVEN_HOME` set to the existing wrapper distribution and `-o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`. No dependency download, Oracle connection, Oracle SQL or Gemini request occurred. E2E's only network traffic was to its embedded local test server.

| Executed command/check | Result / evidence |
|---|---|
| `.\mvnw.cmd -o ... test-compile` after declaring `spring-boot-starter-websocket` | **FAIL** 19:16:25: starter 3.5.6 not cached offline. Replaced with Boot-managed, already cached `spring-websocket` and `spring-messaging` modules. No version changed. |
| Same `test-compile` after direct module declarations | **FAIL** 19:17:01: `ChatStompSecurity.current` accepted `StompHeaderAccessor` while handler injection supplied `SimpMessageHeaderAccessor`. Corrected parameter type. |
| Same `test-compile` after type correction | **PASS** 19:17:29: 78 main/33 then-current test source files compiled. |
| `.\mvnw.cmd -o ... test` before E2E source | **PASS** 19:17:51: 56 existing tests, zero failures/errors/skips. This did not exercise STOMP client traffic. |
| `.\mvnw.cmd -o ... '-Dtest=ChatStompE2ETest' test` | **PASS** 19:18:47, 2 embedded-server STOMP tests (A/B/C queues and CSRF/Origin rejection). |
| Same targeted test after outbound guard | **PASS** 19:19:48, 2 tests. |
| Same targeted test after duplicate/logout extension | **FAIL** 19:20:20, 1 error: test cleanup called `disconnect()` on a socket already closed after logout; message isolation assertions had passed. Fixed test cleanup. |
| `.\mvnw.cmd -o ... clean test package` after cleanup | **PASS** 19:21:22, 58 tests and Boot JAR. |
| Final `.\mvnw.cmd -o ... clean test package` after account-suspension E2E addition | **PASS** **19:22:46 KST**: 78 main/34 test sources, **59 tests, 0 failures, 0 errors, 0 skips**, Boot JAR repackaged. Three tests used the real embedded Tomcat/WebSocket server with process-local `test` repositories; no Oracle. Ignored log `bans_chatbot/step09-final2.log` and Surefire reports record the result. Surefire did not rerun identical tests during the package phase. |
| `git diff --check` | **PASS** after docs synchronization; only line-ending normalization warnings. |
| Ordinary Surefire `*OracleIT` | **SKIPPED** by suite selection; Oracle execution **NOT_RUN**. |
| Real Oracle 26ai commit/event order, persistence, locks, broker fault recovery | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; no JDBC or SQL execution. |
| Real Gemini API | **SKIPPED**; 0 calls. Mock Gemini network test **NOT_RUN**, outside chat scope. |
| Browser UI/manual production deployment | **NOT_RUN**. The E2E used a real test STOMP client and embedded server, not a browser UI or Oracle-backed deployment. |

STEP 09 file inventory: modified `bans_chatbot/pom.xml`, `auth`-agnostic `global/security/SecurityConfiguration.java`, `chat/service/ChatService.java`, and five relevant docs (`api-contract`, `decisions`, `handoff`, `test-status`, `oracle-validation-backlog`). Added `chat/websocket/ChatWebSocketConfiguration.java`, `ChatStompSecurity.java`, `ChatStompController.java` under the production domain and `chat/websocket/ChatStompE2ETest.java` under the mirrored test domain. Existing uncommitted STEP 08 files were not rewritten for STEP 09. No Oracle DDL/Mapper changed in this STEP.

## STEP 10 verification (2026-11-03 KST)

Environment: Windows 11 PowerShell, Temurin Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / Boot 3.5.6. At entry, `dev` had uncommitted STEP 08–09 files; those were preserved. Commands ran from `bans_chatbot` with the wrapper, `-o`, the existing ignored `.maven-repo`, and `MAVEN_HOME` set to the existing Maven 3.9.6 distribution. No Oracle connection/SQL, Gemini call, dependency retrieval or external network operation occurred.

| Actual command/check | Status / evidence |
|---|---|
| `.\mvnw.cmd -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo' test-compile` | **PASS** 19:49:01 KST, 86 main and 34 then-current test Java sources. No tests executed by this goal. |
| Same wrapper/cache with `'-Dtest=ManagedDocumentServiceTest,ManagedDocumentMapperSourceTest,OracleSchemaSourceTest' test` | **PASS** 19:50:11 KST, 9 selected tests. Offline lifecycle, ACL, source/manifest checks; no Oracle execution. |
| Same wrapper/cache with `'-Dtest=ManagedDocumentMockMvcTest' test` | **PASS** 19:51:05 KST, 1 real Spring Security MockMvc test, session/CSRF/approval/grant revoke/XSS. |
| First full `clean test package` | **FAIL** 19:52:09 KST, 64 tests, one failure in pre-existing STOMP E2E suspended-account outbound assertion. New document tests passed. The outbound interceptor checked STOMP command only; broker delivery can have `SimpMessageType.MESSAGE` without a STOMP command. |
| Targeted `'-Dtest=ChatStompE2ETest' test` before fix compiled | **FAIL** 19:53:14 KST, reproduced suspended-account delivery issue. |
| Same targeted test after checking Spring message type | **PASS** 19:54:02 KST, all 3 embedded-server STOMP tests, including suspended-account delivery block. This fixed a real STEP 09 security gap discovered by STEP 10's broader gate. |
| Final full `clean test package` after fix | **PASS 19:54:44 KST**: 86 main/38 test sources; **64 tests, 0 failures/errors/skips**, Boot JAR repackaged. Ignored `bans_chatbot/step10-final-fixed.log` has the evidence. |
| Ordinary `*OracleIT`/tag | **SKIPPED** by suite selection; real Oracle test **NOT_RUN**. New `ManagedDocumentOracleIT` compiles but needs explicit opt-in. |
| V004 migration, document Mapper/lock/trigger/FK/audit rollback/concurrent approval on Oracle | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**; no SQL execution. |
| Real Gemini API | **SKIPPED**, 0 calls. External disclosure remains disabled for user-authored content. |

New STEP 10 files: document `ManagedDocument` domain, `DocumentModels` DTO, `ManagedDocumentService`, `DocumentWorkspace` port, in-memory and Oracle adapters, `ManagedDocumentMapper` interface/XML, `ManagedDocumentController`, V004 reviewer migration, unit/MockMvc/Mapper-source/Oracle-IT tests. Updated Oracle apply/manifest/source/IT, MapperScan, STEP 09 STOMP outbound check, and relevant docs. No H2 or alternate persistent DB was added.

Final `git diff --check` **PASS** after documentation synchronization (only Git LF/CRLF normalization warnings). The 64 selected Surefire tests exclude all Oracle-only cases; no Oracle or Gemini request ran.

## STEP 11 verification (2026-11-03 KST)

Environment: Windows PowerShell, Java 21.0.3, Maven Wrapper 3.9.6, eGov parent 5.0.0 / resolved Spring Boot 3.5.6. Commands ran in `bans_chatbot`, with `MAVEN_HOME` set to the existing Maven 3.9.6 wrapper distribution and `-o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'`. No dependency download, real Oracle connection/SQL or Gemini request occurred. The prior STEP 10 and older uncommitted work was preserved.

| Actual command/check | Result / evidence |
|---|---|
| `.\mvnw.cmd -o ... test-compile` (before new test source) | **PASS 20:20:15 KST**; 91 main / 38 then-current test sources compiled, no tests executed. A preceding checksum helper in the same shell had **FAIL** because its relative path repeated `bans_chatbot`; it did not change the manifest. The corrected `Get-FileHash db/oracle/V005__document_search_tags_indexes.sql` then **PASS** and appended the exact V005 hash. |
| `.\mvnw.cmd -o ... '-Dtest=DocumentSearchServiceTest,DocumentSearchMapperSourceTest,ManagedDocumentMockMvcTest,OracleSchemaSourceTest' test` | **PASS 20:22:16 KST**, 10 selected tests, 0 failures/errors/skips. Covers synthetic 휴가/배포, ACL/grant revoke, stale links, bounds/literal SQL-shaped input, real security MockMvc and offline XML/hash. |
| First `.\mvnw.cmd -o ... clean test package` | **FAIL 20:23:35 KST**, 67 tests, one failure: `OracleSeedSourceTest` detected the manually appended V005 tag SQL differed from the canonical seed renderer. No package was produced by this run. |
| Updated `SyntheticOracleSeedRenderer`, `.\mvnw.cmd -o ... test-compile`, then `java -cp 'target/test-classes;target/classes' ...SyntheticOracleSeedRenderer 'db/oracle/seed/S001__synthetic_company.sql'` | **PASS** around 20:24 KST. Regenerated the checked-in seed from the canonical fixture. Seed and V005 source only; no Oracle SQL executed. |
| `.\mvnw.cmd -o ... clean test package` after generated seed | **PASS 20:25:10 KST**, 67 tests, 0 failures/errors/skips and Boot JAR. |
| `.\mvnw.cmd -o ... '-Dtest=DocumentSearchServiceTest,ManagedDocumentMockMvcTest' test` after added bounds/401 assertions | **PASS 20:25:57 KST**, 3 selected tests, 0 failures/errors/skips. |
| `.\mvnw.cmd -o ... clean test package` after local-mock context test | **PASS 20:28:37 KST**, **68 tests, 0 failures/errors/skips**, Boot JAR. Local-mock Spring context loaded synthetic search data; MockMvc exercised protected API routes. |
| Final `.\mvnw.cmd -o ... clean test package` after retaining the prior published version during review | **PASS 20:30:16 KST**, **68 tests, 0 failures/errors/skips**, Boot JAR. `bans_chatbot/step11-final3.log` and Surefire reports contain the final evidence. Oracle SQL remains unexecuted. |
| Ordinary Surefire `*OracleIT` | **SKIPPED** by suite selection. New `DocumentSearchOracleIT` compiled but was **NOT_RUN**; it requires explicit opt-in and real 26ai. |
| V005 DDL/seed, Mapper JOIN/`DBMS_LOB.INSTR`, locks, plan/Korean analyzer, concurrent revoke on real Oracle | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Source parsing and fake tests are not database execution. |
| Real Gemini API | **SKIPPED**, 0 calls. Mock Gemini network test **NOT_RUN**; no AI endpoint added in STEP 11. |
| Browser/manual local-mock deployment | **NOT_RUN**. Spring context/MockMvc tested, but no browser or separately deployed process was used. |
| `git diff --check` | **PASS** after final docs update, exit 0; only Windows LF/CRLF normalization warnings. |

### Follow-up live local-mock HTTP smoke (2026-11-03 20:50 KST)

The first hidden `Start-Process` attempt was **BLOCKED by automatic approval review** (`blocked by policy`); no server was launched by that attempt. A foreground `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.port=18081` then started successfully with a runtime-generated, temporary admin password outside the workspace. Actual localhost HTTP calls **PASS**: `/api/health` returned `UP`; CSRF/session login and `/api/auth/me` returned `EMP-001`; `GET /api/documents/search?q=휴가&page=0&size=10` returned two fixture hits; a returned fragment source link returned matching content; `q=배포` returned zero hits for D01 admin; `GET /api/documents/DOC-003/published` returned 404; anonymous search returned 401. The temporary password file was deleted and the exact server PID was stopped. This is a real local-mock HTTP result, not Oracle SQL or Gemini execution. Browser UI, mutating document workflow over live HTTP, and Oracle-backed HTTP remain **NOT_RUN** or **BLOCKED** as applicable.

STEP 11 files: new domain search repository port, local-mock/Oracle adapters, `DocumentSearchService`, `DocumentSearchMapper` interface/XML and source/Oracle-IT/service/local-mock tests; modified document Controller/DTO/domain/workspace/Mapper/service/MockMvc tests; V005 migration, runner/manifest/metadata/source checks, canonical seed renderer/SQL/preflight/verification and relevant docs. No H2 or alternate DB was added. See `oracle-validation-backlog.md` for executable Oracle conditions and remaining races.

## STEP 12 provider verification (2026-11-04 KST)

Entry: clean `dev`, commit `1c8fd0a`. Windows PowerShell / Temurin **21.0.3**, wrapper Maven **3.9.6**, actual eGov parent/runtime **5.0.0** / resolved Boot **3.5.6**. Rechecked source, profiles, resolved parent and official eGov/Google documentation before edits. No dependency/framework version, database technology, schema/SQL/Mapper or existing route changed. Logs below are Git-ignored module files; Surefire results are under target/surefire-reports. Official documentation browsing and an unsuccessful Maven artifact download attempt are separate from Gemini API calls. Real Google model requests and Oracle connections were **0**.

Common environment and arguments for actual module-directory wrapper commands (the cache argument below abbreviates the same absolute value used in execution):

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Actual command / check | Result, time and evidence |
|---|---|
| `.\mvnw.cmd -o $cacheArg org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get '-Dartifact=com.google.genai:google-genai:1.80.0' '-Dtransitive=false'` | **FAIL 14:57:21**, artifact/POM absent from offline cache. `step12-sdk-evaluation.log`. No SDK runtime execution and no model request. |
| `.\mvnw.cmd $cacheArg '-Dmaven.wagon.http.retryHandler.count=0' '-Dmaven.wagon.rto=5000' org.apache.maven.plugins:maven-dependency-plugin:3.6.1:get '-Dartifact=com.google.genai:google-genai:1.80.0' '-Dtransitive=false'` | **FAIL 15:05:52**, actual repository transfer denied: `Permission denied: getsockopt`. `step12-sdk-resolution.log`. Latest SDK dependency resolution **BLOCKED by network environment**; SDK Java 21 runtime/transitive validation **NOT_RUN**. No version was guessed or old SDK adopted. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=ProviderConfigurationTest,CallBudgetTest,GeminiProviderNetworkTest' test` first attempt | **FAIL 15:02:50**, 37 tests, 29 failures / 1 error. A relative URI beginning with `gemini-...:generateContent` was parsed as a URI scheme, causing requests to fail before HTTP. Fixed with an explicitly relative `./` model path. `step12-targeted.log`. |
| Same targeted command after URI fix | **PASS 15:03:42**, 37 tests, 0 failures/errors/skips. `step12-targeted2.log`. Contains 32 real loopback HTTP/Mockito network cases, two budget tests and then-three configuration tests. |
| `.\mvnw.cmd -o $cacheArg clean test package` after smoke/error-contract source | **PASS 15:07:05**, 106 tests, 0 failures/errors/skips, Boot JAR. `step12-full.log`. |
| `$env:BANS_GEMINI_SMOKE='NO'` then `.\mvnw.cmd -o $cacheArg -Pgemini-smoke test` | **PASS for the disabled opt-in gate, 15:08:45**; selected `GeminiLiveSmokeIT`, 1 test **SKIPPED**, zero executed test bodies. `step12-smoke-gate.log`. This is not a Gemini smoke success: real model call remains **NOT_RUN**. The permission variable was set only in that child process. |
| Final `.\mvnw.cmd -o $cacheArg clean test package` after wire-debug rejection test | **PASS 15:11:01**, **98 main / 47 test sources, 107 tests, 0 failures/errors/skips**, repackaged Boot JAR. `step12-final.log` and fresh Surefire reports. 39 new selected cases plus 68 prior cases; the package phase did not execute the tests twice. |
| Default Mock/context wiring, active-actor checks, configuration limits and explicit live activation/profile/key/model/acknowledgement gates | **PASS** in final suite. A missing key and an ambient fake key both retain Mock by default. Actual local-mock Spring context remains DB-free. Wire-debug rejects live activation. AI-only `oracle` profile selection test is not an Oracle application startup test. |
| Mock HTTP protocol, filtering and failure paths | **PASS** in final suite. Every actual provider HTTP request targets 127.0.0.1. Checks fixed model path, key only in header, greeting-only body, token cap, no actor/history/tools, no cookie forwarding, 403/429/5xx/302/400, exactly one attempt, Retry-After seconds/date/invalid/large input, cooldown, connect-error classification, header/body stalls, response-size cap, malformed/duplicate/trailing JSON, empty/blocked/truncated/tool/key-echo replies, concurrency rejection/release and no requests for invalid actors. Public error MockMvc and captured output check sanitized fields/no upstream key or payload. |
| Eight Oracle-only classes in ordinary Surefire | **SKIPPED by suite selection**; compiled, not executed. No Failsafe/Oracle command invoked. |
| Oracle 26ai connection/migration/seed/SQL/ACL/transaction/lock/concurrency/restart/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Existing backlog unchanged in execution status. STEP 12 adds no DB operation. |
| Real Gemini synthetic greeting / project-specific Free-tier quota/access | **NOT_RUN**, zero calls. Separate smoke source exists; its disabled gate was tested, not the actual API. No key was inspected, printed or used for a real request. |
| Arbitrary-question safety review, document-to-Gemini integration, AI HTTP/UI | **NOT_RUN / intentionally not implemented in STEP 12**. Only fixed synthetic greetings are accepted. |
| Separate packaged browser/manual live deployment | **NOT_RUN** in STEP 12. Spring contexts, common-error MockMvc, existing embedded-server STOMP E2E and provider loopback tests did execute. |

Java emitted the existing Mockito dynamic-agent warning and an AssertJ deprecated test-helper warning; these were warnings, not build failures. Provider implementation has no persistence or raw request/response logging. The passing REST adapter is built from Java 21 HttpClient and existing Jackson; it does not establish latest Google SDK compatibility, actual Google quota/billing, Oracle behavior or production readiness. Counters are per-process and reset on restart. See `gemini-free-tier.md` for official links/activation details and the STEP 12 handoff for the complete file inventory.

Final documentation/source `git diff --check`: **PASS**, exit 0 (Windows line-ending normalization warnings only). Aggregated 37 fresh Surefire XML reports confirm 107 tests / 0 failures / 0 errors / 0 skips. Git diff contains no database or Mapper XML changes. Source changes are uncommitted for user review; no existing user edits were overwritten.

## STEP 13 read-only tools verification (2026-11-04 KST)

Entry: dev at 1c8fd0a, with all prior STEP 12 modified/untracked files preserved. Windows PowerShell, Temurin **21.0.3**, Maven Wrapper **3.9.6**, actual eGov parent/runtime **5.0.0**, resolved Boot **3.5.6**. Rechecked repository/profile/build state, parent POM and official eGov and Google documents. No framework/dependency version, DB dialect, migration/seed or key activation setting changed. Oracle SQL source changes are classification SELECT columns and fixed audit metadata INSERT only. No Oracle/Gemini API connection, installation, paid activation or dependency retrieval was performed. Documentation web browsing is separate from API execution.

All commands ran in bans_chatbot with the existing workspace cache and wrapper environment:

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Actual command/check | Status, time and scope |
|---|---|
| `.\mvnw.cmd -o $cacheArg compile` | **PASS 15:30:35**, 110 main sources, no tests. `step13-compile.log`. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=AiToolSecurityTest,ToolRegistryTest,AiToolMockMvcTest,AiToolAuditMapperSourceTest,DocumentSearchMapperSourceTest' test` first attempt | **FAIL 15:35:17**, test compilation: missing closing `);` in new mutation-test lambda. No tests executed. Corrected test syntax. `step13-targeted.log`. |
| Same targeted command after syntax correction | **FAIL 15:36:15**, 39 tests / two assertion failures. New test counted successful returns instead of all invocations after expected 404; changed to verify both invocations. Existing mapper source test forbade external flag text anywhere in SQL; updated it to require classification SELECT columns while still forbidding those flags in internal predicates. These were test expectations, not a bypass in production. `step13-targeted2.log`. |
| Same selected classes plus `ManagedDocumentMapperSourceTest` | **PASS 15:38:36**, 41 tests / 0 failures/errors/skips. Added draft-state/current workspace classification validation. `step13-targeted3.log`. |
| `.\mvnw.cmd -o $cacheArg clean test package` after additional within-run status/session and immutable/excerpt tests | **PASS 15:42:15**, 150 tests / 0 failures/errors/skips, Boot JAR. `step13-full.log`. |
| Final `.\mvnw.cmd -o $cacheArg clean test package` after explicit constructor/null binding and transport/forged-actor tests | **PASS 15:44:44**, **110 main / 52 test sources, 152 tests / 0 failures/errors/skips**, repackaged Boot JAR. `step13-final.log` and fresh Surefire XML. Package did not rerun the same tests. |
| New tool/security/projection/budget tests | **PASS** in final suite: 21 AiToolSecurityTest, 17 ToolRegistryTest, 5 actual-security AiToolMockMvcTest, 1 audit XML source test, plus 1 new existing-provider network guard = 45 new selected cases over STEP 12. |
| Forged caller/user/role/department and legacy session actor | **PASS**: current account/session identity is used; cross-department/private document stays hidden. Supplied old admin authority does not bypass resource ACL. Missing real security context is rejected. |
| Revoke/flag/withdraw/version/status/credentials/logout/department changes during runs | **PASS** with serialized memory adapters/Mockito. No stale excerpt reaches Mock after the tested pre-send changes; changed department is reloaded. This is not proof of Oracle transaction races. |
| Question injection/SQL/salary/write tools/unknown fields/numeric or JSON abuse | **PASS**: exact reviewed question gate, registry/schema/size/call bounds reject invalid operations. Allowed document instructions remain data and cannot add a tool or permission. Non-exportable readable document text and noncanonical employee input are absent from captured Mock payload. |
| Outbound/HTTP/audit allowlists | **PASS**: employee six fields, source four fields/200 code points, immutable copies; HTTP only counts/notice. Metadata audit has only actor/outcome/call/item fields; no question/answer/source/argument/key. UI contains transfer notice/unchecked acknowledgement and uses textContent. It was checked by MockMvc, not a visual browser run. |
| Request/run limits and live transport boundary | **PASS**: per-session five-per-minute/one active run, input/tool/result limits, execution budget and safe audit-failure error. Concrete non-Mock provider rejected before reads; actual live adapter default tool method makes 0 HTTP requests in loopback test. |
| Nine Oracle-only `*OracleIT` classes / real-Gemini smoke in ordinary Surefire | **SKIPPED by suite selection**, compiled but not executed. New AiToolOracleIT is opt-in via ORACLE_AI_TOOL_TEST=YES. Final suite's zero skips counts selected tests only. |
| Actual Oracle SELECT/JDBC record mapping, classification flags, audit INSERT/REQUIRES_NEW, ACL JOIN/race/locking/transactions/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. No Oracle suite or SQL was executed. |
| Actual Gemini API/project quota/access and latest SDK runtime validation | **NOT_RUN** in STEP 13, real API requests 0. Inherited STEP 12 SDK dependency/network limitation remains unresolved; no retry or upgrade attempted. |
| Real model function-call loop, arbitrary-question safety review, real outbound tool context | **NOT_RUN / intentionally not implemented**. Only fixed reviewed examples and Mock payloads are enabled. |
| Packaged process/browser/manual UI workflow | **NOT_RUN** in STEP 13. Actual-security MockMvc, Spring contexts, existing embedded STOMP E2E and provider loopback tests did run. |

Warnings about Mockito dynamic agents, a prior deprecated AssertJ helper and a classpath persistence-provider probe did not fail the build or create a DataSource. Ordinary tests retain zero Oracle connections and zero real Gemini calls. Source/mapper parsing is not Oracle SQL validation. Per-session counters are process-local; the final recheck does not remove a concurrent change after that check. Full modified-file inventory and next-step conditions are in the STEP 13 handoff; security/operational bounds are in ai-tool-security.md.

Final checks after documentation synchronization: wrapper `-version` **PASS** (Maven 3.9.6 / Java 21.0.3 / Windows 11); `git diff --check` **PASS**, exit 0 with line-ending warnings only. Aggregating 41 fresh Surefire XML reports confirms 152/0/0/0. Git diff shows no db/oracle migration/seed/manifest changes. Changes remain uncommitted, including preserved STEP 12 work.

## STEP 14 executed validation (2026-11-04 KST)

Entry: dev at 1c8fd0a with prior uncommitted STEP 12/13 work preserved. Windows 11 PowerShell / Java 21.0.3 / Maven Wrapper 3.9.6 / eGovFrame 5.0.0 / Boot 3.5.6. Rechecked actual source, git status, profiles and official version/Google evidence; no versions/dependencies/configuration/DDL/seed changed. Real Oracle connections **0**, real Gemini API calls **0**; loopback HTTP and documentation browsing are separate from external model invocation.

Actual commands ran from bans_chatbot with:

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| Executed command/check | Status / evidence |
|---|---|
| `.\mvnw.cmd -o $cacheArg compile` | **FAIL 16:14:12 KST**, new generate timeout parameter conflicted with a catch variable named timeout. No tests ran. Renamed catch variable to expired. Ignored `step14-compile.log` preserves the failure. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=AiQuestionServiceTest,QuestionPolicyTest,AiQuestionMockMvcTest,AiQuestionAuditMapperSourceTest,GeminiProviderNetworkTest' test` | **PASS 16:19:41 KST**, 77 tests / 0 failures/errors/skips, 121 main and 58 test sources. `step14-targeted.log`. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 16:22:38 KST**, **196 tests / 0 failures/errors/skips**, Java 21 compile of 121 main / 58 test sources and repackaged executable JAR. `step14-final.log`. Package reports Surefire already run, not a second execution. |
| Fresh Surefire XML aggregation | **PASS**, 45 reports total 196/0/0/0, matching the final log. 44 additional selected cases over STEP 13: 26 service + 2 policy/budget + 11 MockMvc + 1 audit mapper + 4 loopback cases. |
| Vacation source / expense owner / canonical public contact | **PASS**, server-built answer/source and six public DTO fields; structured path has zero Provider calls. Source link itself is exercised through authenticated document GET. |
| Retired version, nonexistent rule, other department, ambiguous evidence, forged caller/role | **PASS**, no invented answer, no unauthorized sources or unreviewed outbound. Current actual security session is authoritative. |
| Post-Provider revoke, withdrawal, changed version/content/external flag; structured department change | **PASS**, deterministic Mockito/memory tests return UNANSWERABLE without stale facts/sources. This proves the Java recheck only, not an Oracle cross-transaction race guarantee. |
| Prompt injection/PII/unknown question, invented quote/ID, extra/duplicate/trailing/malformed model JSON | **PASS**, unreviewed questions do not reach Provider; model drafts never become arbitrary facts. Outbound contains only reviewed question/S1/excerpt. |
| Provider 429/503/timeout, zero retries, minimized wire JSON, remaining-time timeout | **PASS**, Mockito and actual loopback HttpServer; no Google requests. AI MockMvc failures leave chat REST and auth available. Existing localhost STOMP regression also PASS; concurrent AI-failure/STOMP load is not separately exercised. |
| Auth/CSRF, shared request limits, stages/tools/provider/time bounds, audit failure and metadata allowlist | **PASS**, actual-security MockMvc and focused tests. No question/answer/source/key fields in audit; answer/prompt toString redacts text. HTML uses textContent, no innerHTML/browser storage. |
| Ten Oracle-only classes and real Gemini smoke in normal Surefire | **SKIPPED by suite selection** (compiled, excluded by name/tag); final zero skips applies only to selected tests. New AiQuestionOracleIT also requires ORACLE_AI_QUESTION_TEST=YES. |
| Oracle ACL JOIN, mapper/JDBC/enum/CLOB mapping, audit INSERT/REQUIRES_NEW, migrations/locks/races/performance | **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. Oracle suite/SQL execution not attempted. Mapper XML parsing is not SQL success. |
| Real Gemini greeting/question, account quota/model access, Oracle+Gemini E2E | **NOT_RUN**. No key used and no live activation. SDK runtime evaluation retains the unresolved STEP 12 dependency limitation; not retried in STEP 14. |
| Packaged live process/manual browser/visual UI verification | **NOT_RUN**. Executable packaging and MockMvc HTML/service integration passed. No final production-readiness assertion. |

The intermediate compile FAIL is resolved by the final PASS; it is not erased. Warnings about Mockito dynamic-agent loading, the existing deprecated assertion helper, missing optional templates and the persistence-provider probe were non-fatal. Profile tests still verify DB-free default/test/local-mock wiring. Counters are process/session-local, and ten seconds is a stage/HTTP budget rather than a forced interrupt of repository/audit I/O. Oracle validation remains pending as listed in oracle-validation-backlog.md.

Post-document checks at **16:28 KST**: wrapper `-version` PASS (Maven 3.9.6, Java 21.0.3, Windows 11); `git diff --check` found no whitespace errors (line-ending warnings only); `git diff --numstat -- bans_chatbot/db/oracle` is empty; new question main/test source trailing-whitespace search is empty. `jar tf target/bans_chatbot-1.0.0.jar` confirms production question/audit classes and XML are packaged, with no QuestionTestSupport or OracleIT entry. No tests were rerun after documentation-only edits. Changes remain uncommitted, including preserved STEP 12/13 work.

## Full regression, route access and Git hygiene (2026-11-04 KST)

User requested a full run of every available feature, API access checks and ignore rules for local notes/diagnostics. This is maintenance of STEP 01–14, not implementation of STEP 15. Preserved existing uncommitted work and framework/DB/model/profile choices. Environment: Windows 11, Java 21.0.3, Maven Wrapper 3.9.6, eGovFrame 5.0.0, Boot 3.5.6. No dependency retrieval or real Oracle/Gemini request.

Executed from bans_chatbot using the same MAVEN_HOME and cacheArg defined in STEP 14 above:

| Actual command/check | Result |
|---|---|
| `.\mvnw.cmd -o $cacheArg '-Dtest=RouteAccessMockMvcTest' test` | **PASS 16:47:12**, 60 tests / 0 failures/errors/skips; ignored regression-routes.log. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 16:48:29**, **256 tests / 0 failures/errors/skips**, 121 main / 59 test sources, executable JAR; ignored regression-full.log. No duplicate Surefire run during package. |
| Fresh Surefire aggregation | **PASS**, 46 XML reports, total 256/0/0/0. Includes all previous 196 cases and 60 additional route checks. |
| Registered MVC mappings versus explicit test inventory | **PASS**, all **37 method/path pairs**: 17 GET and 20 mutations, including the local-mock-only demo and two UI routes. Framework /error, implicit HEAD/OPTIONS and WebSocket upgrade are not counted as these MVC controller routes. |
| Anonymous access to every mapping | **PASS**, 32 protected mappings return 401 with valid CSRF supplied where needed. Public health/CSRF GETs return 200; public login/activation with empty JSON correctly return 400. |
| Every mutation with real login and missing CSRF | **PASS**, all 20 return sanitized 403. This is expected denial, not a broken endpoint. |
| Authenticated read paths, unknown path and wrong method | **PASS**, 17 mapped GETs return 200 with appropriate current test resources/parameters; unknown API returns 404 and POST to GET-only health returns 405. |
| Previously service-only document transitions through MVC | **PASS**, create/edit draft/review/reject/add version/review/publish/withdraw and search hiding. Existing tests also exercise grants/revoke/resource denial and retired sources. |
| Packaged JAR loopback HTTP smoke | **PASS by 16:50 KST**, actual `java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=local-mock --server.address=127.0.0.1 --server.port=0`; cookie-bearing HttpClient fetches CSRF then checks all 37 anonymous mappings against the above expected statuses. Ignored regression-http-check.log ends PACKAGED_HTTP_PASS routes=37. No credentials/CSRF bodies printed. Process stopped in finally. Authenticated business flows use MockMvc and existing embedded STOMP tests, not this anonymous wire smoke. |
| First local smoke-script attempt | **FAIL before HTTP**, startup log was empty and Regex.Match received null. Added an empty-log guard, retried successfully. This was a disposable verification-script defect, not an application startup/API failure; preserved here separately from final PASS. Script is intentionally under ignored .scratch/route-smoke.ps1. |
| Git ignore behavior | **PASS**, 9 required source/doc/example paths remain eligible and 17 representative local artifact/secret paths are ignored using git check-ignore --no-index. No existing source/docs remain silently ignored. |
| Oracle-only ten classes | **SKIPPED by normal-suite selection**; actual SQL/DB/transaction/race/performance validation **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, no Oracle suite attempted. |
| Real Gemini smoke/E2E and latest SDK runtime | **NOT_RUN**, real Gemini requests 0. Real API is not authorized by this request to run offline regression. |

Feature coverage in the executed full suite:

| Area | Executed checks / result |
|---|---|
| Bootstrap/profiles/common | **PASS**: actual eGov module smoke, keyless Mock, no DataSource in normal profiles, unsafe profile/config fail-fast, API error/request-ID contract, validation and source-level Oracle migration/seed invariants. |
| Authentication | **PASS**: activation use/expiry/reissue/concurrency, actual login/logout/session fixation/expiry/credential/status boundaries, roles and CSRF. |
| Organization | **PASS**: 20 invented employees/5 departments, public DTO/filter/page validation, admin create/status/move, privilege injection and session refresh. |
| Direct chat REST | **PASS**: participants, pair reuse, send/idempotency/conflict, pagination/read-state/unread counts, outsiders and invalid input. |
| WebSocket/STOMP | **PASS**: all 3 ChatStompE2ETest scenarios on an actual embedded localhost server: two-party ack/delivery/outsider/reconnect; CSRF/origin rejection; suspended-account outbound denial. |
| Documents/search | **PASS**: role+ACL workflow, review/grants/revoke/current publication, sanitized rendering, source/version/search bounds, internal/external distinction and synthetic fixtures. |
| AI Provider/tools/questions/audit | **PASS**: safe defaults/config/quotas, loopback failure/timeout/JSON/key boundaries, fixed read-only tools, synthetic disclosure, current-source/structured responses, prompt injection/unanswerable and metadata-only audit. No real model output or Oracle facts certified. |

Git hygiene correction: previous `**/*credential*` silently excluded production `auth/domain/CredentialStatus.java`, leaving it absent from Git's index despite builds depending on it. Narrowed secret matching to filenames/directories; this existing source is now visible as untracked and **must be included in a future code commit**. It was not rewritten or staged. Added *.local.md/*.local.txt, docs/local, notes/local, scratch/.scratch, editor/merge backups and JVM dumps/recordings. Existing *.log/target/cache/env rules remain. Shared decisions/contracts/handoff/test evidence stay versioned; do not blanket-ignore Markdown/text or remove tracked docs from the index.

Final checks at **16:52:55 KST**: git diff --check **PASS** (exit 0); actual packaged smoke PID no longer exists; .scratch/route-smoke.ps1 and regression logs match ignore rules; no Oracle migration/seed diff. No application code changed after the full PASS. Changes are not staged/committed.

## 로컬 자료 폴더 정리 (2026-11-04)

과거 로그의 현재 위치는 모두 `bans_chatbot/local-notes/`다. 로그 55개·javac 진단 1개·로컬 HTTP 확인 스크립트 1개를 이동했고, 이동 직후 SHA-256 일치를 확인했다. 스크립트는 이동 후 모듈/출력 경로를 새 위치에 맞게 수정했다. 해당 폴더를 Git에서 제외하고 .gitignore 주석을 한글로 번역했다. 원래 경로와 해시는 이 로컬 폴더의 moved-files.json에 보관한다. 공용 docs는 이동하거나 Git에서 제외하지 않았다.

애플리케이션 기능 테스트·JAR 실행은 이번 정리에서 **NOT_RUN**이며 앞선 전체 256건 PASS/경로 37개 PASS 기록을 그대로 유지한다. 파일 이동은 삭제·덮어쓰기 없이 수행했다. Oracle/Gemini 실행 상태와 SQL은 바뀌지 않았다.

**PASS 16:57:26 KST**: 이동 파일 57개 존재 및 기존 경로 정리, 수정한 스크립트 외 파일 해시 일치, PowerShell AST 구문 검사, local-notes Git 제외, CredentialStatus.java 제외되지 않음, 루트/모듈 최상위 잔여 로그 0개, git diff --check exit 0을 실제 확인했다.

## STEP 15 실행 검증 (2026-11-04 KST)

시작은 깨끗한 dev / 9f1cfad였다. Windows 11 / Java 21.0.3 / Maven Wrapper 3.9.6 / eGovFrame 5.0.0 / Boot 3.5.6을 유지했다. 신규 의존성/SDK/프론트 빌드 도구/모델/SQL/프로필 변경은 없다. Oracle 연결 **0회**, 실제 Gemini 호출 **0회**다. 공식 문서 브라우징과 실제 localhost HTTP/WebSocket 검사는 외부 모델 호출이 아니다.

실행 명령의 공통 환경(모듈에서 실행):

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

| 실제 명령/검사 | 상태·근거 |
|---|---|
| `node --check` app.js/api.js/stomp.js 및 두 test .mjs | **PASS**, 설치된 Node 22.17.0으로 구문 확인. 새 npm 패키지 다운로드 없음. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=RouteAccessMockMvcTest' test` | **PASS 17:12:44**, 63건/실패·오류·skip 0. `local-notes/step15-routes.log`. MVC 등록 39개와 변경 API 21개의 CSRF 경계. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=WorkbenchBrowserTest' test`, 첫 시도 | **FAIL 17:16:17**, Node exit 13/미해결 CDP await. Chrome 클릭 이전 실패. `step15-browser-build.log`. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=WorkbenchBrowserTest,WorkbenchMockMvcTest' test`, 두 번째 | **FAIL 17:19:12**, MVC 3건 PASS, 브라우저 1건 FAIL. Chrome GPU 프로세스 사용 불가로 CDP 연결 종료. test driver timeout/close 처리를 보강해 원인을 확인. `step15-browser2-build.log`. |
| 같은 명령, 세 번째 | **FAIL 17:20:50**, MVC 3건 PASS, GPU 실행 방식 조정 후 CDP Page.enable timeout. `step15-browser3-build.log`. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=WorkbenchBrowserTest' test`, 네 번째 | **FAIL 17:24:40**, 기본 browser context에서도 Page.enable timeout. `step15-browser4-build.log`. 화면 로그인/클릭까지 도달하지 못함. |
| 실제 Chrome 전체 UI/키보드/390px/브라우저 XSS/503 클릭 시나리오 | **BLOCKED: CHROME_RENDERER_CDP_UNAVAILABLE**. 네 번의 실행 FAIL을 보존한다. 브라우저 보안 sandbox를 해제해서 우회하지 않았다. 선택적 실행은 이후 BANS_UI_BROWSER_TEST=YES로 분리했으며 성공은 확인하지 않음. |
| `.\mvnw.cmd -o $cacheArg '-Dtest=WorkbenchClientIntegrationTest,WorkbenchMockMvcTest' test` | **PASS 17:28:32**, 당시 MVC 3건 + 실제 JS 통합 1건, 총 4건. `step15-client-build.log`, `step15-client.log`의 CLIENT_PASS. 이후 6개 화면 대응 MVC 연속 시나리오를 1건 추가. |
| `.\mvnw.cmd -o $cacheArg clean test package` | **PASS 17:31:36**, 265 selected / 실패 0 / 오류 0 / skip 1. `step15-full.log`. |
| 같은 전체 명령, 안전한 현재 게시 본문 표시 추가 후 | **PASS 17:34:33**, 동일 집계, 재패키징 JAR. `step15-final.log`. |
| 최종 같은 전체 명령, CSRF 대기 중 세션 교체 쓰기 차단 보완 후 | **PASS 17:38:32**, **123 main / 63 test source**, **265 selected = 264 PASS + 1 SKIPPED**, 실패·오류 0. `step15-release.log`. Package에서 Surefire를 중복 실행하지 않음. |
| Fresh Surefire XML 집계 | **PASS**, 49개 report, 265/0/0/1. 선택적 WorkbenchBrowserTest 1건은 환경 gate로 SKIPPED. 실제 Node 통합·MockMvc·기존 STOMP/Provider 회귀는 실행해 PASS. |
| 실제 JS + 실제 HTTP/WebSocket | **PASS**, 배포되는 stomp.js와 실제 세션/CSRF를 사용해 두 계정 연결·전송·ack·읽음·재연결·REST gap 복구. 실제 문서/AI 출처/429 및 AI 한도 후 채팅을 확인. API 401/503 전달·403/429 문구·stale 응답 폐기·안전 renderer는 JS 단위 경계 검사 포함. 실제 브라우저 DOM 실행과 동일하다고 주장하지 않음. |
| 패키징 JAR localhost HTTP smoke | **PASS**, local-mock/127.0.0.1/임시 포트에서 39 MVC 경로의 익명 응답을 확인. `local-notes/step15-packaged-http.log`의 routes=39. root 200과 보호 API의 401, 공개 로그인/활성화 빈 JSON 400 등 예상 상태. 업무 로그인/클릭을 수행한 검사는 아님. 최종 JAR 재확인 기록은 step15-release-http.log. |
| HTML/자산 크기·출력·키 노출 | **PASS**, 6개 화면 shell, CSP/no-store/nosniff, 정확한 정적 파일 GET 허용, 브라우저 저장소/HTML 직접 삽입/Google endpoint/API 키 부재. 네 자산 약 25KB, shell 포함 64KiB 미만. 실성능 벤치마크가 아니라 소스/응답 크기 검사. |
| 신규 관리자 발급 API | **PASS**, 현재 관리자만 발급, 비관리자/위조 role·caller/CSRF 누락/자기 자신 거부, 일회용 활성화 및 로그인. 코드 응답 no-store. |
| Oracle-only 11개 클래스 / 실 Gemini smoke | **SKIPPED by suite selection**, 컴파일만 수행. Oracle SQL/DB ACL/잠금/트랜잭션/영속성/실성능 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**, 실행 시도 없음. |
| 실제 Gemini/Oracle+Gemini E2E / SDK 최신 실행 검증 | **NOT_RUN**, 기존 제한 유지. 실 API/유료 전환/Oracle 설치하지 않음. |

최소 5개 화면의 DB 없는 증빙은 다음과 같이 구분한다. (1) 활성화/로그인, (2) 직원·담당업무 검색, (3) 대화/읽음, (4) 문서 작성/ACL/검토/게시/안전 출력, (5) AI 정형/출처, (6) 관리자 변경에 대응하는 full-chain MockMvc 흐름을 실행했다. 공용 HTML의 여섯 화면 구성도 검사했다. 채팅/출처에는 실제 서버+배포용 JS 검사도 있다. 브라우저에서 여섯 화면을 클릭했다는 결과는 없다.

실행 기록/Chrome 진단은 모두 ignored local-notes에 있다. 기존 Mockito agent/AssertJ deprecated/optional persistence provider 경고는 빌드 실패 원인이 아니며 DataSource를 만들지 않았다. 새 Thymeleaf template이 있으므로 이전 template-location 경고는 제거된다. Oracle/프로필/스키마/모델/권한 계약의 기존 제한은 유지하고, 시연 절차·500건 한도·heartbeat 미설정·수동 문서 코드 workflow 등 기능 한계는 web-ui.md에 정리했다.

최종 JAR HTTP 재확인 **PASS 17:40:51 KST**, step15-release-http.log에 routes=39. **17:42:39 KST** git diff --check exit 0, POM/db/oracle diff 없음, 해당 smoke PID 종료, JS 자산 25,335바이트 및 JS 구문 검사를 확인했다. 이후 문서/로컬 메모만 갱신했으며 코드는 변경하지 않았다. 변경은 미커밋 상태다.
# 2026-11-09 demo data bundle review

- **PASS (source only):** `OracleSeedSourceTest,OracleSchemaSourceTest` targeted offline Maven run: 8 tests, 0 failures/errors/skips. Canonical PSV and SQL source checks passed.
- **PASS (local files):** SHA-256 comparison confirmed all four local PSV copies and `S001__synthetic_company.sql` match their tracked canonical sources; `git check-ignore` confirmed the local bundle path is excluded.
- **NOT_RUN (Oracle):** No database connection, seed DML, or `verify_demo_data.sql` execution was performed in this review. The user reported 18 created tables, but their names, seed counts, constraints, and runtime behavior were not verified from this workspace.
- **BLOCKED (full Oracle UI demo):** The initial Oracle administrator account remains PENDING; a secure first-administrator bootstrap is not implemented. No SQL credential shortcut was added.

# 2026-11-09 Oracle properties configuration check

- **PASS:** `ProfileWiringTest,ProfileBoundaryGuardTest` offline Maven run: 3 tests, 0 failures/errors/skips. Normal `test` profile still has no DataSource.
- **PASS:** `git check-ignore` confirms `bans_chatbot/oracle-local.properties` is ignored. The generated local file has no saved account or password yet.
- **NOT_RUN:** Oracle JDBC connection and `oracleIntegrationTest.cmd` against a configured DB. No credentials were supplied in this workspace; file import and the changed runner require a configured Oracle connection for end-to-end confirmation.

## 2026-11-09 Oracle connection attempt after local configuration

- **PASS:** Local properties contain the three required keys and are Git-ignored. Oracle driver and Failsafe plugin were resolved for the explicit connection test.
- **FAIL:** `OracleConnectionOracleIT` reached application startup but the first configured URL (`localhost:1521/FREEPDB1`) failed with Oracle error 12541. The local listener advertises `192.168.1.65:1521` and `freepdb1`; the ignored local URL was corrected to that listener host.
- **FAIL:** A read-only JDBC connection diagnostic with the corrected host returned Oracle error 1017 (invalid credentials). No seed or other database write was attempted. The user needs to confirm the PDB account and password locally before retrying. The test runner output's full stack trace is confined to ignored build output; credentials were not printed in the response.

## 2026-11-09 Oracle credential retry

- **FAIL:** After the user updated the ignored properties file, the read-only JDBC retry still returned Oracle error 1017. The DBeaver connection settings showed the same host, port, service and `BANS_CHAT` user as the local properties file. Parsing did not alter the password; it was neither quoted, whitespace-padded nor an obvious placeholder. The remaining mismatch is the password accepted by Oracle for that PDB account. No seed or schema write occurred.

## 2026-11-09 Oracle connection verified after password change

- **PASS:** Read-only JDBC connection succeeded with the ignored local properties file and reported Oracle as the database product.
- **PASS:** Explicit `OracleConnectionOracleIT` ran with `oracle,oracle-it` and verified the Oracle 26ai Free banner: 1 integration test, 0 failures/errors/skips, `BUILD SUCCESS`. The accompanying scoped normal test had 2 passing cases.
- **PASS (read-only metadata):** Current schema has 18 tables and 5 system-role rows. `department`, `employee`, `employee_account`, and `document` each have 0 rows, consistent with a migrated but unseeded schema.
- **NOT_RUN:** Synthetic seed DML and full Oracle feature tests. No database write was performed during this connection verification.

## 2026-11-09 Oracle synthetic demo seed application

- **FAIL (no data change):** The first SQL*Plus runner attempt rejected the unquoted Easy Connect syntax with `SP2-0306`; a read-only check still found 0 business rows. A corrected SQL*Plus probe then failed with local client `ORA-12638`; the JDBC connection remained valid. The SQL*Plus wrapper `apply_seed.sql` was not successfully run.
- **PASS:** The canonical `S001__synthetic_company.sql` was parsed using the same statement boundary rules as `OracleSeedOracleIT` and applied through JDBC as one transaction. Preflight confirmed `FREEPDB1`, the intended schema owner, 18 tables, 5 system roles, and 0 rows in all 17 business tables. Exactly 216 MERGE/UPDATE statements executed and validation passed before commit.
- **PASS (new connection after commit):** 5 departments, 20 employees, 12 unusable PENDING accounts, 18 employee-role rows, 20 documents/versions/fragments/tags, 18 reviews, 27 grants, 16 approved/2 draft/2 archived documents, 3 external-eligible synthetic fragments, and 0 activation records. Every document's current version resolved to the same document.
- **NOT_RUN:** Repeated seed application, rollback-on-injected-failure, full constraint/trigger negatives, and end-to-end Oracle UI login. No password or activation code was inserted.

## 2026-11-09 Oracle UI conversion check

- **PASS:** Oracle-profile package build; `ProfileBoundaryGuardTest` (3 tests) and `ProfileWiringTest` (1 test), with zero failures/errors/skips. The Oracle JAR started on localhost HTTPS port 8443 using the Git-ignored local DB and SSL properties.
- **PASS:** Python TLS validation against the generated localhost certificate fetched `/` (HTTP 200, login form present) and `/api/auth/csrf` (token returned). The bootstrap command reached its interactive hidden-password prompt and was stopped before input; no activation was made.
- **NOT_RUN:** EMP-001 password entry, committed first-admin activation, Oracle login, and role issuance/second-user activation. These require an operator-selected password. Mock-only UI tests do not certify those Oracle flows.
- **PASS:** Generated localhost certificate imported into the Windows CurrentUser trusted-root store with thumbprint `4065C16464E89A533A47A816DC60ECBE90A4CA5E` after explicit approval.
- **BLOCKED:** Visual/browser-click Oracle flow; the browser-access host was unavailable and binding the visible Whale window failed. No browser security interstitial was bypassed.
- **PASS:** Full ordinary Maven gate after the change, `mvnw.cmd -o -Dmaven.repo.local=C:\Users\user\.m2\repository -Poracle test package`: 289 run, 0 failures, 0 errors, 3 skipped; executable JAR built. The first sandboxed `clean test package` attempt failed at dependency-JAR access before tests; the approved retry completed outside that sandbox restriction.

## 2026-11-09 first-admin landing-page correction

- **PASS:** The Oracle shell now checks only EMP-001's account eligibility/admin role and shows a one-time setup instruction while pending. The login form remains in the DOM but is hidden until setup is complete and the page is refreshed. Local-mock retains its existing login flow.
- **PASS:** Full ordinary `-Poracle test package`: 290 tests, 0 failures, 0 errors, 3 skipped; packaged JAR. Focused setup-state test covers missing, pending, and active administrator states.
- **PASS:** Live Oracle HTTP `GET /` returned 200 with the setup section, hidden login section, and bootstrap command. No password was entered or account changed. The server was stopped and port 8443 confirmed closed afterward.
- **NOT_RUN:** Post-bootstrap Oracle login and complete browser UI scenario; EMP-001 remains pending.

## 2026-11-09 property-driven first-admin setup

- **PASS:** Full ordinary `-Poracle test package` after the property-runner change: 293 tests, 0 failures, 0 errors, 3 skipped; packaged JAR. Unit checks cover no property, active account idempotence, and invalid short property.
- **PASS:** Actual Oracle-profile startup without an admin property returned HTTP 200 for `/`; the page showed `BANS_ORACLE_FIRST_ADMIN_PASSWORD=` guidance, kept the login section hidden, and did not display the former PowerShell bootstrap command. The server was stopped and port 8443 confirmed closed.
- **NOT_RUN:** A configured password activating EMP-001 in the live database, then Oracle login and the complete UI flow. The local property key is present but empty; no usable administrator password was committed or logged.
- **PASS:** A subsequent focused `-Poracle -Dtest=OracleFirstAdminBootstrapTest,FirstAdminSetupAdviceTest package` completed 5 tests with zero failures/errors/skips after adding the pending-account property branch check. The executable JAR was rebuilt.

## 2026-11-10 Oracle administrator login repair

- **FAIL, resolved:** The operator's local property contained a four-character password. The prior 12-character bootstrap check rejected it, causing Oracle startup to stop after Tomcat initialization. The UI also labeled a login 401 as session expiry.
- **PASS:** A short password is now accepted only with `BANS_ORACLE_ALLOW_SHORT_DEMO_PASSWORD=YES` in the ignored local file, loopback address, HTTPS enabled, Mock AI, and `BANS_CHAT` schema. The transaction still checks the dedicated Oracle PDB/schema, fresh seed, pending account, and role before commit. General activation password rules remain 12–128 characters.
- **PASS:** Ordinary `-Poracle test package`: 295 tests, 0 failures, 0 errors, 3 skipped. Production JS client integration verifies login 401 does not invoke session-expiry handling; `WorkbenchClientIntegrationTest` passed. The first packaging retry failed because the running Oracle JAR was locked; after stopping that process, focused UI/bootstrap tests and package passed.
- **PASS, live Oracle:** First configured startup activated EMP-001. HTTPS `/` displayed the login form, and `POST /api/auth/login` followed by `GET /api/auth/me` returned EMP-001 with `ACCOUNT_ADMIN` and a Secure session cookie. A restart with the same property succeeded and the same login still worked. The final JAR served versioned UI JavaScript and repeated the login check successfully.
- **NOT_RUN:** Visual browser click flow, second-user activation, chat/document/AI UI end-to-end. The browser automation host was previously unavailable; this pass used verified TLS HTTP requests.

## 2026-11-10 local Oracle demo account provisioning

- **PASS, live Oracle JDBC:** Dedicated `BANS_CHAT`/`FREEPDB1` schema, 18 tables, 20 synthetic employees, 20 documents, 5 roles, and 12 existing accounts passed preflight. A single transaction activated all 12 accounts with BCrypt cost 10 hashes. Each account's expected demo password matched its stored hash after the update.
- **PASS, rerun:** A second application verified the same 12 active accounts and produced zero additional provision audit records, demonstrating idempotence for the generated local SQL/hash set.
- **PASS:** The generated SQL, verifier, helper, and credential mapping are under the Git-ignored `bans_chatbot/demo-mock-data/` directory.
- **NOT_RUN:** HTTP login for the newly activated secondary accounts and full UI flows. The operator instructed that the web server remain stopped until they start it.
- **NOT_RUN:** Ordinary Maven tests for this local-only data change; no tracked application code was changed in this operation.
- **PASS, corrected mapping:** The initial ordinal-based password assignment was replaced with each account's own employee-code suffix. Direct Oracle JDBC verification checked BCrypt matches for all 12 accounts. Ten affected account hashes and session versions changed; a second run added zero provision audits.
- **PASS, final mapping:** All 12 local Oracle account passwords were changed again to the three-digit employee-code suffix only. Direct JDBC BCrypt checks matched all 12; prior prefixed passwords were rejected by the stored hashes. No web server was started.

## 2026-11-10 login code format repair

- **PASS:** Focused `AuthFlowMockMvcTest`: 5 run, 0 failures/errors/skips. New coverage confirms `EMP-001`, `EMP001`, `emp-001`, and `emp001` resolve to the same canonical login account. MockMvc did not open a listening web server.
- **PASS, live Oracle JDBC:** All 12 active account hashes still match their three-digit demo passwords; a repeat provision run created zero new audit records.
- **PASS:** Oracle-profile executable JAR was rebuilt with `-DskipTests package`; this build did not start the application. The focused test run above supplied the test evidence.
- **FAIL, resolved:** The first sandboxed Maven compile could not read a cached `reactive-streams` JAR. The approved retry outside that sandbox completed successfully.
- **NOT_RUN:** Live HTTP login with the rebuilt JAR. The operator has reserved starting the web server for themselves.

## 2026-11-10 Oracle demo path verification
PASS: ordinary Surefire cases had zero failures; Oracle-profile package succeeded after stopping the process holding the JAR. PASS: HTTPS `EMP-001` login returned 200 and Oracle-backed employee search returned 200 with 20 rows. Initial package attempt failed because the running JAR was locked; retry succeeded. `local-mock` remains solely as an offline test harness.


## Oracle-only 8080 demo (2026-11-10)

2026-11-10 PASS: Oracle build and 296 ordinary tests (0 failures, 3 skipped). PASS: live HTTP 8080 EMP-001 login with the configured local demo credential and Oracle-backed employee query returning 20 records. PASS: port 8443 not listening. Oracle integration suite beyond these smoke checks remains at its prior status.

## 2026-11-10 stored AI configuration
PASS: ignored `bans_chatbot/.env` exists and is excluded by `.gitignore`; Oracle runtime imports it. PASS: repackaged Oracle JAR starts on HTTP 8080 with `.env` in Mock mode and EMP-001 login returns 200. NOT_RUN: real Gemini call; no API key was supplied.

## Consolidated local settings (2026-11-10)

2026-11-10 PASS: Repackaged Oracle JAR started using only `bans_chatbot/.env`; EMP-001 login HTTP 200 and employee query HTTP 200 with total 20. PASS: old and new values for all five migrated Oracle keys matched before old file removal. NOT_RUN: live Gemini, because no key has been supplied.


## Chat UI redesign (2026-11-10)

2026-11-10 PASS: focused WorkbenchMockMvcTest, WorkbenchClientIntegrationTest, and ChatMockMvcTest (6 tests, 0 failures). PASS: Node JavaScript syntax check, Oracle package, live 8080 login, chat-room GET, and delivery of the new HTML/CSS/JS. BLOCKED: visual browser screenshot because the browser access host was unavailable.
