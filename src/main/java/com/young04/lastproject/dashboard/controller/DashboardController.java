package com.young04.lastproject.dashboard.controller;

import com.young04.lastproject.material.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 관리자 대시보드 화면과 요약 정보를 처리하는 Controller
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MaterialService materialService;

    // 관리자 대시보드 조회
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        // 재고 부족 자재 전체 개수
        model.addAttribute(
                "lowStockCount",
                materialService.countLowStockMaterials()
        );

        // 재고 부족 자재 중 최대 3개를 대시보드에 표시
        model.addAttribute(
                "lowStockMaterials",
                materialService.getLowStockMaterials()
                        .stream()
                        .limit(3)
                        .toList()
        );

        return "admin/dashboard";
    }
}
