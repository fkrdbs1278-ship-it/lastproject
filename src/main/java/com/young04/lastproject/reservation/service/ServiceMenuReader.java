package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.ServiceMenuOptionResponse;
import com.young04.lastproject.reservation.exception.ServiceMenuNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class ServiceMenuReader {

    @PersistenceContext
    private EntityManager entityManager;

    public ServiceMenuSnapshot getActiveServiceMenu(Long serviceMenuNo) {
        try {
            Object[] row = (Object[]) entityManager.createNativeQuery("""
                    SELECT NAME, DURATION_MIN
                    FROM SERVICE_MENU
                    WHERE NO = :serviceMenuNo
                      AND ACTIVE_YN = 'Y'
                    """)
                    .setParameter("serviceMenuNo", serviceMenuNo)
                    .getSingleResult();

            return new ServiceMenuSnapshot(
                    serviceMenuNo,
                    (String) row[0],
                    ((Number) row[1]).intValue()
            );
        } catch (NoResultException e) {
            throw new ServiceMenuNotFoundException(serviceMenuNo);
        }
    }

    /*
     * 예약 화면용 목록.
     * Phase 통합테스트 전용 메뉴는 DB에 남겨두되 UI에서는 숨긴다.
     *
     * 이렇게 하면 PHASE2_TEST_CUT_30을 삭제/비활성화하지 않아도
     * 기존 Phase5 테스트와 실제 예약 화면을 동시에 유지할 수 있다.
     */
    public List<ServiceMenuOptionResponse> getActiveServiceMenus() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT NO, CATEGORY, NAME, PRICE, DURATION_MIN
                FROM SERVICE_MENU
                WHERE ACTIVE_YN = 'Y'
                  AND UPPER(NAME) NOT LIKE 'PHASE%TEST%'
                ORDER BY DISPLAY_ORDER ASC, NO ASC
                """)
                .getResultList();

        return rows.stream()
                .map(row -> ServiceMenuOptionResponse.builder()
                        .serviceMenuNo(((Number) row[0]).longValue())
                        .category((String) row[1])
                        .name((String) row[2])
                        .price(((Number) row[3]).intValue())
                        .durationMin(((Number) row[4]).intValue())
                        .build())
                .toList();
    }

    public record ServiceMenuSnapshot(
            Long serviceMenuNo,
            String name,
            Integer durationMin
    ) {}
}
