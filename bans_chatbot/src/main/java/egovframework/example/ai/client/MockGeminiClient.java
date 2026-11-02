package egovframework.example.ai.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Offline demonstration adapter; it never opens a network connection. */
@Component
@Profile({"local-mock", "test"})
public class MockGeminiClient implements AiAnswerPort {
    @Override
    public String answerSyntheticQuestion(String syntheticQuestion) {
        return "가상 자료에 대한 모의 AI 응답입니다.";
    }
}
