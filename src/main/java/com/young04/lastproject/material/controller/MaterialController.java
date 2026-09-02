package com.young04.lastproject.material.controller;

import com.young04.lastproject.material.dto.MaterialRequest;
import com.young04.lastproject.material.dto.MaterialResponse;
import com.young04.lastproject.material.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// final 필드의 생성자를 자동으로 생성
@RequiredArgsConstructor
@Controller

// 자재 관리의 공통 주소 설정
@RequestMapping("/admin/material")
public class MaterialController {

    private final MaterialService materialService;

    // 자재 목록과 검색 결과 표시
    @GetMapping
    public String materialList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String useYn,
            @RequestParam(defaultValue = "false") boolean lowStock,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        // 관리자 목록은 한 페이지에 10개씩 표시
        Page<MaterialResponse> materialPage =
                materialService.getMaterialPage(
                        keyword,
                        useYn,
                        lowStock,
                        page,
                        10
                );

        // 자재가 없어도 1페이지는 화면에 표시합니다.
        int displayTotalPages = Math.max(
                materialPage.getTotalPages(),
                1
        );

        int endPage = Math.min(
                displayTotalPages - 1,
                materialPage.getNumber() + 2
        );

        int startPage = Math.max(0, endPage - 4);

        model.addAttribute(
                "materials",
                materialPage.getContent()
        );
        model.addAttribute("materialPage", materialPage);
        model.addAttribute("currentPage", materialPage.getNumber());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("useYn", useYn);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute(
                "lowStockCount",
                materialService.countLowStockMaterials()
        );

        return "material/list";
    }

    // 자재 상세 화면 표시
    @GetMapping("/{materialNo}")
    public String materialDetail(
            @PathVariable Long materialNo,
            Model model
    ) {
        model.addAttribute(
                "material",
                materialService.getMaterial(materialNo)
        );

        return "material/detail";
    }

    // 자재 등록 화면 표시
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("materialRequest", new MaterialRequest());
        model.addAttribute("editMode", false);

        return "material/form";
    }

    // 입력값 검증 후 자재 등록
    @PostMapping
    public String createMaterial(
            @Valid @ModelAttribute MaterialRequest materialRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);

            return "material/form";
        }

        materialService.createMaterial(materialRequest);

        redirectAttributes.addFlashAttribute(
                "message",
                "자재가 등록되었습니다."
        );

        return "redirect:/admin/material";
    }

    // 기존 자재 정보를 수정 화면에 전달
    @GetMapping("/{materialNo}/edit")
    public String updateForm(
            @PathVariable Long materialNo,
            Model model
    ) {
        MaterialResponse material =
                materialService.getMaterial(materialNo);

        MaterialRequest materialRequest =
                convertToRequest(material);

        model.addAttribute("materialRequest", materialRequest);
        model.addAttribute("materialNo", materialNo);
        model.addAttribute("editMode", true);

        return "material/form";
    }

    // 입력값 검증 후 자재 정보 수정
    @PostMapping("/{materialNo}/edit")
    public String updateMaterial(
            @PathVariable Long materialNo,
            @Valid @ModelAttribute MaterialRequest materialRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("materialNo", materialNo);
            model.addAttribute("editMode", true);

            return "material/form";
        }

        materialService.updateMaterial(materialNo, materialRequest);

        redirectAttributes.addFlashAttribute(
                "message",
                "자재 정보가 수정되었습니다."
        );

        return "redirect:/admin/material/" + materialNo;
    }

    // 사용 중지된 자재와 연결 이력 삭제 처리
    @PostMapping("/{materialNo}/delete")
    public String deleteMaterial(
            @PathVariable Long materialNo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 사용 중지된 자재와 연결 이력 삭제
            materialService.deleteMaterial(materialNo);

            // 삭제 성공 메시지 전달
            redirectAttributes.addFlashAttribute(
                    "message",
                    "자재가 삭제되었습니다."
            );

        } catch (IllegalStateException exception) {
            // 사용 중이거나 다른 업무 내역에 연결되어 있으면 삭제 실패 안내
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        // 사용 중지된 자재 목록으로 이동
        return "redirect:/admin/material?useYn=N";
    }

    // 조회 DTO를 수정 화면용 DTO로 변환
    private MaterialRequest convertToRequest(
            MaterialResponse material
    ) {
        MaterialRequest request = new MaterialRequest();

        request.setMaterialName(material.getMaterialName());
        request.setCategoryCode(material.getCategoryCode());
        request.setUnitCode(material.getUnitCode());
        request.setCurrentStock(material.getCurrentStock());
        request.setSafetyStock(material.getSafetyStock());
        request.setUnitPrice(material.getUnitPrice());
        request.setSupplierName(material.getSupplierName());
        request.setUseYn(material.getUseYn());

        return request;
    }

}