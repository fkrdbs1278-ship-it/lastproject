package com.young04.lastproject.purchaseorder.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderReceiveRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.service.PurchaseOrderService;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemCreateRequest;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemResponse;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 발주서 화면 요청과 응답을 처리하는 Controller
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/purchaseorder")
public class PurchaseOrderController {

    // 발주서 업무 처리를 담당하는 Service
    private final PurchaseOrderService purchaseOrderService;

    // 발주 품목 등록과 조회를 담당하는 Service
    private final PurchaseOrderItemService purchaseOrderItemService;

    // 발주할 자재 목록 조회를 담당하는 Service
    private final MaterialService materialService;

    // 전체·상태별·공급업체별 발주서 목록 조회
    @GetMapping
    public String list(
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String supplierName,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        // 관리자 발주 목록은 한 페이지에 10개씩 조회
        Page<PurchaseOrderResponse> orderPage =
                purchaseOrderService.getOrderPage(
                        orderStatus,
                        supplierName,
                        PageRequest.of(Math.max(page, 0), 10)
                );

        // 조회한 발주서 목록을 화면으로 전달
        model.addAttribute("orders", orderPage.getContent());

        // 페이지 번호와 이전·다음 버튼 처리에 사용할 페이지 정보
        model.addAttribute("orderPage", orderPage);

        // 현재 선택한 발주 상태를 화면으로 전달
        model.addAttribute("orderStatus", orderStatus);

        // 현재 검색한 공급업체명을 화면으로 전달
        model.addAttribute("supplierName", supplierName);

        // 발주서 목록 HTML로 이동
        return "purchaseorder/list";
    }

    // 발주 검색 자동완성에서 사용할 업체명 목록 전달
    @GetMapping("/searchsuggestions")
    @ResponseBody
    public List<String> supplierNameSuggestions() {
        return purchaseOrderService.getSupplierNameSuggestions();
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
        return "redirect:/admin/purchaseorder/" + purchaseOrderNo;
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

        // 발주서에 등록된 품목을 화면으로 전달
        model.addAttribute(
                "items",
                purchaseOrderItemService.getItems(purchaseOrderNo)
        );

        // 사용 중인 자재를 품목 선택란으로 전달
        model.addAttribute(
                "materials",
                materialService.getMaterialsByUseYn("Y")
        );

        // 품목 등록에 사용할 빈 입력 DTO 전달
        model.addAttribute(
                "purchaseOrderItemRequest",
                new PurchaseOrderItemCreateRequest()
        );

        // 입고 처리에 사용할 빈 입력 DTO 전달
        model.addAttribute(
                "purchaseOrderReceiveRequest",
                new PurchaseOrderReceiveRequest()
        );

        // 발주서 상세 HTML로 이동
        return "purchaseorder/detail";
    }

    // 발주서를 인쇄용 화면으로 조회
    @GetMapping("/{purchaseOrderNo}/print")
    public String printPurchaseOrder(
            @PathVariable Long purchaseOrderNo,
            Model model
    ) {
        model.addAttribute(
                "order",
                purchaseOrderService.getOrder(purchaseOrderNo)
        );
        model.addAttribute(
                "items",
                purchaseOrderItemService.getItems(purchaseOrderNo)
        );

        return "purchaseorder/print";
    }

    // 기존 발주서에 새로운 자재 품목 등록
    @PostMapping("/{purchaseOrderNo}/items")
    public String createItem(
            @PathVariable Long purchaseOrderNo,
            @Valid
            @ModelAttribute("purchaseOrderItemRequest")
            PurchaseOrderItemCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 입력값 검증 실패 시 상세 화면에 필요한 정보를 다시 전달
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "order",
                    purchaseOrderService.getOrder(purchaseOrderNo)
            );
            model.addAttribute(
                    "items",
                    purchaseOrderItemService.getItems(purchaseOrderNo)
            );
            model.addAttribute(
                    "materials",
                    materialService.getMaterialsByUseYn("Y")
            );
            model.addAttribute(
                    "purchaseOrderReceiveRequest",
                    new PurchaseOrderReceiveRequest()
            );

            return "purchaseorder/detail";
        }

        // 선택한 자재를 발주 품목으로 등록
        purchaseOrderItemService.createItem(
                purchaseOrderNo,
                request
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "발주 품목이 추가되었습니다."
        );

        return "redirect:/admin/purchaseorder/" + purchaseOrderNo;
    }

    // 공급업체에서 출고한 발주 품목의 입고 완료 처리
    @PostMapping("/{purchaseOrderNo}/receive")
    public String receiveOrder(
            @PathVariable Long purchaseOrderNo,
            @ModelAttribute("purchaseOrderReceiveRequest")
            PurchaseOrderReceiveRequest request,
            RedirectAttributes redirectAttributes
    ) {
        purchaseOrderService.receiveOrder(purchaseOrderNo, request);

        redirectAttributes.addFlashAttribute(
                "message",
                "입고가 완료되어 자재 재고에 반영되었습니다."
        );

        return "redirect:/admin/purchaseorder/" + purchaseOrderNo;
    }
}
