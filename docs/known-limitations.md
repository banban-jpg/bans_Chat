# 알려진 한계와 최종 수락 경계

현재 상태는 **오프라인 구현/검증 산출물**이다. 다음 항목을 통과하기 전 “Oracle DB 검증 완료” 또는 “최종 운영 준비 완료”를 선언하지 않는다.

| 구분 | 상태 / 영향 / 후속 조건 |
|---|---|
| Oracle 26ai Free 미설치 | BLOCKED: ORACLE_26AI_NOT_INSTALLED. 모든 실제 JDBC/DDL/seed/ACL JOIN/제약/락/성능·복구 검증 대기. |
| Oracle 최초 관리자 | 안전한 초기화 기능 미구현. pending EMP-001 seed를 일반 UI로 자기 활성화할 수 없음. 운영자 신원/권한/감사/일회성을 갖춘 bootstrap을 별도 구현·검증해야 Oracle 로그인 시연 가능. 기본 패스워드나 dummy hash 우회 금지. |
| 제품명·내부 버전 | 26ai는 제품명, 현재 공식 릴리스 번호는 23.26 계열. JDBC major 26 단정은 잘못되어 IT를 banner/Free 증거 검사로 수정. 실제 드라이버 보고값은 Oracle 실행까지 대기. runtime startup guard는 Oracle 제품명만 확인하며 target/edition 인증이 아님. |
| 실제 브라우저 | STEP 15 Chrome GPU/CDP Page.enable 실패. BLOCKED: CHROME_RENDERER_CDP_UNAVAILABLE. 실제 클릭·좁은 화면·키보드·paint·실제 DOM XSS 전체검증 미완료. MockMvc/Node 검사를 이를 대신한 PASS로 쓰지 않음. |
| 실제 Gemini/최신 SDK | NOT_RUN. 현재 REST adapter/Mock 검증만 PASS. 최신 Java SDK 의존성·실행 호환 검증은 기존 네트워크/캐시 제한으로 대기. 실제 계정 무료/모델 접근과 결제 상태를 추측하지 않음. |
| 외부 전송 | 정확히 검토된 합성 질문과 현재 허용 구간만. 일반 자유 대화/자동 마스킹 안전 인증/임의 도구 루프 없음. 조회-전송/반환 사이 동시 revoke를 원자적으로 막는 Oracle 보장은 아직 없음. |
| 채팅/세션 | 1:1 전용. single-server SimpleBroker, 비내구성, heartbeat 미설정. 반개방 소켓 즉시 감지/다중 서버/서버 재시작 전달 복구는 미검증. 재접속은 REST cursor 사용. 화면500건/복구10×50건 한도. |
| Mock 저장소 | 모두 프로세스 메모리이며 재시작 시 손실. object replacement 테스트는 OS/process/Oracle 복구 테스트가 아님. 데이터 손실 없는 백업을 제공하지 않음. |
| 성능 | 같은 PC loopback, 최대12명/30세션, 짧은 burst/작은 fixture. DB/인터넷/실 모델/장시간/브라우저 UI 지연 미포함. SLA/Oracle 동시 사용자 수 보장 아님. |
| 감사/로그 | 메타데이터만 저장하는 코드·sentinel 회귀는 PASS. Oracle REQUIRES_NEW 내구성/DB 오류/로그 보관·전송 체계는 대기. debug/wire/body/SQL bind logging을 운영에서 켜지 말 것. |
| UI/관리 범위 | 문서 대기함 API 없이 코드 기반 workflow. 신규 직원은 계정/역할 자동 생성 아님. AUDITOR 역할은 있으나 감사 조회 UI/API 없음. 첨부/그룹채팅/스트리밍/전체 대화 저장/유료 전환은 MVP 제외. |
| 배포/백업 | 운영 TLS/인증서/내부망·방화벽/프록시 경계/비밀 교체·백업 복원 drill 미검증. DB 설치 후 공식 절차로 별도 승인·검증 필요. |
| 시크릿 검사 | 현재 HEAD/index/worktree의 제한 패턴 검사. 전체 Git history/모든 형태의 비밀/고엔트로피 탐지·CVE/외부 DAST 인증 아님. 발견 시 안전한 회수·교체·필요 시 승인된 이력 정리 필요. |

13개 Oracle IT 중 일부는 별도 단계의 빈 스키마/seed 전제를 가지므로 모든 opt-in을 한꺼번에 켜지 않는다. 동시성 IT는 커밋 쓰기를 남긴다. [정확한 실행 순서](oracle-install-validation.md)를 지켜야 한다. 자동 설치/삭제/일괄 schema reset/무료 한도 초과 시 유료 폴백은 없다.
