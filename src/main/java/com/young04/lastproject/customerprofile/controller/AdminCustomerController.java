package com.young04.lastproject.customerprofile.controller;

import com.young04.lastproject.customerprofile.dto.CustomerDetailResponse;
import com.young04.lastproject.customerprofile.dto.CustomerResponse;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerProfileService customerProfileService;

    // 관리자 고객 목록 + 검색
    @GetMapping
    public String customerList(
            CustomerSearchCondition condition,
            Model model
    ) {

        log.info("관리자 고객 목록 조회");

        List<CustomerProfile> customers =
                customerProfileService.searchCustomers(condition);

        List<CustomerResponse> customerResponses =
                customers.stream()
                        .map(CustomerResponse::from)
                        .toList();

        model.addAttribute("customers", customerResponses);
        model.addAttribute("condition", condition);

        return "customer/list";
    }

    // 관리자 고객 상세 조회
    @GetMapping("/{customerId}")
    public String customerDetail(
            @PathVariable Long customerId,
            Model model
    ) {

        log.info(
                "관리자 고객 상세 조회 customerId={}",
                customerId
        );

        Optional<CustomerProfile> customer =
                customerProfileService.findByCustomerId(customerId);

        if (customer.isEmpty()) {

            log.warn(
                    "고객을 찾을 수 없음 customerId={}",
                    customerId
            );

            return "redirect:/admin/customers";
        }

        CustomerDetailResponse customerDetail =
                CustomerDetailResponse.from(customer.get());

        model.addAttribute("customer", customerDetail);

        return "customer/detail";
    }
}