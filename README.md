# BANS Chat — 가상 사내 AI 업무지원 메신저

개인 학습·취업 포트폴리오용 모듈형 모놀리스다. 가상 직원 20명/5개 부서/활성화 대기 계정 12개를 사용한다. 실제 업무자료·개인정보·기밀은 사용하지 않는다.

**로컬 시연은 Oracle DB를 사용한다. 전체 기능·보안·성능 검증과 운영 준비는 계속 진행 중이다.**

| 고정 구성 | 현재 상태 |
|---|---|
| Java 21 / eGovFrame 5.0.0 실제 모듈 / Boot 3.5.6 | Maven parent와 실제 테스트로 확인 |
| MyBatis / Spring Security 서버 세션 / WebSocket+STOMP | 도메인 서비스와 현재 권한 검사 사용 |
| Thymeleaf + 가벼운 JS | 로그인/활성화·검색·1:1 채팅·문서·AI·관리자 6화면 |
| Oracle AI Database 26ai Free | 로컬 전용 합성 스키마 설치 및 로그인·직원 조회 검증 완료 |
| Gemini Developer API 무료 티어 | 기본 Mock. 실제 호출 **NOT_RUN**, 유료/다른 모델 자동 전환 없음 |

## A. 지금 가능한 경로

JDK 21과 Git이 필요하다. Node 22가 있으면 실제 배포 JS+localhost HTTP/STOMP 통합 검사도 실행한다. Node/Chrome이 없거나 사용할 수 없는 선택적 검사는 상태를 구분한다. eGov 개발 도구 설치만으로 앱 의존성이 준비된 것은 아니며 wrapper build가 실제 모듈을 해석해야 한다.

저장소 루트의 Windows PowerShell에서:

```powershell
java -version
.\bans_chatbot\mvnw.cmd -version
.\scripts\verify-source.ps1
.\scripts\local.ps1 -Action build
.\scripts\smoke-local.ps1
.\scripts\local.ps1 -Action run
```

`build`는 모듈에서 wrapper `clean test package`를 실행한다. 최초 의존성 다운로드에는 Maven 저장소 접속이 필요하다. 이 PC의 기존 오프라인 캐시를 사용하는 정확한 환경/명령은 [setup.md](docs/setup.md)에 있다. `.env.example`은 안내 파일이다. 실제 로컬 설정은 Git 제외 `bans_chatbot/.env`에 저장하며 Oracle 실행 시 자동으로 읽는다. 기본 Mock AI에는 Gemini 키가 필요 없다.

`http://127.0.0.1:8080/`에서 Oracle DB 계정 EMP-001로 로그인한다. 현재 전용 로컬 시연 DB의 비밀번호는 직원 코드 끝 세 자리다. 다른 사용자는 관리자 화면에서 일회용 활성화 코드 발급→별도 안전한 전달→활성화 화면에서 비밀번호 설정 순서로 사용한다. EMP-005는 작성자, EMP-002는 승인자다. 둘 이상의 독립 브라우저 세션으로 채팅을 시연한다. 역할·자원 권한은 서버가 다시 검사한다. [역할별 시연](docs/web-ui.md)을 따른다. 기본 비밀번호는 없다.

종료는 실행 터미널의 Ctrl+C다. **시연 데이터는 Oracle DB에 저장된다.** 개인 메모/로그는 Git 제외 `bans_chatbot/local-notes/`에만 둔다. 공용 문서는 Git으로 관리한다.

## B. Oracle 설치 후 경로 — 로컬 검증 진행 중

공식 Windows 설치 전제 확인→운영자 승인·기존 DB 백업→전용 계정/PDB 확인→비파괴 점검→빈 전용 스키마 migration→seed transaction 검증→seed commit→명시적 Oracle IT→Oracle 드라이버 포함 빌드→TLS가 있는 oracle 프로필 기동→영속 기능/ACL/STOMP/AI(Mock)/부하/복구 실측 순서다.

설치·인스턴스 삭제·SQL 적용을 자동 수행하지 않는다. **Oracle 최초 관리자 보안 초기화는 아직 구현되지 않았으므로 Oracle 사용자 로그인 시연의 차단 항목이다.** pending seed를 임의 활성 계정이나 기본 비밀번호로 바꾸지 않는다. Oracle JDBC major version을 26으로 단정하지 말고 공식 제품 banner/Free edition과 23.26 계열 릴리스 증거를 확인한다.

정확한 단계별 명령·환경 변수·필요 데이터·수락 기준·재실행 위험은 [Oracle 설치 후 체크리스트](docs/oracle-install-validation.md)를 따른다. [Oracle backlog](docs/oracle-validation-backlog.md)의 미검증 항목을 통과하기 전에는 “DB 검증 완료”로 표시하지 않는다.

## 인수인계 문서

STEP 19에서 현재 코드를 기준으로 종합 재검증했다. Controller HTTP 매핑39개(JSON REST36+HTML3), 전 매핑 정상 응답을 포함한 실제 로컬 HTTP65요청을 확인했다. 최종 기본 테스트는285 PASS/3 SKIPPED/실패0이며 별도 Mock 부하·장애2건도 PASS다. Oracle13개 IT는 BLOCKED로 유지한다.

- [전체 API 명세(메모장 UTF-8)](docs/API_ENDPOINTS.txt), [최종 종합 보고서](docs/FINAL_VERIFICATION_REPORT.md), [개별 테스트 결과](docs/TEST_RESULTS.md)
- [실행 가이드](docs/RUNBOOK.md), [Oracle 검증 체크리스트](docs/ORACLE_VERIFICATION_CHECKLIST.md)

- [실행 환경·두 경로](docs/setup.md), [현재 아키텍처](docs/architecture.md), [ERD](docs/erd.md), [API 계약](docs/api-contract.md)
- [보안 정책](docs/security-policy.md), [보안 회귀 결과](docs/security-test-results.md), [Mock 성능과 Oracle 실측 구분](docs/performance-and-failure.md)
- [테스트 상태](docs/test-status.md), [알려진 한계](docs/known-limitations.md), [최종 인수인계](docs/handoff.md), [Gemini 조건](docs/gemini-free-tier.md)

Oracle/실 Gemini/실제 Chrome 전체 클릭 검증은 별도 대기다. Mock 관측값은 Oracle 처리량/SLA가 아니다. 라이선스·지원 버전·무료 티어는 실제 외부 사용 직전에 공식 안내와 해당 계정에서 다시 확인한다.
