# STEP 12 — Gemini Developer API 준비와 외부 호출 경계

검토일: **2026-11-04 KST**. 기본값은 키가 필요 없는 Mock이다. 이 문서는 모델의 영구적인 무료 제공이나 특정 계정의 이용 가능성을 보증하지 않는다. 실제 Gemini 호출은 이번 STEP에서 **NOT_RUN, 0회**다.

## 공식 정보와 모델 선택

| 항목 | 공식 확인 내용 / 이 프로젝트의 결정 |
|---|---|
| 서비스 | Google Gemini **Developer API**만 대상으로 한다. Vertex AI, 다른 공급자, 유료 모델 자동 전환은 구현하지 않는다. |
| 무료 티어 | [공식 가격표](https://aistudio.google.com/docs/pricing#gemini-2.5-flash-lite)는 `gemini-2.5-flash-lite`의 Standard 입력·출력 무료 항목과 서비스 개선을 위한 데이터 이용을 표시한다. 유료 프로젝트에서도 같은 모델 ID를 사용할 수 있으므로 ID만으로 무료 여부를 판단할 수 없다. |
| 최초 후보 `gemini-2.5-flash` | [공식 폐기 안내](https://aistudio.google.com/docs/deprecations#gemini-2.5-flash-models)에 폐기 및 2026-12-01 이후 다른 모델로 자동 라우팅되는 일정이 있다. 이번 허용 목록에서 제외한다. |
| 검토 후보 `gemini-2.5-flash-lite` | 같은 공식 안내에는 폐기되지 않았다고 명시돼 있다. 단, 2.5 모델 접근은 과거 이용 이력이 있는 사용자로 제한된다는 안내도 있으므로 **신규 계정의 접근 가능 여부는 미검증**이다. 앱은 이 정확한 ID만 허용하고 기본 모델 값은 비워 둔다. 다른 ID를 추측하거나 자동 선택하지 않는다. |
| 한국 | [지원 지역](https://aistudio.google.com/docs/available-regions)에 South Korea가 포함된다. 계정·연령·서비스 약관 요건은 별도다. 실제 프로젝트 접근 성공 여부는 NOT_RUN이다. |
| 한도 확인 | [공식 한도 설명](https://aistudio.google.com/docs/rate-limits)과 [AI Studio의 프로젝트별 활성 한도 화면](https://aistudio.google.com/rate-limit?timeRange=last-28-days)에서 선택한 프로젝트·모델의 RPM/TPM/RPD와 Free 티어를 확인한다. 프로젝트 단위 한도이며 RPD는 미국 태평양 자정에 초기화된다. 로그인한 프로젝트를 확인하지 않았으므로 공급자 할당량 숫자를 확정하지 않는다. |

실제 사용 전 운영자가 가격·폐기 공지·모델 접근·무료 프로젝트 여부·남은 한도를 다시 확인해야 한다. 결제 연결 프로젝트로 전환하거나 과금 한도를 올리는 절차는 제공하지 않는다. `free-tier-confirmed`는 운영자의 확인 표시이며 Google 결제 상태를 자동 검증하는 기능이 아니다.

## SDK 우선 평가와 실제 구현 선택

[Google 라이브러리 안내](https://aistudio.google.com/docs/libraries)는 공식 Google GenAI SDK를 권장한다. [공식 Java 저장소](https://github.com/googleapis/java-genai)의 검토 당시 예시는 `com.google.genai:google-genai:1.80.0`이다. [해당 태그의 POM](https://github.com/googleapis/java-genai/blob/v1.80.0/pom.xml)은 Java 8 바이트코드 대상으로 선언한다. 이것만으로 본 프로젝트의 Java 21 런타임·전이 의존성 호환성을 PASS로 판단하지 않는다.

실제 Maven Wrapper로 `1.80.0` 의존성을 평가했다. 오프라인 조회는 캐시 부재로 FAIL, 온라인 조회도 저장소 접속의 `Permission denied: getsockopt`로 FAIL했다. 최신 SDK를 이용한 Java 21 컴파일·실행은 **NOT_RUN / dependency resolution BLOCKED**다. 캐시의 이전 `0.2.0` POM과 `javap` API도 확인했지만 오래된 버전을 채택하지 않았다. SDK 버전이나 프레임워크 버전을 임의로 변경하지 않았다.

이번 구현은 추가 의존성 없이 **Java 21 `java.net.http.HttpClient` + 기존 Boot 관리 Jackson**으로 공식 [generateContent REST 계약](https://aistudio.google.com/api-reference/generate-content/generate-content)을 구현한다. 실제 Java 21 컴파일·실행 및 모의 네트워크 검증 대상은 이 REST 어댑터다. SDK가 다시 평가 가능해지면 동일한 실패·타임아웃·헤더·본문 제한 계약을 보존한 상태에서 별도 교체할 수 있다. SDK 연동 성공으로 보고하지 않는다.

## 입력과 전송 고지

**실제 AI를 활성화하면 질문 원문도 Google 서버로 전송될 수 있다. 실제 업무자료·개인정보·기밀은 입력하거나 전송하지 않는다.** 무료 서비스의 데이터 이용 조건을 확인해야 한다.

STEP 12의 `ChatProvider`는 `SessionActor`와 서버가 정의한 `SyntheticPrompt.HELLO_KO` / `HELLO_EN`만 받는다. 임의 문자열, 대화 이력, 문서 본문, 첨부, 사용자 지정 헤더, 도구/SQL을 받는 인자가 없다. 활성 EMPLOYEE 검사 뒤 고정 합성 인사말 하나만 전송한다. 정규식 마스킹이나 클라이언트의 `synthetic=true` 주장을 안전 판정으로 사용하지 않는다.

STEP 03 `AiDisclosurePolicy`와 `QuestionSafetyReview`, STEP 11의 검색/출처 재검증은 보존했다. **문서 연결과 자유 질문은 의도적으로 미구현**이다. 이후 연결할 때는 최신 서버 세션 사용자, 승인된 현재 버전, RBAC+ACL, 정확한 구간, 신뢰할 수 있는 합성·비민감 분류와 `external_ai_allowed`, 질문 안전성 검토를 전송 직전에 다시 확인해야 한다. 검색 가능 여부만으로 외부 전송을 허용해서는 안 된다.

## 설정과 실패 동작

| 설정 | 기본값 / 제한 |
|---|---|
| `bans.ai.provider` | `mock`; 키 유무만으로 실 Provider가 켜지지 않는다. |
| `bans.ai.model` | 빈 값. 실 Provider에서 정확히 `gemini-2.5-flash-lite`를 명시해야 한다. |
| `bans.ai.external-transfer-acknowledged` | `false`; 합성·비민감 입력 및 질문의 외부 전송 고지를 확인한 후만 true. |
| `bans.ai.free-tier-confirmed` | `false`; 프로젝트 무료 티어·모델·한도 확인 후만 true. |
| `GOOGLE_API_KEY` | 실 모드에서만 읽는다. 환경 변수 또는 Git 제외 로컬 설정에 주입하며 명령 인자·공유 파일에 키를 붙이지 않는다. `.env.example`은 자동 로드되지 않는다. |
| `bans.ai.max-output-tokens` | 256; 1–512. 단일 후보·텍스트 응답만 요청한다. |
| `bans.ai.max-concurrent` | 1; 1–2. 대기열 없이 즉시 거절한다. |
| `bans.ai.requests-per-minute` | 2; 1–10. 최근 60초 슬라이딩 윈도. |
| `bans.ai.requests-per-day` | 20; 1–100. 미국 태평양 날짜 기준. 앱 자체 상한이며 Google 무료 할당량이 아니다. |
| `bans.ai.connect-timeout` | 3초; 100ms–10초. |
| `bans.ai.read-timeout` | 10초; 100ms–30초. 응답 헤더 뒤 본문이 멈추는 경우까지 전체 완료 시간을 제한한다. |

실 Provider Bean은 추가 Spring 프로필 `gemini-live`와 위 설정·키가 모두 필요하다. `test`, `local-mock`, `oracle-it`와 실 Provider의 조합은 시작에 실패한다. `oracle`도 기본값은 Mock이며 실제 DB가 없을 때의 기존 fail-fast와 무관하다. 현재 실 어댑터는 내부 Java 계약이며 이를 호출하는 운영 HTTP/화면 엔드포인트는 없다.

서버 endpoint는 `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`로 고정한다. 키는 `x-goog-api-key` 헤더에만 넣고 URL·본문에 넣지 않는다. 리다이렉트·시스템 프록시·쿠키 저장·임의 헤더 전달은 사용하지 않는다. HTTP 클라이언트/TLS wire debug가 지정된 실 Provider는 시작을 거절한다. 모의 서버 주입 경로는 같은 패키지의 테스트용이며 127.0.0.1 HTTP만 추가 허용한다.

자동 재시도는 **0회(호출당 HTTP 시도 최대 1회)**다. 403은 고정 오류, 429/5xx는 고정 오류와 공유 cooldown으로 처리한다. `Retry-After`의 초/HTTP 날짜를 읽되 값은 1–86400초로 제한한다. 부재·오류 시 429는 60초, 5xx는 1초다. 다른 모델/서비스/결제 티어로 우회하지 않는다. 실패한 시도도 앱 할당량을 소비한다. 전송 실패·중단·타임아웃·잘못된 JSON·중복 JSON 키·빈 답·차단/잘린 후보·도구 응답은 안전한 오류가 된다. 응답은 32KiB, 반환 텍스트는 8192 UTF-16 단위까지 허용한다.

대화·키·upstream 오류 본문을 DB/파일/로그에 저장하지 않는다. 내부 결과의 `toString`은 본문을 가리며 Jackson 직렬화에서 텍스트를 제외한다. 상위 오류 원인/본문/헤더는 공개 예외에 붙이지 않는다. 호출 예산은 **단일 프로세스 메모리 상태**라 재시작하면 초기화된다. 여러 인스턴스·같은 Google 프로젝트를 사용하는 다른 앱·TPM을 통합 제어하지 못한다. Google의 실제 한도와 과금 상태를 대신하지 않는다.

## 검증과 선택적 실제 스모크

일반 `mvnw.cmd test`는 Oracle과 `GeminiLiveSmokeIT`를 이름·태그로 제외한다. 모의 HTTP 테스트의 모든 실제 요청은 127.0.0.1에만 간다. Oracle 연결 0회·실 Gemini 호출 0회가 기본 계약이다. 이번 테스트 결과와 중간 FAIL은 `test-status.md`에 기록한다.

아래는 **미실행 절차**다. 사용자의 별도 실제 호출 허가를 받은 뒤 운영자가 무료 프로젝트/모델/한도를 재확인하고 키를 안전하게 주입해야 한다. 현재 허가를 받거나 호출했다고 해석해서는 안 된다.

```powershell
# module directory; GOOGLE_API_KEY is already securely provisioned, never printed.
$env:BANS_GEMINI_SMOKE = 'YES'
$env:BANS_GEMINI_FREE_TIER_CONFIRMED = 'YES'
$env:BANS_GEMINI_TRANSFER_ACKNOWLEDGED = 'YES'
.\mvnw.cmd -Pgemini-smoke test
```

이 Maven 프로필과 환경 게이트를 모두 만족해야 합성 인사말 1회를 보내며 본문을 출력하지 않는다. 키/확인/허가가 없으면 실행을 건너뛴다. Spring/Oracle을 기동하지 않는다. 실제 스모크는 이번 STEP에서 **NOT_RUN**이다. 403/429/타임아웃이 나면 종료하며 재시도하지 않는다.

## STEP 13 재확인과 Mock 도구

2026-11-04에 위 Google 공식 가격·폐기·지원 지역·한도 안내를 다시 확인했다. 모델 ID/무료 티어 확인 정책은 변경하지 않았다. 실제 프로젝트의 quota나 접근 성공은 확인하지 않았으며 키를 사용하지 않았다. 최신 SDK 의존성/런타임 검증과 실 스모크는 STEP 12의 대기 상태를 유지한다.

새 `/api/ai/tools`·`/api/ai/tools/preview`·`/ai/tools`는 인증된 **Mock 도구 시연**이다. 서비스와 UI는 질문 원문도 외부에 전달될 수 있다고 고지하고, 정확한 서버 검토 합성 예제만 받는다. 자유 질문·문서 context를 실 Gemini로 보내는 기능은 여전히 없다. `ChatProvider.previewTools`의 실 구현은 거부하며 서버도 Mock 클래스만 허용한다. 기존 인사말 전용 live 경로를 도구용으로 넓히지 않았다. read/ACL/외부 플래그·감사·한도는 ai-tool-security.md에 정리했다.

## STEP 14의 별도 근거 답변 계약

2026-11-04 공식 가격·폐기·한국 지원·한도 및 [구조화 출력](https://aistudio.google.com/docs/structured-output) 안내를 다시 확인했다. 프레임워크/모델/SDK 버전, 기본 Mock, 명시적 gemini-live 설정과 실제 무료 티어 수동 확인 조건은 유지한다. 실제 프로젝트 quota/모델 접근·SDK 런타임·실 Gemini 호출은 여전히 NOT_RUN이다.

앞선 STEP 12의 인사말 전용/운영 HTTP 없음 설명은 당시 범위다. STEP 14는 인증된 `/api/ai/questions`와 별도 `grounded` 계약을 추가한다. 검토된 합성 질문과 현재 ACL·게시 버전·external_ai_allowed·synthetic·non-sensitive 확인을 거친 단일 200자 이하 구간만 전송할 수 있다. 질문 자체가 전송될 수 있음을 고지하고, 임의 질문은 분류 모델에도 전달하지 않는다. 기본 Mock이며 test/local-mock/oracle-it에서는 live를 사용할 수 없다. previewTools의 live 거부는 유지한다.

실 요청도 고정 endpoint/allowlisted 모델과 같은 한도·0회 재시도·키 보호를 재사용한다. 출력은 min(설정값,256) 토큰, JSON MIME, 남은 요청 예산 이하 HTTP timeout이다. 서버는 정확한 S1/원문 인용 외 모델 주장을 버리고 반환 전에 권한·버전·구간을 재조회한다. 구조화 출력 지원만으로 정확성·반출 적격성을 보장하지 않는다. 임의 업무 원문 반출·유료/다른 모델 자동 전환은 없다. 상세 내용은 [질문 조정 가이드](ai-question-orchestration.md), 실 검증 상태는 test-status.md를 따른다.

## STEP 15 화면 연결

2026-11-04 Google 공식 가격·지원 지역·폐기 일정·프로젝트 한도를 다시 확인했다. UI 작업에서 모델/SDK/무료 티어 정책은 변경하지 않았다. 실 계정의 모델 접근/무료 quota 성공 및 실 API는 NOT_RUN이다. 새 통합 화면도 검토된 가상 질문 선택·기본 Mock이며 질문 원문이 무료 Gemini로 전송될 수 있음과 실제 개인정보·기밀 입력 금지를 명시한다. 브라우저 자산에는 키나 Google API endpoint가 없다. 외부 호출은 기존 백엔드 gate를 충족해야만 가능하며, UI 표시/체크박스는 임의 질문을 외부 적격으로 만드는 근거가 아니다. Chrome 클릭 검증의 환경 제약과 실제 오프라인 검증은 web-ui.md/test-status.md를 따른다.
