import {createApi,ApiError,errorMessage,renderMarkdown,renderPublished} from './api.js';
import {ChatConnection} from './stomp.js';
const $=id=>document.getElementById(id),values=form=>Object.fromEntries(new FormData(form));
const state={me:null,screen:'directory',employeePage:0,documentPage:0,room:0,messages:new Map(),cursor:0,pending:null,epoch:0};
const api=createApi(expired),connection=new ChatConnection(api,{state:text=>{$('connection').textContent=text;},ready:()=>run(async()=>{await refreshRooms();await recover();}),message:incoming});
function notice(text,error=false){$('status').textContent=text;$('status').dataset.error=String(error);}
function fail(error){notice(errorMessage(error.status),true);}
async function run(action){try{await action();}catch(error){fail(error);}}
function form(id,action){$(id).addEventListener('submit',async event=>{event.preventDefault();const button=event.currentTarget.querySelector('button');button.disabled=true;try{await action(values(event.currentTarget),event.currentTarget);}catch(error){fail(error);}finally{button.disabled=false;}});}
function button(text,action){const node=document.createElement('button');node.type='button';node.textContent=text;node.addEventListener('click',()=>run(action));return node;}
function item(parent,text){const node=document.createElement('li');node.textContent=text;parent.appendChild(node);return node;}
function expired(){
  connection.stop();state.epoch++;state.me=null;state.room=0;state.messages.clear();state.pending=null;clearTimeout(state.pendingTimer);
  api.reset();$('workspace').hidden=true;$('auth').hidden=false;$('logout').hidden=true;$('identity').textContent='';
  for(const id of ['employee-results','owner-results','rooms','messages','document-results','document-view','ai-answer','ai-facts','ai-sources','ai-type','activation-code'])$(id).replaceChildren();
  for(const node of document.querySelectorAll('textarea,input:not([type=checkbox])'))node.value='';
  document.querySelectorAll('input[type=checkbox]').forEach(node=>node.checked=false);
  $('message-form').elements.content.readOnly=false;
  notice('세션이 만료되었습니다. 다시 로그인하세요.',true);
}
async function signedIn(){
  state.me=await api.request('/api/auth/me');state.epoch++;$('auth').hidden=true;$('workspace').hidden=false;$('logout').hidden=false;
  $('identity').textContent=state.me.employeeCode+' · '+state.me.departmentCode;
  document.querySelectorAll('[data-role]').forEach(node=>node.hidden=!state.me.roles.includes(node.dataset.role));
  const departments=await api.request('/api/departments');
  document.querySelectorAll('select.departments').forEach(select=>{select.replaceChildren();if(!select.required){const all=document.createElement('option');all.value='';all.textContent='전체';select.appendChild(all);}for(const d of departments){const option=document.createElement('option');option.value=d.departmentCode;option.textContent=d.departmentName;select.appendChild(option);}});
  const examples=await api.request('/api/ai/questions/examples');$('ai-question').replaceChildren();for(const text of examples.questions){const option=document.createElement('option');option.value=text;option.textContent=text;$('ai-question').appendChild(option);}
  show('directory');await searchEmployees();connection.start();notice('로그인되었습니다. 가상 데이터만 사용하세요.');
}
function show(screen){
  if(!state.me)return;if(screen==='admin'&&!state.me.roles.includes('ACCOUNT_ADMIN')){fail(new ApiError(403));return;}
  state.screen=screen;document.querySelectorAll('.screen').forEach(node=>node.hidden=node.id!==screen);
  document.querySelectorAll('[data-screen]').forEach(node=>{if(node.dataset.screen===screen)node.setAttribute('aria-current','page');else node.removeAttribute('aria-current');});
  $('activation-code').textContent='';if(screen!=='ai'){for(const id of ['ai-answer','ai-facts','ai-sources','ai-type'])$(id).replaceChildren();}
  if(screen==='chat')run(async()=>{await refreshRooms();await recover();});
  $(screen).querySelector('h1').setAttribute('tabindex','-1');$(screen).querySelector('h1').focus();
}
document.querySelectorAll('[data-screen]').forEach(node=>node.onclick=()=>show(node.dataset.screen));
form('login-form',async(data,form)=>{await api.request('/api/auth/login',{method:'POST',body:data});form.reset();api.reset();await signedIn();});
form('activate-form',async(data,form)=>{await api.request('/api/auth/activate',{method:'POST',body:data});form.reset();notice('활성화되었습니다. 새 비밀번호로 로그인하세요.');});
$('logout').onclick=()=>run(async()=>{await api.request('/api/auth/logout',{method:'POST'});expired();notice('로그아웃되었습니다.');});
function renderPeople(parent,people){parent.replaceChildren();for(const employee of people){const li=item(parent,employee.displayName+' · '+employee.departmentCode+' · '+employee.jobTitle+' · '+employee.companyEmail+' · 내선 '+employee.extensionNo);if(employee.employeeId!==state.me.employeeCode)li.appendChild(button('대화하기',()=>openRoom(employee.employeeId)));}if(!people.length)item(parent,'검색 결과가 없습니다.');}
async function searchEmployees(){const query=new URLSearchParams({...values($('employee-search')),page:state.employeePage,size:10});const page=await api.request('/api/employees?'+query);renderPeople($('employee-results'),page.items);$('employee-page').textContent='총 '+page.total+'명 · '+(page.page+1)+'페이지';$('employee-prev').disabled=page.page===0;$('employee-next').disabled=(page.page+1)*page.size>=page.total;}
form('employee-search',async()=>{state.employeePage=0;await searchEmployees();notice('직원 검색이 완료되었습니다.');});
$('employee-prev').onclick=()=>run(async()=>{state.employeePage=Math.max(0,state.employeePage-1);await searchEmployees();});
$('employee-next').onclick=()=>run(async()=>{state.employeePage++;await searchEmployees();});
form('owner-search',async data=>{const page=await api.request('/api/tasks/owners?'+new URLSearchParams(data));renderPeople($('owner-results'),page.items);notice('담당자 조회가 완료되었습니다.');});
async function refreshRooms(){const epoch=state.epoch;const rooms=await api.request('/api/chat/rooms');if(epoch!==state.epoch)return;$('rooms').replaceChildren();for(const room of rooms){const li=item($('rooms'),'미읽음 '+room.unreadCount+' · 읽음 '+room.lastReadSeq+'/'+room.lastRoomSeq);li.prepend(button(room.otherEmployeeId,()=>selectRoom(room.roomId,room.otherEmployeeId)));}if(!rooms.length)item($('rooms'),'대화가 없습니다.');}
async function openRoom(employee){if(state.pending)throw new ApiError(409);const room=await api.request('/api/chat/rooms',{method:'POST',body:{recipientEmployeeId:employee}});await selectRoom(Number(room.conversationId),employee);show('chat');}
async function selectRoom(id,employee){if(state.pending)throw new ApiError(409);state.room=id;state.messages.clear();state.cursor=0;$('messages').replaceChildren();$('room-title').textContent=employee+'와의 1:1 대화';await recover();}
function merge(items){for(const message of items)state.messages.set(message.roomSeq,message);if(state.messages.size>500){const keys=[...state.messages.keys()].sort((a,b)=>a-b);for(const key of keys.slice(0,keys.length-500))state.messages.delete(key);notice('화면은 최근 500건까지 보관합니다.');}}
function renderMessages(){const list=$('messages');list.replaceChildren();for(const message of [...state.messages.values()].sort((a,b)=>a.roomSeq-b.roomSeq))item(list,message.senderEmployeeId+' #'+message.roomSeq+'\n'+message.content);$('older').disabled=!state.cursor||state.messages.size>=500;}
async function markRead(){if(!state.room||state.screen!=='chat'||document.hidden||!state.messages.size)return;const seq=Math.max(...state.messages.keys()),roomId=state.room;if(!connection.send('/app/chat.read',{roomId,lastReadSeq:seq}))await api.request('/api/chat/rooms/'+roomId+'/read-state',{method:'PUT',body:{lastReadSeq:seq}});}
let recovery;
async function recover(){
  if(!state.room)return;if(recovery){await recovery;if(!state.room)return;}
  const room=state.room,epoch=state.epoch,known=state.messages.size?Math.max(...state.messages.keys()):0;
  recovery=(async()=>{let before=0,page;
    for(let count=0;count<10;count++){
      page=await api.request('/api/chat/rooms/'+room+'/messages?size=50&beforeSeq='+before);if(room!==state.room||epoch!==state.epoch)return;
      merge(page.items);state.cursor=page.nextBeforeSeq;
      if(!known||!page.items.length||page.items.some(m=>m.roomSeq<=known)||!page.nextBeforeSeq)break;
      before=page.nextBeforeSeq;if(count===9)notice('최근 500건을 복구했습니다. 더 이전 기록은 현재 화면의 표시 범위 밖입니다.',true);
    }
    renderMessages();await markRead();
  })();try{await recovery;}finally{recovery=null;}
}
async function older(){if(!state.room||!state.cursor)return;const room=state.room,epoch=state.epoch;const page=await api.request('/api/chat/rooms/'+room+'/messages?size=50&beforeSeq='+state.cursor);if(room!==state.room||epoch!==state.epoch)return;merge(page.items);state.cursor=page.nextBeforeSeq;renderMessages();}
function settle(message){const pending=state.pending;if(!pending)return;clearTimeout(state.pendingTimer);if(state.room===pending.roomId){merge([message]);renderMessages();}state.pending=null;$('message-form').reset();$('message-form').elements.content.readOnly=false;notice('메시지가 전송되었습니다.');run(async()=>{await markRead();await refreshRooms();});}
function incoming(subscription,data){
  if(!state.me)return;
  if(subscription==='s0'){if(data.roomId===state.room){merge([data]);renderMessages();run(markRead);}run(refreshRooms);}
  else if(subscription==='s1'){if(state.pending&&data.clientRequestId===state.pending.clientRequestId&&data.roomId===state.pending.roomId)settle({roomSeq:data.roomSeq,senderEmployeeId:state.me.employeeCode,content:state.pending.content});run(refreshRooms);}
  else if(subscription==='s2'){notice('채팅 요청이 거부되었습니다. 권한·입력·연결을 확인하세요.',true);}
}
async function confirmPending(pending){if(state.pending!==pending)return;try{const result=await api.request('/api/chat/rooms/'+pending.roomId+'/messages',{method:'POST',body:{clientRequestId:pending.clientRequestId,content:pending.content}});if(state.pending===pending)settle(result);}catch(error){if(state.pending===pending){notice(errorMessage(error.status)+' 동일한 요청으로 전송을 다시 확인할 수 있습니다.',true);}}}
form('message-form',async data=>{
  if(!state.room)throw new ApiError(400);
  if(state.pending){await confirmPending(state.pending);return;}
  if(!data.content.trim()||[...data.content].length>2000||data.content.includes('\0'))throw new ApiError(400);
  const pending={roomId:state.room,clientRequestId:crypto.randomUUID(),content:data.content};state.pending=pending;$('message-form').elements.content.readOnly=true;
  if(connection.send('/app/chat.send',pending)){notice('전송 확인 중');state.pendingTimer=setTimeout(()=>confirmPending(pending),4000);}else await confirmPending(pending);
});
$('rooms-refresh').onclick=()=>run(refreshRooms);$('older').onclick=()=>run(older);$('reconnect').onclick=()=>{connection.start();run(recover);};
document.addEventListener('visibilitychange',()=>{if(!document.hidden&&state.me&&state.screen==='chat')run(recover);});
async function searchDocuments(){const page=await api.request('/api/documents/search?'+new URLSearchParams({...values($('document-search')),page:state.documentPage,size:10}));$('document-results').replaceChildren();for(const hit of page.items){const li=item($('document-results'),hit.title+' · '+hit.excerpt);li.appendChild(button('근거 보기',()=>openSource('/api/documents/'+hit.documentId+'/versions/'+hit.versionId+'/fragments/'+hit.fragmentId)));}if(!page.items.length)item($('document-results'),'접근 가능한 검색 결과가 없습니다.');$('document-more').disabled=!page.hasMore;}
async function openSource(path){if(!/^\/api\/documents\/[1-9][0-9]*\/versions\/[1-9][0-9]*\/fragments\/[1-9][0-9]*$/.test(path))throw new ApiError(400);$('document-view').replaceChildren();const source=await api.request(path);show('documents');renderMarkdown($('document-view'),source.content);notice('현재 권한으로 문서 근거를 확인했습니다.');}
form('document-search',async()=>{state.documentPage=0;await searchDocuments();});$('document-more').onclick=()=>run(async()=>{state.documentPage++;await searchDocuments();});
form('document-create',async(data,form)=>{const body={...data,aiSearchable:true,externalAiAllowed:false};if(data.scope==='DEPARTMENT')body.departmentId=state.me.departmentCode;const code=await api.request('/api/documents',{method:'POST',body});$('document-workflow').elements.code.value=code;form.reset();notice('초안이 작성되었습니다: '+code);});
form('document-workflow',async data=>{
  $('document-view').replaceChildren();const path='/api/documents/'+encodeURIComponent(data.code);let method='POST',suffix=data.action,body;
  if(data.action==='published'){const published=await api.request(path+'/published');renderPublished($('document-view'),published.renderedHtml);const heading=document.createElement('h2');heading.textContent=published.title+' · 버전 '+published.versionNo;$('document-view').prepend(heading);notice('현재 게시본을 확인했습니다.');return;}
  if(data.action==='grant'||data.action==='revoke'){suffix='grants';method=data.action==='revoke'?'DELETE':'POST';body={employeeId:data.employeeId,permission:data.permission};}
  else if(data.action==='review-request')body={reviewerEmployeeId:data.employeeId};
  else if(data.action==='draft'||data.action==='versions'){suffix=data.action==='draft'?'versions/draft':'versions';method=data.action==='draft'?'PUT':'POST';body={format:data.format,body:data.body,aiSearchable:true,externalAiAllowed:false};}
  if(!['approve','reject','withdraw','grant','revoke','review-request','draft','versions'].includes(data.action))throw new ApiError(400);
  await api.request(path+'/'+suffix,{method,body});notice('문서 작업이 완료되었습니다.');
});
form('ai-form',async data=>{
  for(const id of ['ai-answer','ai-sources','ai-facts','ai-type'])$(id).replaceChildren();if(!data.ack)throw new ApiError(400);
  const epoch=state.epoch;const answer=await api.request('/api/ai/questions',{method:'POST',body:{question:data.question}});if(epoch!==state.epoch||state.screen!=='ai')return;
  $('ai-type').textContent=answer.answerType+' · 요청 '+answer.requestId;$('ai-answer').textContent=answer.answer;
  for(const fact of answer.structuredFacts)item($('ai-facts'),fact.displayName+' · '+fact.companyEmail+' · 내선 '+fact.extensionNo);
  for(const source of answer.sources){const li=item($('ai-sources'),source.title);li.appendChild(button('출처 확인',()=>openSource(source.url)));}notice('AI 응답 확인이 완료되었습니다.');
});
form('admin-create',async(data,form)=>{await api.request('/api/admin/employees',{method:'POST',body:data});form.reset();notice('가상 직원이 등록되었습니다. 계정·역할은 자동 생성되지 않습니다.');});
form('admin-change',async data=>{if(!['status','department'].includes(data.action))throw new ApiError(400);const body=data.action==='status'?{status:data.status}:{departmentId:data.departmentId};await api.request('/api/admin/employees/'+encodeURIComponent(data.employeeId)+'/'+data.action,{method:'PATCH',body});notice('직원 정보가 변경되었습니다.');});
form('activation-issue',async data=>{$('activation-code').textContent='';const code=await api.request('/api/admin/activations',{method:'POST',body:data});$('activation-code').textContent=code.code;notice('활성화 코드가 발급되었습니다. 안전하게 전달한 뒤 표시를 지우세요.');});
$('clear-code').onclick=()=>{$('activation-code').textContent='';};
window.addEventListener('pagehide',()=>{connection.stop();$('activation-code').textContent='';for(const id of ['ai-answer','ai-sources','ai-facts'])$(id).replaceChildren();});
run(async()=>{try{await signedIn();}catch(error){if(error.status===401)notice('가상 직원 계정으로 로그인하세요.');else throw error;}});
