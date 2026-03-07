package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sungkyul.chwizizik.entity.Education;
import sungkyul.chwizizik.entity.User;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    // 특정 사용자의 학력 정보를 찾기 위한 메서드
    Optional<Education> findByUser(User user);
}