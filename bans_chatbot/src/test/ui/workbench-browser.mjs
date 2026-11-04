import {spawn} from 'node:child_process';
import {mkdirSync,mkdtempSync,existsSync,readFileSync,writeFileSync,createWriteStream} from 'node:fs';
import {resolve,join} from 'node:path';
import {setTimeout as sleep} from 'node:timers/promises';
const [base,chrome]=process.argv.slice(2),password=process.env.BANS_UI_TEST_PASSWORD;
if(!/^http:\/\/127\.0\.0\.1:\d+$/.test(base)||!password)throw Error('TEST_CONFIGURATION_REQUIRED');
mkdirSync('target',{recursive:true});const profile=mkdtempSync(resolve('target','ui-chrome-'));
const browser=spawn(chrome,['--headless=new','--disable-gpu','--in-process-gpu','--no-first-run','--no-default-browser-check','--disable-background-networking','--disable-component-update','--disable-sync','--disable-extensions','--disable-default-apps','--metrics-recording-only','--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE 127.0.0.1, EXCLUDE localhost','--remote-debugging-address=127.0.0.1','--remote-debugging-port=0','--user-data-dir='+profile,'about:blank'],{windowsHide:true,stdio:['ignore','ignore','pipe']});
const chromeLog=createWriteStream('local-notes/step15-chrome.log');browser.stderr.pipe(chromeLog);
const sockets=[];let stage='startup';
class Cdp {
  constructor(url){this.ws=new WebSocket(url);sockets.push(this.ws);this.seq=0;this.pending=new Map();this.events=new Map();this.open=new Promise((resolve,reject)=>{const timer=setTimeout(()=>reject(Error('CDP_OPEN_TIMEOUT')),5000);this.ws.onopen=()=>{clearTimeout(timer);resolve();};this.ws.onerror=()=>{clearTimeout(timer);reject(Error('CDP_CONNECTION'));};});this.ws.onclose=()=>{for(const waiter of this.pending.values()){clearTimeout(waiter.timer);waiter.reject(Error('CDP_CLOSED'));}this.pending.clear();};this.ws.onmessage=event=>{const message=JSON.parse(event.data);if(message.id){const waiter=this.pending.get(message.id);if(waiter){clearTimeout(waiter.timer);this.pending.delete(message.id);message.error?waiter.reject(Error('CDP_COMMAND')):waiter.resolve(message.result);}}else this.events.get(message.method)?.(message.params);};}
  async call(method,params={}){await this.open;return new Promise((resolve,reject)=>{const id=++this.seq;const timer=setTimeout(()=>{this.pending.delete(id);reject(Error('CDP_TIMEOUT_'+method));},8000);this.pending.set(id,{resolve,reject,timer});this.ws.send(JSON.stringify({id,method,params}));});}
  async eval(expression){const result=await this.call('Runtime.evaluate',{expression,awaitPromise:true,returnByValue:true});if(result.exceptionDetails)throw Error('BROWSER_EVALUATION');return result.result.value;}
}
async function wait(page,expression,label){const start=Date.now();while(Date.now()-start<10000){if(await page.eval(expression))return;await sleep(80);}throw Error('WAIT_'+label);}
const code=value=>JSON.stringify(value);
async function fill(page,id,data){await page.eval(`(()=>{const form=document.getElementById(${code(id)});for(const [key,value] of Object.entries(${code(data)})){const node=form.elements[key];if(node.type==='checkbox')node.checked=!!value;else node.value=value;node.dispatchEvent(new Event('input',{bubbles:true}));}})()`);}
async function submit(page,id){await page.eval(`document.getElementById(${code(id)}).requestSubmit()`);}
async function click(page,selector){await page.eval(`document.querySelector(${code(selector)}).click()`);}
async function screen(page,id){await click(page,`[data-screen="${id}"]`);await wait(page,`!document.getElementById(${code(id)}).hidden`,'screen_'+id);}
async function status(page,text){await wait(page,`document.getElementById('status').textContent.includes(${code(text)})`,'status');}
async function login(page,employee){await wait(page,`!document.getElementById('auth').hidden`,'auth');await fill(page,'login-form',{employeeCode:employee,password});await submit(page,'login-form');await wait(page,`!document.getElementById('workspace').hidden&&document.getElementById('identity').textContent.startsWith(${code(employee)})`,'login');await wait(page,`document.getElementById('employee-results').children.length>0`,'directory_loaded');}
async function logout(page){await click(page,'#logout');await wait(page,`!document.getElementById('auth').hidden`,'logout');}
let root;const resources=new Set(),errors=[];let pageCount=0;
async function pageInContext(){
  const context=pageCount++?await root.call('Target.createBrowserContext'):{};const target=await root.call('Target.createTarget',{url:'about:blank',...context});
  let entry;for(let i=0;i<50;i++){entry=(await (await fetch(devtools+'/json/list')).json()).find(e=>e.id===target.targetId);if(entry)break;await sleep(50);}if(!entry)throw Error('PAGE_TARGET');
  const page=new Cdp(entry.webSocketDebuggerUrl);await page.call('Page.enable');await page.call('Runtime.enable');await page.call('Network.enable');
  page.events.set('Runtime.exceptionThrown',()=>errors.push('javascript exception'));
  page.events.set('Network.requestWillBeSent',event=>{if(event.request.url.startsWith(base))resources.add(event.request.url);else if(!event.request.url.startsWith('ws://127.0.0.1:')&&event.request.url!=='about:blank'&&!event.request.url.startsWith('data:'))errors.push('non-local request');});
  await page.call('Page.addScriptToEvaluateOnNewDocument',{source:`window.__testSockets=[];const NativeSocket=window.WebSocket;window.WebSocket=class extends NativeSocket{constructor(...args){super(...args);window.__testSockets.push(this);}};`});
  await page.call('Page.navigate',{url:base+'/'});await status(page,'가상 직원 계정으로');return page;
}
let devtools;
try{
  for(let i=0;i<150&&!existsSync(join(profile,'DevToolsActivePort'));i++)await sleep(100);
  const [port,path]=readFileSync(join(profile,'DevToolsActivePort'),'utf8').trim().split('\n');devtools='http://127.0.0.1:'+port;root=new Cdp('ws://127.0.0.1:'+port+path);
  const page=await pageInContext();stage='admin login';await login(page,'EMP-001');await screen(page,'admin');
  const activation=new Map();for(const employeeCode of ['EMP-002','EMP-005','EMP-010']){await fill(page,'activation-issue',{employeeCode});await submit(page,'activation-issue');await wait(page,`document.getElementById('activation-code').textContent.length>20`,'issue');activation.set(employeeCode,await page.eval(`document.getElementById('activation-code').textContent`));await click(page,'#clear-code');}
  await fill(page,'admin-create',{employeeId:'EMP-021',displayName:'가상UI신입',departmentId:'D01',jobTitle:'사원',companyEmail:'ui21@bans.example',extensionNo:'1021',task:'가상 테스트 업무'});await submit(page,'admin-create');await status(page,'가상 직원이 등록');
  await logout(page);stage='activation';for(const [employeeCode,activationCode] of activation){await fill(page,'activate-form',{employeeCode,code:activationCode,newPassword:password});await submit(page,'activate-form');await status(page,'활성화되었습니다');}
  stage='employee search';await login(page,'EMP-005');await fill(page,'employee-search',{name:'가상인사02'});await submit(page,'employee-search');await wait(page,`document.getElementById('employee-results').textContent.includes('emp002@bans.example')`,'search');
  await fill(page,'owner-search',{query:'정산'});await submit(page,'owner-search');await wait(page,`document.getElementById('owner-results').textContent.includes('EMP-005')||document.getElementById('owner-results').textContent.includes('가상재무01')`,'owner');
  const second=await pageInContext();await login(second,'EMP-002');
  stage='chat';await click(page,'#employee-results button');await wait(page,`document.getElementById('room-title').textContent.includes('EMP-002')`,'room');await wait(page,`document.getElementById('connection').textContent==='실시간 연결됨'`,'stomp');
  await screen(second,'chat');await click(second,'#rooms-refresh');await wait(second,`document.querySelector('#rooms button')!==null`,'room_list');await click(second,'#rooms button');await wait(second,`document.getElementById('connection').textContent==='실시간 연결됨'`,'stomp_second');
  await fill(page,'message-form',{content:'가상 UI 실시간 메시지'});await submit(page,'message-form');await wait(second,`document.getElementById('messages').textContent.includes('가상 UI 실시간 메시지')`,'delivery');
  stage='reconnect';await page.eval(`window.__testSockets.at(-1).close()`);await fill(second,'message-form',{content:'가상 재연결 누락 복구'});await submit(second,'message-form');await wait(page,`document.getElementById('connection').textContent==='실시간 연결됨'&&document.getElementById('messages').textContent.includes('가상 재연결 누락 복구')`,'recovery');
  if(!await page.eval(`document.querySelectorAll('#messages li').length===2`))throw Error('CHAT_DUPLICATE');
  stage='document author';await screen(page,'documents');await fill(page,'document-create',{documentCode:'DOC-UI',title:'UI-DEMO 가상 문서',scope:'SELECTED',body:'# UI-DEMO\n<img src=x onerror="window.__xss=1">'});await submit(page,'document-create');await status(page,'초안이 작성');
  for(const permission of ['READ','APPROVE']){await fill(page,'document-workflow',{code:'DOC-UI',action:'grant',employeeId:'EMP-002',permission});await submit(page,'document-workflow');await status(page,'문서 작업이 완료');await page.eval(`document.getElementById('status').textContent=''`);}
  await fill(page,'document-workflow',{action:'review-request',employeeId:'EMP-002'});await submit(page,'document-workflow');await status(page,'문서 작업이 완료');
  stage='document approve';await screen(second,'documents');await fill(second,'document-workflow',{code:'DOC-UI',action:'approve'});await submit(second,'document-workflow');await status(second,'문서 작업이 완료');
  await fill(page,'document-search',{q:'UI-DEMO'});await submit(page,'document-search');await wait(page,`document.querySelector('#document-results button')!==null`,'doc_search');await click(page,'#document-results button');await wait(page,`document.getElementById('document-view').textContent.includes('<img')`,'doc_view');if(!await page.eval(`!window.__xss&&document.querySelector('#document-view img')===null`))throw Error('XSS');
  stage='document 403';await fill(second,'document-workflow',{action:'grant',employeeId:'EMP-010',permission:'READ'});await submit(second,'document-workflow');await status(second,'권한이 없거나');
  stage='AI source';await screen(page,'ai');await fill(page,'ai-form',{question:'연차 신청 절차',ack:true});await submit(page,'ai-form');await wait(page,`document.getElementById('ai-type').textContent.includes('DOCUMENT')&&document.querySelector('#ai-sources button')!==null`,'ai_document');await click(page,'#ai-sources button');await wait(page,`document.getElementById('document-view').textContent.includes('합성 직원')`,'source');
  stage='AI 429';await screen(page,'ai');for(let i=0;i<5;i++){await fill(page,'ai-form',{question:'출장비 담당자',ack:true});await submit(page,'ai-form');await wait(page,`!document.querySelector('#ai-form button').disabled`,'ai_done');}
  await status(page,'호출 한도');
  stage='AI 503';await page.call('Fetch.enable',{patterns:[{urlPattern:base+'/api/ai/questions',requestStage:'Request'}]});
  page.events.set('Fetch.requestPaused',event=>{page.call('Fetch.fulfillRequest',{requestId:event.requestId,responseCode:503,responseHeaders:[{name:'Content-Type',value:'application/json'}],body:Buffer.from('{"code":"AI_UPSTREAM_UNAVAILABLE","message":"AI request unavailable."}').toString('base64')}).catch(()=>{});});
  await fill(page,'ai-form',{question:'출장비 담당자',ack:true});await submit(page,'ai-form');await status(page,'서비스를 사용할 수 없습니다');await page.call('Fetch.disable');
  await screen(page,'chat');await fill(page,'message-form',{content:'가상 AI 오류 후 일반 채팅'});await submit(page,'message-form');await wait(second,`document.getElementById('messages').textContent.includes('가상 AI 오류 후 일반 채팅')`,'chat_independent');
  stage='mobile and session';await page.call('Emulation.setDeviceMetricsOverride',{width:390,height:844,deviceScaleFactor:1,mobile:true});if(!await page.eval(`document.documentElement.scrollWidth<=document.documentElement.clientWidth`))throw Error('MOBILE_OVERFLOW');
  const shot=await page.call('Page.captureScreenshot',{format:'png'});writeFileSync('target/step15-ui.png',Buffer.from(shot.data,'base64'));
  await logout(second);await login(second,'EMP-001');await screen(second,'admin');await fill(second,'admin-change',{employeeId:'EMP-005',action:'status',status:'SUSPENDED'});await submit(second,'admin-change');await status(second,'직원 정보가 변경');await click(page,'#rooms-refresh');await status(page,'세션이 만료');
  if(!await page.eval(`document.getElementById('workspace').hidden&&document.getElementById('ai-answer').textContent===''&&document.getElementById('messages').textContent===''`))throw Error('SESSION_CLEAR');
  stage='assets';for(const path of ['/ui/app.js','/ui/api.js','/ui/stomp.js','/ui/app.css']){const text=await(await fetch(base+path)).text();if(/GOOGLE_API_KEY|AIza[A-Za-z0-9_-]{20,}|innerHTML|localStorage|sessionStorage/.test(text))throw Error('UNSAFE_ASSET');}
  if(errors.length)throw Error('BROWSER_RUNTIME_OR_EXTERNAL_REQUEST');
  console.log('BROWSER_PASS screens=6 activation search admin chat read reconnect documents approval sources 401 403 429 503 XSS mobile; Oracle/Gemini=0');
}catch(error){console.error('BROWSER_FAIL stage='+stage+' reason='+error.message);process.exitCode=1;}
finally{if(root)await Promise.race([root.call('Browser.close').catch(()=>{}),sleep(1000)]);for(const socket of sockets)socket.close();browser.kill();}
