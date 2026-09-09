package com.young04.lastproject.member.controller;

import com.young04.lastproject.siteadmin.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 사용자 메인 페이지 이동을 처리하는 Controller
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SiteSettingService siteSettingService;


    // 사용자 메인 페이지
    @GetMapping("/")
    public String home(Model model) {

        // 관리자에서 저장한 사이트 설정을 사용자 메인 화면에 전달
        model.addAttribute(
                "siteSetting",
                siteSettingService.getCurrentSetting()
        );

        return "index";
    }
}