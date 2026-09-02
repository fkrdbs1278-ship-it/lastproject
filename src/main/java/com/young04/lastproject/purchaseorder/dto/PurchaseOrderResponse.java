package com.young04.lastproject.purchaseorder.dto;

import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 발주서 조회 결과를 화면으로 전달하는 DTO
@Getter
@Builder
public class PurchaseOrderResponse {

    // 발주서 번호
    private Long purchaseOrderNo;

    // 공급업체 이름
    private String supplierName;

    // 발주 상태
    private String orderStatus;

    // 발주 날짜와 시간
    private LocalDateTime orderDate;

    // 입고 예정 날짜
    private LocalDate expectedDate;

    // 실제 입고 날짜와 시간
    private LocalDateTime receivedDate;

    // 발주 총금액
    private Long totalAmount;

    // 발주 관련 메모
    private String memo;

    // 발주서 등록 시간
    private LocalDateTime regdate;

    // 발주서 마지막 수정 시간
    private LocalDateTime updatedate;

    // PurchaseOrder Entity를 Response DTO로 변환
    public static PurchaseOrderResponse from(PurchaseOrder purchaseOrder) {
        return PurchaseOrderResponse.builder()
                .purchaseOrderNo(purchaseOrder.getPurchaseOrderNo())
                .supplierName(purchaseOrder.getSupplierName())
                .orderStatus(purchaseOrder.getOrderStatus())
                .orderDate(purchaseOrder.getOrderDate())
                .expectedDate(purchaseOrder.getExpectedDate())
                .receivedDate(purchaseOrder.getReceivedDate())
                .totalAmount(purchaseOrder.getTotalAmount())
                .memo(purchaseOrder.getMemo())
                .regdate(purchaseOrder.getRegdate())
                .updatedate(purchaseOrder.getUpdatedate())
                .build();
    }

    // 영문 발주 상태를 화면에 표시할 한글로 변환
    public String getOrderStatusName() {
        return switch (orderStatus) {
            case "REQUESTED" -> "발주 요청";
            case "ORDERED" -> "발주 완료";
            case "RECEIVED" -> "입고 완료";
            case "CANCELED" -> "발주 취소";
            default -> orderStatus;
        };
    }
}