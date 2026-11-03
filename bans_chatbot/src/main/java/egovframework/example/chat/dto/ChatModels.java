package egovframework.example.chat.dto;
import jakarta.validation.constraints.*;
import java.util.List;
public final class ChatModels {
 private ChatModels() {}
 public record Open(@NotBlank String recipientEmployeeId) {}
 public record Send(@NotBlank @Size(max=80) String clientRequestId, @NotBlank String content) {}
 public record Read(@Min(0) long lastReadSeq) {}
 public record Message(long roomSeq,String senderEmployeeId,String content) {}
 public record Room(long roomId,String otherEmployeeId,long lastRoomSeq,long lastReadSeq,long unreadCount) {}
 public record Messages(List<Message> items,long nextBeforeSeq) {}
}
