package com.young04.lastproject.dashboard.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// 관리자 대시보드에 필요한 통계 값을 조회
@Repository
@RequiredArgsConstructor
public class DashboardStatisticsRepository {

    private final EntityManager entityManager;

    // 이번 달에 실제 방문한 고객 수 조회
    public long countMonthlyVisitCustomers() {

        Number result = (Number) entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM CUSTOMER_PROFILE
            WHERE LAST_VISIT_DATE >= TRUNC(SYSDATE, 'MM')
              AND LAST_VISIT_DATE < ADD_MONTHS(TRUNC(SYSDATE, 'MM'), 1)
            """)
                .getSingleResult();

        return result.longValue();
    }
}