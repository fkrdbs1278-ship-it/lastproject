package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.dto.CustomerCreateRequest;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerProfileService customerProfileService;

    private final CustomerGradeService customerGradeService;


    // =====================================================
    // 관리자 고객 CRM 목록 / 검색
    // =====================================================

    /**
     * 관리자 고객 CRM 목록 화면
     *
     * 검색 조건:
     *
     * - 이름 / 전화번호
     * - 회원 / 비회원
     * - 고객 등급
     * - 활성 여부
     * - 미방문 기간
     * - 재방문 권장일 도래
     */
    @GetMapping
    public String customerList(
            @ModelAttribute("condition")
            CustomerSearchCondition condition,
            Model model
    ) {

        log.info("관리자 고객 CRM 목록 조회");


        List<CustomerProfile> customers =
                customerProfileService
                        .searchCustomers(condition);


        model.addAttribute(
                "customers",
                customers
        );


        model.addAttribute(
                "grades",
                customerGradeService.findAllGrades()
        );


        return "customer/list";
    }


    // =====================================================
    // 전화예약 고객 등록 화면
    // =====================================================

    /**
     * 관리자가 전화로 예약을 받은 신규 고객을
     * 직접 등록하는 화면입니다.
     */
    @GetMapping("/new")
    public String createGuestCustomerForm(
            Model model
    ) {

        log.info("전화예약 고객 등록 화면");


        /*
         * DuplicateCustomerPhoneException 발생 후
         * redirect 되어 들어온 경우를 제외하고
         * 빈 등록 DTO를 생성합니다.
         */
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

    /**
     * 전화예약 고객을 실제 CUSTOMER_PROFILE에 등록합니다.
     *
     * 신규 고객 기본값:
     *
     * CUSTOMER_TYPE   = GUEST
     * GRADE_CODE      = NORMAL
     * GRADE_MANUAL_YN = N
     * VISIT_COUNT     = 0
     * TOTAL_PAYMENT   = 0
     * ACTIVE_YN       = Y
     */
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
        // 입력값 검증 실패
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
                        .createGuestCustomer(request);


        // -------------------------------------------------
        // 등록 성공 메시지
        // -------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "message",
                "전화예약 고객이 등록되었습니다."
        );


        log.info(
                "전화예약 고객 등록 Controller 처리 완료 customerId={}",
                savedCustomer.getCustomerId()
        );


        // -------------------------------------------------
        // 등록된 고객 상세 화면으로 이동
        // -------------------------------------------------

        return "redirect:/admin/customers/"
                + savedCustomer.getCustomerId();
    }


    // =====================================================
    // 관리자 고객 상세 조회
    // =====================================================

    @GetMapping("/{customerId}")
    public String customerDetail(
            @PathVariable Long customerId,
            Model model
    ) {

        log.info(
                "관리자 고객 상세 조회 customerId={}",
                customerId
        );


        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(customerId);


        model.addAttribute(
                "customer",
                customer
        );


        model.addAttribute(
                "grades",
                customerGradeService.findAllGrades()
        );


        return "customer/detail";
    }


    // =====================================================
    // 고객 자동 등급 재계산
    // =====================================================

    @PostMapping("/{customerId}/grade/recalculate")
    public String recalculateGrade(
            @PathVariable Long customerId,
            RedirectAttributes redirectAttributes
    ) {

        log.info(
                "관리자 고객 자동 등급 재계산 요청 customerId={}",
                customerId
        );


        CustomerProfile customer =
                customerProfileService
                        .applyAutomaticGrade(customerId);


        // -------------------------------------------------
        // 수동 등급 고객은 자동 재계산 제외
        // -------------------------------------------------

        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "관리자가 직접 지정한 등급입니다. 자동 등급으로 전환한 후 재계산할 수 있습니다."
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
            @PathVariable Long customerId,

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
                "관리자 고객 등급 수동 변경 처리 완료 customerId={}, gradeCode={}",
                customerId,
                customer.getCustomerGrade()
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
            @PathVariable Long customerId,
            RedirectAttributes redirectAttributes
    ) {

        log.info(
                "관리자 고객 자동 등급 전환 요청 customerId={}",
                customerId
        );


        customerProfileService
                .changeToAutomaticGrade(customerId);


        redirectAttributes.addFlashAttribute(
                "message",
                "자동 등급 관리로 전환되었습니다."
        );


        return "redirect:/admin/customers/"
                + customerId;
    }
}