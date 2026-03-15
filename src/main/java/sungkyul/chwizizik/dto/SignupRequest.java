package sungkyul.chwizizik.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String userId;
    private String name;
    private String password;
    private String email;
    private String phone;
}