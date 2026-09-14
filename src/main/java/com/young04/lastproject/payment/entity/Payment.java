package com.young04.lastproject.payment.entity;

import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    /* 결제 번호 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_NO")
    private Long paymentNo;

    /* 결제 대상 예약 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RESERVATION_NO", nullable = false)
    private Reservation reservation;

    /* 회원 결제일 경우 회원 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;

    /* 할인 전 금액 */
    @Column(name = "ORIGINAL_AMOUNT", nullable = false)
    private Long originalAmount;

    /* 할인 금액 */
    @Column(name = "DISCOUNT_AMOUNT", nullable = false)
    private Long discountAmount;

    /* 실제 결제 금액 */
    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private Long paymentAmount;

    /* 결제 수단 */
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", length = 20)
    private PaymentMethod paymentMethod;

    /* 결제 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /* 결제 완료 시간 */
    @Column(name = "PAID_AT")
    private LocalDateTime paidAt;

    /* 환불 시간 */
    @Column(name = "REFUNDED_AT")
    private LocalDateTime refundedAt;

    /* 결제 메모 */
    @Column(name = "MEMO", length = 500)
    private String memo;

    /* 생성일 */
    @Column(name = "REGDATE", nullable = false)
    private LocalDateTime regdate;

    /* 수정일 */
    @Column(name = "UPDATEDATE")
    private LocalDateTime updatedate;

    /**
     * 시술 완료 후 결제 대기(UNPAID) 데이터 생성
     * 예약 당시 저장된 금액 snapshot을 그대로 사용합니다.
     */
    public static Payment createUnpaid(
            Reservation reservation,
            Member member,
            Long originalAmount,
            Long discountAmount,
            Long paymentAmount,
            String memo
    ) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약 정보가 필요합니다.");
        }

        if (originalAmount == null || originalAmount < 0) {
            throw new IllegalArgumentException("결제 원금이 올바르지 않습니다.");
        }

        long discount = discountAmount == null ? 0L : discountAmount;

        if (discount < 0 || discount > originalAmount) {
            throw new IllegalArgumentException("할인 금액이 올바르지 않습니다.");
        }

        if (paymentAmount == null || paymentAmount < 0) {
            throw new IllegalArgumentException("최종 결제 금액이 올바르지 않습니다.");
        }

        // DB 제약조건과 예약 snapshot이 서로 다른 경우 조용히 재계산하지 않고 오류로 처리합니다.
        if (!paymentAmount.equals(originalAmount - discount)) {
            throw new IllegalStateException("예약 가격 snapshot 값이 서로 일치하지 않습니다.");
        }

        Payment payment = new Payment();
        payment.reservation = reservation;
        payment.member = member;
        payment.originalAmount = originalAmount;
        payment.discountAmount = discount;
        payment.paymentAmount = paymentAmount;
        payment.paymentMethod = null;
        payment.paymentStatus = PaymentStatus.UNPAID;
        payment.paidAt = null;
        payment.refundedAt = null;
        payment.memo = memo;

        return payment;
    }

    /**
     * 기존 호출부 호환용 overload
     */
    public static Payment createUnpaid(
            Reservation reservation,
            Member member,
            Long originalAmount,
            Long discountAmount,
            String memo
    ) {
        long discount = discountAmount == null ? 0L : discountAmount;

        return createUnpaid(
                reservation,
                member,
                originalAmount,
                discount,
                originalAmount == null ? null : originalAmount - discount,
                memo
        );
    }

    /**
     * 결제 완료 처리
     */
    public void pay(PaymentMethod paymentMethod) {
        if (this.paymentStatus != PaymentStatus.UNPAID) {
            throw new IllegalStateException("미결제 상태만 결제 완료 처리할 수 있습니다.");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단이 필요합니다.");
        }

        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.refundedAt = null;
    }

    /**
     * 환불 처리
     */
    public void refund() {
        if (this.paymentStatus != PaymentStatus.PAID) {
            throw new IllegalStateException("결제 완료 건만 환불할 수 있습니다.");
        }

        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void prePersist() {
        if (discountAmount == null) {
            discountAmount = 0L;
        }

        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.UNPAID;
        }

        if (regdate == null) {
            regdate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedate = LocalDateTime.now();
    }
}
