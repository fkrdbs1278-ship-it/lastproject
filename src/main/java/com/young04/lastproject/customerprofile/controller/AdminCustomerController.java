package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.dto.CustomerCreateRequest;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
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



    // =====================================================
    // 관리자 고객 CRM 목록 / 검색 + 페이징
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
     * - 재방문 대상
     *
     * 한 페이지 10명
     */
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
        // 음수 페이지 방지
        // -------------------------------------------------

        int currentPage =
                Math.max(
                        page,
                        0
                );


        // -------------------------------------------------
        // 한 페이지 고객 수
        // -------------------------------------------------

        int pageSize =
                10;


        // -------------------------------------------------
        // Pageable
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
        // 검색 + 페이징
        // -------------------------------------------------

        Page<CustomerProfile> customerPage =
                customerProfileService
                        .searchCustomers(
                                condition,
                                pageable
                        );


        // -------------------------------------------------
        // 화면 데이터
        // -------------------------------------------------

        model.addAttribute(
                "customers",
                customerPage.getContent()
        );


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
    // 관리자 고객 상세 조회
    // =====================================================

    @GetMapping("/{customerId}")
    public String customerDetail(

            @PathVariable
            Long customerId,

            Model model
    ) {


        log.info(
                "관리자 고객 상세 조회 customerId={}",
                customerId
        );


        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );


        model.addAttribute(
                "customer",
                customer
        );


        model.addAttribute(
                "grades",
                customerGradeService
                        .findAllGrades()
        );


        return "customer/detail";
    }



    // =====================================================
    // 고객 기본정보 수정
    // =====================================================

    /**
     * 관리자 고객 상세 화면에서
     * 고객명과 전화번호를 수정합니다.
     *
     * 전화번호 형식 정규화와 중복 검사는
     * Service에서 처리합니다.
     */
    @PostMapping("/{customerId}/update")
    public String updateCustomer(

            @PathVariable
            Long customerId,

            @RequestParam("customerName")
            String customerName,

            @RequestParam("phone")
            String phone,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 기본정보 수정 요청 customerId={}, customerName={}",
                customerId,
                customerName
        );


        customerProfileService
                .updateCustomer(
                        customerId,
                        customerName,
                        phone
                );


        redirectAttributes.addFlashAttribute(
                "message",
                "고객 정보가 수정되었습니다."
        );


        log.info(
                "관리자 고객 기본정보 수정 완료 customerId={}",
                customerId
        );


        return "redirect:/admin/customers/"
                + customerId;
    }



    // =====================================================
    // 고객 비활성 처리
    // =====================================================

    /**
     * 고객 데이터를 삭제하지 않고
     * ACTIVE_YN 값을 N으로 변경합니다.
     */
    @PostMapping("/{customerId}/deactivate")
    public String deactivateCustomer(

            @PathVariable
            Long customerId,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 비활성 요청 customerId={}",
                customerId
        );


        customerProfileService
                .deactivateCustomer(
                        customerId
                );


        redirectAttributes.addFlashAttribute(
                "message",
                "고객이 비활성 상태로 변경되었습니다."
        );


        log.info(
                "관리자 고객 비활성 완료 customerId={}",
                customerId
        );


        return "redirect:/admin/customers/"
                + customerId;
    }



    // =====================================================
    // 고객 활성 처리
    // =====================================================

    /**
     * 비활성 고객을 다시
     * ACTIVE_YN = Y 상태로 변경합니다.
     */
    @PostMapping("/{customerId}/activate")
    public String activateCustomer(

            @PathVariable
            Long customerId,

            RedirectAttributes redirectAttributes
    ) {


        log.info(
                "관리자 고객 활성 요청 customerId={}",
                customerId
        );


        customerProfileService
                .activateCustomer(
                        customerId
                );


        redirectAttributes.addFlashAttribute(
                "message",
                "고객이 활성 상태로 변경되었습니다."
        );


        log.info(
                "관리자 고객 활성 완료 customerId={}",
                customerId
        );


        return "redirect:/admin/customers/"
                + customerId;
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
        // 수동 등급 고객은 자동 재계산 제외
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