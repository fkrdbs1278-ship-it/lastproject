package com.young04.lastproject.reservation.dto;
import lombok.Builder; import lombok.Getter; import java.math.BigDecimal;

@Getter
@Builder
public class ReservationPricePreviewResponse {
    private Long serviceMenuNo;
    private Integer originalPrice;
    private Long eventNo;
    private String eventTitle;
    private String discountType;
    private BigDecimal discountValue;
    private Integer discountAmount;
    private Integer finalPrice;
    private boolean firstVisitEligible;
}
