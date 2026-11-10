package egovframework.example.ai.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Offline demonstration adapter; it never opens a network connection. */
@Component
@Profile({"test"})
public class MockGeminiClient implements AiAnswerPort {
    @Override
    public String answerSyntheticQuestion(String syntheticQuestion) {
        // Legacy arbitrary-string port stays offline; never delegate it to a live provider.
        return egovframework.example.ai.provider.MockGeminiProvider.REPLY;
    }
}
