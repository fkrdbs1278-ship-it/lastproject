package com.young04.lastproject.purchaseorder.controller;

import com.young04.lastproject.purchaseorder.dto.PurchaseOrderRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 발주서 화면 요청과 응답을 처리하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/purchaseorder")
public class PurchaseOrderController {

    // 발주서 업무 처리를 담당하는 Service
    private final PurchaseOrderService purchaseOrderService;

    // 전체·상태별·공급업체별 발주서 목록 조회
    @GetMapping
    public String list(
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String supplierName,
            Model model
    ) {
        // 화면으로 전달할 발주서 목록
        List<PurchaseOrderResponse> orders;

        // 공급업체명이 입력되면 공급업체명으로 검색
        if (supplierName != null && !supplierName.isBlank()) {
            orders = purchaseOrderService.searchOrders(supplierName);

            // 발주 상태가 선택되면 해당 상태만 조회
        } else if (orderStatus != null && !orderStatus.isBlank()) {
            orders = purchaseOrderService.getOrdersByStatus(orderStatus);

            // 검색 조건이 없으면 전체 발주서 조회
        } else {
            orders = purchaseOrderService.getAllOrders();
        }

        // 조회한 발주서 목록을 화면으로 전달
        model.addAttribute("orders", orders);

        // 현재 선택한 발주 상태를 화면으로 전달
        model.addAttribute("orderStatus", orderStatus);

        // 현재 검색한 공급업체명을 화면으로 전달
        model.addAttribute("supplierName", supplierName);

        // 발주서 목록 HTML로 이동
        return "purchaseorder/list";
    }

    // 발주서 등록 화면 이동
    @GetMapping("/new")
    public String createForm(Model model) {

        // 빈 발주서 입력 DTO를 등록 화면으로 전달
        model.addAttribute(
                "purchaseOrderRequest",
                new PurchaseOrderRequest()
        );

        // 발주서 등록 HTML로 이동
        return "purchaseorder/form";
    }

    // 발주서 등록 처리
    @PostMapping
    public String create(
            @Valid
            @ModelAttribute("purchaseOrderRequest")
            PurchaseOrderRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 입력값 검증에 실패하면 등록 화면으로 돌아감
        if (bindingResult.hasErrors()) {
            return "purchaseorder/form";
        }

        // 발주서를 저장하고 생성된 발주서 번호를 받음
        Long purchaseOrderNo =
                purchaseOrderService.createOrder(request);

        // 등록 완료 메시지를 한 번만 전달
        redirectAttributes.addFlashAttribute(
                "message",
                "발주서가 등록되었습니다."
        );

        // 등록된 발주서 상세 화면으로 이동
        return "redirect:/purchaseorder/" + purchaseOrderNo;
    }

    // 발주서 상세 화면 조회
    @GetMapping("/{purchaseOrderNo}")
    public String detail(
            @PathVariable Long purchaseOrderNo,
            Model model
    ) {
        // 발주서 번호로 상세 정보를 조회
        PurchaseOrderResponse order =
                purchaseOrderService.getOrder(purchaseOrderNo);

        // 조회한 발주서 정보를 화면으로 전달
        model.addAttribute("order", order);

        // 발주서 상세 HTML로 이동
        return "purchaseorder/detail";
    }
}