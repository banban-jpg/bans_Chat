export class ApiError extends Error { constructor(status){super('Request unavailable');this.status=status;} }
export const errorMessage=status=>({400:'입력 값을 확인해 주세요.',401:'세션이 만료되었습니다. 다시 로그인하세요.',403:'권한이 없거나 요청 확인 정보가 만료되었습니다. 문서 권한을 확인하세요.',404:'현재 접근할 수 있는 자료가 없습니다.',409:'현재 상태에서 처리할 수 없습니다. 변경 내용을 확인하세요.',429:'호출 한도에 도달했습니다. 잠시 후 다시 시도하세요.',503:'서비스를 사용할 수 없습니다. AI 오류 중에도 일반 채팅은 이용할 수 있습니다.',504:'응답 시간이 초과되었습니다. 잠시 후 다시 확인하세요.'}[status]||'요청을 처리하지 못했습니다. 연결 상태를 확인하세요.');
export function createApi(onExpired){
  let csrf,version=0;
  async function request(path,{method='GET',body}={}){
    const requestVersion=version;
    if(!path.startsWith('/api/')||path.startsWith('//'))throw new ApiError(400);
    const headers={'Accept':'application/json'};
    if(method!=='GET'){
      if(!csrf)csrf=await request('/api/auth/csrf');
      headers[csrf.headerName]=csrf.token;headers['Content-Type']='application/json';
    }
    if(requestVersion!==version)throw new ApiError(0);
    let response;
    try{response=await fetch(path,{method,headers,body:body===undefined?undefined:JSON.stringify(body),credentials:'same-origin',cache:'no-store',signal:AbortSignal.timeout(15000)});}
    catch{throw new ApiError(0);}
    if(requestVersion!==version)throw new ApiError(0);
    if(!response.ok){if(response.status===401){csrf=undefined;if(path!=='/api/auth/login')onExpired();}if(response.status===403)csrf=undefined;throw new ApiError(response.status);}
    const result=await response.json();if(requestVersion!==version)throw new ApiError(0);return result.data;
  }
  return {request,reset(){version++;csrf=undefined;},async csrf(){csrf=await request('/api/auth/csrf');return csrf;}};
}
export function renderMarkdown(parent,text){
  parent.replaceChildren();for(const line of String(text).split('\n')){
    const level=line.startsWith('## ')?2:line.startsWith('# ')?1:0;
    const node=document.createElement(level?'h'+level:'p');node.textContent=level?line.slice(level+1):line;parent.appendChild(node);
  }
}
/** Read only the server renderer's four tags; entities become text, never executable HTML. */
export function renderPublished(parent,html){
  parent.replaceChildren();const entities={amp:'&',lt:'<',gt:'>',quot:'"','#39':"'"};
  for(const match of String(html).matchAll(/<(h1|h2|p|pre)>([\s\S]*?)<\/\1>/g)){
    const node=document.createElement(match[1]);node.textContent=match[2].replace(/&(amp|lt|gt|quot|#39);/g,(_,entity)=>entities[entity]);parent.appendChild(node);
  }
  if(!parent.children.length)parent.textContent=String(html);
}
