package egovframework.example.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.auth.domain.CredentialStatus;
import egovframework.example.chat.repository.InMemoryChatRepository;
import java.lang.reflect.Type;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatStompE2ETest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryChatRepository chat;
    @Autowired PasswordEncoder passwords;
    private WebSocketStompClient stomp;

    @BeforeEach void reset() {
        accounts.resetForTest();chat.resetForTest();
        stomp = new WebSocketStompClient(new StandardWebSocketClient());
    }
    @AfterEach void stop() {stomp.stop();}

    private record Browser(HttpClient http, CookieManager cookies, String csrf, String code) { }
    private Browser login(String code) throws Exception {
        String password=UUID.randomUUID()+"Aa1!";
        accounts.provisionForTest(code,passwords.encode(password));
        var cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        var http=HttpClient.newBuilder().cookieHandler(cookies).build();
        var csrfResponse=http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/auth/csrf")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertThat(csrfResponse.statusCode()).isEqualTo(200);
        String csrf=json.readTree(csrfResponse.body()).path("data").path("token").asText();
        String login=json.writeValueAsString(Map.of("employeeCode",code,"password",password));
        var result=http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/auth/login"))
                .header("X-CSRF-TOKEN",csrf).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(login)).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(result.statusCode()).isEqualTo(200);
        return new Browser(http,cookies,csrf,code);
    }
    private StompSession connect(Browser browser,String csrf,String origin) throws Exception {
        var headers=new WebSocketHttpHeaders();
        headers.setOrigin(origin);
        headers.set("Cookie",String.join("; ",browser.cookies.getCookieStore().getCookies().stream().map(HttpCookie::toString).toList()));
        var connect=new StompHeaders();connect.add("X-CSRF-TOKEN",csrf);
        return stomp.connectAsync("ws://127.0.0.1:"+port+"/ws/chat",headers,connect,new StompSessionHandlerAdapter() {})
                .get(5,TimeUnit.SECONDS);
    }
    private static StompFrameHandler capture(BlockingQueue<String> received) {
        return new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {return byte[].class;}
            public void handleFrame(StompHeaders headers,Object payload) {received.add(new String((byte[])payload,StandardCharsets.UTF_8));}
        };
    }
    @Test void twoParticipantsAckOutsiderAndReconnectRecovery() throws Exception {
        var a=login("EMP-001");var b=login("EMP-002");var c=login("EMP-005");
        String origin="http://127.0.0.1:"+port;
        var sa=connect(a,a.csrf,origin);var sb=connect(b,b.csrf,origin);var sc=connect(c,c.csrf,origin);
        var ack=new LinkedBlockingQueue<String>();var messages=new LinkedBlockingQueue<String>();var outsider=new LinkedBlockingQueue<String>();
        sa.subscribe("/user/queue/chat.ack",capture(ack));
        sb.subscribe("/user/queue/chat.messages",capture(messages));
        sc.subscribe("/user/queue/chat.messages",capture(outsider));
        var room=chat.open("EMP-001","EMP-002");long id=Long.parseLong(room.conversationId());
        var send=new StompHeaders();send.setDestination("/app/chat.send");send.setContentType(MediaType.APPLICATION_JSON);
        sendPayload(sa,send,Map.of("roomId",id,"clientRequestId","e2e-1","content","synthetic hello","senderEmployeeId","EMP-005"));
        assertThat(json.readTree(messages.poll(5,TimeUnit.SECONDS)).path("senderEmployeeId").asText()).isEqualTo("EMP-001");
        assertThat(json.readTree(ack.poll(5,TimeUnit.SECONDS)).path("clientRequestId").asText()).isEqualTo("e2e-1");
        sendPayload(sa,send,Map.of("roomId",id,"clientRequestId","e2e-1","content","synthetic hello"));
        assertThat(json.readTree(ack.poll(5,TimeUnit.SECONDS)).path("roomSeq").asLong()).isEqualTo(1);
        assertThat(json.readTree(messages.poll(5,TimeUnit.SECONDS)).path("roomSeq").asLong()).isEqualTo(1);
        assertThat(chat.messages(id,0,20).items()).hasSize(1);
        assertThat(outsider.poll(250,TimeUnit.MILLISECONDS)).isNull();
        var errors=new LinkedBlockingQueue<String>();sc.subscribe("/user/queue/chat.errors",capture(errors));
        sendPayload(sc,send,Map.of("roomId",id,"clientRequestId","outsider","content","blocked"));
        assertThat(json.readTree(errors.poll(5,TimeUnit.SECONDS)).path("code").asText()).isEqualTo("NOT_FOUND");
        sb.disconnect();
        sendPayload(sa,send,Map.of("roomId",id,"clientRequestId","e2e-2","content","missed"));
        var recovered=b.http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/chat/rooms/"+id+"/messages?beforeSeq=0&size=20")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertThat(recovered.statusCode()).isEqualTo(200);
        assertThat(json.readTree(recovered.body()).path("data").path("items").size()).isEqualTo(2);
        var sb2=connect(b,b.csrf,origin);
        var afterLogout=new LinkedBlockingQueue<String>();
        sb2.subscribe("/user/queue/chat.messages",capture(afterLogout));
        var logout=b.http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/auth/logout"))
                .header("X-CSRF-TOKEN",b.csrf).POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(logout.statusCode()).isEqualTo(200);
        sendPayload(sa,send,Map.of("roomId",id,"clientRequestId","e2e-3","content","after logout"));
        assertThat(afterLogout.poll(400,TimeUnit.MILLISECONDS)).isNull();
        // Logout may close the WebSocket before explicit cleanup.
        try {sb2.disconnect();} catch (IllegalStateException alreadyClosed) { }
        sa.disconnect();sc.disconnect();
    }
    private void sendPayload(StompSession session,StompHeaders headers,Object payload) throws Exception {
        session.send(headers,json.writeValueAsBytes(payload));
    }
    @Test void csrfAndOriginRejectConnect() throws Exception {
        var a=login("EMP-001");String origin="http://127.0.0.1:"+port;
        assertThatThrownBy(()->connect(a,"wrong",origin)).isInstanceOf(Exception.class);
        assertThatThrownBy(()->connect(a,a.csrf,"https://elsewhere.example")).isInstanceOf(Exception.class);
    }
    @Test void suspendedAccountCannotReceiveQueuedMessage() throws Exception {
        var a=login("EMP-001");var b=login("EMP-002");
        String origin="http://127.0.0.1:"+port;
        var sa=connect(a,a.csrf,origin);var sb=connect(b,b.csrf,origin);
        var received=new LinkedBlockingQueue<String>();
        sb.subscribe("/user/queue/chat.messages",capture(received));
        long id=Long.parseLong(chat.open("EMP-001","EMP-002").conversationId());
        accounts.setStatusForTest("EMP-002",CredentialStatus.SUSPENDED);
        var send=new StompHeaders();send.setDestination("/app/chat.send");send.setContentType(MediaType.APPLICATION_JSON);
        sendPayload(sa,send,Map.of("roomId",id,"clientRequestId","suspended-test","content","synthetic only"));
        assertThat(received.poll(400,TimeUnit.MILLISECONDS)).isNull();
        sa.disconnect();
        try {sb.disconnect();} catch (IllegalStateException alreadyClosed) { }
    }
}
