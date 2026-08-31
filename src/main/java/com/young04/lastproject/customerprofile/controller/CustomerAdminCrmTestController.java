package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import com.young04.lastproject.treatmenthistory.dto.TreatmentHistoryResponse;
import com.young04.lastproject.treatmenthistory.service.TreatmentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/test/admin/customers")
public class CustomerAdminCrmTestController {


    private final CustomerProfileService customerProfileService;

    private final TreatmentHistoryService treatmentHistoryService;



    // =====================================================
    // 관리자 CRM 임시 테스트 화면
    // =====================================================

    @GetMapping("/{customerId}/crm")
    public String crmTestPage(
            @PathVariable Long customerId,
            Model model
    ) {

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );


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


        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "customerId",
                customerId
        );

        model.addAttribute(
                "treatments",
                treatments
        );

        model.addAttribute(
                "treatmentCount",
                treatments.size()
        );


        // 방문 완료 테스트 기본 날짜
        model.addAttribute(
                "defaultVisitDate",
                LocalDate.now()
        );


        // 임시 재방문 권장일 = 오늘 + 30일
        model.addAttribute(
                "defaultRevisitDate",
                LocalDate.now().plusDays(30)
        );


        log.info(
                "관리자 CRM 임시 테스트 화면 customerId={}, treatmentCount={}",
                customerId,
                treatments.size()
        );


        return "customer/test/admin-crm-test";
    }



    // =====================================================
    // 전화번호 기준 고객 조회 테스트
    // =====================================================

    @GetMapping("/phone-search")
    public String phoneSearch(

            @RequestParam
            String phone,

            @RequestParam
            Long currentCustomerId,

            RedirectAttributes redirectAttributes
    ) {

        Optional<CustomerProfile> foundCustomer =
                customerProfileService
                        .findByPhone(
                                phone
                        );


        if (foundCustomer.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "해당 전화번호의 고객을 찾을 수 없습니다."
            );


            return "redirect:/test/admin/customers/"
                    + currentCustomerId
                    + "/crm";
        }


        CustomerProfile customer =
                foundCustomer.get();


        redirectAttributes.addFlashAttribute(
                "message",
                "전화번호 기준 고객 조회에 성공했습니다."
        );


        return "redirect:/test/admin/customers/"
                + customer.getCustomerId()
                + "/crm";
    }



    // =====================================================
    // 방문 완료 처리 테스트
    // =====================================================

    @PostMapping("/{customerId}/visit")
    public String completeVisit(

            @PathVariable
            Long customerId,

            @RequestParam
            LocalDate visitDate,

            @RequestParam(required = false)
            LocalDate revisitRecommendedDate,

            RedirectAttributes redirectAttributes
    ) {

        try {

            CustomerProfile customer =
                    customerProfileService
                            .completeVisit(
                                    customerId,
                                    visitDate
                            );


            redirectAttributes.addFlashAttribute(
                    "message",
                    "방문 완료 처리 성공: 방문 횟수 "
                            + customer.getVisitCount()
                            + "회 / 최근 방문일 "
                            + customer.getLastVisitDate()
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/test/admin/customers/"
                + customerId
                + "/crm";
    }



    // =====================================================
    // 결제 누적 테스트
    // =====================================================

    @PostMapping("/{customerId}/payment")
    public String addPayment(

            @PathVariable
            Long customerId,

            @RequestParam
            BigDecimal paymentAmount,

            RedirectAttributes redirectAttributes
    ) {

        try {

            CustomerProfile customer =
                    customerProfileService
                            .addCustomerPayment(
                                    customerId,
                                    paymentAmount
                            );


            redirectAttributes.addFlashAttribute(
                    "message",
                    "결제 누적 처리 성공: 현재 누적 결제액 "
                            + customer.getTotalPayment()
                            + "원"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/test/admin/customers/"
                + customerId
                + "/crm";
    }
}
