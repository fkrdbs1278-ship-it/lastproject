package com.young04.lastproject.stockhistory.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.stockhistory.dto.StockAdjustmentRequest;
import com.young04.lastproject.stockhistory.service.StockHistoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/stockhistory")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;
    private final MaterialService materialService;

    // 전체 또는 특정 자재의 재고 변동 이력 조회
    @GetMapping
    public String list(
            @RequestParam(required = false) Long materialNo,
            Model model
    ) {
        if (materialNo == null) {
            model.addAttribute(
                    "histories",
                    stockHistoryService.getAllHistories()
            );
        } else {
            model.addAttribute(
                    "histories",
                    stockHistoryService.getHistoriesByMaterialNo(materialNo)
            );
        }

        model.addAttribute("selectedMaterialNo", materialNo);

        return "stockhistory/list";
    }

    // 재고 조정 입력 화면 표시
    @GetMapping("/adjustment")
    public String adjustmentForm(Model model) {
        model.addAttribute(
                "stockAdjustmentRequest",
                new StockAdjustmentRequest()
        );
        addActiveMaterials(model);

        return "stockhistory/adjustment";
    }

    // 입력값 검증 후 현재 재고와 변동 이력 반영
    @PostMapping("/adjustment")
    public String adjustStock(
            @Valid @ModelAttribute
            StockAdjustmentRequest stockAdjustmentRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addActiveMaterials(model);

            return "stockhistory/adjustment";
        }

        try {
            stockHistoryService.adjustStock(stockAdjustmentRequest);

        } catch (EntityNotFoundException
                 | IllegalArgumentException
                 | IllegalStateException exception) {
            bindingResult.reject(
                    "stockAdjustment",
                    exception.getMessage()
            );
            addActiveMaterials(model);

            return "stockhistory/adjustment";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "재고가 조정되었습니다."
        );

        return "redirect:/admin/stockhistory";
    }

    // 조정 화면의 자재 선택 목록에 사용 중인 자재만 전달
    private void addActiveMaterials(Model model) {
        model.addAttribute(
                "materials",
                materialService.getMaterialsByUseYn("Y")
        );
    }
}