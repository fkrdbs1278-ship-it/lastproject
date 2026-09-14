package com.young04.lastproject.payment.dto;

public enum PaymentTrendUnit {

    DAY,
    MONTH,
    YEAR;

    public static PaymentTrendUnit from(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }

        try {
            return PaymentTrendUnit.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DAY;
        }
    }
}
