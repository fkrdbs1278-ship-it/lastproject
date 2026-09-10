package com.young04.lastproject.servicematerial.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.servicematerial.dto.ServiceMaterialRequest;
import com.young04.lastproject.servicematerial.dto.ServiceMaterialResponse;
import com.young04.lastproject.servicematerial.service.ServiceMaterialService;
import com.young04.lastproject.servicemenu.service.ServiceMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 관리자 시술별 기본 자재 설정 화면을 처리하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/servicematerial")
public class ServiceMaterialController {

    private final ServiceMaterialService serviceMaterialService;
    private final MaterialService materialService;
    private final ServiceMenuService serviceMenuService;


    // 시술별 기본 자재 설정 화면
    @GetMapping
    public String list(
            @RequestParam(required = false)
            Long serviceMenuNo,
            Model model
    ) {

        ServiceMaterialRequest request =
                new ServiceMaterialRequest();

        request.setServiceMenuNo(serviceMenuNo);

        model.addAttribute(
                "serviceMaterialRequest",
                request
        );

        addPageData(
                serviceMenuNo,
                model
        );

        return "admin/servicematerial";
    }


    // 시술에 기본 자재 등록 또는 사용량 수정
    @PostMapping("/save")
    public String save(
            @Valid
            @ModelAttribute("serviceMaterialRequest")
            ServiceMaterialRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            addPageData(
                    request.getServiceMenuNo(),
                    model
            );

            return "admin/servicematerial";
        }

        try {

            serviceMaterialService.saveOrUpdate(
                    request.getServiceMenuNo(),
                    request.getMaterialNo(),
                    request.getUsageQuantity()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            bindingResult.reject(
                    "serviceMaterial",
                    exception.getMessage()
            );

            addPageData(
                    request.getServiceMenuNo(),
                    model
            );

            return "admin/servicematerial";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "시술 자재 설정이 저장되었습니다."
        );

        return "redirect:/admin/servicematerial"
                + "?serviceMenuNo="
                + request.getServiceMenuNo();
    }


    // 기존 기본 자재를 다른 제품으로 교체
    @PostMapping("/change")
    public String changeMaterial(
            @RequestParam Long serviceMaterialNo,
            @RequestParam Long serviceMenuNo,
            @RequestParam Long newMaterialNo,
            RedirectAttributes redirectAttributes
    ) {

        serviceMaterialService.changeMaterial(
                serviceMaterialNo,
                newMaterialNo
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "기본 자재가 변경되었습니다."
        );

        return "redirect:/admin/servicematerial"
                + "?serviceMenuNo="
                + serviceMenuNo;
    }


    // 시술에 연결된 특정 자재 삭제
    @PostMapping("/delete")
    public String deleteMaterial(
            @RequestParam Long serviceMenuNo,
            @RequestParam Long materialNo,
            RedirectAttributes redirectAttributes
    ) {

        serviceMaterialService.deleteMaterial(
                serviceMenuNo,
                materialNo
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "시술 자재 설정이 삭제되었습니다."
        );

        return "redirect:/admin/servicematerial"
                + "?serviceMenuNo="
                + serviceMenuNo;
    }


    // 화면에서 사용할 시술 메뉴, 자재, 현재 설정값 전달
    private void addPageData(
            Long serviceMenuNo,
            Model model
    ) {

        // 사용 가능한 전체 시술 메뉴
        model.addAttribute(
                "serviceMenus",
                serviceMenuService.getServiceMenus(null)
        );

        // 현재 사용 중인 자재
        model.addAttribute(
                "materials",
                materialService.getMaterialsByUseYn("Y")
        );

        // 현재 선택한 시술 번호
        model.addAttribute(
                "selectedServiceMenuNo",
                serviceMenuNo
        );

        if (serviceMenuNo == null) {

            model.addAttribute(
                    "serviceMaterials",
                    List.of()
            );

        } else {

            model.addAttribute(
                    "serviceMaterials",
                    serviceMaterialService
                            .getMaterialsByServiceMenuNo(
                                    serviceMenuNo
                            )
                            .stream()
                            .map(ServiceMaterialResponse::from)
                            .toList()
            );
        }
    }
}