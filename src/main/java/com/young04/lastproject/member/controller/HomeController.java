package com.young04.lastproject.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /* 사용자 메인 페이지 */

    @GetMapping("/")
    public String home() {

        return "index";
    }
}