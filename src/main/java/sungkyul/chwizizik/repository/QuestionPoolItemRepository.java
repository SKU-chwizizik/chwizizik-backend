package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sungkyul.chwizizik.entity.QuestionPoolItem;
import sungkyul.chwizizik.entity.User;

import java.util.List;

public interface QuestionPoolItemRepository extends JpaRepository<QuestionPoolItem, Long> {

    List<QuestionPoolItem> findByUserAndInterviewType(User user, String interviewType);

    List<QuestionPoolItem> findByUserAndInterviewTypeAndUseCount(User user, String interviewType, int useCount);

    List<QuestionPoolItem> findByUserAndInterviewTypeAndCategory(User user, String interviewType, String category);

    List<QuestionPoolItem> findByUserAndInterviewTypeAndLastAnswerQuality(User user, String interviewType, String lastAnswerQuality);

    List<QuestionPoolItem> findByUserAndInterviewTypeAndCategoryAndLastAnswerQuality(User user, String interviewType, String category, String lastAnswerQuality);
}
