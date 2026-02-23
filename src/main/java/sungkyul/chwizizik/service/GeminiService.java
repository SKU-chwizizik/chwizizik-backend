package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    // 면접 대화 내용 저장소
    private List<String> conversationHistory = new ArrayList<>();

    // 페르소나 설정
    public String getInitialQuestion() {
        conversationHistory.clear();
        String prompt = "당신은 23년 차 베테랑 임원 면접관 '박 부장'입니다.\n" +
                        "지침:\n" +
                        "1. 정중하고 무게감 있게 말하세요.\n" +
                        "2. 음성 출력을 고려해 2문장 이내로 간결하게 첫 질문을 던지세요.\n" +
                        "3. (서류를 보며)와 같은 행동 묘사를 섞어주세요.";
        
        String response = callGeminiApi(prompt);
        if (response != null) {
            conversationHistory.add("면접관: " + response);
        }
        return response;
    }

    // 대화 규칙과 조건
    public String getNextQuestion(String userResponse) {
        conversationHistory.add("지원자: " + userResponse);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("당신은 면접관 '박 부장'입니다. 아래 대화 흐름을 바탕으로 날카로운 질문을 던지세요.\n\n");
        for (String history : conversationHistory) {
            promptBuilder.append(history).append("\n");
        }
        promptBuilder.append("\n[지침]\n")
                     .append("- 답변은 2~3문장 이내로 간결하게 할 것.\n")
                     .append("- 지원자의 이전 답변을 논리적으로 파고드는 꼬리 질문을 할 것.\n")
                     .append("- 면접 종료 시 정중한 인사 뒤에 [면접 종료] 태그를 붙일 것.");

        String response = callGeminiApi(promptBuilder.toString());
        if (response != null) {
            conversationHistory.add("면접관: " + response);
        }
        return response;
    }

    private String callGeminiApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        // 💡 trim() 사용으로 apikey에 대한 공백 에러 방지 코드
        String urlWithKey = apiUrl.trim() + "?key=" + apiKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(urlWithKey, entity, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> resContent = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
            return (String) parts.get(0).get("text");
        } catch (HttpClientErrorException e) {
            // 에러 발생 시 터미널에 문제 출력
            System.err.println("\n[ERROR] API 호출 실패: " + e.getStatusCode());
            System.err.println("에러 내용: " + e.getResponseBodyAsString());
            return "(면접관이 잠시 자리를 비웠습니다. 다시 시도해 주세요.)";
        } catch (Exception e) {
            return "(연결 오류 발생) " + e.getMessage();
        }
    }
}