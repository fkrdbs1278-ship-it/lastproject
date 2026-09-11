package com.young04.lastproject.payment.service;

import com.young04.lastproject.payment.dto.MemberPaymentHistoryResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
}
