# Oracle 설치 후 검증 체크리스트

**이 문서의 Oracle 명령은 전부 미실행이다. 현재 상태: BLOCKED: ORACLE_26AI_NOT_INSTALLED.** 설치/데이터 변경에 대한 자동 허가가 아니다. 프로젝트의 최종 DB는 Oracle AI Database 26ai Free이며 다른 DB로 바꾸지 않는다.

## 0. 설치·변경 전 승인과 백업

- [공식 Windows 설치 요구사항](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/requirements.html)과 [설치 가이드](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/)를 운영자가 확인한다. 지원 Windows edition/x64, 관리자 권한, 소프트웨어·임시 디스크 공간/RAM, 기존 Oracle 서비스·PDB·포트 충돌을 확인한다. 이 단계에서 설치/서비스 제거/인스턴스 삭제는 하지 않는다.
- [Free 자원 제한](https://docs.oracle.com/en/database/oracle/oracle-database/26/xeinw/licensing-restrictions.html)은 처리 2 cores/RAM 2GB/user data 12GB다. 설치 요구 RAM과 DB 사용 상한을 혼동하지 않는다.
- 적용 대상 host/PDB/schema/작업자/변경 시간/승인자/예상 변경/복구 책임자를 기록한다. 기존 DB가 있으면 공식 [Backup and Recovery 가이드](https://docs.oracle.com/en/database/oracle/oracle-database/26/bradv/)에 따라 별도 승인된 백업과 다른 환경 복원 검증부터 수행한다. 백업 파일도 비밀·직원 데이터를 포함할 수 있어 Git·공개 로그에 넣지 않는다.
- **전용 빈 합성 스키마만** migration 대상으로 한다. 계정/테이블스페이스/최소 DDL 권한은 DBA가 기존 SQL을 검토해 구성한다. CREATE USER/GRANT/SHUTDOWN/DROP을 자동 실행하는 스크립트는 제공하지 않는다. SYS/SYSTEM을 앱 계정으로 쓰지 않는다.
- Oracle 26ai의 현재 내부 릴리스는 23.26 계열이다. [공식 Upgrade Guide](https://docs.oracle.com/en/database/oracle/oracle-database/26/upgrd/updating-compatibility-level-oracle-database.html)를 기준으로 제품 banner/Free edition/정확한 RU를 별도로 기록한다. JDBC major=26만 요구하던 예비 IT는 수정했다. 버전·edition 증거 확보가 수락 조건이다.

## 1. 연결과 환경

모듈 `bans_chatbot`에서 수행한다. `oracle-local.properties.example`을 같은 폴더의 `oracle-local.properties`로 복사하고 URL, 전용 스키마 계정, 비밀번호를 입력한다. 실제 파일은 Git에서 제외한다. 서비스/PDB는 실제 설치 결과를 기준으로 입력한다. 파일의 URL은 형식 예시이며 존재 여부를 가정하지 않는다. 값은 명령줄이나 로그에 출력하지 않는다.

```powershell
if (-not (Test-Path .\oracle-local.properties)) {
    Copy-Item .\oracle-local.properties.example .\oracle-local.properties
}
# oracle-local.properties를 편집해 실제 접속값을 입력한 후:
# 기존 MAVEN_HOME / $cacheArg 설정 후. artifact 미준비 시 -o를 빼고 허용된 저장소에서 준비.
.\oracleIntegrationTest.cmd '-Dit.test=OracleConnectionOracleIT' -o $cacheArg
```

runner는 `-Poracle,oracle-it verify`를 사용하고 Maven oracle 프로필이 ojdbc17:23.26.4.0.0을 추가한다. driver 다운로드/Mapper parse는 SQL 성공 증거가 아니다. 연결 IT는 실제 Oracle 제품/banner/Free edition을 요구한다. startup guard 자체는 Oracle 제품명만 확인하므로 그것만으로 26ai 인증을 하지 않는다. 환경 변수가 없으면 runner는 설정 누락으로 실패하며, DB 연결 불가도 Mock으로 전환하지 않는다.

## 2. 비파괴 점검과 migration — 한 가지 방법만 선택

SQLcl/SQL*Plus에서 비밀번호를 커맨드라인에 넣지 않고 접속한다. `@` 경로는 모듈 기준이며 스크립트가 EXIT하면 다시 접속해야 한다.

```text
sqlplus /nolog
CONNECT <전용스키마>@//<host>:<port>/<PDB>
-- 비밀번호는 도구 프롬프트에서 입력
SELECT SYS_CONTEXT('USERENV','DB_NAME'), SYS_CONTEXT('USERENV','CON_NAME'), USER FROM dual;
SELECT COUNT(*) FROM user_objects;
@db/oracle/verify_empty.sql
@db/oracle/apply.sql
```

대상과 빈 스키마를 확인하고 사전 승인을 받은 뒤에만 마지막 apply를 수행한다. V001→V005 순서/manifest SHA-256을 오프라인 source test와 대조한다. DDL은 자동 커밋하므로 rollback으로 되돌릴 수 없다. 실패 중간 상태에서 임의 재적용/삭제하지 말고 원인·적용 위치를 기록한 후 별도 승인된 전용 스키마 재구성 절차를 따른다. 기존 데이터 스키마에 적용하지 않는다.

**대안**으로 완전 빈 스키마에서만 다음 migration IT를 사용한다. SQL apply와 둘 다 수행하지 않는다.

```powershell
$env:ORACLE_EMPTY_SCHEMA_MIGRATION_TEST = 'YES'
.\oracleIntegrationTest.cmd '-Dit.test=OracleSchemaOracleIT' -o $cacheArg
Remove-Item Env:ORACLE_EMPTY_SCHEMA_MIGRATION_TEST
```

수락 기준: 18개 후보 테이블, 5개 역할, 3개 VALID trigger, FK/UK/CHECK/negative rollback 검사, 재적용 사전 점검 거절. 수동 apply 후 metadata/negative IT만 수행하려면 **empty migration gate를 제거한 상태로** 같은 Schema IT를 실행한다.

## 3. seed 검증과 명시적 commit

먼저 migrated schema에 직원/부서 데이터가 비어 있을 때만 rollback seed IT를 수행한다. migration gate는 제거돼 있어야 한다.

```powershell
$env:ORACLE_SYNTHETIC_SEED_TEST = 'YES'
.\oracleIntegrationTest.cmd '-Dit.test=OracleSeedOracleIT' -o $cacheArg
Remove-Item Env:ORACLE_SYNTHETIC_SEED_TEST
```

이 IT는 seed를 두 번 적용해 가상 데이터·관계를 확인하고 DML을 rollback한다. **seed가 영구 저장됐다는 뜻이 아니다.** 이후 작업 승인 아래 SQLcl/SQL*Plus에 다시 접속해 아래를 한 번 수행한다.

```text
@db/oracle/seed/verify_seed_preconditions.sql
@db/oracle/seed/apply_seed.sql
```

apply_seed는 검증 실패 시 rollback, 성공 시 commit한다. 기대 데이터는 5부서/20직원/12 pending 계정/20문서·버전·fragment와 canonical fixture grants/tags다. 이미 활성 계정이 있는 스키마에는 seed를 재적용하지 않는다. preflight는 non-pending 계정을 거부한다. seed의 MERGE를 일반 데이터 초기화/사용자 상태 reset 수단으로 쓰지 않는다.

## 4. 도메인 Oracle IT — 단계별 선택

모든 검사는 dedicated migrated+committed synthetic seed schema를 전제로 한다. 아래 각 행의 flag만 YES로 설정하고 해당 class만 `-Dit.test=`로 선택한다. 여러 전제의 gate를 일괄 YES로 켜지 않는다. 다른 domain IT 뒤 데이터가 변했다면 새로 준비한 승인된 전용 스키마를 사용한다.

```powershell
# 예: 인증 IT. 다른 행도 동일 형식이며 class/flag를 한 행씩 바꾼다.
$env:ORACLE_AUTH_TEST = 'YES'
.\oracleIntegrationTest.cmd '-Dit.test=AuthOracleIT' -o $cacheArg
Remove-Item Env:ORACLE_AUTH_TEST
```

| class | gate | 수락 기준 / 재실행 위험 |
|---|---|---|
| AuthOracleIT | ORACLE_AUTH_TEST | pending seed 조회, hash/활성화/로그인 transaction rollback. 실제 첫 관리자 생성 기능 아님. 이미 활성화된 계정은 전제 위반. |
| OrganizationOracleIT | ORACLE_ORGANIZATION_TEST | 공개 DTO/검색/상태/부서 Oracle 동작. 전용 합성 schema 외 실행 금지. |
| ChatOracleIT | ORACLE_CHAT_TEST | seq/동일 요청/cursor/읽음, outer rollback. 기존 방/고정 요청키가 남아 있으면 전제 확인. |
| ManagedDocumentOracleIT | ORACLE_DOCUMENT_TEST | 작성/검토/승인/현재 게시/ACL/CLOB/rollback. 고정 fixture·권한 전제 확인. |
| DocumentSearchOracleIT | ORACLE_DOCUMENT_SEARCH_TEST | 실제 ACL JOIN/출처/current version/grant revoke, transaction rollback. |
| AiToolOracleIT | ORACLE_AI_TOOL_TEST | Oracle service 읽기/반출 분류/metadata insert. 실 모델 호출 없음, REQUIRES_NEW 내구성 증명 아님. |
| AiQuestionOracleIT | ORACLE_AI_QUESTION_TEST | Oracle 조회+Mock Provider의 문서/정형 응답. 실제 로그인/browser E2E 아님. |
| WorkbenchOracleIT | ORACLE_UI_TEST | Oracle 모드 shell/자산/인증 경계. 실제 영속 UI 클릭은 별도 대기. |
| SecurityAttackOracleIT | ORACLE_SECURITY_TEST | SQL 모양 입력 literal/실제 revoke 거절/rollback. 다중 연결 commit race는 별도 대기. |
| ChatConcurrencyOracleIT | ORACLE_LOAD_TEST + ORACLE_LOAD_WRITES_ACK | 독립 20 worker, 10개 키 중복/seq/read. **커밋 기록이 남음**, 자동 삭제/rollback 없음. 별도 폐기 가능 스키마에서만. |

Connection/Schema/Seed 3개와 위 10개가 총 13개 Oracle IT다. tag/name 격리로 일반 test에서 제외한다. 선택했다고 모든 시나리오가 실행된 것은 아니므로 Failsafe report의 PASS/FAIL/SKIP과 실제 실행 개수를 기록한다.

## 5. Oracle 앱과 남은 수락 항목

```powershell
# 실제 DB가 준비된 후 JDBC driver를 포함해 빌드. normal test는 DB 없이 동작해야 함.
.\mvnw.cmd -Poracle clean test package
java -jar target/bans_chatbot-1.0.0.jar --spring.profiles.active=oracle --bans.ai.provider=mock --server.address=127.0.0.1
```

이 예시는 **서버 기동 명령**일 뿐 HTTPS 인증서/프록시를 구성하지 않는다. oracle의 Secure 쿠키를 유지한 채 TLS/동일 origin WS를 준비해야 브라우저 세션 기능을 수락할 수 있다. DB가 없으면 명확히 실패해야 한다.

Oracle 최초 관리자 보안 bootstrap은 미구현이다. 운영자 본인 확인·최소 권한·일회성·hash·감사·재실행 거부 설계를 별도 승인/구현/검증하기 전에는 앱 로그인 경로가 완성됐다고 하지 않는다. 테스트의 dummy hash/메모리 provisionForTest를 복사하거나 pending 계정을 일괄 ACTIVE로 바꾸지 않는다.

“DB 검증 완료”를 위한 필수 체크:

- [ ] 설치 제품/RU/Free edition, 실제 연결/PDB/계정과 driver 증거.
- [ ] migration/seed/제약·immutable trigger·rollback·재실행 거절.
- [ ] 안전한 최초 관리자·활성화·세션 만료/자격증명/상태 변경.
- [ ] 앱 재시작 이후 직원/채팅/문서/읽음 상태 영속성과 동일 키 중복 복구.
- [ ] Oracle ACL JOIN/부서 이동/grant 철회/current version의 HTTP·출처·AI 재검사.
- [ ] 두 연결 승인/revoke/departure 경쟁, 락 순서·timeout·FK 오류/rollback.
- [ ] STOMP는 실제 DB commit 후 발송, commit 실패 시 미발송, 재접속 cursor 복구와 중복 수신 처리.
- [ ] AI Oracle 검색+Mock, 반출 금지 구간 요청0회, 감사의 원문/키 부재 및 독립 commit 내구성.
- [ ] [트랙 B](performance-and-failure.md)의 실 DB 검색/계획/부하/pool/CPU·메모리·디스크 관측.
- [ ] 운영자 별도 승인 하에 DB 중단/재개, 실패 중 동일 요청 재전송, 손실·중복·seq 확인.
- [ ] 백업을 분리 환경에 복원해 row/권한/문서 버전·대화/세션 무효화를 확인하고 복구시간/손실범위를 실제 기록.
- [ ] TLS/내부망·방화벽·프록시 신뢰/비밀 교체·로그 보관/운영 계정 분리 검증.

DB 중단/복원 명령은 현재 환경을 모르는 상태에서 제공·실행하지 않는다. 정확한 RMAN/Data Pump/운영자 권한·저장 경로·복원 대상·RPO/RTO는 실제 설치/백업 정책 결정 후 공식 가이드로 작성하고 승인받는다. 실측하지 않은 RPO/RTO나 SLA를 약속하지 않는다. 모든 체크는 지금 미완료이며 Oracle backlog의 BLOCKED를 유지한다.
