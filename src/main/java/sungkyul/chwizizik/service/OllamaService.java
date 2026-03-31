package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sungkyul.chwizizik.config.InterviewPrompts;
import sungkyul.chwizizik.entity.Interview;
import sungkyul.chwizizik.entity.Question;
import sungkyul.chwizizik.repository.InterviewRepository;
import sungkyul.chwizizik.repository.QuestionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OllamaService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:solar}")
    private String ollamaModel;

    private final RestTemplate restTemplate;
    private final InterviewPrompts interviewPrompts;
    private final QuestionRepository questionRepository;
    private final InterviewRepository interviewRepository;

    public String getInitialQuestion(String type) {
        return interviewPrompts.getInitialQuestion(type);
    }

    @Transactional
    public Map<String, Object> getNextQuestion(Long interviewId, String userResponse, String lastQuestion, String type) {
        Map<String, Object> requestBody = Map.of(
            "model", ollamaModel,
            "messages", List.of(
                Map.of("role", "system", "content", interviewPrompts.getPrompt(type)),
                Map.of("role", "user", "content", "면접관의 이전 질문: " + lastQuestion + "\n지원자 답변: " + userResponse)
            ),
            "stream", false
        );

        String nextQuestion = "잠시 통신이 원활하지 않네요. 다시 한 번 말씀해 주시겠어요?";

        try {
            Map<?, ?> response = restTemplate.postForObject(ollamaUrl + "/api/chat", requestBody, Map.class);
            if (response != null && response.containsKey("message")) {
                nextQuestion = (String) ((Map<?, ?>) response.get("message")).get("content");
            }
        } catch (Exception e) {
            nextQuestion = "연결 오류가 발생했습니다: " + e.getMessage();
        }

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 면접 세션을 찾을 수 없습니다."));

        boolean isFinished = nextQuestion.contains("[면접 종료]");
        if (isFinished) {
            nextQuestion = nextQuestion.replace("[면접 종료]", "").trim();
            interview.setStatus("COMPLETED");
            interviewRepository.save(interview);
        }

        questionRepository.save(Question.builder()
                .interview(interview)
                .questionText(lastQuestion)
                .answerText(userResponse)
                .createdAt(LocalDateTime.now())
                .build());

        return Map.of("question", nextQuestion, "isFinished", isFinished);
    }
}
