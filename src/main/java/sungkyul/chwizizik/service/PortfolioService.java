package sungkyul.chwizizik.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sungkyul.chwizizik.entity.Certificates;
import sungkyul.chwizizik.entity.Resume;
import sungkyul.chwizizik.entity.User;
import sungkyul.chwizizik.repository.CertificatesRepository;
import sungkyul.chwizizik.repository.ResumeRepository;
import sungkyul.chwizizik.repository.UserRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);
    private static final String AI_SERVICE_URL = "http://localhost:8000/parse-resume";

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final CertificatesRepository certificatesRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public Map<String, Object> saveResumeFile(String userId, MultipartFile pdfFile) throws IOException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String resumeMarkdown = parseResumeSafely(pdfFile);

        Resume resume = resumeRepository.save(Resume.builder()
                .fileName(pdfFile.getOriginalFilename())
                .resumeMarkdown(resumeMarkdown)
                .user(user)
                .build());

        return Map.of("id", resume.getId(), "fileName", resume.getFileName());
    }

    @Transactional
    public void savePortfolio(String userId, String eduLevel, String schoolName,
                              String major, String desiredJob, String certNames,
                              MultipartFile pdfFile) throws IOException {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 1. AI 서비스에 PDF 전송 → Markdown 수신 (AI 서비스 미실행 시 빈 문자열로 대체)
        String resumeMarkdown = parseResumeSafely(pdfFile);

        // 2. User 업데이트 (취업 희망 분야, 학력 정보)
        user.setDesiredJob(desiredJob);
        user.setEduLevel(eduLevel);
        user.setSchoolName(schoolName);
        user.setMajor(major);

        // 3. Resume 저장 (업로드마다 새 레코드)
        resumeRepository.save(Resume.builder()
                .fileName(pdfFile.getOriginalFilename())
                .resumeMarkdown(resumeMarkdown)
                .user(user)
                .build());

        // 4. Certificates 저장 (기존 삭제 후 재저장, 쉼표 구분)
        certificatesRepository.deleteByUser(user);
        Arrays.stream(certNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .forEach(name -> certificatesRepository.save(
                        Certificates.builder().certName(name).user(user).build()
                ));
    }

    private String parseResumeSafely(MultipartFile pdfFile) {
        try {
            return parseResume(pdfFile);
        } catch (Exception e) {
            log.warn("AI 서비스 호출 실패 — PDF 파싱 생략: {}", e.getMessage());
            return "";
        }
    }

    private String parseResume(MultipartFile pdfFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(pdfFile.getBytes()) {
            @Override
            public String getFilename() {
                return pdfFile.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                AI_SERVICE_URL, HttpMethod.POST, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("markdown");
        }
        return "";
    }
}
