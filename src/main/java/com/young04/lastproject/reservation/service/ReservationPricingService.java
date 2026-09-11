package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.ReservationPricePreviewResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationPricingService {

    private final EntityManager entityManager;

    private final ServiceMenuReader serviceMenuReader;


    public ReservationPricePreviewResponse calculate(
            Long memberNo,
            String guestPhone,
            Long serviceMenuNo,
            LocalDateTime startAt
    ) {

        var menu =
                serviceMenuReader.getActiveServiceMenu(
                        serviceMenuNo
                );

        int originalPrice =
                menu.price();

        boolean firstVisit =
                isFirstVisit(
                        memberNo,
                        guestPhone
                );


        @SuppressWarnings("unchecked")
        List<Object[]> eventRows =
                entityManager
                        .createNativeQuery(
                                """
                                SELECT
                                    EVENT_NO,
                                    EVENT_TITLE,
                                    EVENT_TYPE,
                                    DISCOUNT_TYPE,
                                    DISCOUNT_VALUE,
                                    MIN_PAYMENT_AMOUNT,
                                    MAX_DISCOUNT_AMOUNT
                                FROM SALON_EVENT
                                WHERE USE_YN = 'Y'
                                  AND START_DATE <= :startAt
                                  AND END_DATE >= :startAt
                                  AND (
                                        TARGET_CATEGORY = 'ALL'
                                        OR TARGET_CATEGORY = :category
                                      )
                                """
                        )
                        .setParameter(
                                "startAt",
                                startAt
                        )
                        .setParameter(
                                "category",
                                menu.category()
                        )
                        .getResultList();


        PriceEvent bestEvent =
                eventRows.stream()
                        .map(row ->
                                calculateEventDiscount(
                                        row,
                                        originalPrice,
                                        firstVisit
                                )
                        )
                        .filter(event ->
                                event.discountAmount() > 0
                        )
                        .max(
                                Comparator.comparingInt(
                                        PriceEvent::discountAmount
                                )
                        )
                        .orElse(null);


        if (bestEvent == null) {

            return ReservationPricePreviewResponse
                    .builder()
                    .serviceMenuNo(serviceMenuNo)
                    .originalPrice(originalPrice)
                    .discountAmount(0)
                    .finalPrice(originalPrice)
                    .firstVisitEligible(firstVisit)
                    .build();
        }


        return ReservationPricePreviewResponse
                .builder()
                .serviceMenuNo(serviceMenuNo)
                .originalPrice(originalPrice)
                .eventNo(bestEvent.eventNo())
                .eventTitle(bestEvent.eventTitle())
                .discountType(bestEvent.discountType())
                .discountValue(bestEvent.discountValue())
                .discountAmount(bestEvent.discountAmount())
                .finalPrice(
                        originalPrice
                                - bestEvent.discountAmount()
                )
                .firstVisitEligible(firstVisit)
                .build();
    }


    private PriceEvent calculateEventDiscount(
            Object[] row,
            int originalPrice,
            boolean firstVisit
    ) {

        String eventType =
                (String) row[2];


        if (
                "FIRST_VISIT".equals(eventType)
                        && !firstVisit
        ) {

            return PriceEvent.empty();
        }


        BigDecimal discountValue =
                row[4] == null
                        ? null
                        : new BigDecimal(
                        row[4].toString()
                );


        if (discountValue == null) {

            return PriceEvent.empty();
        }


        long minPaymentAmount =
                row[5] == null
                        ? 0
                        : ((Number) row[5]).longValue();


        if (originalPrice < minPaymentAmount) {

            return PriceEvent.empty();
        }


        String discountType =
                (String) row[3];


        long discountAmount;


        if ("RATE".equals(discountType)) {

            discountAmount =
                    BigDecimal
                            .valueOf(originalPrice)
                            .multiply(discountValue)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    0,
                                    RoundingMode.FLOOR
                            )
                            .longValue();

        } else if ("AMOUNT".equals(discountType)) {

            discountAmount =
                    discountValue.longValue();

        } else {

            return PriceEvent.empty();
        }


        if (row[6] != null) {

            long maxDiscountAmount =
                    ((Number) row[6])
                            .longValue();

            discountAmount =
                    Math.min(
                            discountAmount,
                            maxDiscountAmount
                    );
        }


        discountAmount =
                Math.min(
                        discountAmount,
                        originalPrice
                );


        return new PriceEvent(
                ((Number) row[0]).longValue(),
                (String) row[1],
                discountType,
                discountValue,
                Math.toIntExact(discountAmount)
        );
    }


    private boolean isFirstVisit(
            Long memberNo,
            String guestPhone
    ) {

        if (memberNo != null) {

            Number completedCount =
                    (Number) entityManager
                            .createNativeQuery(
                                    """
                                    SELECT COUNT(*)
                                    FROM RESERVATION
                                    WHERE MEMBER_NO = :memberNo
                                      AND STATUS = 'COMPLETED'
                                    """
                            )
                            .setParameter(
                                    "memberNo",
                                    memberNo
                            )
                            .getSingleResult();


            return completedCount.longValue() == 0;
        }


        String normalizedPhone =
                guestPhone == null
                        ? null
                        : guestPhone.replaceAll(
                        "[^0-9]",
                        ""
                );


        if (
                normalizedPhone == null
                        || normalizedPhone.isBlank()
        ) {

            return false;
        }


        Number completedCount =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                SELECT COUNT(*)
                                FROM RESERVATION
                                WHERE CUSTOMER_TYPE = 'GUEST'
                                  AND GUEST_PHONE = :guestPhone
                                  AND STATUS = 'COMPLETED'
                                """
                        )
                        .setParameter(
                                "guestPhone",
                                normalizedPhone
                        )
                        .getSingleResult();


        return completedCount.longValue() == 0;
    }


    private record PriceEvent(
            Long eventNo,
            String eventTitle,
            String discountType,
            BigDecimal discountValue,
            int discountAmount
    ) {

        static PriceEvent empty() {

            return new PriceEvent(
                    null,
                    null,
                    null,
                    null,
                    0
            );
        }
    }
}