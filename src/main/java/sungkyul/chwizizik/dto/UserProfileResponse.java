package sungkyul.chwizizik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String userId;
    private String name;
    private String phoneNumber;
    private String email;
    private String jobField;
    private EducationDto education;
    private List<String> certificates;
    private List<AttachedFileDto> files;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationDto {
        private String level;
        private String school;
        private String major;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachedFileDto {
        private Long id;
        private String fileName;
        private LocalDateTime uploadedAt;
    }
}
