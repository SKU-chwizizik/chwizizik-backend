package sungkyul.chwizizik.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// mysql에서는 생성일과 수정일이 자동으로 나오지만 JPA에서는 직접하기에 이걸 자동으로 하기 위한 어노테이션 추가임.
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "desired_job", length = 100)
    private String desiredJob;

    @Column(name = "resume_file", length = 255)
    private String resumeFile;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "kakao_nickname", length = 100)
    private String kakaoNickname;

    @Column(name = "kakao_profile", length = 500)
    private String kakaoProfile;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // FK 키 설정 연관관계까지 설정한거임
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Interview> interviews = new ArrayList<>();
}