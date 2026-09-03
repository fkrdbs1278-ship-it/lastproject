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
 * 관리자 고객관리 목록
 *
 * /admin/customers
 *
 * 에서 사용하는
 * 복합 검색 + 페이징 기능입니다.
 *
 *
 * 검색 조건
 *
 * 1. 고객명 / 전화번호
 * 2. 회원 / 비회원
 * 3. 고객 등급
 * 4. 활성 / 비활성
 * 5. 30일 / 60일 이상 미방문
 *
 *
 * 전화번호 기준
 *
 * DB 저장:
 *
 * 010-1234-5678
 *
 *
 * 검색:
 *
 * 010-1234-5678
 * 01012345678
 * 0101234
 *
 * 모두 검색할 수 있도록
 * DB 전화번호의 '-'를 제거하여 비교합니다.
 *
 *
 * 재방문 권장일은 사용하지 않습니다.
 *
 * 장기 미방문 고객은
 * LAST_VISIT_DATE 기준으로 계산합니다.
 */
@Repository
@RequiredArgsConstructor
public class CustomerProfileRepositoryImpl
        implements CustomerProfileRepositoryCustom {


    // =====================================================
    // EntityManager
    // =====================================================

    private final EntityManager entityManager;



    // =====================================================
    // 고객 검색 + 페이징
    // =====================================================

    @Override
    public Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    ) {


        // =================================================
        // 고객 목록 조회 JPQL
        // =================================================

        StringBuilder jpql =
                new StringBuilder(
                        """
                        SELECT c
                        FROM CustomerProfile c
                        JOIN FETCH c.customerGrade g
                        WHERE 1 = 1
                        """
                );



        // =================================================
        // 검색 결과 전체 개수 JPQL
        // =================================================

        StringBuilder countJpql =
                new StringBuilder(
                        """
                        SELECT COUNT(c)
                        FROM CustomerProfile c
                        JOIN c.customerGrade g
                        WHERE 1 = 1
                        """
                );



        // =================================================
        // Query Parameter
        // =================================================

        Map<String, Object> parameters =
                new HashMap<>();



        // =====================================================
        // 1. 고객명 / 전화번호 통합 검색
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getKeyword()
        )) {


            String keyword =
                    condition
                            .getKeyword()
                            .trim();


            /*
             * 전화번호 검색용
             *
             * 입력:
             *
             * 010-1234-5678
             * 01012345678
             * 010 1234 5678
             *
             * ↓
             *
             * 01012345678
             */
            String phoneDigits =
                    keyword.replaceAll(
                            "[^0-9]",
                            ""
                    );



            // ---------------------------------------------
            // 숫자가 포함된 검색어
            // ---------------------------------------------
            //
            // 이름 검색 +
            // 전화번호 검색을 동시에 수행
            //
            // ---------------------------------------------

            if (StringUtils.hasText(
                    phoneDigits
            )) {


                String searchCondition =
                        """
                         AND (
                             LOWER(c.customerName)
                                 LIKE :nameKeyword

                             OR

                             REPLACE(
                                 c.phone,
                                 '-',
                                 ''
                             )
                                 LIKE :phoneDigits
                         )
                        """;


                jpql.append(
                        searchCondition
                );


                countJpql.append(
                        searchCondition
                );


                parameters.put(
                        "nameKeyword",
                        "%" + keyword.toLowerCase() + "%"
                );


                parameters.put(
                        "phoneDigits",
                        "%" + phoneDigits + "%"
                );

            } else {


                // -----------------------------------------
                // 문자만 입력한 경우
                // -----------------------------------------
                //
                // 이름만 검색
                //
                // -----------------------------------------

                String searchCondition =
                        """
                         AND LOWER(c.customerName)
                             LIKE :nameKeyword
                        """;


                jpql.append(
                        searchCondition
                );


                countJpql.append(
                        searchCondition
                );


                parameters.put(
                        "nameKeyword",
                        "%" + keyword.toLowerCase() + "%"
                );
            }
        }



        // =====================================================
        // 2. 회원 / 비회원
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getCustomerType()
        )) {


            String searchCondition =
                    """
                     AND c.customerType = :customerType
                    """;


            jpql.append(
                    searchCondition
            );


            countJpql.append(
                    searchCondition
            );


            parameters.put(
                    "customerType",
                    condition
                            .getCustomerType()
                            .trim()
                            .toUpperCase()
            );
        }



        // =====================================================
        // 3. 고객 등급
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getGradeCode()
        )) {


            String searchCondition =
                    """
                     AND g.gradeCode = :gradeCode
                    """;


            jpql.append(
                    searchCondition
            );


            countJpql.append(
                    searchCondition
            );


            parameters.put(
                    "gradeCode",
                    condition
                            .getGradeCode()
                            .trim()
                            .toUpperCase()
            );
        }



        // =====================================================
        // 4. 활성 / 비활성
        // =====================================================

        if (condition != null
                && StringUtils.hasText(
                condition.getActiveYn()
        )) {


            String searchCondition =
                    """
                     AND c.activeYn = :activeYn
                    """;


            jpql.append(
                    searchCondition
            );


            countJpql.append(
                    searchCondition
            );


            parameters.put(
                    "activeYn",
                    condition
                            .getActiveYn()
                            .trim()
                            .toUpperCase()
            );
        }



        // =====================================================
        // 5. 장기 미방문 고객
        // =====================================================
        //
        // inactiveDays = 30
        //
        // → 최근 방문일이 오늘 기준
        //   30일 이전인 고객
        //
        //
        // inactiveDays = 60
        //
        // → 최근 방문일이 오늘 기준
        //   60일 이전인 고객
        //
        //
        // 방문 기록이 없는 신규 고객은
        // 장기 미방문 대상에 포함하지 않습니다.
        //
        // =====================================================

        if (condition != null
                && condition.getInactiveDays() != null
                && condition.getInactiveDays() > 0) {


            LocalDate inactiveDate =
                    LocalDate
                            .now()
                            .minusDays(
                                    condition
                                            .getInactiveDays()
                            );


            String searchCondition =
                    """
                     AND c.lastVisitDate IS NOT NULL
                     AND c.lastVisitDate <= :inactiveDate
                    """;


            jpql.append(
                    searchCondition
            );


            countJpql.append(
                    searchCondition
            );


            parameters.put(
                    "inactiveDate",
                    inactiveDate
            );
        }



        // =====================================================
        // 정렬
        // =====================================================
        //
        // 최근 등록 고객부터 표시
        //
        // =====================================================

        jpql.append(
                """
                 ORDER BY c.customerId DESC
                """
        );



        // =====================================================
        // 고객 조회 Query
        // =====================================================

        TypedQuery<CustomerProfile> query =
                entityManager.createQuery(
                        jpql.toString(),
                        CustomerProfile.class
                );



        // =====================================================
        // Count Query
        // =====================================================

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );



        // =====================================================
        // Parameter 적용
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
        // 페이징
        // =====================================================
        //
        // 예:
        //
        // page = 0
        // size = 10
        //
        // 0 ~ 9
        //
        //
        // page = 1
        // size = 10
        //
        // 10 ~ 19
        //
        // =====================================================

        query.setFirstResult(
                (int) pageable.getOffset()
        );


        query.setMaxResults(
                pageable.getPageSize()
        );



        // =====================================================
        // 현재 페이지 고객
        // =====================================================

        List<CustomerProfile> customers =
                query.getResultList();



        // =====================================================
        // 전체 검색 결과 개수
        // =====================================================

        Long totalCount =
                countQuery.getSingleResult();



        // =====================================================
        // Page 반환
        // =====================================================

        return new PageImpl<>(
                customers,
                pageable,
                totalCount
        );
    }
}