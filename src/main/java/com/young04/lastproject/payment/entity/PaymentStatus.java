package com.young04.lastproject.payment.entity;

/**
 * 결제 상태
 */
public enum PaymentStatus {

    UNPAID("미결제"),
    PAID("완료"),
    REFUNDED("환불");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
