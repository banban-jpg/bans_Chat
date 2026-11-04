/** Small STOMP 1.2 client for the server's fixed JSON queues; no library/CDN or arbitrary destinations. */
export class ChatConnection {
  constructor(api,handlers){this.api=api;this.handlers=handlers;this.Socket=globalThis.WebSocket;this.stopped=true;this.attempts=0;this.generation=0;}
  start(){this.stop();this.stopped=false;this.attempts=0;this.connect();}
  stop(){this.stopped=true;this.ready=false;this.generation++;clearTimeout(this.timer);clearTimeout(this.watchdog);if(this.socket){this.socket.onclose=null;this.socket.onmessage=null;this.socket.onerror=null;this.socket.onopen=null;this.socket.close();this.socket=null;}}
  async connect(){
    const generation=++this.generation;
    try{
      await this.api.request('/api/auth/me');const csrf=await this.api.csrf();if(this.stopped||generation!==this.generation)return;
      const ws=new this.Socket((location.protocol==='https:'?'wss://':'ws://')+location.host+'/ws/chat');this.socket=ws;
      let buffer='';this.handlers.state('실시간 연결 중');
      this.watchdog=setTimeout(()=>ws.close(),8000);
      ws.onopen=()=>this.frame('CONNECT',{'accept-version':'1.2',host:location.host,'heart-beat':'0,0',[csrf.headerName]:csrf.token});
      ws.onmessage=event=>{
        if(this.stopped||generation!==this.generation||this.socket!==ws)return;
        if(typeof event.data!=='string'||new TextEncoder().encode(buffer+event.data).length>65536){ws.close();return;}
        buffer+=event.data;let end;
        while((end=buffer.indexOf('\0'))>=0){
          const raw=buffer.slice(0,end).replace(/^\n+/,'');buffer=buffer.slice(end+1);if(!raw)continue;
          const split=raw.indexOf('\n\n');if(split<0){ws.close();return;}
          const lines=raw.slice(0,split).split('\n'),command=lines.shift(),headers={};
          for(const line of lines){const colon=line.indexOf(':');if(colon>0)headers[line.slice(0,colon)]=line.slice(colon+1);}
          if(command==='CONNECTED'){
            for(const [index,queue] of ['chat.messages','chat.ack','chat.errors'].entries()){
              this.frame('SUBSCRIBE',{id:'s'+index,destination:'/user/queue/'+queue,ack:'auto'});
            }
            // Spring SimpleBroker has no STOMP receipts. Recover from REST after subscribing.
            clearTimeout(this.watchdog);this.ready=true;this.attempts=0;this.handlers.state('실시간 연결됨');this.handlers.ready();
          }else if(command==='MESSAGE'){
            try{this.handlers.message(headers.subscription,JSON.parse(raw.slice(split+2)));}catch{this.handlers.state('메시지를 해석하지 못했습니다. 새로고침으로 복구하세요.');}
          }else if(command==='ERROR'){ws.close();}
        }
        if(buffer==='\n')buffer='';
      };
      ws.onerror=()=>ws.close();ws.onclose=()=>{clearTimeout(this.watchdog);this.ready=false;this.retry();};
    }catch(error){if(error.status===401){this.stop();return;}this.retry();}
  }
  retry(){if(this.stopped)return;this.ready=false;if(this.attempts>=5){this.handlers.state('연결 중단 · 재연결 버튼을 누르세요. REST 조회·전송은 사용할 수 있습니다.');return;}const delay=Math.min(15000,1000*2**this.attempts++);this.handlers.state('연결 끊김 · 재접속 및 누락 복구 대기');this.timer=setTimeout(()=>this.connect(),delay);}
  frame(command,headers,body=''){
    if(!this.socket||this.socket.readyState!==WebSocket.OPEN)return false;
    const escape=value=>command==='CONNECT'?String(value):String(value).replace(/\\/g,'\\\\').replace(/\r/g,'\\r').replace(/\n/g,'\\n').replace(/:/g,'\\c');
    this.socket.send(command+'\n'+Object.entries(headers).map(([key,value])=>key+':'+escape(value)).join('\n')+'\n\n'+body+'\0');return true;
  }
  send(destination,payload){if(!this.ready||!['/app/chat.send','/app/chat.read'].includes(destination))return false;return this.frame('SEND',{destination,'content-type':'application/json'},JSON.stringify(payload));}
}
