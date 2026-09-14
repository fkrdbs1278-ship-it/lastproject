package com.young04.lastproject.payment.dto;

import lombok.Getter;

@Getter
public class PopularServiceDto {

    private final String serviceName;
    private final Long paymentCount;
    private final Long salesAmount;

    public PopularServiceDto(
            String serviceName,
            Long paymentCount,
            Long salesAmount
    ) {
        this.serviceName = serviceName;
        this.paymentCount = paymentCount == null ? 0L : paymentCount;
        this.salesAmount = salesAmount == null ? 0L : salesAmount;
    }
}
