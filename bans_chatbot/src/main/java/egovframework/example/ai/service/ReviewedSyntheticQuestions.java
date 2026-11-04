package egovframework.example.ai.service;

import egovframework.example.common.exception.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Exact server-reviewed invented examples, not regex-based certification of arbitrary user text. */
@Component
public class ReviewedSyntheticQuestions implements QuestionSafetyReview {
    public static final String NOTICE = "실제 AI를 활성화하면 질문 원문도 외부로 전송될 수 있습니다. 실제 업무자료·개인정보·기밀을 입력하지 마세요. 이 화면은 서버가 검토한 합성·비민감 예제만 Mock으로 처리합니다.";
    public record Example(String id, String text) { }
    private static final List<Example> EXAMPLES = List.of(
            new Example("VACATION_HELP", "가상 회사의 휴가 신청 안내를 찾아주세요."),
            new Example("DEPLOYMENT_HELP", "가상 개발부의 배포 안내를 찾아주세요."),
            new Example("DIRECTORY_HELP", "가상 회사의 업무 담당자를 찾아주세요."));
    public List<Example> examples() { return EXAMPLES; }
    @Override public boolean permitsExternalTransfer(String question) {
        return EXAMPLES.stream().anyMatch(example -> example.text().equals(question));
    }
    public void requireReviewed(String id, String question, boolean acknowledged) {
        if (!acknowledged || EXAMPLES.stream().noneMatch(example -> example.id().equals(id) && example.text().equals(question))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "AI_QUESTION_NOT_REVIEWED", "Only reviewed synthetic examples are permitted. Question text may leave the server.");
        }
    }
}
