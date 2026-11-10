package egovframework.example.chat.repository;
import egovframework.example.chat.domain.DirectConversation;
import egovframework.example.chat.dto.ChatModels.*;
import egovframework.example.common.exception.ApiException;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
@Repository @Profile({"test"})
public class InMemoryChatRepository implements ChatRepository {
 private static final class State {
  final DirectConversation room; long activity; final List<Message> messages=new ArrayList<>(); final Map<String,Long> read=new HashMap<>();
  State(DirectConversation room) { this.room=room; read.put(room.firstEmployeeId(),0L); read.put(room.secondEmployeeId(),0L); }
 }
 private final Map<Long,State> byId=new HashMap<>();
 private final Map<String,Long> byPair=new HashMap<>();
 private final Map<String,Map.Entry<Long,Message>> byRequest=new HashMap<>();
 private long nextId=1; private long nextActivity=1;
 public synchronized void resetForTest() { byId.clear(); byPair.clear(); byRequest.clear(); nextId=1; nextActivity=1; }
 public synchronized DirectConversation open(String low,String high) {
  String pair=low+":"+high;
  Long existing=byPair.get(pair); if(existing!=null) return byId.get(existing).room;
  long id=nextId++; var room=new DirectConversation(Long.toString(id),low,high); byId.put(id,new State(room));byPair.put(pair,id);return room;
 }
 public synchronized DirectConversation room(long id) { var state=byId.get(id);return state==null?null:state.room; }
 public synchronized List<Room> rooms(String actor) {
  return byId.entrySet().stream().filter(e->e.getValue().read.containsKey(actor)).map(e->{var s=e.getValue();long last=s.messages.size();long read=s.read.get(actor);long unread=s.messages.stream().filter(m->m.roomSeq()>read && !m.senderEmployeeId().equals(actor)).count();String other=s.room.firstEmployeeId().equals(actor)?s.room.secondEmployeeId():s.room.firstEmployeeId();return new Room(e.getKey(),other,last,read,unread);}).sorted(Comparator.<Room>comparingLong(r->byId.get(r.roomId()).activity).reversed().thenComparing(Comparator.comparingLong(Room::roomId).reversed())).toList();
 }
 public synchronized Messages messages(long id,long before,int size) {
  var all=byId.get(id).messages;long end=before==0?all.size():Math.min(before-1,all.size());
  var items=all.stream().filter(m->m.roomSeq()<=end).skip(Math.max(0,end-size)).toList();
  return new Messages(items,items.isEmpty()?0:items.getFirst().roomSeq());
 }
 public synchronized Message send(long id,String sender,String key,String content) {
  String unique=sender+":"+key;var prior=byRequest.get(unique);
  if(prior!=null) {
   if(prior.getKey()!=id || !prior.getValue().content().equals(content)) throw new ApiException(HttpStatus.CONFLICT,"CHAT_REQUEST_CONFLICT","Request ID already used.");
   return prior.getValue();
  }
  var state=byId.get(id);var message=new Message(state.messages.size()+1,sender,content);
  state.messages.add(message);state.activity=nextActivity++;byRequest.put(unique,Map.entry(id,message));return message;
 }
 public synchronized long read(long id,String actor,long seq) {
  var state=byId.get(id);
  if(seq>state.messages.size()) throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","Invalid read position.");
  return state.read.merge(actor,seq,Math::max);
 }
}
