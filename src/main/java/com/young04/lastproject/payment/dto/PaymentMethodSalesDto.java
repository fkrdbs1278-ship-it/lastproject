package com.young04.lastproject.payment.dto;

import com.young04.lastproject.payment.entity.PaymentMethod;
import lombok.Getter;

@Getter
public class PaymentMethodSalesDto {

    private final PaymentMethod paymentMethod;
    private final Long salesAmount;
    private final Long paymentCount;
    private final double percentage;

    /**
     * Repository 조회용
     */
    public PaymentMethodSalesDto(
            PaymentMethod paymentMethod,
            Long salesAmount,
            Long paymentCount
    ) {
        this(paymentMethod, salesAmount, paymentCount, 0.0);
    }

    /**
     * 화면 표시용
     */
    public PaymentMethodSalesDto(
            PaymentMethod paymentMethod,
            Long salesAmount,
            Long paymentCount,
            double percentage
    ) {
        this.paymentMethod = paymentMethod;
        this.salesAmount = salesAmount == null ? 0L : salesAmount;
        this.paymentCount = paymentCount == null ? 0L : paymentCount;
        this.percentage = percentage;
    }

    public String getMethodLabel() {
        if (paymentMethod == null) {
            return "-";
        }

        return paymentMethod.getLabel();
    }
}
