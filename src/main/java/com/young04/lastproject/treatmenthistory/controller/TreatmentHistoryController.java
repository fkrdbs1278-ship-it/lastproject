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
@RequestMapping("/admin/customers/{customerId}/treatments")
public class TreatmentHistoryController {

    // =====================================================
    // Service
    // =====================================================

    private final TreatmentHistoryService treatmentHistoryService;

    // 고객 기본정보 조회
    private final CustomerProfileService customerProfileService;


    // =====================================================
    // 고객별 시술 이력 조회
    // =====================================================

    @GetMapping
    public String treatmentList(
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
        // 2. 고객별 시술 이력 조회
        // -------------------------------------------------

        List<TreatmentHistoryResponse> treatments =
                treatmentHistoryService
                        .findByCustomerId(customerId)
                        .stream()
                        .map(TreatmentHistoryResponse::from)
                        .toList();


        log.info(
                "고객 시술 이력 화면 조회 customerId={}, customerName={}, count={}",
                customerId,
                customer.getCustomerName(),
                treatments.size()
        );


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
                "treatments",
                treatments
        );


        return "customer/treatment";
    }
}