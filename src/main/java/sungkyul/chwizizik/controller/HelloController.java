package sungkyul.chwizizik.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/test")
    public String hello() {
        return "서버 연동 성공";
    }
}