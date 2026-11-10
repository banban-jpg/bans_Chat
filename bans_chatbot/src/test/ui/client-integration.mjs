import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {setTimeout as sleep} from 'node:timers/promises';
const base=process.argv[2],password=process.env.BANS_UI_TEST_PASSWORD;
assert.match(base,/^http:\/\/127\.0\.0\.1:\d+$/);
const load=path=>import('data:text/javascript;base64,'+Buffer.from(readFileSync(path,'utf8')).toString('base64'));
const {ChatConnection}=await load('src/main/resources/static/ui/stomp.js');
const {errorMessage,renderMarkdown,renderPublished,createApi}=await load('src/main/resources/static/ui/api.js');
const NativeSocket=globalThis.WebSocket;const url=new URL(base);globalThis.location={host:url.host,protocol:url.protocol};
async function until(check,label){for(let i=0;i<100;i++){if(await check())return;await sleep(60);}throw Error(label);}
async function browser(employeeCode){
  let cookie='',csrf;
  const http=async(path,{method='GET',body}={})=>{
    const headers={Cookie:cookie};if(method!=='GET'){headers[csrf.headerName]=csrf.token;headers['Content-Type']='application/json';}
    const response=await fetch(base+path,{method,headers,body:body===undefined?undefined:JSON.stringify(body)});
    const cookies=response.headers.getSetCookie();if(cookies.length)cookie=cookies.map(c=>c.split(';')[0]).join('; ');
    const json=await response.json();if(!response.ok){const error=Error('SAFE_HTTP');error.status=response.status;throw error;}return json.data;
  };
  csrf=await http('/api/auth/csrf');await http('/api/auth/login',{method:'POST',body:{employeeCode,password}});csrf=await http('/api/auth/csrf');
  class Socket extends NativeSocket{constructor(endpoint){super(endpoint,{headers:{Cookie:cookie,Origin:base}});}}
  return {http,Socket,csrf:async()=>csrf,request:http};
}
let a,b;let stage='HTTP';
try{
  const first=await browser('EMP-001'),second=await browser('EMP-005');
  assert.equal((await first.http('/api/employees?size=20')).total,20);assert.equal((await first.http('/api/departments')).length,5);
  assert.ok((await first.http('/api/tasks/owners?query='+encodeURIComponent('정산'))).items.length);
  const room=await first.http('/api/chat/rooms',{method:'POST',body:{recipientEmployeeId:'EMP-005'}}),roomId=Number(room.conversationId);
  let aReady=0,bReady=0,ack,received=[],recovered=[];
  globalThis.WebSocket=first.Socket;a=new ChatConnection(first,{state(){},ready(){aReady++;if(aReady>1)first.http('/api/chat/rooms/'+roomId+'/messages?size=50').then(page=>{recovered=page.items;});},message(subscription,data){if(subscription==='s1')ack=data;}});
  globalThis.WebSocket=second.Socket;b=new ChatConnection(second,{state(){},ready(){bReady++;},message(subscription,data){if(subscription==='s0')received.push(data);}});
  stage='STOMP connected';a.start();b.start();await until(()=>a.ready&&b.ready,'CONNECT');await sleep(150);
  assert.equal(a.send('/topic/forbidden',{}),false);
  assert.ok(a.send('/app/chat.send',{roomId,clientRequestId:'ui-wire-1',content:'가상 실제 JS 전송'}));
  await until(()=>ack?.clientRequestId==='ui-wire-1'&&received.length===1,'DELIVERY');
  assert.equal(received[0].senderEmployeeId,'EMP-001');
  stage='reconnect';a.socket.close();await until(()=>!a.ready,'CLOSE');
  b.send('/app/chat.send',{roomId,clientRequestId:'ui-gap-1',content:'가상 연결 중단 구간'});
  await until(()=>aReady>1&&recovered.some(m=>m.content==='가상 연결 중단 구간'),'RECOVERY');
  a.send('/app/chat.read',{roomId,lastReadSeq:2});await until(async()=>(await first.http('/api/chat/rooms'))[0].lastReadSeq===2,'READ');
  stage='documents';const found=await first.http('/api/documents/search?q='+encodeURIComponent('휴가 신청'));assert.equal(found.items.length,1);
  const hit=found.items[0];assert.ok((await first.http(`/api/documents/${hit.documentId}/versions/${hit.versionId}/fragments/${hit.fragmentId}`)).content);
  stage='AI';const answer=await first.http('/api/ai/questions',{method:'POST',body:{question:'연차 신청 절차'}});assert.equal(answer.answerType,'DOCUMENT');assert.equal(answer.sources.length,1);
  for(let i=0;i<4;i++)await first.http('/api/ai/questions',{method:'POST',body:{question:'출장비 담당자'}});
  await assert.rejects(()=>first.http('/api/ai/questions',{method:'POST',body:{question:'출장비 담당자'}}),e=>e.status===429);
  assert.match(errorMessage(403),/권한/);assert.match(errorMessage(429),/한도/);assert.match(errorMessage(503),/일반 채팅/);
  b.send('/app/chat.send',{roomId,clientRequestId:'ui-after-ai',content:'가상 AI 한도 후 채팅'});await until(async()=>(await first.http('/api/chat/rooms/'+roomId+'/messages')).items.length===3,'CHAT_INDEPENDENT');
  stage='XSS';globalThis.document={createElement:tag=>({tag,textContent:''})};const parent={children:[],replaceChildren(){this.children=[];},appendChild(child){this.children.push(child);}};renderMarkdown(parent,'# 가상\n<img src=x onerror=alert(1)>');assert.equal(parent.children[1].tag,'p');assert.match(parent.children[1].textContent,/<img/);
  renderPublished(parent,'<h1>가상</h1><p>&lt;img src=x onerror=alert(1)&gt;</p>');assert.equal(parent.children[1].tag,'p');assert.equal(parent.children[1].textContent,'<img src=x onerror=alert(1)>');
  stage='API client errors';const nativeFetch=globalThis.fetch;let expire=0;const api=createApi(()=>expire++);
  globalThis.fetch=async()=>new Response('{"code":"SAFE"}',{status:503});await assert.rejects(()=>api.request('/api/ai/questions'),e=>e.status===503);assert.equal(expire,0);
  globalThis.fetch=async(path)=>path==='/api/auth/csrf'?new Response('{"data":{"headerName":"X-CSRF-TOKEN","token":"demo"}}'):new Response('{"code":"AUTH_REQUIRED"}',{status:401});await assert.rejects(()=>api.request('/api/auth/login',{method:'POST',body:{employeeCode:'EMP-001',password:'wrong'}}),e=>e.status===401);assert.equal(expire,0);
  globalThis.fetch=async()=>new Response('{"code":"SAFE"}',{status:401});await assert.rejects(()=>api.request('/api/auth/me'),e=>e.status===401);assert.equal(expire,1);
  globalThis.fetch=async()=>new Response('{"data":{"ok":true}}');let resolveOld;globalThis.fetch=()=>new Promise(resolve=>{resolveOld=resolve;});const old=api.request('/api/employees');api.reset();resolveOld(new Response('{"data":{"private":"stale"}}'));await assert.rejects(()=>old,e=>e.status===0);globalThis.fetch=nativeFetch;
  console.log('CLIENT_PASS real HTTP + production JS STOMP connect/send/read/reconnect/REST recovery; document/AI sources; 401/403/429/503/XSS/stale-response; Oracle/Gemini=0');
}catch(error){console.error('CLIENT_FAIL stage='+stage+' reason='+error.message);process.exitCode=1;}
finally{a?.stop();b?.stop();}
