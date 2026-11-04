package egovframework.example.ai.controller;

import egovframework.example.ai.question.*;
import egovframework.example.auth.service.CurrentSessionService;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.*;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class AiQuestionController {
    public record Examples(String notice, List<String> questions) { }
    private final AiQuestionService service;
    private final CurrentSessionService sessions;
    private final QuestionPolicy policy;
    public AiQuestionController(AiQuestionService service, CurrentSessionService sessions, QuestionPolicy policy) {
        this.service = service; this.sessions = sessions; this.policy = policy;
    }
    private void noStore(HttpServletResponse response) { response.setHeader("Cache-Control", "no-store"); response.setHeader("Pragma", "no-cache"); }
    @PostMapping(value="/api/ai/questions", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<QuestionModels.Answer> ask(HttpServletRequest request, HttpServletResponse response) {
        noStore(response); return ApiResponse.of(service.answer(request));
    }
    @GetMapping("/api/ai/questions/examples")
    public ApiResponse<Examples> examples(HttpServletRequest request, HttpServletResponse response) {
        sessions.requireCurrent(request); noStore(response); return ApiResponse.of(new Examples(QuestionPolicy.NOTICE, policy.examples()));
    }
    @GetMapping(value="/ai/questions", produces=MediaType.TEXT_HTML_VALUE)
    public String page(HttpServletRequest request, HttpServletResponse response) {
        sessions.requireCurrent(request); noStore(response);
        return """
                <!doctype html><html lang="ko"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>가상 AI 근거 답변</title>
                <style>body{font:16px system-ui;max-width:800px;margin:40px auto;padding:0 20px;color:#18243a;line-height:1.7}section{background:#f2f5fa;padding:24px;border-radius:12px}select,button{padding:10px;margin:12px 0}pre{white-space:pre-wrap}li{margin:8px 0}</style>
                <h1>가상 AI 근거 답변</h1><section>
                <p id="notice">질문 원문도 실제 AI 사용 시 Google로 전송될 수 있습니다. 실제 업무자료·개인정보·기밀을 입력하지 마세요.</p>
                <label>검토된 가상 질문 <select id="question" disabled></select></label><br>
                <label><input id="consent" type="checkbox"> 고지와 가상 예제 제한을 확인했습니다.</label><br>
                <button id="send" disabled>질문하기</button><pre id="answer" role="status"></pre><ul id="facts"></ul><ul id="sources"></ul>
                </section><script>
                const choice=document.getElementById('question'),button=document.getElementById('send'),answer=document.getElementById('answer');
                const facts=document.getElementById('facts'),sources=document.getElementById('sources');
                fetch('/api/ai/questions/examples',{credentials:'same-origin'}).then(r=>{if(!r.ok)throw Error();return r.json()}).then(body=>{
                  document.getElementById('notice').textContent=body.data.notice;
                  for(const text of body.data.questions){const option=document.createElement('option');option.textContent=text;option.value=text;choice.appendChild(option);}
                  button.disabled=false;choice.disabled=false;
                }).catch(()=>{answer.textContent='로그인 상태를 확인해 주세요.';});
                button.onclick=async()=>{
                  if(!document.getElementById('consent').checked){answer.textContent='고지 내용을 먼저 확인해 주세요.';return;}
                  button.disabled=true;answer.textContent='조회 중입니다.';facts.replaceChildren();sources.replaceChildren();
                  try{const tokenResponse=await fetch('/api/auth/csrf',{credentials:'same-origin'});if(!tokenResponse.ok)throw Error();
                    const csrf=(await tokenResponse.json()).data;
                    const response=await fetch('/api/ai/questions',{method:'POST',credentials:'same-origin',cache:'no-store',
                      headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},body:JSON.stringify({question:choice.value})});
                    if(!response.ok){answer.textContent='요청을 처리할 수 없습니다. 권한·호출 제한을 확인해 주세요.';return;}
                    const data=(await response.json()).data;answer.textContent=data.answer;
                    for(const fact of data.structuredFacts){const li=document.createElement('li');li.textContent=fact.displayName+' · '+fact.companyEmail+' · 내선 '+fact.extensionNo;facts.appendChild(li);}
                    for(const source of data.sources){const li=document.createElement('li'),link=document.createElement('a');link.textContent=source.title;link.href=source.url;li.appendChild(link);sources.appendChild(li);}
                  }catch{answer.textContent='요청을 처리할 수 없습니다.';}finally{button.disabled=false;}
                };
                </script></html>
                """;
    }
}
