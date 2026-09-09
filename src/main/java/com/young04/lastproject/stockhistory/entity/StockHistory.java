package com.young04.lastproject.stockhistory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 자재의 입고·사용·폐기·조정 이력을 저장하는 Entity
@Entity
@Table(name = "STOCK_HISTORY")
@Getter
@Setter
@NoArgsConstructor
public class StockHistory {

    // 재고 이력 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_HISTORY_NO")
    private Long stockHistoryNo;

    // 자재 번호
    @Column(name = "MATERIAL_NO", nullable = false)
    private Long materialNo;

    // 변동 구분: PURCHASE_IN, USE, DISCARD, MANUAL_IN, MANUAL_OUT
    @Column(
            name = "MOVEMENT_TYPE",
            nullable = false,
            length = 30
    )
    private String movementType;

    // 변동 수량
    @Column(
            name = "QUANTITY",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal quantity;

    // 재고 변동 당시 사용한 단위
    @Column(name = "UNIT_CODE", length = 20)
    private String unitCode;

    // 변동 전 재고
    @Column(
            name = "BEFORE_STOCK",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal beforeStock;

    // 변동 후 재고
    @Column(
            name = "AFTER_STOCK",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal afterStock;

    // 변동이 발생한 기능 구분: PURCHASE_ORDER, RESERVATION, MANUAL
    @Column(name = "REFERENCE_TYPE", length = 30)
    private String referenceType;

    // 발주 번호 또는 예약 번호
    @Column(name = "REFERENCE_NO")
    private Long referenceNo;

    // 재고 변동 사유
    @Column(name = "MEMO", length = 500)
    private String memo;

    // 재고 변동 등록일
    @CreationTimestamp
    @Column(
            name = "REGDATE",
            nullable = false,
            updatable = false
    )
    private LocalDateTime regdate;
}