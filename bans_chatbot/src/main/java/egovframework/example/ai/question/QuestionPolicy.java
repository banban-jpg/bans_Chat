package egovframework.example.ai.question;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import egovframework.example.common.exception.ApiException;
import egovframework.example.organization.fixture.SyntheticOrganizationFixture;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Closed, reviewed synthetic intents. Unknown text is never sent to a classifier/model. */
@Component
public class QuestionPolicy {
    public static final int MAX_BYTES = 2048, MAX_QUESTION = 200;
    public static final String NOTICE = "질문 원문도 실제 AI 사용 시 Google로 전송될 수 있습니다. 실제 업무자료·개인정보·기밀을 입력하지 마세요. 검토된 가상 예제만 처리하며 대화 원문은 저장하지 않습니다.";
    public enum Kind { DOCUMENT, TASK_OWNER, CONTACT }
    public record Intent(Kind kind, String canonicalQuestion, String query) {
        @Override public String toString() { return "QuestionIntent[kind=" + kind + ", text=<redacted>]"; }
    }
    private static final Map<String, Intent> REVIEWED = Map.ofEntries(
            doc("연차 신청 절차", "휴가 신청"), doc("연차 신청 절차 알려줘", "휴가 신청"),
            doc("가상 회사의 휴가 신청 안내를 찾아주세요.", "휴가 신청"),
            doc("배포 절차", "배포 체크리스트"), doc("가상 개발부의 배포 안내를 찾아주세요.", "배포 체크리스트"),
            doc("폐기된 장비 안내", "구형 장비 안내"), doc("비존재 규정", "비존재 규정"),
            doc("가상 순간이동 규정", "순간이동"),
            Map.entry("출장비 담당자", new Intent(Kind.TASK_OWNER, "출장비 담당자", "정산")),
            Map.entry("출장비 담당자를 알려주세요.", new Intent(Kind.TASK_OWNER, "출장비 담당자를 알려주세요.", "정산")));
    private final ObjectMapper json = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(4).maxStringLength(512).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static Map.Entry<String, Intent> doc(String question, String query) {
        return Map.entry(question, new Intent(Kind.DOCUMENT, question, query));
    }
    public String parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) throw invalid();
        try {
            var root = json.readTree(bytes);
            if (root == null || !root.isObject()) throw invalid();
            Set<String> allowed = Set.of("question", "callerId", "userId", "employeeId", "role", "roles", "departmentId");
            root.fieldNames().forEachRemaining(key -> { if (!allowed.contains(key)) throw invalid(); });
            var question = root.path("question");
            if (!question.isTextual()) throw invalid(); String value = question.textValue();
            if (value.isBlank() || value.codePointCount(0, value.length()) > MAX_QUESTION
                    || value.chars().anyMatch(Character::isISOControl)) throw invalid();
            return value;
        } catch (ApiException safe) { throw safe; }
        catch (Exception discarded) { throw invalid(); }
    }
    public Optional<Intent> review(String question) {
        if (question == null) return Optional.empty();
        var known = REVIEWED.get(question); if (known != null) return Optional.of(known);
        return SyntheticOrganizationFixture.employees().stream()
                .filter(e -> (e.displayName() + " 연락처").equals(question))
                .findFirst().map(e -> new Intent(Kind.CONTACT, e.displayName() + " 연락처", e.displayName()));
    }
    public List<String> examples() { return List.of("연차 신청 절차", "출장비 담당자", "가상인사02 연락처", "배포 절차", "비존재 규정"); }
    static ApiException invalid() { return new ApiException(HttpStatus.BAD_REQUEST, "AI_QUESTION_INPUT_INVALID", "Invalid question request."); }
}
