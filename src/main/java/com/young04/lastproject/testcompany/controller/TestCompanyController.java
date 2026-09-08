package com.young04.lastproject.testcompany.controller;

import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.service.PurchaseOrderService;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemResponse;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

        // 미용실에서 등록한 전체 발주서 조회
        List<PurchaseOrderResponse> orders =
                purchaseOrderService.getAllOrders();

        // 조회한 발주서 목록을 화면으로 전달
        model.addAttribute("orders", orders);

        // 테스트 공급업체 발주 목록 HTML로 이동
        return "testcompany/list";
    }

    // 테스트 공급업체용 발주서 상세 조회
    @GetMapping("/{purchaseOrderNo}")
    public String detail(
            @PathVariable Long purchaseOrderNo,
            Model model
    ) {
        // 발주서 기본 정보 조회
        PurchaseOrderResponse order =
                purchaseOrderService.getOrder(purchaseOrderNo);

        // 발주서에 등록된 품목 조회
        List<PurchaseOrderItemResponse> items =
                purchaseOrderItemService.getItems(purchaseOrderNo);

        // 발주서 정보를 화면으로 전달
        model.addAttribute("order", order);

        // 발주 품목 목록을 화면으로 전달
        model.addAttribute("items", items);

        // 테스트 공급업체 발주 상세 HTML로 이동
        return "testcompany/detail";
    }

    // 공급업체 출고 완료 처리
    @PostMapping("/{purchaseOrderNo}/shipment")
    public String processShipment(
            @PathVariable Long purchaseOrderNo,
            RedirectAttributes redirectAttributes
    ) {
        // 발주 요청을 공급업체 출고 완료 상태로 변경
        purchaseOrderService.processShipment(purchaseOrderNo);

        // 처리 완료 메시지를 한 번만 전달
        redirectAttributes.addFlashAttribute(
                "message",
                "발주서의 출고 처리가 완료되었습니다."
        );

        // 처리한 발주서 상세 화면으로 다시 이동
        return "redirect:/testcompany/purchaseorder/"
                + purchaseOrderNo;
    }

    // 공급업체 발주 거절 처리
    @PostMapping("/{purchaseOrderNo}/reject")
    public String rejectOrder(
            @PathVariable Long purchaseOrderNo,
            RedirectAttributes redirectAttributes
    ) {
        // 발주 요청을 취소 상태로 변경
        purchaseOrderService.rejectOrder(purchaseOrderNo);

        // 거절 완료 메시지를 한 번만 전달
        redirectAttributes.addFlashAttribute(
                "message",
                "발주 요청을 거절했습니다."
        );

        // 처리한 발주서 상세 화면으로 다시 이동
        return "redirect:/testcompany/purchaseorder/"
                + purchaseOrderNo;
    }
}