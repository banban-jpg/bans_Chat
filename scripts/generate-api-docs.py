"""STEP 19: source mappings + observed runtime mappings -> UTF-8 BOM Notepad specification.

Standard library only. Run FinalApiHttpVerificationTest first. Unsupported/changed mappings
or missing reviewed metadata fail generation rather than inventing a contract.
"""
import collections
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "bans_chatbot/src/main/java"
EVIDENCE = ROOT / "bans_chatbot/local-notes/step19-http-evidence.json"

# Reviewed against controller, DTO, service and SecurityConfiguration. No credentials in examples.
# handler: purpose | access | query | body/validation | response data structure | extra statuses
CATALOG = {
"HealthController.health": ("프로세스 health", "공개", "없음", "없음", '예외적으로 envelope 없음: {"status":"UP"}', "200"),
"HealthController.apiHealth": ("공통 health", "공개", "없음", "없음", '{status:string="UP"}', "200"),
"WorkbenchController.index": ("6개 핵심 화면 shell", "공개", "없음", "없음", "text/html; templates/workbench.html, CSP/no-store", "200"),
"AuthController.csrf": ("세션 CSRF 토큰 발급", "공개; 세션 생성 가능", "없음", "없음", "CsrfView{headerName:string,token:string(값 기록 금지)}", "200"),
"AuthController.me": ("현재 사용자", "활성 세션", "없음", "없음", "MeView{employeeCode:string,departmentCode:string,roles:SecurityRole[]}", "200/401"),
"AuthController.login": ("서버 세션 로그인", "공개+CSRF", "없음", "필수 employeeCode:string @NotBlank, password:string @NotBlank. DTO 최대 길이 제한 없음. 서비스: pending/비활성 거부, 실패5회 15분 잠금. 비밀번호 예시값 생략", "MeView{employeeCode,departmentCode,roles}; session ID 변경; 쿠키 값 기록 금지", "200/400/401/403/429"),
"AuthController.activate": ("일회용 코드로 계정 활성화", "공개+CSRF; pending+재직 ACTIVE", "없음", "필수 employeeCode:string @NotBlank, code:string @NotBlank, newPassword:string @NotBlank @Size(12..128). 코드 유효24h/1회/재발급 무효. 민감 값 예시 생략", "ActivationView{activated:boolean}", "200/400/403/409"),
"AuthController.logout": ("현재 세션 로그아웃", "활성 세션", "없음", "없음", "LogoutView{loggedOut:boolean}; 세션 무효화", "200/401/403"),
"ActivationAdminController.issue": ("대상 pending 계정 활성화 코드 발급", "ACCOUNT_ADMIN; 현재 계정 재검사; 자기 발급 금지", "없음", "필수 employeeCode:string @NotBlank @Pattern(EMP-[0-9]{3,10}); 예: {\"employeeCode\":\"EMP-005\"}", "Issued{employeeCode:string,code:string(값 기록 금지)}; Cache-Control:no-store; 안전한 별도 전달 필요", "200/400/401/403/409"),
"OrganizationController.employees": ("공개 직원/담당업무 검색", "활성 세션", "name='', departmentId='', task='', page=0(0..10000), size=20(1..100), sort='employeeId'(employeeId|name|departmentId). name/task 각 <=100자, 제어문자 금지; 부서는 빈 값 또는 D[0-9]{2}", "없음", "Page{items:EmployeeView[],total:long,page:int,size:int}; EmployeeView{employeeId,displayName,departmentCode,jobTitle,companyEmail,extensionNo}:모두string", "200/400/401"),
"OrganizationController.departments": ("부서 목록", "활성 세션", "없음", "없음", "Department[]{departmentCode:string,departmentName:string}", "200/401"),
"OrganizationController.owners": ("업무 담당자 검색", "활성 세션", "필수 query:string <=100자/제어문자 금지; page0/size100 고정. 빈 문자열은 서비스에서 별도 거절하지 않음", "없음", "Page{items:EmployeeView[],total:long,page:int,size:int}; EmployeeView는 직원 검색과 동일", "200/400/401"),
"SyntheticEmployeeController.employees": ("초기 가상 직원 데모 목록", "활성 세션; fixture-test 전용(oracle에는 매핑 없음)", "없음", "없음", "Employee[]{employeeId,displayName,departmentCode,jobTitle,extensionNo,companyEmail,dutySummary}:모두string; 초기 fixture DTO, 관리 변경과 동기화하는 API 아님", "200/401"),
"OrganizationController.create": ("가상 직원 등록(계정/역할 생성 제외)", "ACCOUNT_ADMIN", "없음", "필수 employeeId:EMP-[0-9]{3,10}, displayName:1..100, departmentId:D[0-9]{2}, jobTitle:1..100, companyEmail:@Email/<=200/도메인bans.example, extensionNo:[0-9-]{1,10}, task:1..300. 모두string/@NotBlank, 미지 필드 거부. 예: employeeId=EMP-099, companyEmail=verify99@bans.example", "boolean=true", "201/400/401/403/404/409"),
"OrganizationController.status": ("직원 재직 상태 부분 변경", "ACCOUNT_ADMIN", "없음", "필수 status:string ACTIVE|SUSPENDED|DEPARTED @NotBlank; 미지 필드 거부. 예: {\"status\":\"SUSPENDED\"}; 기존 employee 대상", "boolean=true", "200/400/401/403/404"),
"OrganizationController.department": ("직원 부서 부분 변경", "ACCOUNT_ADMIN", "없음", "필수 departmentId:string D[0-9]{2} @NotBlank; 미지 필드 거부. 예: {\"departmentId\":\"D02\"}", "boolean=true", "200/400/401/403/404"),
"ChatController.open": ("1:1 방 생성 또는 기존 방 반환", "활성 세션; 상대 활성 계정; 자기 대화 금지", "없음", "필수 recipientEmployeeId:string @NotBlank/EMP-[0-9]{3,10}; 예: {\"recipientEmployeeId\":\"EMP-002\"}", "DirectConversation{conversationId:string,firstEmployeeId:string,secondEmployeeId:string}", "200/400/401/404"),
"ChatController.rooms": ("현재 직원의 방 목록", "활성 세션; 본인 참여 방만", "없음", "없음", "Room[]{roomId:long,otherEmployeeId:string,lastRoomSeq:long,lastReadSeq:long,unreadCount:long}", "200/401"),
"ChatController.messages": ("방 메시지 cursor 조회", "활성 세션+방 참여 ACL; 타인 방404", "beforeSeq:long=0(>=0), size:int=20(1..100); 0은 최신 페이지", "없음", "Messages{items:Message[],nextBeforeSeq:long}; Message{roomSeq:long,senderEmployeeId:string,content:string}", "200/400/401/404"),
"ChatController.send": ("REST 메시지 저장/중복키 확인", "활성 세션+방 참여 ACL", "없음", "필수 clientRequestId:string @NotBlank/1..80/[A-Za-z0-9._:-]+, content:string @NotBlank/<=2000 code points/NUL금지. 예: {\"clientRequestId\":\"example-1\",\"content\":\"합성 인사말\"}; 키 동일 payload는 기존 결과/다른 payload409. REST 자체 STOMP broadcast 없음", "Message{roomSeq:long,senderEmployeeId:string,content:string}", "200/400/401/404/409"),
"ChatController.read": ("방 읽음 위치 단조 갱신", "활성 세션+방 참여 ACL", "없음", "lastReadSeq:long @Min(0); 저장된 마지막 seq 이하. 예: {\"lastReadSeq\":1}. primitive 미전달 시0; null 엄격 거부 계약은 없음", "long (최종 읽음 seq); 기존보다 낮은 값으로 역행하지 않음", "200/400/401/404"),
"ManagedDocumentController.create": ("문서와 첫 draft 작성", "DOCUMENT_EDITOR", "없음", "필수 string documentCode:DOC-[A-Za-z0-9-]{1,32}, title:<=200/@NotBlank, category:VACATION|EXPENSE|DEPLOYMENT|EQUIPMENT|CONTRACT|ONBOARDING|SECURITY, scope:INTERNAL|DEPARTMENT|SELECTED, format:TEXT|MARKDOWN, body:1..20000/@NotBlank. departmentId는 DEPARTMENT일 때 본인 부서, 그 외null/빈 값. 선택 aiSearchable:boolean=false, externalAiAllowed:boolean=false(현재true 거부), tags:string[] <=8/각[문자숫자_-]{1,32}. 예: documentCode=DOC-EXAMPLE, scope=SELECTED, format=TEXT, body=합성 안내", "string documentCode", "200/400/401/403/409"),
"ManagedDocumentController.version": ("다음 버전 draft 추가", "DOCUMENT_EDITOR+소유자; ARCHIVED/기존pending 거부", "없음", "필수 format:string TEXT|MARKDOWN, body:string @NotBlank/<=20000. 선택 aiSearchable:boolean=false, externalAiAllowed:boolean=false(true거부). 예: {\"format\":\"TEXT\",\"body\":\"합성 수정\"}", "int (새 버전 번호)", "200/400/401/403/404/409"),
"ManagedDocumentController.draft": ("현재 draft 본문 교체", "DOCUMENT_EDITOR+소유자; 현재pending=DRAFT", "없음", "Version:필수 format(TEXT|MARKDOWN), body(@NotBlank/<=20000); 선택 aiSearchable=false,externalAiAllowed=false(true거부). 수정 대상 최신draft, 게시본 수정 아님", "boolean=true", "200/400/401/403/404/409"),
"ManagedDocumentController.review": ("지정 담당자에게 검토 요청", "DOCUMENT_EDITOR+소유자; 자기검토 금지; 대상 활성 DOCUMENT_MANAGER+명시적 READ/APPROVE grant", "없음", "필수 reviewerEmployeeId:string @NotBlank. 예: {\"reviewerEmployeeId\":\"EMP-002\"}; DRAFT→SUBMITTED/IN_REVIEW", "boolean=true", "200/400/401/403/404/409"),
"ManagedDocumentController.approve": ("검토 버전 게시 승인", "현재 지정된 DOCUMENT_MANAGER+READ/APPROVE grant; 소유자 자기승인 금지", "없음", "없음; SUBMITTED→PUBLISHED, current version 교체", "boolean=true", "200/401/403/404/409"),
"ManagedDocumentController.reject": ("검토 버전 반려", "현재 지정된 DOCUMENT_MANAGER+READ/APPROVE grant; 자기승인 금지", "없음", "없음; SUBMITTED→DISCARDED, 기존 current 보존", "boolean=true", "200/401/403/404/409"),
"ManagedDocumentController.withdraw": ("현재 게시 문서 폐기", "DOCUMENT_EDITOR+소유자; 게시본 존재/pending 없음", "없음", "없음; document.status=ARCHIVED 논리 폐기, 물리 삭제 아님", "boolean=true", "200/401/403/404/409"),
"ManagedDocumentController.grant": ("직원별 문서 권한 부여", "DOCUMENT_EDITOR+소유자", "없음", "필수 employeeId:string @NotBlank/EMP-[0-9]{3,10}/계정 존재, permission:string @NotBlank/READ|APPROVE. 예: {\"employeeId\":\"EMP-002\",\"permission\":\"READ\"}", "boolean=true", "200/400/401/403/404"),
"ManagedDocumentController.revoke": ("직원별 명시적 문서 grant 철회", "DOCUMENT_EDITOR+소유자", "없음", "필수 employeeId:string @NotBlank/계정 존재, permission:string READ|APPROVE. DELETE에도 JSON body+CSRF 필요. Oracle revoke는 revoked_at UPDATE, Mock은 grant set에서 제거. 문서 물리 삭제 아님. 부서/INTERNAL 범위 자체 접근을 막는 deny는 아님", "boolean=true", "200/400/401/403/404"),
"ManagedDocumentController.published": ("현재 게시본 조회", "활성 세션+소유/READ grant/INTERNAL/본인부서 DEPARTMENT; 미게시/폐기/불허404", "없음", "없음", "Published{documentCode:string,title:string,versionNo:int,format:string,renderedHtml:string(서버 escape),aiSearchable:boolean}", "200/401/404"),
"ManagedDocumentController.search": ("현재 권한 문서 검색", "활성 세션+현재게시/aiSearchable+READ ACL", "필수 q:string 1..100 code points; page=0(0..100), size=20(1..50)", "없음", "SearchPage{items:SearchHit[],page:int,size:int,hasMore:boolean}; SearchHit{documentId:long,versionId:long,fragmentId:long,title:string,excerpt:string(<=240 code points),updatedAt:string}", "200/400/401"),
"ManagedDocumentController.source": ("현재 문서 출처 fragment 조회", "활성 세션+현재버전/게시/aiSearchable+READ ACL; 내부 조회와 외부 허용 판정 별개", "없음", "없음", "SourceFragment{documentId:long,versionId:long,fragmentId:long,title:string,content:string,updatedAt:string}", "200/400/401/404"),
"AiQuestionController.examples": ("검토된 합성 질문 목록/고지", "활성 세션", "없음", "없음", "Examples{notice:string,questions:string[]}; no-store", "200/401"),
"AiQuestionController.page": ("보조 AI 질문 HTML 화면", "활성 세션", "없음", "없음", "text/html; inline JS를 포함한 보조 화면; 루트 shell과 CSP 범위 다름", "200/401"),
"AiQuestionController.ask": ("단일 턴 정형/문서 근거 질문", "활성 세션; 정확히 검토된 합성 질문만; 서버 현재 RBAC/ACL 재검사", "없음", "필수 question:string 1..200 code points/제어문자 금지; 전체 JSON <=2048 bytes/중복키·trailing JSON·미지 필드 거부. callerId,userId,employeeId,role,roles,departmentId는 무시. 예: {\"question\":\"출장비 담당자\"}. 미검토 정상형 질문은200 UNANSWERABLE; 최대8stage/4tool/1provider/10초", "Answer{answer:string,answerType:STRUCTURED|DOCUMENT|UNANSWERABLE,structuredFacts:EmployeeView[],sources:Source[],requestId:string}; Source{documentId:long,versionId:long,fragmentId:long,title:string,url:string(서버작성)}; no-store", "200/400/401/403/429/502/503/504"),
"AiToolController.catalog": ("고정 읽기 도구 JSON Schema 목록", "활성 세션", "없음", "없음", "Catalog{notice:string,mockOnly:boolean,examples:Example[]{id,text},tools:Definition[]{name,inputSchema:JSON},limits:Limits{maxInputBytes,maxCalls,maxRowsPerTool,maxRowsTotal,requestsPerMinute,maxRunSeconds}:int}", "200/401"),
"AiToolController.page": ("보조 AI 도구 HTML 화면", "활성 세션", "없음", "없음", "text/html; 보조 inline JS 화면", "200/401"),
"AiToolController.preview": ("고정 읽기 도구 Mock 반출 미리보기", "활성 세션; MockGeminiProvider 전용; 현재 ACL+external/synthetic/nonSensitive 모두 확인", "없음", "필수 questionId:string1..40,question:string1..500(서버 예제 정확일치),externalTransferAcknowledged:boolean=true,calls:1..4개{name,arguments}. 전체8192bytes 이하/중복키·trailing JSON 거부. tools는 searchEmployees{name1..80,limit1..5},findTaskOwner/query,searchDocuments/query,findDepartment{},getDocumentExcerpt{documentId,versionId,fragmentId:양수long}. limit선택 기본5. identity 필드는 무시. 도구별<=5행/총<=20행/세션당분5회/10초. 예: questionId=VACATION_HELP,question=가상 회사의 휴가 신청 안내를 찾아주세요.,calls=[{name:searchDocuments,arguments:{query:휴가,limit:3}}]", "Preview{mock:boolean,toolCalls:int,returnedItems:int,notice:string}; payload 원문 미반환", "200/400/401/403/429/503"),
}

EXAMPLE_BODIES = {
    "AuthController.login": {"employeeCode":"EMP-001","password":"<값 생략>"},
    "AuthController.activate": {"employeeCode":"EMP-005","code":"<값 생략>","newPassword":"<값 생략>"},
    "ActivationAdminController.issue": {"employeeCode":"EMP-005"},
    "OrganizationController.create": {"employeeId":"EMP-099","displayName":"가상검증99","departmentId":"D01","jobTitle":"가상 담당","companyEmail":"verify99@bans.example","extensionNo":"1999","task":"합성 검증"},
    "OrganizationController.status": {"status":"SUSPENDED"},
    "OrganizationController.department": {"departmentId":"D02"},
    "ChatController.open": {"recipientEmployeeId":"EMP-002"},
    "ChatController.send": {"clientRequestId":"example-1","content":"합성 인사말"},
    "ChatController.read": {"lastReadSeq":1},
    "ManagedDocumentController.create": {"documentCode":"DOC-EXAMPLE","title":"합성 안내","category":"ONBOARDING","scope":"SELECTED","format":"TEXT","body":"합성 안내","aiSearchable":True,"externalAiAllowed":False},
    "ManagedDocumentController.version": {"format":"TEXT","body":"합성 수정","aiSearchable":True,"externalAiAllowed":False},
    "ManagedDocumentController.draft": {"format":"TEXT","body":"합성 수정","aiSearchable":True,"externalAiAllowed":False},
    "ManagedDocumentController.review": {"reviewerEmployeeId":"EMP-002"},
    "ManagedDocumentController.grant": {"employeeId":"EMP-002","permission":"READ"},
    "ManagedDocumentController.revoke": {"employeeId":"EMP-002","permission":"READ"},
    "AiQuestionController.ask": {"question":"출장비 담당자"},
    "AiToolController.preview": {"questionId":"VACATION_HELP","question":"가상 회사의 휴가 신청 안내를 찾아주세요.","externalTransferAcknowledged":True,"calls":[{"name":"searchDocuments","arguments":{"query":"휴가","limit":3}}]},
}


def example(row):
    path=row['path']
    for variable in re.findall(r"\{([^}]+)\}",path):
        value="EMP-099" if path.startswith('/api/admin/employees') else "DOC-EXAMPLE" if variable=='code' else "1"
        path=path.replace('{'+variable+'}',value)
    path += {"OrganizationController.employees":"?page=0&size=20&sort=employeeId",
             "OrganizationController.owners":"?query=%EC%A0%95%EC%82%B0",
             "ManagedDocumentController.search":"?q=%ED%9C%B4%EA%B0%80&page=0&size=20",
             "ChatController.messages":"?beforeSeq=0&size=20"}.get(row['handler'],'')
    body=EXAMPLE_BODIES.get(row['handler'])
    return row['method']+' '+path+(" ; JSON "+json.dumps(body,ensure_ascii=False) if body else " ; body 없음")


def extract():
    rows=[]
    for path in sorted(MAIN.rglob("*.java")):
        text=path.read_text(encoding="utf-8-sig")
        if not re.search(r"@(RestController|Controller)\b",text):
            continue
        cls=re.search(r"\bclass\s+(\w+)",text)
        package=re.search(r"package\s+([\w.]+);",text).group(1)
        prefix_match=re.search(r'@RequestMapping\(\s*"([^"]*)"\s*\)',text[:cls.start()])
        prefix=prefix_match.group(1) if prefix_match else ""
        for match in re.finditer(r"@(Get|Post|Put|Patch|Delete)Mapping\b(?:\(([^)]*)\))?",text):
            annotation=match.group(2) or ""
            # Mapping arguments currently contain one literal path and optional media type constants.
            paths=re.findall(r'"([^"]*)"',annotation)
            if len(paths)>1:
                raise ValueError("Review multi-path mapping: "+str(path))
            suffix=paths[0] if paths else ""
            method=re.search(r"\bpublic\s+[^\n{]+?\s+(\w+)\(",text[match.end():])
            if not method:
                raise ValueError("Method not found: "+str(path))
            handler=cls.group(1)+"."+method.group(1)
            if handler not in CATALOG:
                raise ValueError("Missing reviewed contract: "+handler)
            rows.append(dict(method=match.group(1).upper(),path=(prefix+suffix) or "/",handler=handler,
                             controller=package+"."+cls.group(1),file=path.relative_to(ROOT).as_posix(),
                             line=text[:match.start()].count("\n")+1,domain=package.split(".")[2],
                             html=handler in {"WorkbenchController.index","AiQuestionController.page","AiToolController.page"}))
    return rows


STOMP = """
WebSocket/STOMP (MVC/REST 개수에 포함하지 않음)
WS-01 연결 endpoint: /ws/chat (native WebSocket, SockJS 없음).
WS-02 SEND /app/chat.send: {roomId:long,clientRequestId:string,content:string}.
WS-03 SEND /app/chat.read: {roomId:long,lastReadSeq:long}.
WS-04 SUBSCRIBE /user/queue/chat.messages: MessageEvent{roomId,roomSeq,senderEmployeeId,clientRequestId,content}.
WS-05 SUBSCRIBE /user/queue/chat.ack: Ack{roomId,roomSeq,clientRequestId,lastReadSeq}; send ack의 lastReadSeq=-1, read ack의 roomSeq=-1/clientRequestId=null.
WS-06 SUBSCRIBE /user/queue/chat.errors: ErrorEvent{code:string}.
구체적 연결/발행/구독 경로 6개=연결1+SEND2+SUBSCRIBE3. /app,/user,/queue prefix 자체는 추가 API 아님.
Application prefix=/app, user prefix=/user, SimpleBroker=/queue; 서버 convertAndSendToUser(employeeCode,"/queue/chat.*",payload).
HTTP Spring Security 세션+same-origin handshake. CONNECT native header에 현재 X-CSRF-TOKEN 값 필요(실제 값 문서/로그 금지).
principal은 세션 AuthenticatedEmployee에서 생성. client userId/role/login/passcode로 인증하지 않음.
ChatStompSecurity.current는 모든 inbound frame의 현재 계정/자격증명 버전/세션을 재검사. outbound도 현재 수신자 세션/상태 재검사.
SUBSCRIBE는 위 3개 user queue만 허용; /topic 또는 타인 user destination 직접 구독 거부. SEND는 위2개만 허용.
ChatService.room/send/read가 1:1 참여 권한 확인. 잘못된 방404, 입력400, 중복키 충돌409에 대응하는 업무 code 전달.
저장: ChatStompController.send→ChatService→Repository.send 반환→상대 MessageEvent+발신자 Ack.
Oracle Repository @Transactional 반환 후 전송하는 구조지만 실제 DB commit/실패 시 미발송 보장은 Oracle IT까지 BLOCKED.
REST POST messages는 저장 결과만 반환하며 STOMP broadcast 없음. REST fallback 때 상대 화면의 즉시 갱신은 보장되지 않음.
read는 단조 position 저장 후 본인의 ack만 전송; 상대에게 읽음 통지 이벤트는 구현 없음.
ApiException은 고정 업무 code, 일반 변환 예외는 CHAT_REQUEST_INVALID. interceptor 거부는 STOMP ERROR/연결 종료가 될 수 있음.
DISCONNECT에서 liveSessions 제거. heartbeat 0,0/서버 heartbeat 미설정; 반개방 소켓 즉시 탐지 보장 없음.
클라이언트 연결 watchdog8초, 재연결 최대5회(1/2/4/8/15초), REST cursor로 누락 복구(최대10x50개), 수신 seq 중복 제거.
화면500개/직전 요청 동일 clientRequestId로 REST 확인. read는 화면활성/visible일 때만; 다중서버/영속broker 미구현.
구현: bans_chatbot/src/main/java/egovframework/example/chat/websocket/{ChatWebSocketConfiguration,ChatStompSecurity,ChatStompController}.java
프론트: bans_chatbot/src/main/resources/static/ui/stomp.js, app.js.
실행 검증: 현재 Surefire ChatStompE2ETest 및 WorkbenchClientIntegrationTest 결과는 TEST_RESULTS.md 참조. Oracle 증거 아님.
"""


def main():
    rows=extract()
    evidence=json.loads(EVIDENCE.read_text(encoding="utf-8-sig"))
    static={(r["method"],r["path"],r["controller"],r["handler"].split(".")[-1]) for r in rows}
    runtime={(r["method"],r["path"],r["controller"],r["handler"]) for r in evidence["inventory"]}
    if static!=runtime:
        raise ValueError("Source/runtime mapping mismatch: "+repr(static^runtime))
    selected={r["method"]+" "+r["path"] for r in rows}
    if not selected.issubset(evidence["covered"]) or any(c["actual"]!=c["expected"] for c in evidence["checks"]):
        raise ValueError("HTTP evidence incomplete/failed")
    methods=("GET","POST","PUT","PATCH","DELETE")
    all_counts=collections.Counter(r["method"] for r in rows)
    rest_counts=collections.Counter(r["method"] for r in rows if not r["html"])
    lines=["BANS Chat — STEP 19 실제 구현 API 명세", "UTF-8 BOM / Windows 메모장용", "",
           f"전체 Controller HTTP 매핑: {len(rows)}개 (동일 path의 method별 별도 계산)",
           "MVC method별: "+", ".join(f"{m} {all_counts[m]}" for m in methods),
           f"그중 JSON REST: {sum(rest_counts.values())}개 / HTML 화면: {sum(r['html'] for r in rows)}개",
           "REST method별: "+", ".join(f"{m} {rest_counts[m]}" for m in methods),
           "정적 파일4개/WS handshake1개/STOMP5개는 Controller REST 집계에서 제외.",
           "계산: scripts/generate-api-docs.py가 실제 annotation을 추출하고 Spring runtime mapping과 정확히 대조.",
           "현재 설정 context-path/API prefix 없음(루트 context). Oracle 기본 http://127.0.0.1:8080 + 아래 path.",
           "Reverse proxy/context-path 변경은 이 검증 범위 밖. GET의 자동 HEAD/OPTIONS, Boot /error, 정적 resource handler는 선언 API 개수에 포함하지 않음.",
           "fixture-test 테스트 전용 demo1개 포함, oracle에는 그 매핑 없음.",
           "인증: SecurityConfiguration + CurrentActorFilter + SessionBoundaryInterceptor + 서비스 RBAC/ACL.",
           "성공 JSON 기본 {\"data\":<아래 DTO>} / 오류 {\"code\":string,\"message\":string,\"requestId\":string}.",
           "모든 요청 X-Request-Id 서버생성. 401 세션없음/만료, 403 CSRF 또는 역할거부, 400 입력오류, 404 불허 자원 은닉.",
           "정상 코드는 별도201 표시 외200. 주요 상태는 코드/공통 handler의 가능한 상태이며 모든 분기 HTTP 검증을 뜻하지 않음.",
           "Gemini Provider 오류: upstream403→503,429→429,5xx→503,timeout→504,잘못된JSON/빈답변→502. Retry-After는 서버 cooldown만 반영하고 HTTP 응답 헤더로 원문 전달하지 않음; 자동 재시도0/모델 자동전환 없음.",
           "unsafe method 모두 CSRF 필요(공개 login/activate 포함). GET /api/auth/csrf로 얻은 headerName/token을 메모리에서 사용.",
           "쿠키 HttpOnly/SameSite=Lax; Oracle 루프백 HTTP Secure=false. form/basic/JWT 인증 없음, cross-origin 허용 설정 없음.",
           "Content-Type: body가 있는 API는 application/json. body 없는 unsafe 호출도 UI는 application/json 사용. 질문/preview는 consumes로 JSON을 강제.",
           "비밀번호/활성화코드/세션ID/API키/CSRF값을 문서·로그에 넣지 않음. 예시의 EMP/DOC는 합성 식별자.",
           "SQL은 Oracle 전용/자동 초기화 없음. 이 명세의 HTTP PASS는 메모리+Mock 실험이며 DB 동작 증거 아님.",
           f"HTTP 실행 증거 시각: {evidence['time']}; FinalApiHttpVerificationTest; raw 응답/민감 body 기록 없음.",
           ""]
    number=0
    for method in methods:
        lines += ["="*72,method+" API", "="*72]
        group=sorted((r for r in rows if r["method"]==method),key=lambda r:r["path"])
        if not group: lines += ["구현된 API 없음"]
        for row in group:
            number+=1
            purpose,access,query,body,result,statuses=CATALOG[row["handler"]]
            variables=re.findall(r"\{([^}]+)\}",row["path"])
            observed=sorted({c['actual'] for c in evidence['checks'] if c['method']==method and c['path']==row['path']})
            lines += ["",f"API-{number:03d} [{row['domain']}] {purpose}"+(" [HTML 화면]" if row['html'] else " [JSON REST]"),
                      "HTTP Method: "+method,"전체 URL Path: "+row['path'],
                      "Path Variable: "+(", ".join(variables) if variables else "없음"),
                      "Path 검증: "+("id:string EMP-[0-9]{3,10}" if row['path'].startswith('/api/admin/employees/') else "code:string (문서 코드 조회)" if 'code' in variables else "각 long, 양수 자원ID(불허/비존재404)" if variables else "해당 없음"),
                      "Query Parameter: "+query,"인증/권한: "+access,
                      "Request Body / 필수·선택 / 입력 검증 / 처리: "+body,
                      "Content-Type: "+("해당 없음(GET)" if method=="GET" else "application/json (body 없는 명령에는 payload 불필요)"),
                      "CSRF: "+("불필요(GET)" if method=="GET" else "필수; 세션 token header 사용, 값 생략"),
                      "요청 예시: "+example(row)+" (구조 예시, 순서/실제 존재 ID·권한 필요; Cookie/CSRF/민감 값은 생략, 예시 요청 자체의 실행 증거 아님)",
                      "정상 응답 DTO(data) / HTML: "+result,
                      "오류 응답: ApiError{code:string,message:string,requestId:string}; 민감 입력/내부 예외 원문 미반환",
                      "주요 HTTP 상태: "+statuses+("; 공통 인증/CSRF/예상외500 별도" if method!="GET" else "; 공통 예상외500 별도"),
                      f"실제 HTTP 검증: PASS fixture-test; 관측 status={observed}; 성공 응답 존재. Oracle BLOCKED.",
                      f"구현 Controller/메서드: {row['file']}:{row['line']} -> {row['handler']}"]
    lines += ["",STOMP,"", "프론트/백엔드 연결", "root shell: workbench.html + static/ui/{app.js,api.js,stomp.js,app.css}; 별도 frontend 빌드서버 없음.",
              "app.js의 동적 문서 workflow 모든 action, admin status/department, room/messages/read-state는 위 선언 경로와 대조했다.",
              "보조 /ai/tools와 /ai/questions inline fetch도 catalog/examples/csrf/preview/questions 경로 사용.",
              "상세 계약·보안·불일치/한계는 FINAL_VERIFICATION_REPORT.md. 현재 경로 전체 HTTP 성공과 UI 전체 클릭 성공을 혼동하지 않음."]
    (ROOT/"docs/API_ENDPOINTS.txt").write_text("\n".join(lines)+"\n",encoding="utf-8-sig")
    (ROOT/"bans_chatbot/local-notes/step19-endpoints.json").write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding="utf-8")
    print(f"API_DOC_PASS mvc={len(rows)} rest={sum(rest_counts.values())} html={sum(r['html'] for r in rows)} counts={dict(all_counts)}")


if __name__=="__main__":
    main()
