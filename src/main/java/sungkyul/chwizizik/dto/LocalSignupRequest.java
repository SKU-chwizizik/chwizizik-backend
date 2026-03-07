package sungkyul.chwizizik.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalSignupRequest {
    private String userId;
    private String password;
    private String name;
    private String email;
    private String phoneNumber;
}