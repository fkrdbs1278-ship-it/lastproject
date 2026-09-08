package com.young04.lastproject.purchaseorder.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// 발주 품목별 실제 입고 수량을 전달하는 DTO
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderReceiveRequest {

    // Key는 발주 품목 번호이고 Value는 실제 입고된 수량입니다.
    private Map<Long, BigDecimal> receivedQuantities = new HashMap<>();
}