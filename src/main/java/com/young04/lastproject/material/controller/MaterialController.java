package com.young04.lastproject.material.controller;

import com.young04.lastproject.material.dto.MaterialRequest;
import com.young04.lastproject.material.dto.MaterialResponse;
import com.young04.lastproject.material.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// final 필드의 생성자를 자동으로 생성
@RequiredArgsConstructor
@Controller

// 자재 관리의 공통 주소 설정
@RequestMapping("/material")
public class MaterialController {

    private final MaterialService materialService;

    // 자재 목록과 검색 결과 표시
    @GetMapping
    public String materialList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String useYn,
            @RequestParam(defaultValue = "false") boolean lowStock,
            Model model
    ) {
        List<MaterialResponse> materials;

        if (StringUtils.hasText(keyword)) {
            materials = materialService.searchMaterials(keyword);
        } else if (lowStock) {
            materials = materialService.getLowStockMaterials();
        } else if (StringUtils.hasText(useYn)) {
            materials = materialService.getMaterialsByUseYn(useYn);
        } else {
            materials = materialService.getAllMaterials();
        }

        model.addAttribute("materials", materials);
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

        return "redirect:/material";
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

        return "redirect:/material/" + materialNo;
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