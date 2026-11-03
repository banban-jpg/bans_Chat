package egovframework.example.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.auth.repository.InMemoryAccountRepository;
import egovframework.example.chat.repository.InMemoryChatRepository;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ChatMockMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder passwords;
    @Autowired InMemoryAccountRepository accounts;
    @Autowired InMemoryChatRepository chat;
    @BeforeEach void reset() {accounts.resetForTest();chat.resetForTest();}
    private MockHttpSession login(String code) throws Exception {
        String password=UUID.randomUUID()+"Aa1!";
        accounts.provisionForTest(code,passwords.encode(password));
        var response=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("employeeCode",code,"password",password))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession)response.getRequest().getSession(false);
    }
    @Test void restRequiresSessionParticipantAndCsrf() throws Exception {
        mvc.perform(get("/api/chat/rooms")).andExpect(status().isUnauthorized());
        var a=login("EMP-001");var b=login("EMP-002");var stranger=login("EMP-005");
        var request=post("/api/chat/rooms").session(a).contentType("application/json")
                .content("{\"recipientEmployeeId\":\"EMP-002\"}");
        mvc.perform(request).andExpect(status().isForbidden());
        var opened=mvc.perform(post("/api/chat/rooms").session(a).with(csrf()).contentType("application/json")
                .content("{\"recipientEmployeeId\":\"EMP-002\"}"))
                .andExpect(status().isOk()).andReturn();
        String id=json.readTree(opened.getResponse().getContentAsString()).path("data").path("conversationId").asText();
        mvc.perform(get("/api/chat/rooms/"+id+"/messages").session(stranger)).andExpect(status().isNotFound());
        mvc.perform(post("/api/chat/rooms/"+id+"/messages").session(a).with(csrf()).contentType("application/json")
                .content("{\"clientRequestId\":\"k1\",\"content\":\"synthetic hello\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.roomSeq").value(1));
        mvc.perform(get("/api/chat/rooms/"+id+"/messages").session(b))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].content").value("synthetic hello"));
        mvc.perform(get("/api/chat/rooms").session(b)).andExpect(jsonPath("$.data[0].unreadCount").value(1));
        mvc.perform(put("/api/chat/rooms/"+id+"/read-state").session(b).with(csrf()).contentType("application/json")
                .content("{\"lastReadSeq\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(1));
        mvc.perform(get("/api/chat/rooms").session(b)).andExpect(jsonPath("$.data[0].unreadCount").value(0));
    }
}
