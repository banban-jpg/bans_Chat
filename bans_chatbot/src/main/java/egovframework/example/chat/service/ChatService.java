package egovframework.example.chat.service;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.repository.AccountRepository;
import egovframework.example.chat.domain.DirectConversation;
import egovframework.example.chat.dto.ChatModels.*;
import egovframework.example.chat.repository.ChatRepository;
import egovframework.example.common.exception.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
@Service
public class ChatService {
 private final ChatRepository repository;
 private final AccountRepository accounts;
 private final ConversationAccessPolicy access;
 public ChatService(ChatRepository repository,AccountRepository accounts,ConversationAccessPolicy access) { this.repository=repository; this.accounts=accounts; this.access=access; }
 private ApiException error(HttpStatus status,String code) { return new ApiException(status,code,"Chat request could not be completed."); }
 private void active(SessionActor actor) { if(actor==null || !actor.activeEmployee()) throw error(HttpStatus.UNAUTHORIZED,"AUTH_REQUIRED"); }
 private DirectConversation require(SessionActor actor,long id) {
  active(actor);
  if(id<1) throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");
  var room=repository.room(id);
  if(!access.canRead(actor,room)) throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");
  return room;
 }
 public DirectConversation room(SessionActor actor,long id) { return require(actor,id); }
 public DirectConversation open(SessionActor actor,String recipient) {
  active(actor);
  if(recipient==null || !recipient.matches("EMP-[0-9]{3,10}") || actor.employeeId().equals(recipient)) throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  var current=accounts.find(recipient);
  if(current.isEmpty() || !current.get().eligible()) throw error(HttpStatus.NOT_FOUND,"NOT_FOUND");
  var pair=List.of(actor.employeeId(),recipient).stream().sorted().toList();
  return repository.open(pair.get(0),pair.get(1));
 }
 public List<Room> rooms(SessionActor actor) { active(actor); return repository.rooms(actor.employeeId()); }
 public Messages messages(SessionActor actor,long id,long beforeSeq,int size) {
  require(actor,id);
  if(beforeSeq<0 || size<1 || size>100) throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  return repository.messages(id,beforeSeq,size);
 }
 public Message send(SessionActor actor,long id,String key,String content) {
  require(actor,id);
  if(key==null || key.length()<1 || key.length()>80 || !key.matches("[A-Za-z0-9._:-]+") || content==null || content.isBlank() || content.codePointCount(0,content.length())>2000 || content.indexOf('\u0000')>=0) throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  return repository.send(id,actor.employeeId(),key,content);
 }
 public long read(SessionActor actor,long id,long seq) {
  require(actor,id);
  if(seq<0) throw error(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED");
  return repository.read(id,actor.employeeId(),seq);
 }
}
