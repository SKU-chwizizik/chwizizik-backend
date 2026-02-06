package sungkyul.chwizizik.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserInfoResponse {

    public Long id;
    
    @JsonProperty("connected_at")
    public String connectedAt;

    public KakaoAccount kakao_account;

    @Data
    public static class KakaoAccount {
        public Boolean profile_nickname_needs_agreement;
        public Profile profile;
    }

    @Data
    public static class Profile {
        public String nickname;
        public String thumbnail_image_url;
        public String profile_image_url;
    }
}