package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomerProfileRepositoryImpl
        implements CustomerProfileRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition
    ) {

        StringBuilder jpql = new StringBuilder("""
                SELECT c
                FROM CustomerProfile c
                JOIN FETCH c.customerGrade g
                WHERE 1 = 1
                """);

        Map<String, Object> parameters = new HashMap<>();


        // =====================================================
        // 이름 또는 전화번호 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(condition.getKeyword())) {

            jpql.append("""
                     AND (
                         LOWER(c.customerName) LIKE LOWER(:keyword)
                         OR c.phone LIKE :keyword
                     )
                    """);

            parameters.put(
                    "keyword",
                    "%" + condition.getKeyword().trim() + "%"
            );
        }


        // =====================================================
        // 회원 / 비회원 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(condition.getCustomerType())) {

            jpql.append("""
                     AND c.customerType = :customerType
                    """);

            parameters.put(
                    "customerType",
                    condition.getCustomerType()
            );
        }


        // =====================================================
        // 고객 등급 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(condition.getGradeCode())) {

            jpql.append("""
                     AND g.gradeCode = :gradeCode
                    """);

            parameters.put(
                    "gradeCode",
                    condition.getGradeCode()
            );
        }


        // =====================================================
        // 활성 여부 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(condition.getActiveYn())) {

            jpql.append("""
                     AND c.activeYn = :activeYn
                    """);

            parameters.put(
                    "activeYn",
                    condition.getActiveYn()
            );
        }


        // =====================================================
        // 장기 미방문 고객 검색
        // =====================================================

        /**
         * 예:
         *
         * inactiveDays = 30
         *
         * 오늘 기준 최근 방문일이
         * 30일 이상 지난 고객을 조회합니다.
         */
        if (condition != null
                && condition.getInactiveDays() != null
                && condition.getInactiveDays() > 0) {

            LocalDate inactiveDate =
                    LocalDate.now()
                            .minusDays(
                                    condition.getInactiveDays()
                            );

            jpql.append("""
                     AND c.lastVisitDate IS NOT NULL
                     AND c.lastVisitDate <= :inactiveDate
                    """);

            parameters.put(
                    "inactiveDate",
                    inactiveDate
            );
        }


        // =====================================================
        // 재방문 권장일 도래 고객 검색
        // =====================================================

        /**
         * revisitDueYn = Y 인 경우:
         *
         * 재방문 권장일이 존재하고
         * 권장일이 오늘이거나 이미 지난 고객만 조회합니다.
         *
         * 예:
         *
         * 오늘: 2026-08-27
         *
         * 2026-08-26 → 조회
         * 2026-08-27 → 조회
         * 2026-09-05 → 제외
         */
        if (condition != null
                && "Y".equalsIgnoreCase(
                condition.getRevisitDueYn()
        )) {

            jpql.append("""
                     AND c.revisitRecommendedDate IS NOT NULL
                     AND c.revisitRecommendedDate <= :today
                    """);

            parameters.put(
                    "today",
                    LocalDate.now()
            );
        }


        // =====================================================
        // 최근 등록 고객부터 조회
        // =====================================================

        jpql.append("""
                 ORDER BY c.customerId DESC
                """);


        TypedQuery<CustomerProfile> query =
                entityManager.createQuery(
                        jpql.toString(),
                        CustomerProfile.class
                );


        parameters.forEach(
                query::setParameter
        );


        return query.getResultList();
    }
}