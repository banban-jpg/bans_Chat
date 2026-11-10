package egovframework.example.ai.provider;
import java.net.*;
import java.net.http.HttpClient;
import java.time.*;
import java.util.List;
/** Test-only endpoint seam. Production endpoint remains fixed. */
public final class LoopbackProviderFactory {
    public static GeminiProvider create(URI uri,String key){
        return create(uri,key,Duration.ofSeconds(2));
    }
    public static GeminiProvider create(URI uri,String key,Duration timeout){
        var http=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).proxy(new ProxySelector(){
            public List<Proxy> select(URI target){return List.of(Proxy.NO_PROXY);}
            public void connectFailed(URI uri,SocketAddress address,java.io.IOException error){}
        }).build();
        return new GeminiProvider(CallBudgetTest.settings(10,100,timeout),key,Clock.systemUTC(),http,uri);
    }
}
