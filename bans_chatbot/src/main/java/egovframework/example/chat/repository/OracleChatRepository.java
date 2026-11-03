package egovframework.example.chat.repository;
import egovframework.example.chat.domain.DirectConversation;
import egovframework.example.chat.dto.ChatModels.*;
import egovframework.example.chat.mapper.ChatMapper;
import egovframework.example.common.exception.ApiException;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository @Profile("oracle")
public class OracleChatRepository implements ChatRepository {
 private final ChatMapper mapper;
 public OracleChatRepository(ChatMapper mapper) { this.mapper=mapper; }
 private DirectConversation convert(ChatMapper.RoomRow r) { return r==null?null:new DirectConversation(Long.toString(r.roomId()),r.low(),r.high()); }
 @Transactional public DirectConversation open(String low,String high) {
  var found=mapper.byPair(low,high);if(found!=null)return convert(found);
  try { mapper.insertRoom(low,high); }
  catch(DuplicateKeyException race) { // UNIQUE pair: concurrent creator won.
   found=mapper.byPair(low,high);if(found==null)throw race;return convert(found);
  }
  found=mapper.byPair(low,high);if(found==null)throw new IllegalStateException("CHAT_ROOM_INSERT_FAILED");
  if(mapper.insertReadStates(found.roomId())!=2)throw new IllegalStateException("CHAT_READ_INIT_FAILED");
  return convert(found);
 }
 public DirectConversation room(long id) { return convert(mapper.byId(id)); }
 public List<Room> rooms(String actor) { return mapper.rooms(actor).stream().map(r->new Room(r.roomId(),r.otherEmployeeId(),r.lastRoomSeq(),r.lastReadSeq(),r.unreadCount())).toList(); }
 public Messages messages(long id,long before,int size) {
  var rows=mapper.messages(id,before,size).stream().map(r->new Message(r.roomSeq(),r.senderEmployeeId(),r.content())).toList();
  return new Messages(rows,rows.isEmpty()?0:rows.getFirst().roomSeq());
 }
 @Transactional public Message send(long id,String sender,String key,String content) {
  var locked=mapper.lockRoom(id);if(locked==null)throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Room unavailable.");
  var prior=mapper.byRequest(sender,key);
  if(prior!=null) {
   var priorRoom=mapper.requestRoom(sender,key);
   if(priorRoom!=id || !prior.content().equals(content))throw new ApiException(HttpStatus.CONFLICT,"CHAT_REQUEST_CONFLICT","Request ID already used.");
   return new Message(prior.roomSeq(),prior.senderEmployeeId(),prior.content());
  }
  if(mapper.increment(id)!=1)throw new IllegalStateException("CHAT_SEQ_UPDATE_FAILED");
  long seq=locked.lastRoomSeq()+1;
  try { if(mapper.insertMessage(id,seq,sender,key,content)!=1)throw new IllegalStateException("CHAT_INSERT_FAILED"); }
  catch(DuplicateKeyException duplicate) { throw new ApiException(HttpStatus.CONFLICT,"CHAT_REQUEST_CONFLICT","Request ID already used."); }
  return new Message(seq,sender,content);
 }
 @Transactional public long read(long id,String actor,long seq) {
  var locked=mapper.lockRoom(id);if(locked==null)throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Room unavailable.");
  if(seq>locked.lastRoomSeq())throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","Invalid read position.");
  if(mapper.updateRead(id,actor,seq)!=1)throw new IllegalStateException("CHAT_READ_UPDATE_FAILED");
  return mapper.lastRead(id,actor);
 }
}
