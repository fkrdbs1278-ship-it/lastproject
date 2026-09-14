package com.young04.lastproject.payment.dto;

import com.young04.lastproject.payment.entity.PaymentMethod;
import com.young04.lastproject.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentPaymentDto {

    private LocalDateTime paidAt;
    private String customerName;
    private String serviceName;
    private PaymentMethod paymentMethod;
    private Long paymentAmount;
    private PaymentStatus status;

    public String getPaymentMethodLabel() {
        if (paymentMethod == null) {
            return "-";
        }

        return paymentMethod.getLabel();
    }

    public String getStatusLabel() {
        if (status == null) {
            return "-";
        }

        return status.getLabel();
    }
}
