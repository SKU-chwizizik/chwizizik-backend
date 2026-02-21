package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_interviewer_id", nullable = false)
    private AiInterviewer aiInterviewer;  

    @Column(name = "interview_at", nullable = false)
    private LocalDateTime interviewAt;

    @Column(name = "interview_type", length = 50, nullable = false)
    private String interviewType;

    @Column(name = "language", length = 20, nullable = false)
    private String language;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    // 연관관계 설정 - 인터뷰는 여러 질문을 가질 수 있으므로 1:N 관계로 설정한거임
    @OneToMany(mappedBy = "interview",
               fetch = FetchType.LAZY,
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    // 연관관계 설정 - 인터뷰는 하나의 결과 리포트를 가질 수 있으므로 1:1 관계로 설정한거임
    @OneToOne(mappedBy = "interview",
              fetch = FetchType.LAZY,
              cascade = CascadeType.ALL,
              orphanRemoval = true)
    private ResultReport resultReport;
}