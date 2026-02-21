package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_interviewers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInterviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_interviewer_id")
    private Long aiInterviewerId;

    @Column(name = "interviewer_type", length = 50, nullable = false)
    private String interviewerType;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "career", length = 200)
    private String career;

    @Column(name = "focus", length = 200)
    private String focus;

    // 연관관계 설정 - AI 면접관은 여러 인터뷰를 가질 수 있으므로 1:N 관계로 설정한거임
    @OneToMany(mappedBy = "aiInterviewer", fetch = FetchType.LAZY)
    private List<Interview> interviews = new ArrayList<>();
}