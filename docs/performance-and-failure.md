# STEP 17 동시성·장애·부하 실험

**Mock 환경 결과: DB 포함 안 됨.** 측정일 2026-11-04 KST. Oracle 설치/실측은 하지 않았다. 이 문서는 처리량/SLA 또는 운영 준비 완료를 보증하지 않는다.

## 환경과 재현

- Windows 11 build 26200, Java 21.0.3, Maven Wrapper 3.9.6, Boot 3.5.6/eGovFrame 5.0.0, Node 22.17.0.
- OS가 보고한 AMD Ryzen 7 7800X3D, 논리 CPU 16개, 메모리 33,444,917,248 bytes(약 31.15 GiB). 클라이언트·서버는 같은 PC의 loopback. CIM 하드웨어 조회는 권한 거부로 실패했으나 Node os API로 이 값을 수집했다. 프로세스/컨테이너의 실제 할당 상한이나 CPU/메모리 사용률을 측정한 값은 아니다.
- 진입은 dev / 88836b4와 미커밋 STEP 16 변경. 기존 내용을 보존하고 main/POM/운영 SQL/DB/모델/프로필은 변경하지 않았다.
- 계정 fixture 12개에 테스트 프로세스에서 무작위 비밀번호를 주입한다. 10연결은 서로 다른 10명, 20/30연결은 12명의 여러 세션이다. 회사 직원 수 20명/부서 5개는 유지한다. 30명의 고유 직원 실험이 아니다.
- 10→20→30 연결을 각 3회. 별도 warmup 없이 증가 순서 고정, DB 없는 앱 기동·계정 준비는 측정에서 제외하고 각 HTTP 로그인/연결의 cold cost는 포함한다. 장시간 steady state/포화점/실 네트워크 지연 실험이 아니다.

루트에서 재현할 명령(이 PC의 기존 Maven 환경/캐시 설정 필요):

```powershell
$env:MAVEN_HOME = 'C:\Users\user\.m2\wrapper\dists\apache-maven-3.9.6\0f95e7798d182e3371f3fee6d8202d3a56e6d71dfd3a2402139882ef2fbe7476'
.\performance\scripts\track-a.ps1 -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

runner는 Maven Wrapper로 `MockLoadExperimentTest,ProviderFailureExperimentTest`를 선택하고 gate를 종료 후 복원한다. Node 스크립트는 localhost URL, 명시적 테스트 gate, local-mock 응답 라벨을 요구한다. 임의 원격/Oracle 대상에 재사용하지 않는다. 실 Google/Oracle 호출은 0회다. 자동 재시도·폴백으로 실 API를 켜지 않는다. Node가 없으면 해당 실험은 SKIPPED다.

최종 성공 스크립트 실행은 **18:28:25 KST**다. 앞서 직접 명령 2회도 PASS했고, 첫 runner 실행은 PowerShell이 Mockito stderr 경고를 종료 오류로 처리하여 FAIL했다. native stderr와 exit code를 분리하도록 스크립트를 수정한 뒤 실제 runner를 재실행해 PASS했다. 최종 수치는 마지막 실행의 9개 burst만 사용하며 서로 다른 구현 단계의 관측값을 합치지 않는다.

raw JSON/log는 ignored `bans_chatbot/local-notes/step17-track-a.json`, `step17-failures.json`, `step17-script-final.log`에 있다. HTTP body·비밀번호·쿠키·세션 ID·AI 대화 원문은 report에 기록하지 않는다. 아래 표는 재시작/clean으로 사라질 수 있는 raw artifact와 별도로 유지하는 집계다.

## 트랙 A: 실제 실행한 기능 부하

로그인→직원/문서 검색→STOMP 연결→방 생성→송신/상대 수신/ack→동일 키 REST 중복 전송 두 번→메시지 1건 확인→높은/낮은 읽음 동시 쓰기→AI 정형 조회를 병렬로 수행한다. 이후 문서 생성/검토/동시 승인(200 한 건·409 한 건), grant 철회, 부서 이동, Mock 문서 AI/반출 거절/429, 연결 단절 중 메시지 저장과 재연결 REST 복구, 계정 비활성·401·복원을 반복한다. REST 중복 전송 자체는 STOMP 이벤트를 발행하지 않으며 이미 송신된 메시지의 저장 결과를 확인하는 용도다.

최대 30개 STOMP 연결, HTTP 2,763회, 복합 단계/이벤트 포함 지연 표본 3,312개. HTTP 거부 응답 45회(의도한 409/404/401/429), 전체 HTTP 오류 응답률 **1.629%**, 예상 밖 HTTP 오류 **0/2,763 = 0%**. 거부 응답을 성공 200으로 세지 않는다. 미래 무오류 보장도 아니다.

다음은 매 burst의 혼합 표본 p50/p95(ms)다. HTTP·연결·ack·복구·수신 시점을 섞은 값이므로 단일 endpoint SLA나 평균 서버 응답 시간으로 해석하지 않는다.

| 동시 연결 / 고유 직원 | 회차 | 표본 | burst 소요 ms | p50 ms | p95 ms |
|---|---:|---:|---:|---:|---:|
| 10 / 10 | 1 | 198 | 2001.446 | 16.655 | 180.957 |
| 10 / 10 | 2 | 198 | 1409.084 | 4.789 | 63.064 |
| 10 / 10 | 3 | 198 | 1401.988 | 4.417 | 66.857 |
| 20 / 12 | 1 | 368 | 1547.307 | 8.334 | 66.265 |
| 20 / 12 | 2 | 368 | 1491.659 | 8.730 | 70.169 |
| 20 / 12 | 3 | 368 | 1533.752 | 8.301 | 73.052 |
| 30 / 12 | 1 | 538 | 1687.838 | 13.329 | 104.071 |
| 30 / 12 | 2 | 538 | 1605.042 | 11.967 | 84.679 |
| 30 / 12 | 3 | 538 | 1606.084 | 10.736 | 70.371 |

percentile은 정렬 후 `ceil(n*q)-1` nearest-rank다. endpoint별 전체 9회 집계:

| 동작 | n | p50 ms | p95 ms |
|---|---:|---:|---:|
| 로그인 | 180 | 104.071 | 193.124 |
| 직원 검색 | 180 | 6.815 | 14.054 |
| 문서 검색 | 180 | 6.339 | 17.029 |
| STOMP 연결 확인 | 180 | 42.843 | 88.085 |
| 송신→애플리케이션 ack 확인 | 180 | 38.269 | 62.184 |
| 송신→상대 JS 수신 callback | 180 | 4.338 | 28.664 |
| 동일 키 중복 REST 전송 | 360 | 13.717 | 18.276 |
| 읽음 위치 | 360 | 13.402 | 31.871 |
| 승인 경쟁(200/409 포함) | 18 | 0.776 | 18.195 |
| AI 정형(Provider 호출 없음) | 180 | 6.993 | 23.702 |
| AI 문서(Mock Gemini) | 9 | 2.019 | 11.180 |
| 미검토 질문 반출 차단 | 9 | 0.830 | 2.361 |
| 연결 단절→REST gap 복구 | 9 | 1040.791 | 1045.520 |

ack/연결 확인은 20ms polling 오차와 클라이언트 scheduling을 포함한다. 수신 callback은 송신 시각과 event 처리 시각 차이다. 복구에는 정상적인 첫 backoff 1초가 포함된다. 모든 metric은 DB/브라우저 paint/인터넷 모델 추론 시간을 제외한다. 부하 발생기 CPU와 서버 CPU는 같은 PC를 경쟁한다.

## 트랙 A: 모의 장애와 UI 클라이언트 제어

실제 Gemini 어댑터를 localhost 모의 HTTP 서버로만 연결했다. 각 장애는 3회, 자동 재시도 0회/각 회 실제 HTTP 요청 1회다. 429/503 직후 다른 요청도 COOLDOWN으로 거부하고 wire 수는 늘지 않는다.

| 주입 장애 | n | p50 ms | p95 ms | 기대 실패율 |
|---|---:|---:|---:|---:|
| 429 | 3 | 4.829 | 145.716 | 100% |
| 503 | 3 | 3.487 | 3.754 | 100% |
| 잘못된 JSON → 502 | 3 | 3.628 | 13.959 | 100% |
| 읽기 timeout 150ms → 504 | 3 | 165.701 | 166.557 | 100% |

n=3의 p95는 최댓값에 해당하며 안정된 확률 추정이 아니다. 초기 HTTP 비용/JIT를 포함한다. 별도 20개 동시 Provider 시도에서는 슬롯 1개를 차지한 1회만 HTTP로 나갔고 나머지 19회는 AI_BUSY(429)로 거부됐다. 거부 p50 0.0063ms/p95 0.1103ms이며 측정에 assertion 비용도 포함한다.

UI용 API client의 429/503/504 오류 전달은 fake fetch에서 n=3, p50 0.047ms/p95 0.524ms였다. **브라우저 화면 반응/paint 측정이 아니다.** production STOMP retry를 fake timer로 실행해 1/2/4/8/15초, 최대 5회 후 중단을 확인했다. 가짜 시계이므로 30초를 실제 기다린 값이 아니다. AI 어댑터는 원래 자동 재시도 0회 정책을 유지한다. 이를 AI 지수 재시도로 확대하지 않는다. 기존 fake-clock quota/minute/Pacific-day/cooldown 회귀도 전체 test에서 실행한다.

메모리 저장소 30-worker 중복/순번/읽음 unit test를 추가했다. 새 저장소 객체 생성 시 기존 기록이 없다는 것도 검사했다. 객체 교체는 OS 서버 재시작 시험이 아니며 실제 프로세스 kill/restart는 NOT_RUN이다. Mock은 장애 후 영구 데이터 복원을 제공하지 않는다. 실제 Chrome 클릭/접속 실패 화면/모바일 paint는 기존 환경 BLOCKED를 유지한다. 실제 JS 소켓 재접속·REST 복구와 fake-timer 제어만 이번에 측정했다.

## 트랙 B: Oracle 설치 후

전체 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**. 현재 일반 test는 Oracle에 연결하지 않는다. 기존 `*OracleIT`/oracle-integration/Failsafe 격리를 재사용한다. `ChatConcurrencyOracleIT`를 추가해 20 worker가 별도 repository transaction으로 같은 10개 요청 키를 두 번 전송하고, 정확히 10개 연속 seq와 읽음 역행 방지를 검사하도록 준비했다. 실제 커밋 쓰기이며 자동 rollback/삭제가 아니다. 전용 폐기 가능한 합성 스키마에서만 명시적으로 실행한다.

Oracle 공식 [26ai Free 제한](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/licensing-restrictions.html)은 CPU를 “two cores for processing”, RAM은 “cannot exceed 2 GB”, 사용자 데이터는 “cannot exceed 12 GB”로 설명한다. 이 제한을 기준으로 heap/pool/연결 수를 계획하되 실측 없는 동시 사용자 수/처리량/SLA를 약속하지 않는다. 개발 PC의 16 logical CPU 측정값을 Free DB 자원으로 바꾸어 표현하지 않는다.

운영자가 Oracle 26ai Free를 별도 설치하고 전용 빈 스키마에서 기존 V001–V005와 synthetic seed를 명시적으로 준비한 뒤에만 사용할 **미실행 명령**:

```powershell
# 기존 ORACLE_JDBC_URL/ORACLE_DB_USER/ORACLE_DB_PASSWORD를 안전하게 설정한 뒤
$env:ORACLE_LOAD_TEST = 'YES'
$env:ORACLE_LOAD_WRITES_ACK = 'YES'
.\performance\scripts\track-b.ps1 -o '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
```

runner는 설치/seed/데이터 삭제/DB 중단을 자동 수행하지 않는다. 이미 데이터가 있는 스키마를 reset하지 않는다. AI는 Mock으로 둔다. resource 관측용 `performance/scripts/oracle-observe.sql`은 read-only이며 v$ 조회 권한이 필요하면 DBA가 별도로 수행한다. 애플리케이션/AI에 관리 권한을 주지 않는다. user_segments 할당량을 Free 전체 데이터 사용량과 같다고 해석하지 않는다.

| 실험 계획 | 준비 수준 / 상태 |
|---|---|
| migration/seed/FK/trigger/ACL/constructor mapping | 기존 Oracle IT와 명시적 opt-in 재사용. BLOCKED. |
| room_seq/동일 요청/읽음 경쟁 | 신규 20-worker Oracle IT 코드 준비·컴파일. BLOCKED. 외부 writer가 없는 전용 스키마 전제. |
| 문서 승인 2명 경쟁·grant/revoke/departure commit race | Oracle 다중 연결 추가 case/fixture 설계 TODO, NOT_RUN/BLOCKED. Mock 승인 race는 이를 대체하지 않음. |
| 검색 계획/인덱스/페이지/CLOB/부하 | 대표 합성 데이터량별 SQL_ID, DBMS_XPLAN.DISPLAY_CURSOR, 지연/pool wait 수집 계획. SQL 계획 실측 NOT_RUN/BLOCKED. |
| DB 중단/복구·commit 경계·중복 재전송 | 운영자가 전용 DB 중단을 별도 승인·수행한 뒤 새 쓰기 실패, 기존 session, 복구 후 같은 requestId 확인·seq/gap·트랜잭션 rollback을 점검. 자동 중단 스크립트는 만들지 않음. NOT_RUN/BLOCKED. |
| 리소스 점유 | CPU/SGA+PGA/pool/락 대기/디스크를 시간축으로 수집. 제공 SQL은 예비 관측이며 수치 없음. BLOCKED. |

## 병목과 다음 단계

관측: 이번 Mock 로그인 p95가 검색보다 높았다. 가설: BCrypt·JIT·같은 PC의 부하 발생기 경쟁이 원인 후보다. profiling/통제 실험이 없어 원인으로 확정하지 않으며 보안 비용을 낮추지 않았다. InMemoryChatRepository의 전역 synchronized도 코드상 잠재 병목이나 실제 Oracle row-lock 성능 근거가 아니다. 현 단계에서 production 최적화는 하지 않았다.

원격 API/실 DB/장시간/브라우저 paint의 실제 검증은 남아 있다. 최종 완성·운영 준비 완료 판정은 보류한다. STEP 18은 별도 지시 후 진행한다. 공식 버전 근거는 기존 eGovFrame 5.0 Boot 예제와 저장소 resolved parent이며 의존성을 변경하지 않았다.
