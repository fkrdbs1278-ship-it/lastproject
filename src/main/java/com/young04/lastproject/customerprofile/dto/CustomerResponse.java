package com.young04.lastproject.customerprofile.dto;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CustomerResponse {

    private Long customerId;

    private String customerName;

    private String phone;

    private String customerType;

    private String gradeCode;

    private String gradeName;

    private LocalDate lastVisitDate;

    private Integer visitCount;

    private BigDecimal totalPayment;

    private LocalDate revisitRecommendedDate;

    private String activeYn;


    // Entity → DTO 변환
    public static CustomerResponse from(CustomerProfile customer) {

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .phone(customer.getPhone())
                .customerType(customer.getCustomerType())
                .gradeCode(customer.getCustomerGrade().getGradeCode())
                .gradeName(customer.getCustomerGrade().getGradeName())
                .lastVisitDate(customer.getLastVisitDate())
                .visitCount(customer.getVisitCount())
                .totalPayment(customer.getTotalPayment())
                .revisitRecommendedDate(
                        customer.getRevisitRecommendedDate()
                )
                .activeYn(customer.getActiveYn())
                .build();
    }
}
