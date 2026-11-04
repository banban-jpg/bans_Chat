package egovframework.example.ai.controller;

import egovframework.example.ai.service.ReviewedSyntheticQuestions;
import egovframework.example.ai.tool.AiToolService;
import egovframework.example.ai.tool.ToolRegistry;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiToolController {
    public record Limits(int maxInputBytes, int maxCalls, int maxRowsPerTool, int maxRowsTotal, int requestsPerMinute, int maxRunSeconds) { }
    public record Catalog(String notice, boolean mockOnly, List<ReviewedSyntheticQuestions.Example> examples,
                          List<ToolRegistry.Definition> tools, Limits limits) { }
    private final CurrentSessionService sessions;
    private final ToolRegistry registry;
    private final ReviewedSyntheticQuestions questions;
    private final AiToolService tools;
    public AiToolController(CurrentSessionService sessions, ToolRegistry registry, ReviewedSyntheticQuestions questions, AiToolService tools) {
        this.sessions = sessions; this.registry = registry; this.questions = questions; this.tools = tools;
    }
    @GetMapping("/api/ai/tools")
    public ApiResponse<Catalog> catalog(HttpServletRequest request) {
        sessions.requireCurrent(request);
        return ApiResponse.of(new Catalog(ReviewedSyntheticQuestions.NOTICE, true, questions.examples(), registry.definitions(),
                new Limits(ToolRegistry.MAX_INPUT_BYTES, ToolRegistry.MAX_CALLS, ToolRegistry.MAX_ITEMS,
                        ToolRegistry.MAX_TOTAL_ITEMS, ToolRegistry.MAX_REQUESTS_PER_MINUTE, 10)));
    }
    @PostMapping(value = "/api/ai/tools/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AiToolService.Preview> preview(HttpServletRequest request) { return ApiResponse.of(tools.preview(request)); }

    @GetMapping(value = "/ai/tools", produces = MediaType.TEXT_HTML_VALUE)
    public String page(HttpServletRequest request) {
        sessions.requireCurrent(request);
        return """
                <!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>가상 AI 읽기 도구</title>
                <style>body{font:16px system-ui;max-width:760px;margin:48px auto;padding:0 20px;color:#172033;line-height:1.7}section{padding:24px;background:#f2f5fa;border-radius:12px}button{padding:12px 20px;margin-top:20px}pre{white-space:pre-wrap}</style>
                <h1>가상 AI 읽기 도구</h1><section>
                <p id="notice">실제 AI를 활성화하면 질문 원문도 외부로 전송될 수 있습니다. 실제 업무자료·개인정보·기밀을 입력하지 마세요.</p>
                <p>서버가 검토한 합성 예제로 문서 검색 권한을 확인합니다. 현재 처리는 Mock 전용입니다.</p>
                <p id="example"></p><label><input id="consent" type="checkbox"> 외부 전송 가능성 및 합성·비민감 예제 제한을 확인했습니다.</label><br>
                <button id="run" type="button" disabled>가상 휴가 안내 확인</button><pre id="result" role="status"></pre></section>
                <script>
                const result=document.getElementById('result'),button=document.getElementById('run');let example;
                fetch('/api/ai/tools',{credentials:'same-origin'}).then(r=>{if(!r.ok)throw Error();return r.json()}).then(body=>{
                  document.getElementById('notice').textContent=body.data.notice;
                  example=body.data.examples.find(e=>e.id==='VACATION_HELP');document.getElementById('example').textContent=example.text;button.disabled=false;
                }).catch(()=>{result.textContent='로그인 상태를 확인해 주세요.'});
                button.onclick=async()=>{
                  if(!document.getElementById('consent').checked){result.textContent='고지 내용을 먼저 확인해 주세요.';return;}
                  button.disabled=true;
                  try{const tokenResponse=await fetch('/api/auth/csrf',{credentials:'same-origin'});if(!tokenResponse.ok)throw Error();
                    const csrf=(await tokenResponse.json()).data;
                    const response=await fetch('/api/ai/tools/preview',{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},
                      body:JSON.stringify({questionId:example.id,question:example.text,externalTransferAcknowledged:true,calls:[{name:'searchDocuments',arguments:{query:'휴가',limit:3}}]})});
                    if(!response.ok){result.textContent='요청을 처리할 수 없습니다. 권한과 호출 제한을 확인해 주세요.';return;}
                    const data=(await response.json()).data;result.textContent='Mock 처리 완료 · 허용된 항목 '+data.returnedItems+'개';
                  }catch{result.textContent='요청을 처리할 수 없습니다.';}finally{button.disabled=false;}
                };
                </script></html>
                """;
    }
}
