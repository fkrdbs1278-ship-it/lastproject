package com.young04.lastproject.stockhistory.controller;

import com.young04.lastproject.stockhistory.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stockhistory")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

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
}