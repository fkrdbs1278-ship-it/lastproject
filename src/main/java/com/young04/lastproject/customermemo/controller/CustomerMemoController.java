package com.young04.lastproject.customermemo.controller;

import com.young04.lastproject.customermemo.dto.CustomerMemoRequest;
import com.young04.lastproject.customermemo.dto.CustomerMemoResponse;
import com.young04.lastproject.customermemo.entity.CustomerMemo;
import com.young04.lastproject.customermemo.service.CustomerMemoService;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/customers/{customerId}/memos")
public class CustomerMemoController {

    private final CustomerMemoService customerMemoService;

    // 고객명 등 고객 기본정보 조회
    private final CustomerProfileService customerProfileService;


    // =====================================================
    // 고객별 상담 메모 목록
    // =====================================================

    @GetMapping
    public String memoList(
            @PathVariable Long customerId,
            Model model
    ) {

        // -------------------------------------------------
        // 1. 고객 정보 조회
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(customerId);


        // -------------------------------------------------
        // 2. 고객별 상담 메모 조회
        // -------------------------------------------------

        List<CustomerMemoResponse> memos =
                customerMemoService
                        .findByCustomerId(customerId)
                        .stream()
                        .map(CustomerMemoResponse::from)
                        .toList();


        // -------------------------------------------------
        // 3. 화면 전달
        // -------------------------------------------------

        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "customerId",
                customerId
        );

        model.addAttribute(
                "memos",
                memos
        );


        // -------------------------------------------------
        // 4. 신규 메모 등록 Form 객체
        // -------------------------------------------------

        if (!model.containsAttribute(
                "memoRequest"
        )) {

            model.addAttribute(
                    "memoRequest",
                    new CustomerMemoRequest()
            );
        }


        return "customer/memo";
    }


    // =====================================================
    // 상담 메모 등록
    // =====================================================

    @PostMapping
    public String createMemo(
            @PathVariable Long customerId,

            @Valid
            @ModelAttribute("memoRequest")
            CustomerMemoRequest request,

            BindingResult bindingResult,

            Model model
    ) {

        // -------------------------------------------------
        // Validation 실패
        // -------------------------------------------------

        if (bindingResult.hasErrors()) {

            CustomerProfile customer =
                    customerProfileService
                            .getCustomerById(customerId);


            List<CustomerMemoResponse> memos =
                    customerMemoService
                            .findByCustomerId(customerId)
                            .stream()
                            .map(CustomerMemoResponse::from)
                            .toList();


            model.addAttribute(
                    "customer",
                    customer
            );

            model.addAttribute(
                    "customerId",
                    customerId
            );

            model.addAttribute(
                    "memos",
                    memos
            );


            return "customer/memo";
        }


        // -------------------------------------------------
        // 상담 메모 등록
        // -------------------------------------------------

        CustomerMemo savedMemo =
                customerMemoService.createMemo(
                        customerId,
                        request
                );


        log.info(
                "상담 메모 등록 요청 완료 customerId={}, memoId={}",
                customerId,
                savedMemo.getMemoId()
        );


        return "redirect:/admin/customers/"
                + customerId
                + "/memos";
    }


    // =====================================================
    // 상담 메모 수정
    // =====================================================

    @PostMapping("/{memoId}/update")
    public String updateMemo(
            @PathVariable Long customerId,
            @PathVariable Long memoId,

            @Valid
            @ModelAttribute
            CustomerMemoRequest request,

            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {

            log.warn(
                    "상담 메모 수정 Validation 실패 customerId={}, memoId={}",
                    customerId,
                    memoId
            );


            return "redirect:/admin/customers/"
                    + customerId
                    + "/memos";
        }


        customerMemoService.updateMemo(
                customerId,
                memoId,
                request
        );


        return "redirect:/admin/customers/"
                + customerId
                + "/memos";
    }


    // =====================================================
    // 상담 메모 삭제
    // =====================================================

    @PostMapping("/{memoId}/delete")
    public String deleteMemo(
            @PathVariable Long customerId,
            @PathVariable Long memoId
    ) {

        customerMemoService.deleteMemo(
                customerId,
                memoId
        );


        return "redirect:/admin/customers/"
                + customerId
                + "/memos";
    }
}