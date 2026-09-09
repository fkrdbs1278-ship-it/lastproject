package com.young04.lastproject.purchaseorderitem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// 발주 품목 등록 화면의 입력값을 전달하는 DTO
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItemCreateRequest {

    // 발주할 자재 번호
    @NotNull(message = "발주할 자재를 선택해 주세요.")
    private Long materialNo;

    // 발주 요청 수량
    @NotNull(message = "발주 수량을 입력해 주세요.")
    @DecimalMin(
            value = "0.01",
            message = "발주 수량은 0보다 커야 합니다."
    )
    private BigDecimal orderQuantity;

    // 발주 당시 자재 한 단위의 구매 가격
    @NotNull(message = "구매 가격을 입력해 주세요.")
    @DecimalMin(
            value = "0",
            message = "구매 가격은 0원 이상이어야 합니다."
    )
    private BigDecimal unitPrice;
}