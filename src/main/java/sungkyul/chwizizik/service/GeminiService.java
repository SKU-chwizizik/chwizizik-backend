package sungkyul.chwizizik.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private int turnCount = 0; // 면접 질문 횟수 추적
    private final int MAX_TURNS = 5; // 실제 면접 수준인 5~6회로 설정

    // 시스템 프롬프트
    private final String SYSTEM_PROMPT = 
        "당신은 깐깐하고 보수적인 면접관 '박 부장'입니다. 다음 지침을 엄격히 따르세요:\n" +
        "1. 지원자의 역량을 파악하기 위해 날카로운 꼬리 질문을 던지세요.\n" +
        "2. 질문은 한 번에 하나씩만 하세요.\n" +
        "3. 대화 횟수가 부족하면 절대 면접을 끝내지 마세요.\n" +
        "4. 마지막 질문이 끝난 후에는 '수고하셨습니다. 면접을 마칩니다.'라고 말하고 마지막에 반드시 [면접 종료]를 붙이세요.";

    public String getInitialQuestion() {
        turnCount = 0; 
        return "반갑습니다, 지원자님. 우리 회사의 핵심 가치와 본인의 기술적 강점이 어떻게 맞닿아 있는지 구체적인 사례를 들어 말씀해 주시겠습니까?";
    }

    public String getNextQuestion(String userResponse) {
        turnCount++;

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT + "\n현재 질문 순서: " + turnCount + "/" + MAX_TURNS + "\n지원자 답변: " + userResponse)))
            )
        );

        try {
            String url = apiUrl + "?key=" + apiKey;
            Map<?, ?> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                List<?> parts = (List<?>) content.get("parts");
                Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                
                String aiText = (String) firstPart.get("text");

                // 강제 종료 방지 로직
                if (turnCount < MAX_TURNS) {
                    aiText = aiText.replace("[면접 종료]", "").trim();
                    if (!aiText.endsWith("?")) {
                        aiText += "\n추가로, 이 부분에 대해 더 자세히 설명해 주시겠습니까?";
                    }
                } else if (!aiText.contains("[면접 종료]")) {
                    aiText += "\n\n오늘 면접은 여기까지 하겠습니다. 수고하셨습니다. [면접 종료]";
                }

                return aiText;
            }
            return "박 부장님이 서류를 검토 중입니다. 다시 답변해 주세요.";
        } catch (Exception e) {
            return "연결 오류가 발생했습니다: " + e.getMessage();
        }
    }
}