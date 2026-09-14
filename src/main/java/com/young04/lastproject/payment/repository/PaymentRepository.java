package com.young04.lastproject.payment.repository;

import com.young04.lastproject.payment.dto.PaymentMethodSalesDto;
import com.young04.lastproject.payment.dto.PaymentTrendDto;
import com.young04.lastproject.payment.dto.PopularServiceDto;
import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /**
     * 예약에 결제 데이터가 있는지 확인
     */
    boolean existsByReservation_ReservationNo(Long reservationNo);

    /**
     * 기간 내 결제 완료 금액 합계
     */
    @Query("""
            SELECT COALESCE(SUM(p.paymentAmount), 0)
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            """)
    Long sumPaymentAmount(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 기간 내 결제 완료 건수
     */
    @Query("""
            SELECT COUNT(p)
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            """)
    long countPayments(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 일별 매출
     */
    @Query("""
            SELECT new com.young04.lastproject.payment.dto.PaymentTrendDto(
                function('TO_CHAR', p.paidAt, 'YYYY-MM-DD'),
                SUM(p.paymentAmount)
            )
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            GROUP BY function('TO_CHAR', p.paidAt, 'YYYY-MM-DD')
            ORDER BY function('TO_CHAR', p.paidAt, 'YYYY-MM-DD')
            """)
    List<PaymentTrendDto> findDailyTrend(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 월별 매출
     */
    @Query("""
            SELECT new com.young04.lastproject.payment.dto.PaymentTrendDto(
                function('TO_CHAR', p.paidAt, 'YYYY-MM'),
                SUM(p.paymentAmount)
            )
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            GROUP BY function('TO_CHAR', p.paidAt, 'YYYY-MM')
            ORDER BY function('TO_CHAR', p.paidAt, 'YYYY-MM')
            """)
    List<PaymentTrendDto> findMonthlyTrend(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 연도별 매출
     */
    @Query("""
            SELECT new com.young04.lastproject.payment.dto.PaymentTrendDto(
                function('TO_CHAR', p.paidAt, 'YYYY'),
                SUM(p.paymentAmount)
            )
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            GROUP BY function('TO_CHAR', p.paidAt, 'YYYY')
            ORDER BY function('TO_CHAR', p.paidAt, 'YYYY')
            """)
    List<PaymentTrendDto> findYearlyTrend(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 조회 기간 인기 시술
     */
    @Query("""
            SELECT new com.young04.lastproject.payment.dto.PopularServiceDto(
                r.serviceNameSnapshot,
                COUNT(p),
                SUM(p.paymentAmount)
            )
            FROM Payment p
            JOIN p.reservation r
            WHERE p.paymentStatus = :status
              AND p.paidAt >= :start
              AND p.paidAt < :end
            GROUP BY r.serviceNameSnapshot
            ORDER BY COUNT(p) DESC, SUM(p.paymentAmount) DESC
            """)
    List<PopularServiceDto> findPopularServices(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    /**
     * 조회 기간 결제 수단별 매출
     */
    @Query("""
            SELECT new com.young04.lastproject.payment.dto.PaymentMethodSalesDto(
                p.paymentMethod,
                SUM(p.paymentAmount),
                COUNT(p)
            )
            FROM Payment p
            WHERE p.paymentStatus = :status
              AND p.paymentMethod IS NOT NULL
              AND p.paidAt >= :start
              AND p.paidAt < :end
            GROUP BY p.paymentMethod
            ORDER BY SUM(p.paymentAmount) DESC
            """)
    List<PaymentMethodSalesDto> findPaymentMethodSales(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 최근 결제/환불 내역
     */
    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.reservation r
            LEFT JOIN FETCH p.member m
            WHERE p.paymentStatus IN :statuses
            ORDER BY p.paidAt DESC
            """)
    List<Payment> findRecentPayments(
            @Param("statuses") Collection<PaymentStatus> statuses,
            Pageable pageable
    );
}
