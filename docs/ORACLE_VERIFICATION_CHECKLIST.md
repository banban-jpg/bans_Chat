# Oracle 26ai Free 검증 체크리스트 — STEP 19

현재 모든 DB 실행 검증은 **BLOCKED: ORACLE_26AI_NOT_INSTALLED**다. 다른 DB로 대체하지 않는다. 체크박스는 실제 결과를 기록한 후에만 변경한다.

2026-11-04 19:09 KST 로컬 확인: `Get-Service Oracle*` 결과 없음, PATH의 sqlplus/sql/tnsping 없음, Listen 상태 1521 없음, 설치 registry의 Oracle Database/Free/Express 항목 없음. ORACLE_JDBC_URL/ORACLE_DB_USER/ORACLE_DB_PASSWORD도 설정되지 않았다. 비표준 포트·원격 DB·모든 디스크를 전수 탐지한 것은 아니며, 사용 가능한 설치/접속 증거가 없어 JDBC 연결 시도는 **0회**다. 증거는 ignored `local-notes/step19-oracle-environment.json`이다.

정확한 SQL 경로·승인·빈 스키마/seed 단계 순서는 [기존 상세 매뉴얼](oracle-install-validation.md)을 따른다. 이 문서의 명령은 **전부 미실행 계획**이며 설치/데이터 변경을 승인하거나 자동 수행하지 않는다.

## 실행 전 준비

1. [공식 Windows 설치 요구사항](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/requirements.html)과 [설치 매뉴얼](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/)을 확인한다. 설치/삭제/서비스 중단은 운영자가 별도로 승인·수행한다.
2. 실제 host/PDB/service/schema/버전/RU/Free edition/변경 승인자/백업·복구 책임자를 기록한다. SYS/SYSTEM을 앱 계정으로 쓰지 않는다.
3. 기존 DB는 [공식 백업·복구 매뉴얼](https://docs.oracle.com/en/database/oracle/oracle-database/26/bradv/)에 따라 승인된 백업과 별도 환경 복원 검증부터 한다. SQL 적용 전 대상 USER/DB_NAME/CON_NAME와 빈 스키마를 확인한다.
4. JDK21/기존 Maven 캐시 또는 허용된 의존성 저장소, SQLcl/SQL*Plus, 전용 합성 schema를 준비한다. 원격/실제 업무 DB에 시험하지 않는다.
5. 환경 값은 아래처럼 입력한다. `.env.example`은 변수 이름만 있으며 자동 로드되지 않는다. 비밀을 인수나 로그에 출력하지 않는다.

```powershell
# bans_chatbot 디렉터리, setup.md의 MAVEN_HOME 설정 후
$cacheArg = '-Dmaven.repo.local=C:\eGovFrameDev-5.0.2-Windows-64bit\workspace-egov\bans_Chat\.maven-repo'
$env:ORACLE_JDBC_URL = Read-Host '실제 Oracle Thin JDBC URL'
$env:ORACLE_DB_USER = Read-Host '전용 합성 스키마 계정'
$secret = Read-Host 'DB 비밀번호' -AsSecureString
$env:ORACLE_DB_PASSWORD = [Net.NetworkCredential]::new('', $secret).Password
.\oracleIntegrationTest.cmd '-Dit.test=OracleConnectionOracleIT' -o $cacheArg
```

`oracleIntegrationTest.cmd`는 Maven `-Poracle,oracle-it verify`와 Failsafe의 `oracle-integration` tag를 사용한다. JDBC 의존성은 oracle Maven profile의 ojdbc17:23.26.4.0.0이다. 다운로드/mapper parse를 SQL 성공으로 취급하지 않는다. Oracle26ai는 제품명이며 내부23.26 계열과 Free banner를 실제 서버에서 별도 확인한다.

## 단계별 수락 기록

| 완료 | 검증 항목 / 필요한 데이터·실행 조건 | 실행 명령 또는 class / gate | 현재 결과 | 실제 시각·보고서·관측값 |
|---|---|---|---|---|
| [ ] | 서비스/설치 도구/리스너/제품·PDB·전용 계정 확인 | 서비스·listener·SQLcl 확인, 위 Connection IT | BLOCKED | 설치 탐지에서 미발견; SQL 연결0 |
| [ ] | DDL V001~V005/18테이블/5역할/3trigger/FK·UK·CHECK | OracleSchemaOracleIT / 빈 스키마 최초 적용 때만 ORACLE_EMPTY_SCHEMA_MIGRATION_TEST=YES | BLOCKED | 미실행 |
| [ ] | seed5부서/20직원/12pending계정/20문서 및 버전·fragment/grant/tag, 두 번 실행·rollback | OracleSeedOracleIT / ORACLE_SYNTHETIC_SEED_TEST=YES; migrated schema의 직원/부서 비어있어야 함 | BLOCKED | 미실행 |
| [ ] | 영구 가상 seed 적용 | 승인 후 `@db/oracle/seed/apply_seed.sql` | BLOCKED | 미실행; 성공 시 COMMIT |
| [ ] | 실제 계정·잠금·활성화·rollback | AuthOracleIT / ORACLE_AUTH_TEST=YES | BLOCKED | 미실행; first-admin bootstrap 대체 아님 |
| [ ] | 직원/상태/부서 SQL·공개 DTO | OrganizationOracleIT / ORACLE_ORGANIZATION_TEST=YES | BLOCKED | 미실행 |
| [ ] | 채팅 저장/조회/room_seq/키 중복/읽음/rollback | ChatOracleIT / ORACLE_CHAT_TEST=YES | BLOCKED | 미실행 |
| [ ] | 문서 작성/승인/현재게시/ACL/CLOB/rollback | ManagedDocumentOracleIT / ORACLE_DOCUMENT_TEST=YES | BLOCKED | 미실행 |
| [ ] | MyBatis ACL JOIN/게시버전/검색/출처/revoke | DocumentSearchOracleIT / ORACLE_DOCUMENT_SEARCH_TEST=YES | BLOCKED | 미실행 |
| [ ] | AI 도구 실제 SQL 조회+합성·반출 검증 | AiToolOracleIT / ORACLE_AI_TOOL_TEST=YES | BLOCKED | 미실행; 원격 모델 호출 없음 |
| [ ] | Oracle 근거와 Mock 답변 조합 | AiQuestionOracleIT / ORACLE_AI_QUESTION_TEST=YES | BLOCKED | 미실행; 실제 Gemini E2E 아님 |
| [ ] | Oracle shell/인증 경계 | WorkbenchOracleIT / ORACLE_UI_TEST=YES | BLOCKED | 미실행; 실제 로그인 클릭은 별도 |
| [ ] | SQL injection literal/문서 철회·접근 거부 | SecurityAttackOracleIT / ORACLE_SECURITY_TEST=YES | BLOCKED | 미실행; 입력 검증 PASS와 구별 |
| [ ] | 20worker/중복키/seq/읽음 다중 transaction 경쟁 | ChatConcurrencyOracleIT / ORACLE_LOAD_TEST=YES + ORACLE_LOAD_WRITES_ACK=YES | BLOCKED | 미실행; COMMIT 쓰기 남음 |
| [ ] | 첫 관리자 안전한 일회성 bootstrap·감사·재실행 차단 | **미구현**, 별도 설계/구현/검증 필요 | BLOCKED | pending EMP-001 자기발급 불가 |
| [ ] | FK·락·승인/revoke/부서이동 경쟁·timeout·rollback | 다중 연결 Oracle 시나리오 추가/운영자 실행 | BLOCKED | 기존 단위/Mock으로 증명하지 않음 |
| [ ] | 실제 commit 후 STOMP 발송/실패 시 미발송/재접속 복구 | Oracle 서버+두 세션+fault 시나리오 | BLOCKED | 실제 HTTP Mock 검증과 별개 |
| [ ] | 앱 재시작 후 인증/직원/메시지/문서/읽음 영속성 | TLS oracle 앱+UI/HTTP 재시작 시나리오 | BLOCKED | 미실행 |
| [ ] | 실제 검색/실행계획/인덱스/pool/CPU·RAM·disk | performance/scripts/track-b.ps1 및 oracle-observe.sql | BLOCKED | SQL관측/부하 모두 미실행 |
| [ ] | DB 중단/재개·백업/분리복원·RPO/RTO | 설치 후 DBA의 승인된 RMAN/Data Pump 절차 확정 | BLOCKED | 정확한 명령/복구 대상은 환경 확정 후 작성 |
| [ ] | TLS/Secure cookie/내부망/프록시 신뢰/로그·비밀 정책 | 별도 승인 환경 구성과 브라우저 E2E | BLOCKED | HTTP 예시만으로 수락하지 않음 |

도메인 IT는 migrated+committed canonical seed를 전제로 하며 각 gate 하나씩 선택한다. 예:

```powershell
$env:ORACLE_CHAT_TEST='YES'
.\oracleIntegrationTest.cmd '-Dit.test=ChatOracleIT' -o $cacheArg
Remove-Item Env:ORACLE_CHAT_TEST
```

빈 스키마 migration IT와 SQL apply를 둘 다 적용하지 않는다. DDL은 자동 커밋되므로 단순 rollback 복구가 불가능하다. seed rollback 검증 후에만 별도 승인 아래 영구 seed를 적용한다. 활성화된 계정이 있으면 seed를 재적용하지 않는다. 동시성 실험은 쓰기를 커밋하므로 폐기 가능한 별도 스키마와 두 gate가 필수다. 자세한 실행법/재실행 거절은 기존 상세 매뉴얼에 있다.

현재 Oracle 프로필은 DB가 없으면 fail-fast하고 Mock으로 바뀌지 않는다. 기본 JAR에는 JDBC driver가 없으므로 설치 후 `mvnw.cmd -Poracle clean test package`가 필요하다. Oracle 쿠키는 Secure=true이므로 로그인 수락에는 TLS가 필요하다. first-admin 초기화 미구현을 dummy hash나 일괄 ACTIVE SQL로 우회하지 않는다.

모든 항목의 실제 증거를 채워야 **Oracle 포함 DB 검증 완료**로 판정한다. Mock 수치/단위 테스트/SQL 소스 검사만으로 이 표를 PASS로 변경하지 않는다.
