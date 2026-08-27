package com.young04.lastproject.customergrade.service;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import com.young04.lastproject.customergrade.repository.CustomerGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerGradeService {

    private final CustomerGradeRepository customerGradeRepository;

    // 고객 등급 코드
    private static final String NORMAL = "NORMAL";
    private static final String REGULAR = "REGULAR";
    private static final String VIP = "VIP";

    // VIP 누적 결제 기준
    private static final BigDecimal VIP_PAYMENT_STANDARD
            = new BigDecimal("1000000");


    // =====================================================
    // 고객 등급 전체 조회
    // =====================================================

    public List<CustomerGrade> findAllGrades() {

        log.info("고객 등급 전체 조회");

        return customerGradeRepository
                .findAllByOrderByGradePriorityAsc();
    }


    // =====================================================
    // 고객 등급 코드로 조회
    // =====================================================

    public Optional<CustomerGrade> findByGradeCode(
            String gradeCode
    ) {

        log.info(
                "고객 등급 조회 gradeCode={}",
                gradeCode
        );

        return customerGradeRepository.findById(gradeCode);
    }


    // =====================================================
    // 자동 고객 등급 코드 계산
    // =====================================================

    /**
     * 고객 방문 횟수와 누적 결제 금액을 기준으로
     * 자동 고객 등급을 계산합니다.
     *
     * NORMAL
     * - 방문 0 ~ 2회
     *
     * REGULAR
     * - 방문 3 ~ 9회
     *
     * VIP
     * - 방문 10회 이상
     * 또는
     * - 누적 결제 금액 1,000,000원 이상
     */
    public String calculateGradeCode(
            Integer visitCount,
            BigDecimal totalPayment
    ) {

        // null 방어 처리
        int safeVisitCount =
                visitCount == null
                        ? 0
                        : visitCount;

        BigDecimal safeTotalPayment =
                totalPayment == null
                        ? BigDecimal.ZERO
                        : totalPayment;


        // VIP
        // 방문 10회 이상 또는 누적 결제 100만원 이상
        if (
                safeVisitCount >= 10
                        ||
                        safeTotalPayment.compareTo(
                                VIP_PAYMENT_STANDARD
                        ) >= 0
        ) {

            log.info(
                    "고객 자동 등급 계산 결과=VIP, visitCount={}, totalPayment={}",
                    safeVisitCount,
                    safeTotalPayment
            );

            return VIP;
        }


        // REGULAR
        // 방문 3회 이상
        if (safeVisitCount >= 3) {

            log.info(
                    "고객 자동 등급 계산 결과=REGULAR, visitCount={}, totalPayment={}",
                    safeVisitCount,
                    safeTotalPayment
            );

            return REGULAR;
        }


        // NORMAL
        log.info(
                "고객 자동 등급 계산 결과=NORMAL, visitCount={}, totalPayment={}",
                safeVisitCount,
                safeTotalPayment
        );

        return NORMAL;
    }


    // =====================================================
    // 자동 계산된 CustomerGrade 조회
    // =====================================================

    /**
     * 방문 횟수 / 누적 결제 금액으로 등급 코드를 계산한 뒤
     * CUSTOMER_GRADE 테이블에서 실제 등급 Entity를 조회합니다.
     */
    public Optional<CustomerGrade> calculateGrade(
            Integer visitCount,
            BigDecimal totalPayment
    ) {

        String gradeCode =
                calculateGradeCode(
                        visitCount,
                        totalPayment
                );

        log.info(
                "자동 고객 등급 Entity 조회 gradeCode={}",
                gradeCode
        );

        return customerGradeRepository
                .findById(gradeCode);
    }
}