package egovframework.example.chat.controller;
import egovframework.example.chat.dto.ChatModels.*;
import egovframework.example.chat.domain.DirectConversation;
import egovframework.example.chat.service.ChatService;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import egovframework.example.common.response.ApiResponse;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.env.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/chat/rooms")
public class ChatController {
 private final ChatService service; private final Environment environment;
 public ChatController(ChatService service,Environment environment) { this.service=service; this.environment=environment; }
 @ModelAttribute void label(HttpServletResponse response) { if(environment.acceptsProfiles(Profiles.of("local-mock"))) response.setHeader("X-Demo-Data","SYNTHETIC_EPHEMERAL"); }
 private SessionActor actor(HttpServletRequest request) { return (SessionActor)request.getAttribute(SessionBoundaryInterceptor.SESSION_ACTOR); }
 @PostMapping public ApiResponse<DirectConversation> open(HttpServletRequest request,@Valid @RequestBody Open body) { return ApiResponse.of(service.open(actor(request),body.recipientEmployeeId())); }
 @GetMapping public ApiResponse<List<Room>> rooms(HttpServletRequest request) { return ApiResponse.of(service.rooms(actor(request))); }
 @GetMapping("/{id}/messages") public ApiResponse<Messages> messages(HttpServletRequest request,@PathVariable long id,@RequestParam(defaultValue="0") long beforeSeq,@RequestParam(defaultValue="20") int size) { return ApiResponse.of(service.messages(actor(request),id,beforeSeq,size)); }
 @PostMapping("/{id}/messages") public ApiResponse<Message> send(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody Send body) { return ApiResponse.of(service.send(actor(request),id,body.clientRequestId(),body.content())); }
 @PutMapping("/{id}/read-state") public ApiResponse<Long> read(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody Read body) { return ApiResponse.of(service.read(actor(request),id,body.lastReadSeq())); }
}
