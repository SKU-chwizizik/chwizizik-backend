package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sungkyul.chwizizik.entity.Interview;
import sungkyul.chwizizik.entity.ResultReport;

import java.util.Optional;

public interface ResultReportRepository extends JpaRepository<ResultReport, Long> {
    Optional<ResultReport> findByInterview(Interview interview);
    Optional<ResultReport> findByInterview_InterviewId(Long interviewId);
}