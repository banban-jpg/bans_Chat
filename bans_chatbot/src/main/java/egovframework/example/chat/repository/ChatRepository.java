package egovframework.example.chat.repository;
import egovframework.example.chat.dto.ChatModels.*;
import egovframework.example.chat.domain.DirectConversation;
import java.util.List;
public interface ChatRepository {
 DirectConversation open(String low,String high);
 DirectConversation room(long id);
 List<Room> rooms(String actor);
 Messages messages(long id,long beforeSeq,int size);
 Message send(long id,String sender,String requestId,String content);
 long read(long id,String actor,long seq);
}
