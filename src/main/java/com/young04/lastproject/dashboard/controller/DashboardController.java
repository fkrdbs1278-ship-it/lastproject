package com.young04.lastproject.dashboard.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // 재고 부족 자재 중 최대 5개를 대시보드에 표시
        model.addAttribute(
                "lowStockMaterials",
                materialService.getLowStockMaterials()
                        .stream()
                        .limit(5)
                        .toList()
        );

        // 발주 완료 후 아직 입고되지 않은 발주서 개수
        model.addAttribute(
                "pendingReceiptCount",
                purchaseOrderRepository.countByOrderStatus("ORDERED")
        );

        // 입고 예정일이 가까운 발주서 중 최대 5개 조회
        List<PurchaseOrder> pendingReceiptOrders = purchaseOrderRepository
                .findTop5ByOrderStatusOrderByExpectedDateAscOrderDateAsc(
                        "ORDERED"
                );

        model.addAttribute("pendingReceiptOrders", pendingReceiptOrders);

        // 각 발주서에 포함된 대표 자재 이름을 화면으로 전달
        Map<Long, List<String>> pendingReceiptMaterialNames =
                new LinkedHashMap<>();

        for (PurchaseOrder order : pendingReceiptOrders) {
            pendingReceiptMaterialNames.put(
                    order.getPurchaseOrderNo(),
                    purchaseOrderItemService.getMaterialNames(
                            order.getPurchaseOrderNo()
                    )
            );
        }

        model.addAttribute(
                "pendingReceiptMaterialNames",
                pendingReceiptMaterialNames
        );

        return "admin/dashboard";
    }
}
