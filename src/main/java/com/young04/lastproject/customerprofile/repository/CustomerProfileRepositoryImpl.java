package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * =========================================================
 * 고객 CRM Custom Repository 구현체
 * =========================================================
 *
 * 관리자 고객 목록의 복합 조건 검색과
 * 페이징 처리를 담당합니다.
 *
 * 검색 조건:
 *
 * - 이름 / 전화번호
 * - 회원 / 비회원
 * - 고객 등급
 * - 활성 여부
 * - 30일 / 60일 미방문
 * - 재방문 권장일 도래
 */
@Repository
@RequiredArgsConstructor
public class CustomerProfileRepositoryImpl
        implements CustomerProfileRepositoryCustom {


    private final EntityManager entityManager;


    // =====================================================
    // 고객 조건 검색 + 페이징
    // =====================================================

    @Override
    public Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    ) {


        // =================================================
        // 실제 고객 데이터 조회 JPQL
        // =================================================

        StringBuilder jpql = new StringBuilder("""
                SELECT c
                FROM CustomerProfile c
                JOIN FETCH c.customerGrade g
                WHERE 1 = 1
                """);


        // =================================================
        // 전체 검색 결과 개수 조회 JPQL
        //
        // Page 객체를 만들려면
        // 전체 고객 수가 필요합니다.
        // =================================================

        StringBuilder countJpql = new StringBuilder("""
                SELECT COUNT(c)
                FROM CustomerProfile c
                JOIN c.customerGrade g
                WHERE 1 = 1
                """);


        // =================================================
        // 공통 Query Parameter
        // =================================================

        Map<String, Object> parameters =
                new HashMap<>();


        // =====================================================
        // 이름 또는 전화번호 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getKeyword()
        )) {

            String searchCondition = """
                     AND (
                         LOWER(c.customerName) LIKE LOWER(:keyword)
                         OR c.phone LIKE :keyword
                     )
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


            parameters.put(
                    "keyword",
                    "%"
                            + condition
                            .getKeyword()
                            .trim()
                            + "%"
            );
        }


        // =====================================================
        // 회원 / 비회원 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getCustomerType()
        )) {

            String searchCondition = """
                     AND c.customerType = :customerType
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


            parameters.put(
                    "customerType",
                    condition.getCustomerType()
            );
        }


        // =====================================================
        // 고객 등급 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getGradeCode()
        )) {

            String searchCondition = """
                     AND g.gradeCode = :gradeCode
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


            parameters.put(
                    "gradeCode",
                    condition.getGradeCode()
            );
        }


        // =====================================================
        // 활성 여부 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getActiveYn()
        )) {

            String searchCondition = """
                     AND c.activeYn = :activeYn
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


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
                                    condition
                                            .getInactiveDays()
                            );


            String searchCondition = """
                     AND c.lastVisitDate IS NOT NULL
                     AND c.lastVisitDate <= :inactiveDate
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


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
         * 권장일이 오늘이거나
         * 이미 지난 고객만 조회합니다.
         */
        if (condition != null
                && "Y".equalsIgnoreCase(
                condition.getRevisitDueYn()
        )) {


            String searchCondition = """
                     AND c.revisitRecommendedDate IS NOT NULL
                     AND c.revisitRecommendedDate <= :today
                    """;


            jpql.append(searchCondition);

            countJpql.append(searchCondition);


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


        // =====================================================
        // 실제 고객 조회 Query 생성
        // =====================================================

        TypedQuery<CustomerProfile> query =
                entityManager.createQuery(
                        jpql.toString(),
                        CustomerProfile.class
                );


        // =====================================================
        // 전체 검색 결과 수 Query 생성
        // =====================================================

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );


        // =====================================================
        // Query Parameter 적용
        // =====================================================

        parameters.forEach(
                (key, value) -> {

                    query.setParameter(
                            key,
                            value
                    );

                    countQuery.setParameter(
                            key,
                            value
                    );
                }
        );


        // =====================================================
        // 페이징 적용
        //
        // page = 0
        // size = 10
        //
        // firstResult = 0
        //
        // page = 1
        // size = 10
        //
        // firstResult = 10
        // =====================================================

        query.setFirstResult(
                (int) pageable.getOffset()
        );


        query.setMaxResults(
                pageable.getPageSize()
        );


        // =====================================================
        // 현재 페이지 데이터 조회
        // =====================================================

        List<CustomerProfile> customers =
                query.getResultList();


        // =====================================================
        // 전체 검색 결과 개수
        // =====================================================

        Long totalCount =
                countQuery.getSingleResult();


        // =====================================================
        // Page 객체 생성
        // =====================================================

        return new PageImpl<>(
                customers,
                pageable,
                totalCount
        );
    }
}