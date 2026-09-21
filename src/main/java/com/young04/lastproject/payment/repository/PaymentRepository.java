package com.young04.lastproject.payment.repository;

import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentStatus;
import com.young04.lastproject.reservation.entity.CustomerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 예약 번호로 결제 조회
     */
    Optional<Payment> findByReservation_ReservationNo(Long reservationNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.reservation.reservationNo = :reservationNo")
    Optional<Payment> findByReservationNoForUpdate(@Param("reservationNo") Long reservationNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentNo = :paymentNo")
    Optional<Payment> findByIdForUpdate(@Param("paymentNo") Long paymentNo);

    /**
     * 예약에 결제 데이터가 있는지 확인
     */
    boolean existsByReservation_ReservationNo(Long reservationNo);

    /**
     * 지정 기간의 결제 완료 내역 조회
     * 통계 계산은 Service에서 처리하여 Oracle/Hibernate 함수 의존성을 줄입니다.
     */
    @EntityGraph(attributePaths = {"reservation", "member"})
    List<Payment> findByPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtAsc(
            PaymentStatus paymentStatus,
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("""
            select coalesce(sum(p.paymentAmount), 0)
            from Payment p
            where p.paymentStatus = :paymentStatus
              and p.reservation.memberNo = :memberNo
            """)
    Long sumPaymentAmountByMemberNo(
            @Param("memberNo") Long memberNo,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("""
            select coalesce(sum(p.paymentAmount), 0)
            from Payment p
            where p.paymentStatus = :paymentStatus
              and p.reservation.customerType = :customerType
              and p.reservation.guestPhone = :guestPhone
            """)
    Long sumPaymentAmountByGuestPhone(
            @Param("guestPhone") String guestPhone,
            @Param("customerType") CustomerType customerType,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );


    /**
     * 최근 결제/환불 내역 조회
     */
    @EntityGraph(attributePaths = {"reservation", "member"})
    List<Payment> findByPaymentStatusInOrderByPaidAtDesc(
            Collection<PaymentStatus> paymentStatuses,
            Pageable pageable
    );
}
