package com.young04.lastproject.testcompany.controller;

import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.service.PurchaseOrderService;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemResponse;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 외부 공급업체의 발주 확인과 출고 처리를 테스트하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/testcompany/purchaseorder")
public class TestCompanyController {

    // 발주서 조회와 상태 변경을 담당하는 Service
    private final PurchaseOrderService purchaseOrderService;

    // 발주 품목 조회를 담당하는 Service
    private final PurchaseOrderItemService purchaseOrderItemService;

    // 테스트 공급업체용 발주서 목록 조회
    @GetMapping
    public String list(Model model) {

        List<PurchaseOrderResponse> orders =
                purchaseOrderService.getAllOrders();

        model.addAttribute(
                "orders",
                orders
        );

        return "testcompany/list";
    }

    // 테스트 공급업체용 발주서 상세 조회
    @GetMapping("/{purchaseOrderNo}")
    public String detail(
            @PathVariable Long purchaseOrderNo,
            Model model
    ) {

        PurchaseOrderResponse order =
                purchaseOrderService.getOrder(
                        purchaseOrderNo
                );

        List<PurchaseOrderItemResponse> items =
                purchaseOrderItemService.getItems(
                        purchaseOrderNo
                );

        model.addAttribute(
                "order",
                order
        );

        model.addAttribute(
                "items",
                items
        );

        return "testcompany/detail";
    }

    // 공급업체 출고 완료 처리
    @PostMapping("/{purchaseOrderNo}/shipment")
    public String processShipment(
            @PathVariable Long purchaseOrderNo,
            RedirectAttributes redirectAttributes
    ) {

        purchaseOrderService.processShipment(
                purchaseOrderNo
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "발주서의 출고 처리가 완료되었습니다."
        );

        return "redirect:/testcompany/purchaseorder/"
                + purchaseOrderNo;
    }

    // 공급업체 발주 거절 처리
    @PostMapping("/{purchaseOrderNo}/reject")
    public String rejectOrder(
            @PathVariable Long purchaseOrderNo,
            RedirectAttributes redirectAttributes
    ) {

        purchaseOrderService.rejectOrder(
                purchaseOrderNo
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "발주 요청을 거절했습니다."
        );

        return "redirect:/testcompany/purchaseorder/"
                + purchaseOrderNo;
    }
}