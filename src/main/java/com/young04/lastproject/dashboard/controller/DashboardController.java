package com.young04.lastproject.dashboard.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 관리자 대시보드 화면과 요약 정보를 처리하는 Controller
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MaterialService materialService;

    // 발주 상태별 건수 조회를 담당하는 Repository
    private final PurchaseOrderRepository purchaseOrderRepository;

    // 발주서에 포함된 자재 품목 조회를 담당하는 Service
    private final PurchaseOrderItemService purchaseOrderItemService;

    // 관리자 대시보드 조회
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        // 재고 부족 자재 전체 개수
        model.addAttribute(
                "lowStockCount",
                materialService.countLowStockMaterials()
        );

        // 재고 부족 자재 중 대표 1개를 대시보드에 표시
        model.addAttribute(
                "lowStockMaterials",
                materialService.getLowStockMaterials()
                        .stream()
                        .limit(1)
                        .toList()
        );

        // 발주 완료 후 아직 입고되지 않은 발주서 개수
        model.addAttribute(
                "pendingReceiptCount",
                purchaseOrderRepository.countByOrderStatus("ORDERED")
        );

        // 입고 예정일이 가장 가까운 발주서 한 건 조회
        PurchaseOrder pendingReceiptOrder = purchaseOrderRepository
                .findFirstByOrderStatusOrderByExpectedDateAscOrderDateAsc("ORDERED")
                .orElse(null);

        model.addAttribute("pendingReceiptOrder", pendingReceiptOrder);

        // 대표 발주서에 포함된 자재 이름을 화면으로 전달
        model.addAttribute(
                "pendingReceiptMaterialNames",
                pendingReceiptOrder == null
                        ? java.util.List.of()
                        : purchaseOrderItemService.getMaterialNames(
                        pendingReceiptOrder.getPurchaseOrderNo()
                )
        );

        return "admin/dashboard";
    }
}
