package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.SalonEventOptionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class SalonEventReader {

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * 4part SALON_EVENT를 2part 예약 화면에서 읽기만 한다.
     * 4part Entity/Service/Controller에는 컴파일 의존하지 않는다.
     */
    public List<SalonEventOptionResponse> getOngoingEvents() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT
                    EVENT_NO,
                    EVENT_TITLE,
                    EVENT_CONTENT,
                    EVENT_TYPE,
                    EVENT_IMAGE_URL,
                    START_DATE,
                    END_DATE
                FROM SALON_EVENT
                WHERE USE_YN = 'Y'
                  AND START_DATE <= CURRENT_TIMESTAMP
                  AND END_DATE >= CURRENT_TIMESTAMP
                ORDER BY START_DATE DESC, EVENT_NO DESC
                """)
                .getResultList();

        return rows.stream()
                .map(row -> SalonEventOptionResponse.builder()
                        .eventNo(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .content((String) row[2])
                        .eventType((String) row[3])
                        .imageUrl((String) row[4])
                        .startDate(toLocalDateTime(row[5]))
                        .endDate(toLocalDateTime(row[6]))
                        .build())
                .toList();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        throw new IllegalStateException(
                "지원하지 않는 TIMESTAMP 타입입니다: "
                        + value.getClass().getName()
        );
    }
}
