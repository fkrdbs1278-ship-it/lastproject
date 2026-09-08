package com.young04.lastproject.purchaseorderitem.dto;

import com.young04.lastproject.purchaseorderitem.entity.PurchaseOrderItem;
import lombok.Getter;

import java.math.BigDecimal;

// 발주 품목 조회 결과를 화면에 전달하는 DTO
@Getter
public class PurchaseOrderItemResponse {

    // 발주 품목 번호
    private final Long purchaseOrderItemNo;

    // 자재 번호
    private final Long materialNo;

    // 자재명
    private final String materialName;

    // 자재 단위
    private final String unitCode;

    // 발주 요청 수량
    private final BigDecimal orderQuantity;

    // 실제 입고 수량
    private final BigDecimal receivedQuantity;

    // 한 단위의 구매 가격
    private final BigDecimal unitPrice;

    // 수량과 구매 가격을 곱한 품목 금액
    private final BigDecimal itemAmount;

    // Entity 조회 결과를 화면 출력용 DTO로 변환
    public PurchaseOrderItemResponse(PurchaseOrderItem item) {
        this.purchaseOrderItemNo = item.getPurchaseOrderItemNo();
        this.materialNo = item.getMaterial().getMaterialNo();
        this.materialName = item.getMaterial().getMaterialName();
        this.unitCode = item.getMaterial().getUnitCode();
        this.orderQuantity = item.getOrderQuantity();
        this.receivedQuantity = item.getReceivedQuantity();
        this.unitPrice = item.getUnitPrice();
        this.itemAmount = item.getOrderQuantity()
                .multiply(item.getUnitPrice());
    }
}