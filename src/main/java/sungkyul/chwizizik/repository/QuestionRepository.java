package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sungkyul.chwizizik.entity.Interview;
import sungkyul.chwizizik.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    int countByInterviewAndParentQuestionIsNotNull(Interview interview);

    int countByParentQuestion(Question parentQuestion);
}