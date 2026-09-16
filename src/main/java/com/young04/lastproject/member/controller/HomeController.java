package com.young04.lastproject.member.controller;

import com.young04.lastproject.hairstyle.service.HairStyleService;
import com.young04.lastproject.siteadmin.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

// 사용자 메인 페이지 이동을 처리하는 Controller
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SiteSettingService siteSettingService;

    private final HairStyleService hairStyleService;


    // 사용자 메인 페이지
    @GetMapping("/")
    public String home(
            Model model
    ) {

        /*
         * 관리자에서 저장한 사이트 설정을
         * 사용자 메인 화면에 전달
         */
        model.addAttribute(
                "siteSetting",
                siteSettingService.getCurrentSetting()
        );


        /*
         * ACTIVE_YN = Y인 헤어스타일 중
         * 랜덤으로 9개를 메인 화면에 전달
         */
        model.addAttribute(
                "randomHairStyles",
                hairStyleService.getRandomHairStyles(9)
        );


        return "index";
    }

    @GetMapping("/api/hairstyles/random")
    @ResponseBody
    public List<HairStylePreviewResponse> randomHairStyles() {

        return hairStyleService
                .getRandomHairStyles(9)
                .stream()
                .map(style ->
                        new HairStylePreviewResponse(
                                style.getNo(),
                                style.getTitle(),
                                style.getImageUrl()
                        )
                )
                .toList();
    }


    private record HairStylePreviewResponse(
            Long no,
            String title,
            String imageUrl
    ) {
    }

}