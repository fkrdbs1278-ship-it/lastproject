package com.young04.lastproject.payment.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Getter
public class PaymentTrendDto {

    private final String periodKey;
    private final Long amount;

    /**
     * 일반 생성자
     */
    public PaymentTrendDto(String periodKey, Long amount) {
        this.periodKey = periodKey;
        this.amount = amount == null ? 0L : amount;
    }

    /**
     * Hibernate function('TO_CHAR', ...) 조회 대응용
     */
    public PaymentTrendDto(Object periodKey, Long amount) {
        this.periodKey =
                periodKey == null
                        ? ""
                        : periodKey.toString();

        this.amount =
                amount == null
                        ? 0L
                        : amount;
    }

    /**
     * 그래프에 표시할 날짜 이름
     */
    public String getLabel() {

        if (periodKey == null || periodKey.isBlank()) {
            return "";
        }

        if (periodKey.length() == 10) {

            LocalDate date =
                    LocalDate.parse(periodKey);

            return date.format(
                    DateTimeFormatter.ofPattern("M/d")
            );
        }

        if (periodKey.length() == 7) {

            YearMonth yearMonth =
                    YearMonth.parse(periodKey);

            return yearMonth.format(
                    DateTimeFormatter.ofPattern("yyyy.MM")
            );
        }

        return periodKey;
    }
}