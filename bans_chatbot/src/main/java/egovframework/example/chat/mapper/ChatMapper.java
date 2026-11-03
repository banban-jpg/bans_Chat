package egovframework.example.chat.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface ChatMapper {
 record RoomRow(long roomId,String low,String high,long lastRoomSeq) {}
 record MessageRow(long roomSeq,String senderEmployeeId,String content) {}
 record SummaryRow(long roomId,String otherEmployeeId,long lastRoomSeq,long lastReadSeq,long unreadCount) {}
 RoomRow byPair(@Param("low") String low,@Param("high") String high);
 RoomRow byId(@Param("id") long id);
 RoomRow lockRoom(@Param("id") long id);
 int insertRoom(@Param("low") String low,@Param("high") String high);
 int insertReadStates(@Param("id") long id);
 List<SummaryRow> rooms(@Param("actor") String actor);
 List<MessageRow> messages(@Param("id") long id,@Param("before") long before,@Param("size") int size);
 MessageRow byRequest(@Param("sender") String sender,@Param("key") String key);
 Long requestRoom(@Param("sender") String sender,@Param("key") String key);
 int increment(@Param("id") long id);
 int insertMessage(@Param("id") long id,@Param("seq") long seq,@Param("sender") String sender,@Param("key") String key,@Param("content") String content);
 Long lastRead(@Param("id") long id,@Param("actor") String actor);
 int updateRead(@Param("id") long id,@Param("actor") String actor,@Param("seq") long seq);
}
