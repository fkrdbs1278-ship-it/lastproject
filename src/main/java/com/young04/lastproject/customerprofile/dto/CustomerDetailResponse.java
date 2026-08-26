package com.young04.lastproject.customerprofile.dto;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerDetailResponse {

    private Long customerId;

    private Long memberNo;

    private String customerName;

    private String phone;

    private String customerType;

    private String gradeCode;

    private String gradeName;

    private String gradeManualYn;

    private LocalDate lastVisitDate;

    private Integer visitCount;

    private BigDecimal totalPayment;

    private LocalDate revisitRecommendedDate;

    private String activeYn;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // Entity → 상세 조회용 DTO 변환
    public static CustomerDetailResponse from(
            CustomerProfile customer
    ) {

        return CustomerDetailResponse.builder()
                .customerId(customer.getCustomerId())
                .memberNo(customer.getMemberNo())
                .customerName(customer.getCustomerName())
                .phone(customer.getPhone())
                .customerType(customer.getCustomerType())
                .gradeCode(
                        customer.getCustomerGrade().getGradeCode()
                )
                .gradeName(
                        customer.getCustomerGrade().getGradeName()
                )
                .gradeManualYn(customer.getGradeManualYn())
                .lastVisitDate(customer.getLastVisitDate())
                .visitCount(customer.getVisitCount())
                .totalPayment(customer.getTotalPayment())
                .revisitRecommendedDate(
                        customer.getRevisitRecommendedDate()
                )
                .activeYn(customer.getActiveYn())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}