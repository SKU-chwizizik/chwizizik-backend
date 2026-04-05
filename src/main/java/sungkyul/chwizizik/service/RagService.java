package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sungkyul.chwizizik.config.InterviewPrompts;
import sungkyul.chwizizik.entity.Interview;
import sungkyul.chwizizik.entity.Question;
import sungkyul.chwizizik.entity.Resume;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.InterviewRepository;
import sungkyul.chwizizik.repository.QuestionRepository;
import sungkyul.chwizizik.repository.ResumeRepository;
import sungkyul.chwizizik.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    /** Interview 생성 + 초기 질문 저장 후 interviewId, questionId 반환 */
    @Transactional
    public Map<String, Object> startInterview(String userId, String type, String lang) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .interviewType(type)
                .language(lang)
                .interviewAt(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build());

        String initialQuestion = interviewPrompts.getInitialQuestion(type);

        Question saved = questionRepository.save(Question.builder()
                .interview(interview)
                .questionText(initialQuestion)
                .createdAt(LocalDateTime.now())
                .build());

        return Map.of(
            "question", initialQuestion,
            "questionId", saved.getQuestionId(),
            "interviewId", interview.getInterviewId()
        );
    }

    /** DB에 저장된 이력서 markdown을 ChromaDB에 임베딩 */
    public Map<String, Object> syncResumes(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        List<Resume> resumes = resumeRepository.findByUserOrderByUploadedAtDesc(user);
        if (resumes.isEmpty()) {
            return Map.of("synced", 0, "message", "이력서가 없습니다.");
        }

        int synced = 0;
        for (Resume resume : resumes) {
            if (resume.getResumeMarkdown() == null || resume.getResumeMarkdown().isBlank()) continue;
            Map<String, Object> body = Map.of(
                "user_id", userId,
                "text", resume.getResumeMarkdown(),
                "filename", resume.getFileName()
            );
            try {
                restTemplate.postForObject(aiServiceUrl + "/embed-text", body, Map.class);
                synced++;
            } catch (Exception e) {
                // 개별 실패는 건너뜀
            }
        }
        return Map.of("synced", synced, "total", resumes.size());
    }

    @Transactional
    public Map<String, Object> getNextQuestion(
            Long interviewId, Long lastQuestionId, String userId,
            String userResponse, String lastQuestion, String type) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 면접 세션을 찾을 수 없습니다."));

        // 1. 이전 질문에 사용자 답변 저장
        if (lastQuestionId != null) {
            questionRepository.findById(lastQuestionId).ifPresent(q -> {
                q.setAnswerText(userResponse);
                questionRepository.save(q);
            });
        }

        // 2. FastAPI RAG 호출
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

        // 3. 면접 종료 처리
        if (isFinished) {
            interview.setStatus("COMPLETED");
            interviewRepository.save(interview);
            return Map.of("question", nextQuestion, "isFinished", true);
        }

        // 4. 다음 질문 저장
        Question saved = questionRepository.save(Question.builder()
                .interview(interview)
                .questionText(nextQuestion)
                .createdAt(LocalDateTime.now())
                .build());

        return Map.of("question", nextQuestion, "questionId", saved.getQuestionId(), "isFinished", false);
    }
}
