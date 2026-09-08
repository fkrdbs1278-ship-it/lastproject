package com.young04.lastproject.siteadmin.controller;

import com.young04.lastproject.siteadmin.dto.SiteSettingDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteAdminController {

    // 사용자 사이트 관리 화면
    @GetMapping("/admin/siteadmin")
    public String siteAdmin(Model model) {

        // DB 연결 전 사용할 기본 화면 설정
        SiteSettingDto siteSetting = SiteSettingDto.builder()
                .heroTitle("나에게 어울리는 스타일을\n찾아보세요.")
                .heroDescription(
                        "원하는 시술과 헤어스타일을 확인하고\n" +
                                "편리하게 예약 서비스를 이용해보세요."
                )
                .heroImageUrl("/images/hero/hero1.jpg")
                .serviceVisible(true)
                .serviceTitle("서비스 안내")
                .styleVisible(true)
                .styleTitle("헤어스타일 둘러보기")
                .styleDescription(
                        "다양한 스타일을 확인하고\n" +
                                "원하는 헤어스타일을 찾아보세요."
                )
                .build();

        model.addAttribute("siteSetting", siteSetting);

        return "admin/siteadmin";
    }
}