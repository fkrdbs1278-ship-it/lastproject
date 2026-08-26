package com.young04.lastproject.stockhistory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "STOCK_HISTORY")
@Getter
@Setter
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_HISTORY_NO")
    private Long stockHistoryNo;

    @Column(name = "MATERIAL_NO", nullable = false)
    private Long materialNo;

    @Column(name = "MOVEMENT_TYPE", nullable = false, length = 20)
    private String movementType;

    @Column(name = "QUANTITY", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "BEFORE_STOCK", nullable = false, precision = 12, scale = 2)
    private BigDecimal beforeStock;

    @Column(name = "AFTER_STOCK", nullable = false, precision = 12, scale = 2)
    private BigDecimal afterStock;

    @Column(name = "REFERENCE_TYPE", length = 30)
    private String referenceType;

    @Column(name = "REFERENCE_NO")
    private Long referenceNo;

    @Column(name = "MEMO", length = 500)
    private String memo;

    @CreationTimestamp
    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;
}
