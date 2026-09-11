package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder public class SalonEventOptionResponse {
    private Long eventNo;
    private String title;
    private String content;
    private String eventType;
    private String imageUrl;
    private String targetCategory;
    private String discountType;
    private BigDecimal discountValue;
    private Long minPaymentAmount;
    private Long maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
