package egovframework.example.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="BANS_MOCK_LOAD_TEST",matches="YES")
class ProviderFailureExperimentTest {
 static final String VALID="{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"Synthetic hello\"}]}}]}";
 @Test @Timeout(30) void measureLoopbackFailuresAndConcurrencyRejection() throws Exception {
  var report=new LinkedHashMap<String,Object>();report.put("label","Mock 환경 결과: DB 포함 안 됨");report.put("started",Instant.now().toString());
  for(String mode:List.of("429","503","malformed","timeout")){
   var timings=new ArrayList<Double>();int requests=0;
   for(int repeat=0;repeat<3;repeat++){
    var calls=new AtomicInteger();var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/",exchange->{calls.incrementAndGet();try(exchange){
     if(mode.equals("timeout"))try{new CountDownLatch(1).await(400,TimeUnit.MILLISECONDS);}catch(InterruptedException ignored){Thread.currentThread().interrupt();}
     byte[] bytes=(mode.equals("malformed")?"{broken":VALID).getBytes(java.nio.charset.StandardCharsets.UTF_8);
     exchange.getResponseHeaders().set("Content-Type","application/json");exchange.getResponseHeaders().set("Retry-After","1");
     exchange.sendResponseHeaders(mode.equals("429")?429:mode.equals("503")?503:200,bytes.length);exchange.getResponseBody().write(bytes);
    }catch(java.io.IOException closed){/* Timed-out client may disconnect. No request/exception logging. */}});server.start();
    try(var provider=LoopbackProviderFactory.create(URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/"),"FAKE_LOAD_KEY",mode.equals("timeout")?Duration.ofMillis(150):Duration.ofSeconds(2))){
     long start=System.nanoTime();var failure=catchThrowableOfType(()->provider.chat(ProviderConfigurationTest.actor(),ChatProvider.SyntheticPrompt.HELLO_EN),ProviderFailure.class);timings.add((System.nanoTime()-start)/1_000_000.0);
     assertThat(failure).isNotNull();assertThat(failure.status().value()).isEqualTo(mode.equals("429")?429:mode.equals("503")?503:mode.equals("timeout")?504:502);
     if(mode.equals("429")||mode.equals("503"))assertThatThrownBy(()->provider.chat(ProviderConfigurationTest.actor(),ChatProvider.SyntheticPrompt.HELLO_EN)).hasMessage("AI_COOLDOWN");
     assertThat(calls.get()).isEqualTo(1);requests+=calls.get();
    }finally{server.stop(0);}
   }
   Collections.sort(timings);report.put(mode,Map.of("samples",3,"p50Ms",timings.get(1),"p95Ms",timings.get(2),"expectedFailureRate",1.0,"wireRequests",requests,"automaticRetries",0));
  }
  var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var calls=new AtomicInteger();
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",exchange->{calls.incrementAndGet();entered.countDown();try(exchange){try{release.await(2,TimeUnit.SECONDS);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();}byte[] bytes=VALID.getBytes();exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);}});server.start();
  try(var provider=LoopbackProviderFactory.create(URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/"),"FAKE_LOAD_KEY");var workers=Executors.newVirtualThreadPerTaskExecutor()){
   var first=workers.submit(()->provider.chat(ProviderConfigurationTest.actor(),ChatProvider.SyntheticPrompt.HELLO_EN));assertThat(entered.await(1,TimeUnit.SECONDS)).isTrue();
   var rejected=new ArrayList<Future<Double>>();for(int i=0;i<19;i++)rejected.add(workers.submit(()->{long start=System.nanoTime();assertThatThrownBy(()->provider.chat(ProviderConfigurationTest.actor(),ChatProvider.SyntheticPrompt.HELLO_EN)).hasMessage("AI_BUSY");return (System.nanoTime()-start)/1_000_000.0;}));
   var times=new ArrayList<Double>();for(var task:rejected)times.add(task.get(1,TimeUnit.SECONDS));release.countDown();assertThat(first.get(2,TimeUnit.SECONDS).mock()).isFalse();assertThat(calls.get()).isEqualTo(1);Collections.sort(times);
   report.put("busyBurst",Map.of("concurrentAttempts",20,"wireRequests",1,"busyRejections",19,"p50RejectedMs",times.get(9),"p95RejectedMs",times.get(18)));
  }finally{release.countDown();server.stop(0);}
  report.put("realGeminiCalls",0);report.put("realOracleConnections",0);report.put("finished",Instant.now().toString());
  Files.createDirectories(Path.of("local-notes"));new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Path.of("local-notes/step17-failures.json").toFile(),report);
 }
}
