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
    // 관리자 고객관리 목록
    // 검색 + 필터 + 페이징
    // =====================================================

    /**
     * 고객관리의 중심 화면입니다.
     *
     * URL:
     *
     * /admin/customers
     *
     *
     * 이 화면에서:
     *
     * - 고객 전체 목록
     * - 고객명 검색
     * - 전화번호 검색
     * - 회원 / 비회원 필터
     * - 고객 등급 필터
     * - 활성 / 비활성 필터
     * - 30일 / 60일 이상 미방문 필터
     * - 페이징
     *
     * 을 처리합니다.
     *
     *
     * 한 페이지 고객 수:
     *
     * 10명
     *
     *
     * 재방문 권장일 기능은 사용하지 않습니다.
     * 장기 미방문 관리는 LAST_VISIT_DATE를 기준으로
     * 30일 / 60일 단위로 처리합니다.
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
        // Pageable 생성
        // -------------------------------------------------

        Pageable pageable =
                PageRequest.of(
                        currentPage,
                        pageSize
                );



        log.info(
                "관리자 고객관리 목록 조회 page={}, size={}, keyword={}, customerType={}, gradeCode={}, activeYn={}, inactiveDays={}",
                currentPage,
                pageSize,
                condition.getKeyword(),
                condition.getCustomerType(),
                condition.getGradeCode(),
                condition.getActiveYn(),
                condition.getInactiveDays()
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
        //
        // HTML에서:
        //
        // ${customers}
        //
        // 형태로 사용할 수 있도록 content만 전달합니다.
        //
        // -------------------------------------------------

        model.addAttribute(
                "customers",
                customerPage.getContent()
        );



        // -------------------------------------------------
        // Page 객체 전체 전달
        // -------------------------------------------------

        model.addAttribute(
                "customerPage",
                customerPage
        );



        // -------------------------------------------------
        // 현재 페이지
        // -------------------------------------------------

        model.addAttribute(
                "currentPage",
                customerPage.getNumber()
        );



        // -------------------------------------------------
        // 전체 페이지 수
        // -------------------------------------------------

        model.addAttribute(
                "totalPages",
                customerPage.getTotalPages()
        );



        // -------------------------------------------------
        // 전체 검색 결과 수
        // -------------------------------------------------

        model.addAttribute(
                "totalElements",
                customerPage.getTotalElements()
        );



        // -------------------------------------------------
        // 페이지당 고객 수
        // -------------------------------------------------

        model.addAttribute(
                "pageSize",
                customerPage.getSize()
        );



        // -------------------------------------------------
        // 고객 등급 필터 / 등급관리용 목록
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

    /**
     * 고객관리 목록에서
     *
     * [전화예약 고객 등록]
     *
     * 버튼을 클릭하면 이동합니다.
     *
     *
     * URL:
     *
     * /admin/customers/new
     */
    @GetMapping("/new")
    public String createGuestCustomerForm(
            Model model
    ) {

        log.info(
                "전화예약 고객 등록 화면"
        );


        /*
         * 예외 처리 후 RedirectAttributes를 통해
         * 기존 입력값이 전달되어 온 경우에는
         * 새 DTO로 덮어쓰지 않습니다.
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
     * 전화예약 고객을
     * CUSTOMER_PROFILE에 등록합니다.
     *
     *
     * 신규 고객 기본값:
     *
     * MEMBER_NO       = NULL
     * CUSTOMER_TYPE   = GUEST
     * GRADE_CODE      = NORMAL
     * GRADE_MANUAL_YN = N
     * LAST_VISIT_DATE = NULL
     * VISIT_COUNT     = 0
     * TOTAL_PAYMENT   = 0
     * ACTIVE_YN       = Y
     *
     *
     * 전화번호:
     *
     * 01012345678
     *
     * 또는
     *
     * 010-1234-5678
     *
     * 로 입력해도 Service에서
     *
     * 010-1234-5678
     *
     * 형식으로 저장합니다.
     *
     *
     * 등록 성공 후:
     *
     * /admin/customers
     *
     * 고객관리 목록으로 돌아갑니다.
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
        // 입력값 Validation 실패
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
        // 등록 성공 메시지
        // -------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "message",
                "전화예약 고객이 정상적으로 등록되었습니다."
        );



        log.info(
                "전화예약 고객 등록 완료 customerId={}, customerName={}",
                savedCustomer.getCustomerId(),
                savedCustomer.getCustomerName()
        );



        // -------------------------------------------------
        // 고객관리 목록으로 이동
        // -------------------------------------------------
        //
        // 기존:
        //
        // /admin/customers/{customerId}
        //
        //
        // 변경:
        //
        // /admin/customers
        //
        // -------------------------------------------------

        return "redirect:/admin/customers";
    }



    // =====================================================
    // 관리자 고객 상세
    // =====================================================

    /**
     * 고객관리 목록의
     *
     * [상세]
     *
     * 버튼에서 접근합니다.
     *
     *
     * URL:
     *
     * /admin/customers/{customerId}
     */
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



        // -------------------------------------------------
        // 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );



        // -------------------------------------------------
        // 고객 정보
        // -------------------------------------------------

        model.addAttribute(
                "customer",
                customer
        );



        // -------------------------------------------------
        // 등급 변경용 목록
        // -------------------------------------------------

        model.addAttribute(
                "grades",
                customerGradeService
                        .findAllGrades()
        );



        return "customer/detail";
    }



    // =====================================================
    // 고객 자동 등급 재계산
    // =====================================================

    /**
     * 현재:
     *
     * 방문 횟수
     * 누적 결제액
     *
     * 기준으로 고객 등급을 다시 계산합니다.
     */
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
        // 수동 등급 고객
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
                    "현재 방문 횟수와 누적 결제액을 기준으로 "
                            + "고객 등급을 재계산했습니다."
            );
        }



        return "redirect:/admin/customers/"
                + customerId;
    }



    // =====================================================
    // 관리자 고객 등급 수동 변경
    // =====================================================

    /**
     * 관리자가 고객 등급을
     * NORMAL / REGULAR / VIP 중 하나로
     * 직접 지정합니다.
     *
     * 변경 후:
     *
     * GRADE_MANUAL_YN = Y
     */
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
    // 수동 등급 → 자동 등급 관리
    // =====================================================

    /**
     * 관리자가 수동으로 지정했던 등급을 해제하고
     * 다시 자동 등급 관리 상태로 전환합니다.
     *
     * 변경 후:
     *
     * GRADE_MANUAL_YN = N
     */
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



        CustomerProfile customer =
                customerProfileService
                        .changeToAutomaticGrade(
                                customerId
                        );



        redirectAttributes.addFlashAttribute(
                "message",
                "자동 등급 관리로 전환되었습니다."
        );



        log.info(
                "관리자 고객 자동 등급 전환 완료 customerId={}, gradeCode={}",
                customerId,
                customer
                        .getCustomerGrade()
                        .getGradeCode()
        );



        return "redirect:/admin/customers/"
                + customerId;
    }
}