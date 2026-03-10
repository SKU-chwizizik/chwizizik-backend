package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    // 연관관계 설정 - 질문은 하나의 인터뷰에 속하므로 N:1 관계로 설정한거임
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // columnDefinition = "TEXT" 를 명시하여 용량 부족 에러 방지
    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Lob
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Lob
    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Lob
    @Column(name = "improved_answer", columnDefinition = "TEXT")
    private String improvedAnswer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}