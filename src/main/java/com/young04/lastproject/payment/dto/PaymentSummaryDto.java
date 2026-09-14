package com.young04.lastproject.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSummaryDto {

    private long todaySales;
    private long monthSales;
    private long todayPaymentCount;
    private long averagePaymentAmount;
    private Double todayChangeRate;
    private Double monthChangeRate;
}
