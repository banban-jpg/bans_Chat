package egovframework.example.chat.service;
import egovframework.example.chat.repository.InMemoryChatRepository;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MockConcurrencyRecoveryTest {
 @Test void thirtyWorkersReusePairDeduplicateAndNeverRegressRead() throws Exception {
  var repository=new InMemoryChatRepository();var start=new CountDownLatch(1);
  try(var workers=Executors.newVirtualThreadPerTaskExecutor()){
   var tasks=new ArrayList<Future<Long>>();
   for(int i=0;i<30;i++){int key=i%10;tasks.add(workers.submit(()->{start.await();long room=Long.parseLong(repository.open("EMP-001","EMP-002").conversationId());return repository.send(room,"EMP-001","concurrent-"+key,"Synthetic message").roomSeq();}));}
   start.countDown();var sequences=new HashSet<Long>();for(var task:tasks)sequences.add(task.get(5,TimeUnit.SECONDS));
   assertThat(sequences).containsExactlyInAnyOrder(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);
   long id=Long.parseLong(repository.open("EMP-001","EMP-002").conversationId());
   var high=workers.submit(()->repository.read(id,"EMP-002",10));var low=workers.submit(()->repository.read(id,"EMP-002",0));high.get();low.get();
   assertThat(repository.messages(id,0,100).items()).hasSize(10);assertThat(repository.rooms("EMP-002").getFirst().lastReadSeq()).isEqualTo(10);
  }
 }
 @Test void constructingNewMockStoreLosesAllSavedStateRatherThanPretendingDurability(){
  var original=new InMemoryChatRepository();long id=Long.parseLong(original.open("EMP-001","EMP-002").conversationId());
  original.send(id,"EMP-001","before-restart","Synthetic data");
  var restarted=new InMemoryChatRepository();assertThat(restarted.room(id)).isNull();assertThat(restarted.rooms("EMP-001")).isEmpty();
  // Object replacement models ephemeral storage loss. This is not an OS/process/Oracle recovery test.
 }
}
