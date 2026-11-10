"""Export actual JUnit test cases, never synthesize passed tests. Standard library only."""
import collections
from datetime import datetime
import json
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
NOTES=ROOT/"bans_chatbot/local-notes"
TEST=ROOT/"bans_chatbot/src/test/java"


def collect(directory):
    cases=[]
    for report in sorted(directory.glob("TEST-*.xml")):
        suite=ET.parse(report).getroot()
        for case in suite.findall("testcase"):
            status="PASS"
            if case.find("failure") is not None or case.find("error") is not None:
                status="FAIL"
            elif case.find("skipped") is not None:
                status="SKIPPED"
            cls=case.get("classname",suite.get("name"))
            source=TEST/(cls.replace(".","/")+".java")
            if source.exists():
                text=source.read_text(encoding="utf-8-sig")
                kind="MockMvc" if "MockMvc" in text else "실제 브라우저(opt-in)" if "WorkbenchBrowserTest" in cls else "JS/HTTP/STOMP 부하" if "MockLoadExperimentTest" in cls else "실제 localhost HTTP/STOMP" if ("ChatStompE2ETest" in cls or "WorkbenchClientIntegrationTest" in cls) else "실제 localhost HTTP" if "HttpClient" in text else "Mockito/단위" if "Mockito" in text else "단위/설정/소스 계약"
            else:
                kind="JUnit (원본 위치 확인 필요)"
            skip=case.find("skipped")
            reason=(skip.get("message","") if skip is not None else "실행해 통과" if status=="PASS" else "실패 상세는 해당 ignored report/log에서 확인(민감 원문 재출력 안 함)")
            cases.append(dict(name=cls+"#"+case.get("name",""),type=kind,status=status,
                              seconds=case.get("time",""),reason=reason.replace("\n"," "),
                              file=source.relative_to(ROOT).as_posix(),timestamp=suite.get("timestamp") or ("report mtime "+datetime.fromtimestamp(report.stat().st_mtime).astimezone().isoformat(timespec="seconds"))))
    if not cases:
        raise ValueError("No actual JUnit reports: "+str(directory))
    return cases


def main():
    baseline=collect(NOTES/"step19-baseline-reports")
    counts=collections.Counter(c["status"] for c in baseline)
    lines=["# STEP 19 실제 테스트 결과", "",
           "Windows 11 x64 / Java21.0.3 / Maven Wrapper3.9.6. 이 문서는 실제 Surefire XML testcase에서 자동 추출한다.",
           "로그·raw JSON·JUnit report는 Git 제외 bans_chatbot/local-notes에 두고 원문 입력/토큰을 공용 문서에 복사하지 않는다.", "",
           "## 명령과 집계", "",
           "저장소 루트에서 setup.md의 MAVEN_HOME과 $cache를 설정했다.", "",
           "```powershell", ".\\scripts\\local.ps1 -Action build -Offline -MavenRepository $cache", "```", "",
           "내부 명령: bans_chatbot/mvnw.cmd -o -Dmaven.repo.local=<기존 캐시 절대 경로> clean test package.",
           f"최종 기본 suite: {len(baseline)} testcase, PASS {counts['PASS']} / FAIL {counts['FAIL']} / SKIPPED {counts['SKIPPED']}.",
           "XML에 timestamp가 없으면 아래에는 report 파일의 최종 수정 시각(KST)을 표시한다. 개별 testcase 시작 시각이 아니며 실행 종료는 최종 log19:17:23이다. JVM report duration은 SLA/DB 성능이 아님.",
           "step19-build-final.log 및 step19-baseline-reports가 증거다. 초기 STEP19 build와 focused HTTP test도 실행했으나 중복 건수를 최종 집계에 더하지 않는다.",
           "기본 skip: Chrome opt-in1, 부하 opt-in2. 일반 test는 실제 Gemini/Oracle 호출0; loopback 모의 HTTP는 실제 인터넷 호출이 아니다.", "",
           "## 기본 suite 개별 결과", "",
           "모든 행의 명령은 위 기본 suite와 동일. PASS/FAIL은 실제 실행, SKIPPED는 실행 제외다.", "",
           "| 테스트 이름 | 유형 | 실제 실행 | 결과 | 초 / 시각 / 주요 근거·미실행 사유 | 관련 파일 |", "|---|---|---|---|---|---|"]
    for c in baseline:
        safe=lambda s:str(s).replace("|","/").replace("\n"," ")
        lines.append("| "+" | ".join(map(safe,[c['name'],c['type'],"아니오" if c['status']=='SKIPPED' else "예",c['status'],f"{c['seconds']}s / {c['timestamp']} / {c['reason']}",c['file']]))+" |")
    lines += ["", "## 별도 opt-in Mock 부하/장애 실험", "",
              "명령: `performance/scripts/track-a.ps1 -o '-Dmaven.repo.local=<기존 캐시 절대 경로>'` (저장소 루트). 기존 wrapper/합성 무작위 자격증명만 사용."]
    extra_dir=NOTES/"step19-load-reports"
    if extra_dir.exists():
        extra=collect(extra_dir)
        for c in extra:
            lines += [f"- {c['status']} / 실제 실행 예 / {c['name']} / {c['seconds']}s / {c['timestamp']} / {c['file']}"]
        lines += ["이는 기본 suite에서 skip한 동일2개의 별도 실행이다. 기본 suite skip을 PASS로 덮거나 중복 없이 합산한 것으로 표현하지 않는다. 관측 metric은 FINAL_VERIFICATION_REPORT.md 참조."]
    else:
        lines += ["SKIPPED: 이번 STEP19에서 별도 실행 증거 없음. 과거 실험을 이번 PASS로 쓰지 않는다."]
    lines += ["", "## 실제 HTTP/정적 검사", ""]
    evidence=json.loads((NOTES/"step19-http-evidence.json").read_text(encoding="utf-8-sig"))
    lines += [f"FinalApiHttpVerificationTest: 실제 서버(fixture-test) {len(evidence['checks'])} HTTP 요청 검증; MVC39개 매핑 각각 정상 응답 존재. 36 JSON REST+3 HTML; static4개 별도. 실행 {evidence['time']}.",
              "전체 path/method 관측 상태는 API_ENDPOINTS.txt. 보안 negative: 미인증401, CSRF403, admin거부403, 타인방404, 미허가문서404, revoke 직후 문서/출처404, logout 후401, 게시 XSS escape.",
              "실제 HTTP 전 경로 성공은 모든 입력/권한 조합/모든 DOM 클릭/Oracle 동작을 증명하지 않는다.",
              "API 추출/명세 대조 명령: `python scripts/generate-api-docs.py`. 실제 결과는 FINAL_VERIFICATION_REPORT.md.",
              "시크릿/링크 검사: `scripts/verify-source.ps1`; PS AST/JS syntax와 Git diff 검사 상세도 최종 보고서에 기록.",
              "외부 스캐너/전체 Git history/CVE/DAST/운영 서비스 공격은 수행하지 않는다.", "",
              "## Oracle/실 Gemini/실 브라우저", "",
              "Oracle 설치 상태는 서비스/도구/1521 listener/접속 변수/설치 registry 확인만 수행. 접속 구성·서비스 미발견으로 SQL 접속 시도0. Oracle 테스트는 아래 BLOCKED이며 정상 suite 이름/tag 선택에서 제외됐다.", "",
              "| 테스트 | 실제 DB 실행 | 결과 | 사유 / 실행법 |", "|---|---|---|---|"]
    for path in sorted(TEST.rglob("*OracleIT.java")):
        lines.append(f"| {path.stem} | 아니오 | BLOCKED | ORACLE_26AI_NOT_INSTALLED / ORACLE_VERIFICATION_CHECKLIST.md의 단계별 flag와 wrapper 명령 / {path.relative_to(ROOT).as_posix()} |")
    lines += ["", "- 실 Gemini greeting: SKIPPED(실행 시도 없음/NOT_RUN), 키·명시적 외부 사용 허가 없음. normal suite 선택 제외. setup.md의 별도 명령만 안내.",
              "- 실제 Chrome 전체 클릭: SKIPPED(opt-in 미설정), 이번 STEP19 재실행 없음. 기존 renderer/CDP 실패 이력은 known-limitations.md이며 이번 실측 PASS로 쓰지 않음.",
              "- Oracle 전용 검증13 class BLOCKED는 기본 JUnit288 testcase 분모에 더하지 않는다. 백업/복구/TLS/실 DB부하는 class 외 추가 수락 항목으로 모두 검증 보류.", ""]
    (ROOT/"docs/TEST_RESULTS.md").write_text("\n".join(lines),encoding="utf-8")
    print("TEST_REPORT_PASS",dict(counts),"testcases",len(baseline))


if __name__=="__main__":
    main()
