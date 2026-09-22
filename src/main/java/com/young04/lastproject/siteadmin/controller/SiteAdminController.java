package com.young04.lastproject.siteadmin.controller;

import com.young04.lastproject.hairstyle.service.HairStyleService;
import com.young04.lastproject.siteadmin.dto.SiteSettingRequest;
import com.young04.lastproject.siteadmin.service.SiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 사용자 사이트 관리 화면 조회와 설정 저장을 처리하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/siteadmin")
public class SiteAdminController {

    private final SiteSettingService siteSettingService;

    private final HairStyleService hairStyleService;


    // 사용자 사이트 관리 화면 조회
    @GetMapping
    public String siteAdmin(
            Model model
    ) {

        addPreviewData(model);


        // 설정 저장용 DTO
        model.addAttribute(
                "siteSettingRequest",
                new SiteSettingRequest()
        );


        return "admin/siteadmin";
    }


    // 사용자 사이트 설정 저장
    @PostMapping("/save")
    public String saveSetting(
            @Valid
            @ModelAttribute("siteSettingRequest")
            SiteSettingRequest request,

            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // 입력값 검증 실패
        if (bindingResult.hasErrors()) {

            addPreviewData(model);

            return "admin/siteadmin";
        }


        try {

            // 문구 + Hero 1번 이미지 + 헤어스타일 설정 저장
            siteSettingService.saveSetting(
                    request
            );


            redirectAttributes.addFlashAttribute(
                    "message",
                    "사용자 사이트 설정이 저장되었습니다."
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException exception
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }


        return "redirect:/admin/siteadmin";
    }


    @PostMapping("/reset-hero-image")
    public String resetHeroImage(
            RedirectAttributes redirectAttributes
    ) {

        siteSettingService.resetHeroImage();


        redirectAttributes.addFlashAttribute(
                "message",
                "1번 슬라이드가 기본 이미지로 복원되었습니다."
        );


        return "redirect:/admin/siteadmin";
    }


    // 조회와 저장 검증 실패 시 동일한 미리보기 데이터를 전달한다.
    private void addPreviewData(Model model) {

        model.addAttribute(
                "siteSetting",
                siteSettingService.getCurrentSetting()
        );

        // 메인 화면과 같은 활성 헤어스타일의 실제 이미지 URL을 사용한다.
        model.addAttribute(
                "previewHairStyles",
                hairStyleService.getRandomHairStyles(4)
        );
    }


}
