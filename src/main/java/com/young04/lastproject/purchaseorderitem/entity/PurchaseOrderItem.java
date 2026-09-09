package com.young04.lastproject.purchaseorderitem.entity;

import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// PURCHASE_ORDER_ITEM 테이블과 연결되는 발주 품목 Entity
@Entity
@Table(
        name = "PURCHASE_ORDER_ITEM",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "PURCHASE_ORDER_ITEM_ORDER_MATERIAL_UK",
                        columnNames = {
                                "PURCHASE_ORDER_NO",
                                "MATERIAL_NO"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderItem {

    // 발주 품목 번호이자 기본키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PURCHASE_ORDER_ITEM_NO")
    private Long purchaseOrderItemNo;

    // 해당 품목이 포함된 발주서
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PURCHASE_ORDER_NO", nullable = false)
    private PurchaseOrder purchaseOrder;

    // 발주할 자재
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MATERIAL_NO", nullable = false)
    private Material material;

    // 발주 요청 수량
    @Column(
            name = "ORDER_QUANTITY",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal orderQuantity;

    // 실제 입고된 수량
    @Column(
            name = "RECEIVED_QUANTITY",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal receivedQuantity;

    // 발주 당시 자재 한 단위의 구매 가격
    @Column(
            name = "UNIT_PRICE",
            precision = 12,
            scale = 0,
            nullable = false
    )
    private BigDecimal unitPrice;

    // 발주 품목이 등록된 날짜와 시간
    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;

    // 새로운 발주 품목 생성
    public PurchaseOrderItem(
            PurchaseOrder purchaseOrder,
            Material material,
            BigDecimal orderQuantity,
            BigDecimal unitPrice
    ) {
        this.purchaseOrder = purchaseOrder;
        this.material = material;
        this.orderQuantity = orderQuantity;
        this.unitPrice = unitPrice;
        this.receivedQuantity = BigDecimal.ZERO;
    }

    // 실제 입고된 수량 변경
    public void changeReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    // 처음 저장하기 전에 기본값과 등록일 설정
    @PrePersist
    protected void onCreate() {
        if (receivedQuantity == null) {
            receivedQuantity = BigDecimal.ZERO;
        }

        regdate = LocalDateTime.now();
    }
}