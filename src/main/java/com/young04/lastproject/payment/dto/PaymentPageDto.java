package com.young04.lastproject.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaymentPageDto {

    private PaymentSummaryDto summary;
    private List<PaymentTrendDto> trend;
    private long periodTotal;
    private List<PopularServiceDto> popularServices;
    private List<PaymentMethodSalesDto> paymentMethods;
    private List<RecentPaymentDto> recentPayments;
}
