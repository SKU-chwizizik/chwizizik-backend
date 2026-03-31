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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagService {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;
    private final InterviewPrompts interviewPrompts;
    private final QuestionRepository questionRepository;
    private final InterviewRepository interviewRepository;

    public String getInitialQuestion(String type) {
        return interviewPrompts.getInitialQuestion(type);
    }

    @Transactional
    public Map<String, Object> getNextQuestion(
            Long interviewId, String userId, String userResponse, String lastQuestion, String type) {

        Map<String, Object> requestBody = Map.of(
            "user_id", userId,
            "interview_type", type,
            "last_question", lastQuestion,
            "user_answer", userResponse,
            "interview_id", interviewId
        );

        String nextQuestion = "잠시 통신이 원활하지 않네요. 다시 한 번 말씀해 주시겠어요?";
        boolean isFinished = false;

        try {
            Map<?, ?> response = restTemplate.postForObject(aiServiceUrl + "/rag-chat", requestBody, Map.class);
            if (response != null) {
                nextQuestion = (String) response.get("question");
                isFinished = Boolean.TRUE.equals(response.get("isFinished"));
            }
        } catch (Exception e) {
            nextQuestion = "연결 오류가 발생했습니다: " + e.getMessage();
        }

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 면접 세션을 찾을 수 없습니다."));

        if (isFinished) {
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
