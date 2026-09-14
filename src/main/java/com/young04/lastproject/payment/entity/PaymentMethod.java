package com.young04.lastproject.payment.entity;

/**
 * 결제 수단
 */
public enum PaymentMethod {

    CARD("카드"),
    CASH("현금"),
    TRANSFER("계좌이체"),
    PREPAID("선결제");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
