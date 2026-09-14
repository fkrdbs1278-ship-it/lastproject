package com.young04.lastproject.payment.service;

import com.young04.lastproject.payment.dto.MemberPaymentHistoryResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPaymentHistoryService {

    private final EntityManager entityManager;


    public List<MemberPaymentHistoryResponse> findMyPaymentHistory(
            Long memberNo
    ) {

        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                SELECT
                                    P.PAYMENT_NO,
                                    P.RESERVATION_NO,
                                    R.SERVICE_NAME_SNAPSHOT,
                                    R.START_AT,
                                    P.ORIGINAL_AMOUNT,
                                    P.DISCOUNT_AMOUNT,
                                    P.PAYMENT_AMOUNT,
                                    P.EVENT_TITLE_SNAPSHOT,
                                    P.PAYMENT_METHOD,
                                    P.PAYMENT_STATUS,
                                    P.PAID_AT,
                                    P.REFUNDED_AT
                                FROM PAYMENT P
                                JOIN RESERVATION R
                                  ON R.RESERVATION_NO = P.RESERVATION_NO
                                WHERE P.MEMBER_NO = :memberNo
                                ORDER BY
                                    NVL(
                                        P.PAID_AT,
                                        R.START_AT
                                    ) DESC,
                                    P.PAYMENT_NO DESC
                                """
                        )
                        .setParameter(
                                "memberNo",
                                memberNo
                        )
                        .getResultList();


        return rows.stream()
                .map(this::toResponse)
                .toList();
    }


    public Page<MemberPaymentHistoryResponse> findMyPaymentHistory(
            Long memberNo,
            int page,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        Math.max(
                                page,
                                0
                        ),
                        Math.clamp(
                                size,
                                1,
                                50
                        )
                );


        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                SELECT
                                    P.PAYMENT_NO,
                                    P.RESERVATION_NO,
                                    R.SERVICE_NAME_SNAPSHOT,
                                    R.START_AT,
                                    P.ORIGINAL_AMOUNT,
                                    P.DISCOUNT_AMOUNT,
                                    P.PAYMENT_AMOUNT,
                                    P.EVENT_TITLE_SNAPSHOT,
                                    P.PAYMENT_METHOD,
                                    P.PAYMENT_STATUS,
                                    P.PAID_AT,
                                    P.REFUNDED_AT
                                FROM PAYMENT P
                                JOIN RESERVATION R
                                  ON R.RESERVATION_NO = P.RESERVATION_NO
                                WHERE P.MEMBER_NO = :memberNo
                                ORDER BY
                                    NVL(
                                        P.PAID_AT,
                                        R.START_AT
                                    ) DESC,
                                    P.PAYMENT_NO DESC
                                """
                        )
                        .setParameter(
                                "memberNo",
                                memberNo
                        )
                        .setFirstResult(
                                Math.toIntExact(
                                        pageable.getOffset()
                                )
                        )
                        .setMaxResults(
                                pageable.getPageSize()
                        )
                        .getResultList();


        Number total =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                SELECT COUNT(*)
                                FROM PAYMENT
                                WHERE MEMBER_NO = :memberNo
                                """
                        )
                        .setParameter(
                                "memberNo",
                                memberNo
                        )
                        .getSingleResult();


        List<MemberPaymentHistoryResponse> content =
                rows.stream()
                        .map(
                                this::toResponse
                        )
                        .toList();


        return new PageImpl<>(
                content,
                pageable,
                total.longValue()
        );
    }


    public PaymentHistorySummary getSummary(
            Long memberNo
    ) {

        Object[] row =
                (Object[]) entityManager
                        .createNativeQuery(
                                """
                                SELECT
                                    NVL(
                                        SUM(
                                            CASE
                                                WHEN PAYMENT_STATUS = 'PAID'
                                                THEN 1
                                                ELSE 0
                                            END
                                        ),
                                        0
                                    ),
                                    NVL(
                                        SUM(
                                            CASE
                                                WHEN PAYMENT_STATUS = 'PAID'
                                                THEN PAYMENT_AMOUNT
                                                ELSE 0
                                            END
                                        ),
                                        0
                                    ),
                                    NVL(
                                        SUM(
                                            CASE
                                                WHEN PAYMENT_STATUS = 'REFUNDED'
                                                THEN 1
                                                ELSE 0
                                            END
                                        ),
                                        0
                                    )
                                FROM PAYMENT
                                WHERE MEMBER_NO = :memberNo
                                """
                        )
                        .setParameter(
                                "memberNo",
                                memberNo
                        )
                        .getSingleResult();


        return new PaymentHistorySummary(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2])
        );
    }


    public long calculateTotalPaidAmount(
            List<MemberPaymentHistoryResponse> payments
    ) {

        return payments.stream()
                .filter(payment ->
                        "PAID".equals(
                                payment.getPaymentStatus()
                        )
                )
                .map(
                        MemberPaymentHistoryResponse::getPaymentAmount
                )
                .filter(amount ->
                        amount != null
                )
                .mapToLong(
                        Long::longValue
                )
                .sum();
    }


    private MemberPaymentHistoryResponse toResponse(
            Object[] row
    ) {

        return MemberPaymentHistoryResponse
                .builder()
                .paymentNo(
                        toLong(row[0])
                )
                .reservationNo(
                        toLong(row[1])
                )
                .serviceName(
                        toStringValue(row[2])
                )
                .reservationStartAt(
                        toLocalDateTime(row[3])
                )
                .originalAmount(
                        toLong(row[4])
                )
                .discountAmount(
                        toLong(row[5])
                )
                .paymentAmount(
                        toLong(row[6])
                )
                .eventTitle(
                        toStringValue(row[7])
                )
                .paymentMethod(
                        toStringValue(row[8])
                )
                .paymentStatus(
                        toStringValue(row[9])
                )
                .paidAt(
                        toLocalDateTime(row[10])
                )
                .refundedAt(
                        toLocalDateTime(row[11])
                )
                .build();
    }


    private Long toLong(
            Object value
    ) {

        if (value == null) {

            return null;
        }


        return ((Number) value)
                .longValue();
    }


    private String toStringValue(
            Object value
    ) {

        if (value == null) {

            return null;
        }


        return value.toString();
    }


    private LocalDateTime toLocalDateTime(
            Object value
    ) {

        if (value == null) {

            return null;
        }


        if (value instanceof LocalDateTime localDateTime) {

            return localDateTime;
        }


        if (value instanceof Timestamp timestamp) {

            return timestamp.toLocalDateTime();
        }


        throw new IllegalArgumentException(
                "지원하지 않는 날짜 타입입니다: "
                        + value.getClass()
        );
    }

    public record PaymentHistorySummary(
            Long paidCount,
            Long totalPaidAmount,
            Long refundedCount
    ) {
    }

}