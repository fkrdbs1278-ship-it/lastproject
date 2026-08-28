package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customergrade.service.CustomerGradeService;

import com.young04.lastproject.customermemo.dto.CustomerMemoResponse;
import com.young04.lastproject.customermemo.service.CustomerMemoService;

import com.young04.lastproject.customerprofile.dto.CustomerCreateRequest;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;

import com.young04.lastproject.treatmenthistory.dto.TreatmentHistoryResponse;
import com.young04.lastproject.treatmenthistory.service.TreatmentHistoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/customers")
public class AdminCustomerController {


    // =====================================================
    // Service
    // =====================================================

    private final CustomerProfileService customerProfileService;

    private final CustomerGradeService customerGradeService;

    private final CustomerMemoService customerMemoService;

    private final TreatmentHistoryService treatmentHistoryService;



    // =====================================================
    // 관리자 고객 CRM 목록 / 검색 + 페이징
    // =====================================================

    @GetMapping
    public String customerList(

            @ModelAttribute("condition")
            CustomerSearchCondition condition,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            Model model
    ) {


        // -------------------------------------------------
        // 잘못된 음수 페이지 방지
        // -------------------------------------------------

        int currentPage =
                Math.max(
                        page,
                        0
                );


        // -------------------------------------------------
        // 한 페이지 고객 수
        // -------------------------------------------------

        int pageSize = 10;


        // -------------------------------------------------
        // Pageable 생성
        // -------------------------------------------------

        Pageable pageable =
                PageRequest.of(
                        currentPage,
                        pageSize
                );


        log.info(
                "관리자 고객 CRM 목록 조회 page={}, size={}",
                currentPage,
                pageSize
        );


        // -------------------------------------------------
        // 고객 검색 + 페이징
        // -------------------------------------------------

        Page<CustomerProfile> customerPage =
                customerProfileService
                        .searchCustomers(
                                condition,
                                pageable
                        );


        // -------------------------------------------------
        // 현재 페이지 고객 목록
        // -------------------------------------------------

        model.addAttribute(
                "customers",
                customerPage.getContent()
        );


        // -------------------------------------------------
        // 페이징 정보
        // -------------------------------------------------

        model.addAttribute(
                "customerPage",
                customerPage
        );

        model.addAttribute(
                "currentPage",
                customerPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                customerPage.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                customerPage.getTotalElements()
        );

        model.addAttribute(
                "pageSize",
                customerPage.getSize()
        );


        // -------------------------------------------------
        // 등급 목록
        // -------------------------------------------------

        model.addAttribute(
                "grades",
                customerGradeService
                        .findAllGrades()
        );


        return "customer/list";
    }



    // =====================================================
    // 전화예약 고객 등록 화면
    // =====================================================

    @GetMapping("/new")
    public String createGuestCustomerForm(
            Model model
    ) {

        log.info(
                "전화예약 고객 등록 화면"
        );


        if (!model.containsAttribute(
                "customerCreateRequest"
        )) {

            model.addAttribute(
                    "customerCreateRequest",
                    new CustomerCreateRequest()
            );
        }


        return "customer/create";
    }



    // =====================================================
    // 전화예약 고객 등록 처리
    // =====================================================

    @PostMapping("/new")
    public String createGuestCustomer(

            @Valid
            @ModelAttribute("customerCreateRequest")
            CustomerCreateRequest request,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 전화예약 고객 등록 요청 customerName={}",
                request.getCustomerName()
        );


        // -------------------------------------------------
        // Validation 실패
        // -------------------------------------------------

        if (bindingResult.hasErrors()) {

            log.warn(
                    "전화예약 고객 등록 입력값 검증 실패 errorCount={}",
                    bindingResult.getErrorCount()
            );


            return "customer/create";
        }


        // -------------------------------------------------
        // 고객 등록
        // -------------------------------------------------

        CustomerProfile savedCustomer =
                customerProfileService
                        .createGuestCustomer(
                                request
                        );


        // -------------------------------------------------
        // 성공 메시지
        // -------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "message",
                "전화예약 고객이 등록되었습니다."
        );


        log.info(
                "전화예약 고객 등록 완료 customerId={}",
                savedCustomer.getCustomerId()
        );


        return "redirect:/admin/customers/"
                + savedCustomer.getCustomerId();
    }



    // =====================================================
    // 관리자 고객 상세 통합 조회
    // =====================================================

    /**
     * 고객 상세 화면에서
     *
     * - 고객 기본정보
     * - 고객 등급
     * - 상담 메모 / 특이사항
     * - 시술 이력
     *
     * 을 한 화면에서 함께 조회합니다.
     */
    @GetMapping("/{customerId}")
    public String customerDetail(

            @PathVariable
            Long customerId,

            Model model
    ) {


        log.info(
                "관리자 고객 상세 통합 조회 customerId={}",
                customerId
        );


        // -------------------------------------------------
        // 1. 고객 기본정보
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );


        // -------------------------------------------------
        // 2. 상담 메모
        // -------------------------------------------------

        List<CustomerMemoResponse> memos =
                customerMemoService
                        .findByCustomerId(
                                customerId
                        )
                        .stream()
                        .map(
                                CustomerMemoResponse::from
                        )
                        .toList();


        // -------------------------------------------------
        // 3. 시술 이력
        // -------------------------------------------------

        List<TreatmentHistoryResponse> treatments =
                treatmentHistoryService
                        .findByCustomerId(
                                customerId
                        )
                        .stream()
                        .map(
                                TreatmentHistoryResponse::from
                        )
                        .toList();


        // -------------------------------------------------
        // 4. 고객 기본정보
        // -------------------------------------------------

        model.addAttribute(
                "customer",
                customer
        );


        // -------------------------------------------------
        // 5. 고객 등급
        // -------------------------------------------------

        model.addAttribute(
                "grades",
                customerGradeService
                        .findAllGrades()
        );


        // -------------------------------------------------
        // 6. 상담 메모
        // -------------------------------------------------

        model.addAttribute(
                "memos",
                memos
        );


        // -------------------------------------------------
        // 7. 시술 이력
        // -------------------------------------------------

        model.addAttribute(
                "treatments",
                treatments
        );


        log.info(
                "고객 상세 통합 조회 완료 customerId={}, memoCount={}, treatmentCount={}",
                customerId,
                memos.size(),
                treatments.size()
        );


        return "customer/detail";
    }



    // =====================================================
    // 고객 자동 등급 재계산
    // =====================================================

    @PostMapping("/{customerId}/grade/recalculate")
    public String recalculateGrade(

            @PathVariable
            Long customerId,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 자동 등급 재계산 요청 customerId={}",
                customerId
        );


        CustomerProfile customer =
                customerProfileService
                        .applyAutomaticGrade(
                                customerId
                        );


        // -------------------------------------------------
        // 수동 관리 고객
        // -------------------------------------------------

        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "관리자가 직접 지정한 등급입니다. "
                            + "자동 등급으로 전환한 후 재계산할 수 있습니다."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "고객 등급이 현재 실적을 기준으로 재계산되었습니다."
            );
        }


        return "redirect:/admin/customers/"
                + customerId;
    }



    // =====================================================
    // 관리자 고객 등급 수동 변경
    // =====================================================

    @PostMapping("/{customerId}/grade/manual")
    public String changeGradeManually(

            @PathVariable
            Long customerId,

            @RequestParam("gradeCode")
            String gradeCode,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 등급 수동 변경 요청 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        CustomerProfile customer =
                customerProfileService
                        .changeGradeManually(
                                customerId,
                                gradeCode
                        );


        redirectAttributes.addFlashAttribute(
                "message",
                "고객 등급이 수동으로 변경되었습니다."
        );


        log.info(
                "관리자 고객 등급 수동 변경 완료 customerId={}, gradeCode={}",
                customerId,
                customer
                        .getCustomerGrade()
                        .getGradeCode()
        );


        return "redirect:/admin/customers/"
                + customerId;
    }



    // =====================================================
    // 수동 등급 해제 → 자동 등급 관리
    // =====================================================

    @PostMapping("/{customerId}/grade/automatic")
    public String changeToAutomaticGrade(

            @PathVariable
            Long customerId,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 자동 등급 전환 요청 customerId={}",
                customerId
        );


        customerProfileService
                .changeToAutomaticGrade(
                        customerId
                );


        redirectAttributes.addFlashAttribute(
                "message",
                "자동 등급 관리로 전환되었습니다."
        );


        return "redirect:/admin/customers/"
                + customerId;
    }
}