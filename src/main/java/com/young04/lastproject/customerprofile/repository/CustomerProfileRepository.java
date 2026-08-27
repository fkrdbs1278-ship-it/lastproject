package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


/**
 * =========================================================
 * 고객 CRM Repository
 * =========================================================
 *
 * CUSTOMER_PROFILE 테이블의 기본 조회 기능을 담당합니다.
 *
 * JpaRepository를 상속하므로 기본적인 CRUD 기능을
 * 사용할 수 있습니다.
 *
 * 고객 상세 조회 시 CustomerGrade가 LAZY 관계이기 때문에
 * JPQL JOIN FETCH를 사용하여 고객 정보와 등급 정보를
 * 한 번에 조회합니다.
 */
public interface CustomerProfileRepository
        extends JpaRepository<CustomerProfile, Long> {


    // =====================================================
    // 고객 상세 조회
    // =====================================================

    /**
     * CUSTOMER_ID를 기준으로 고객 상세 정보를 조회합니다.
     *
     * CustomerProfile의 customerGrade는
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     *
     * 로 설정되어 있습니다.
     *
     * 일반 findById()로 고객만 조회하면
     * CustomerGrade는 실제 접근 시점에 조회하려고 합니다.
     *
     * 그런데 Service의 Transaction이 종료된 뒤
     * Controller 또는 DTO 변환 과정에서
     *
     * customer.getCustomerGrade().getGradeName()
     *
     * 같은 코드를 실행하면 Hibernate Session이 이미 종료되어
     * LazyInitializationException이 발생할 수 있습니다.
     *
     * 그래서 JPQL JOIN FETCH를 사용하여
     *
     * CUSTOMER_PROFILE
     * +
     * CUSTOMER_GRADE
     *
     * 를 한 번의 조회에서 함께 가져옵니다.
     */
    @Query("""
            SELECT cp
            FROM CustomerProfile cp
            JOIN FETCH cp.customerGrade
            WHERE cp.customerId = :customerId
            """)
    Optional<CustomerProfile> findById(
            @Param("customerId") Long customerId
    );


    // =====================================================
    // 전화번호로 고객 조회
    // =====================================================

    /**
     * 전화번호를 기준으로 고객을 조회합니다.
     *
     * 사용 예:
     *
     * - 전화번호 고객 검색
     * - 전화예약 고객 조회
     * - 고객 중복 여부 확인
     */
    Optional<CustomerProfile> findByPhone(
            String phone
    );


    // =====================================================
    // 회원 번호로 고객 조회
    // =====================================================

    /**
     * MEMBER 테이블의 회원 번호를 기준으로
     * CRM 고객 정보를 조회합니다.
     *
     * 회원 고객은 MEMBER_NO가 존재하고,
     * 비회원 / 전화예약 고객은 MEMBER_NO가 NULL일 수 있습니다.
     */
    Optional<CustomerProfile> findByMemberNo(
            Long memberNo
    );


    // =====================================================
    // 전화번호 중복 여부 확인
    // =====================================================

    /**
     * 동일한 전화번호의 고객이 이미 존재하는지 확인합니다.
     *
     * 추후 전화예약 고객 직접 등록 기능에서
     * 중복 고객 생성을 방지할 때 사용할 수 있습니다.
     */
    boolean existsByPhone(
            String phone
    );
}