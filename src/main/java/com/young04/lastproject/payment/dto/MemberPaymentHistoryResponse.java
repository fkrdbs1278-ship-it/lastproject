package com.young04.lastproject.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberPaymentHistoryResponse {

    private Long paymentNo;

    private Long reservationNo;

    private String serviceName;

    private LocalDateTime reservationStartAt;

    private Long originalAmount;

    private Long discountAmount;

    private Long paymentAmount;

    private String eventTitle;

    private String paymentMethod;

    private String paymentStatus;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;


    public String getPaymentMethodName() {

        if (paymentMethod == null) {

            return "-";
        }


        return switch (paymentMethod) {

            case "CARD" ->
                    "카드";

            case "CASH" ->
                    "현금";

            case "TRANSFER" ->
                    "계좌이체";

            case "PREPAID" ->
                    "선불";

            default ->
                    paymentMethod;
        };
    }


    public String getPaymentStatusName() {

        if (paymentStatus == null) {

            return "-";
        }


        return switch (paymentStatus) {

            case "PAID" ->
                    "결제 완료";

            case "REFUNDED" ->
                    "환불 완료";

            case "UNPAID" ->
                    "결제 대기";

            default ->
                    paymentStatus;
        };
    }
}
