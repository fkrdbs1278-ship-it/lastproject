package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
     * 관리자 고객 CRM 목록 화면입니다.
     *
     * 검색 조건:
     *
     * - 고객 이름 / 전화번호
     * - 회원 / 비회원
     * - 고객 등급
     * - 활성 여부
     * - 미방문 기간
     */
    @GetMapping
    public String customerList(
            @ModelAttribute("condition")
            CustomerSearchCondition condition,
            Model model
    ) {

        log.info("관리자 고객 CRM 목록 조회");


        // -------------------------------------------------
        // 검색 조건을 이용하여 고객 목록 조회
        // -------------------------------------------------

        List<CustomerProfile> customers =
                customerProfileService
                        .searchCustomers(condition);


        // -------------------------------------------------
        // 화면에 고객 목록 전달
        // -------------------------------------------------

        model.addAttribute(
                "customers",
                customers
        );


        // -------------------------------------------------
        // 고객 등급 검색 Select 등에 사용할 등급 목록
        // NORMAL / REGULAR / VIP
        // -------------------------------------------------

        model.addAttribute(
                "grades",
                customerGradeService.findAllGrades()
        );


        return "customer/list";
    }


    // =====================================================
    // 관리자 고객 상세 조회
    // =====================================================

    /**
     * 고객 한 명의 CRM 상세 정보를 조회합니다.
     *
     * 고객 기본정보와 함께
     * 등급 변경 화면에서 사용할 전체 등급 목록도 전달합니다.
     */
    @GetMapping("/{customerId}")
    public String customerDetail(
            @PathVariable Long customerId,
            Model model
    ) {

        log.info(
                "관리자 고객 상세 조회 customerId={}",
                customerId
        );


        // -------------------------------------------------
        // 고객 조회
        // -------------------------------------------------
        // 존재하지 않는 고객이면
        // CustomerNotFoundException 발생
        // → Advice에서 공통 처리
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(customerId);


        // -------------------------------------------------
        // 고객 상세정보
        // -------------------------------------------------

        model.addAttribute(
                "customer",
                customer
        );


        // -------------------------------------------------
        // 등급 수동 변경용 전체 등급
        // -------------------------------------------------

        model.addAttribute(
                "grades",
                customerGradeService.findAllGrades()
        );


        return "customer/detail";
    }


    // =====================================================
    // 고객 자동 등급 재계산
    // =====================================================

    /**
     * 현재 방문 횟수와 누적 결제 금액으로
     * 고객 등급을 다시 계산합니다.
     *
     * 단,
     * 관리자가 수동으로 등급을 지정한 고객
     * (GRADE_MANUAL_YN = Y)은 변경되지 않습니다.
     */
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
        // 수동 등급 고객이면 자동 계산 제외
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

    /**
     * 관리자가 고객 등급을 직접 지정합니다.
     *
     * gradeCode:
     *
     * NORMAL
     * REGULAR
     * VIP
     *
     * 수동 변경 후:
     *
     * GRADE_MANUAL_YN = Y
     */
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

    /**
     * 관리자가 직접 설정했던 등급을 해제하고
     * 다시 자동 등급 관리 상태로 전환합니다.
     *
     * 현재 고객의
     *
     * VISIT_COUNT
     * TOTAL_PAYMENT
     *
     * 값을 이용해서 등급을 즉시 다시 계산합니다.
     *
     * 처리 후:
     *
     * GRADE_MANUAL_YN = N
     */
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