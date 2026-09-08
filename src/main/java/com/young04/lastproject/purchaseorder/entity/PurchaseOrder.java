package com.young04.lastproject.purchaseorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// PURCHASE_ORDER 테이블과 연결되는 발주서 Entity
@Entity
@Table(name = "PURCHASE_ORDER")
@Getter
@Setter
@NoArgsConstructor

public class PurchaseOrder {

    // 발주서 번호이자 기본키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PURCHASE_ORDER_NO")
    private Long purchaseOrderNo;

    // 발주를 요청할 공급업체 이름
    @Column(name = "SUPPLIER_NAME", nullable = false, length = 100)
    private String supplierName;

    // 발주 상태: REQUESTED, ORDERED, RECEIVED, CANCELED
    @Column(name = "ORDER_STATUS", nullable = false, length = 20)
    private String orderStatus = "REQUESTED";

    // 발주서가 생성된 날짜와 시간
    @CreationTimestamp
    @Column(name = "ORDER_DATE", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    // 자재가 입고될 예정 날짜
    @Column(name = "EXPECTED_DATE")
    private LocalDate expectedDate;

    // 자재가 실제로 입고된 날짜와 시간
    @Column(name = "RECEIVED_DATE")
    private LocalDateTime receivedDate;

    // 발주 품목들의 전체 금액
    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Long totalAmount = 0L;

    // 발주와 관련된 추가 메모
    @Column(name = "MEMO", length = 500)
    private String memo;

    // 발주서 데이터가 처음 등록된 시간
    @CreationTimestamp
    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;

    // 발주서 데이터가 마지막으로 수정된 시간
    @UpdateTimestamp
    @Column(name = "UPDATEDATE")
    private LocalDateTime updatedate;
}