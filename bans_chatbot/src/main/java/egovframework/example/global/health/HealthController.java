package egovframework.example.global.health;

import egovframework.example.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> apiHealth() {
        return ApiResponse.of(Map.of("status", "UP"));
    }
}
