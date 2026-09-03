package com.young04.lastproject.treatmenthistory.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/test/customer")
public class CustomerUserTestController {


    // =====================================================
    // Service
    // =====================================================

    private final CustomerProfileService customerProfileService;

    private final TreatmentHistoryService treatmentHistoryService;



    // =====================================================
    // 1. 고객용 내 시술 이력 테스트
    // =====================================================

    @GetMapping("/treatments/{customerId}")
    public String myTreatmentTest(
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


        log.info(
                "고객용 내 시술 이력 테스트 customerId={}, customerName={}, count={}",
                customerId,
                customer.getCustomerName(),
                treatments.size()
        );


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


        return "customer/test/my-treatment-test";
    }



    // =====================================================
    // 2. 고객 활동 내역 테스트
    // =====================================================

    @GetMapping("/activity/{customerId}")
    public String myActivityTest(
            @PathVariable Long customerId,
            Model model
    ) {

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );


        log.info(
                "고객 활동 내역 테스트 customerId={}, customerName={}",
                customerId,
                customer.getCustomerName()
        );


        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "customerId",
                customerId
        );


        // 아직 실제 파트가 통합되지 않았음을 명확히 표시
        model.addAttribute(
                "reservationIntegrated",
                false
        );

        model.addAttribute(
                "reviewIntegrated",
                false
        );


        return "customer/test/my-activity-test";
    }



    // =====================================================
    // 3. 개인정보 제공 범위 테스트
    // =====================================================

    /**
     * 사용자 기능:
     *
     * 개인정보 제공 범위 확인
     *
     * 현재 3part에서는 CRM이 사용하는 개인정보 범주를
     * 사용자 화면에서 확인하는 구조까지만 테스트합니다.
     *
     * 실제:
     *
     * - 이용약관 동의 여부
     * - 개인정보 수집·이용 동의 여부
     * - 마케팅 수신 동의 여부
     *
     * 는 1part 회원 기능 통합 후 MEMBER 기준으로 연결합니다.
     */
    @GetMapping("/privacy/{customerId}")
    public String myPrivacyTest(
            @PathVariable Long customerId,
            Model model
    ) {

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );


        log.info(
                "고객 개인정보 제공 범위 테스트 customerId={}, customerName={}",
                customerId,
                customer.getCustomerName()
        );


        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "customerId",
                customerId
        );


        // 회원 약관 동의정보는 1part 통합 후 사용
        model.addAttribute(
                "memberConsentIntegrated",
                false
        );


        return "customer/test/my-privacy-test";
    }
}