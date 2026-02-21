package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Lob
    @Column(name = "answer_text")
    private String answerText;

    @Lob
    @Column(name = "feedback_text")
    private String feedbackText;

    @Lob
    @Column(name = "improved_answer")
    private String improvedAnswer;
}