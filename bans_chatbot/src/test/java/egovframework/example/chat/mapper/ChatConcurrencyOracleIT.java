package egovframework.example.chat.mapper;
import egovframework.example.chat.repository.ChatRepository;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

/** Commits through repository proxies on independent worker transactions. Dedicated disposable schema only. */
@Tag("oracle-integration")
@EnabledIfEnvironmentVariable(named="ORACLE_LOAD_TEST",matches="YES")
@EnabledIfEnvironmentVariable(named="ORACLE_LOAD_WRITES_ACK",matches="YES")
@SpringBootTest @ActiveProfiles({"oracle","oracle-it"})
class ChatConcurrencyOracleIT {
 @Autowired ChatRepository repository;
 @Test @Timeout(90) void simultaneousDuplicateRequestsAllocateExactlyTenSequences() throws Exception {
  long id=Long.parseLong(repository.open("EMP-001","EMP-002").conversationId());
  var before=repository.messages(id,0,1).items();long head=before.isEmpty()?0:before.getLast().roomSeq();
  String run=UUID.randomUUID().toString();var start=new CountDownLatch(1);
  try(var workers=Executors.newVirtualThreadPerTaskExecutor()){
   var tasks=new ArrayList<Future<Long>>();
   for(int i=0;i<20;i++){int key=i/2;tasks.add(workers.submit(()->{start.await();return repository.send(id,"EMP-001","load-"+run+"-"+key,"Synthetic concurrency data").roomSeq();}));}
   start.countDown();var sequences=new HashSet<Long>();for(var task:tasks)sequences.add(task.get(60,TimeUnit.SECONDS));
   assertThat(sequences).hasSize(10);assertThat(Collections.min(sequences)).isEqualTo(head+1);assertThat(Collections.max(sequences)).isEqualTo(head+10);
   var high=workers.submit(()->repository.read(id,"EMP-002",head+10));var low=workers.submit(()->repository.read(id,"EMP-002",0));high.get(30,TimeUnit.SECONDS);low.get(30,TimeUnit.SECONDS);
   assertThat(repository.rooms("EMP-002").stream().filter(room->room.roomId()==id).findFirst().orElseThrow().lastReadSeq()).isEqualTo(head+10);
  }
  // Writes are intentionally committed and retained. No automatic deletion or schema reset.
 }
}
