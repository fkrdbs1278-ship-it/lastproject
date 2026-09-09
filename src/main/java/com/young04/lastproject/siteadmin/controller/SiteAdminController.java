package com.young04.lastproject.siteadmin.controller;

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


    // 사용자 사이트 관리 화면 조회
    @GetMapping
    public String siteAdmin(Model model) {

        // 현재 사이트 설정값을 관리자 화면에 전달
        model.addAttribute(
                "siteSetting",
                siteSettingService.getCurrentSetting()
        );

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

            model.addAttribute(
                    "siteSetting",
                    siteSettingService.getCurrentSetting()
            );

            return "admin/siteadmin";
        }


        // 관리자에서 변경한 설정 DB 저장
        siteSettingService.saveSetting(request);


        // 저장 완료 메시지
        redirectAttributes.addFlashAttribute(
                "message",
                "사용자 사이트 설정이 저장되었습니다."
        );

        return "redirect:/admin/siteadmin";
    }
}