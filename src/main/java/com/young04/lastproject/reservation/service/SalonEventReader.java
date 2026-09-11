package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.SalonEventOptionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class SalonEventReader {

    @PersistenceContext
    private EntityManager entityManager;


    public List<SalonEventOptionResponse> getOngoingEvents() {

        @SuppressWarnings("unchecked")
        List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                SELECT
                                    EVENT_NO,
                                    EVENT_TITLE,
                                    EVENT_CONTENT,
                                    EVENT_TYPE,
                                    EVENT_IMAGE_URL,
                                    TARGET_CATEGORY,
                                    DISCOUNT_TYPE,
                                    DISCOUNT_VALUE,
                                    MIN_PAYMENT_AMOUNT,
                                    MAX_DISCOUNT_AMOUNT,
                                    START_DATE,
                                    END_DATE
                                FROM SALON_EVENT
                                WHERE USE_YN = 'Y'
                                  AND START_DATE <= CURRENT_TIMESTAMP
                                  AND END_DATE >= CURRENT_TIMESTAMP
                                ORDER BY
                                    START_DATE DESC,
                                    EVENT_NO DESC
                                """
                        )
                        .getResultList();


        return rows.stream()
                .map(row ->
                        SalonEventOptionResponse
                                .builder()
                                .eventNo(
                                        ((Number) row[0])
                                                .longValue()
                                )
                                .title(
                                        (String) row[1]
                                )
                                .content(
                                        (String) row[2]
                                )
                                .eventType(
                                        (String) row[3]
                                )
                                .imageUrl(
                                        (String) row[4]
                                )
                                .targetCategory(
                                        (String) row[5]
                                )
                                .discountType(
                                        (String) row[6]
                                )
                                .discountValue(
                                        row[7] == null
                                                ? null
                                                : new BigDecimal(
                                                row[7].toString()
                                        )
                                )
                                .minPaymentAmount(
                                        row[8] == null
                                                ? null
                                                : ((Number) row[8])
                                                .longValue()
                                )
                                .maxDiscountAmount(
                                        row[9] == null
                                                ? null
                                                : ((Number) row[9])
                                                .longValue()
                                )
                                .startDate(
                                        toDateTime(
                                                row[10]
                                        )
                                )
                                .endDate(
                                        toDateTime(
                                                row[11]
                                        )
                                )
                                .build()
                )
                .toList();
    }


    private LocalDateTime toDateTime(
            Object value
    ) {

        if (value instanceof LocalDateTime localDateTime) {

            return localDateTime;
        }


        if (value instanceof Timestamp timestamp) {

            return timestamp.toLocalDateTime();
        }


        return null;
    }
}