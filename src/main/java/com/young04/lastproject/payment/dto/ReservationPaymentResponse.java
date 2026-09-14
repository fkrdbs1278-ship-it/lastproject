package com.young04.lastproject.payment.dto;

import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentMethod;
import com.young04.lastproject.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationPaymentResponse {

    private Long paymentNo;
    private Long reservationNo;
    private Long originalAmount;
    private Long discountAmount;
    private Long paymentAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    public static ReservationPaymentResponse from(Payment payment) {
        return ReservationPaymentResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .reservationNo(
                        payment.getReservation() == null
                                ? null
                                : payment.getReservation().getReservationNo()
                )
                .originalAmount(payment.getOriginalAmount())
                .discountAmount(payment.getDiscountAmount())
                .paymentAmount(payment.getPaymentAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .build();
    }
}
