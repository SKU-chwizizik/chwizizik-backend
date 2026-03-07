package sungkyul.chwizizik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sungkyul.chwizizik.entity.Certificates;
import sungkyul.chwizizik.entity.User;

@Repository
public interface CertificatesRepository extends JpaRepository<Certificates, Long> {
    
    @Transactional
    void deleteByUserAndCertName(User user, String certName);
}