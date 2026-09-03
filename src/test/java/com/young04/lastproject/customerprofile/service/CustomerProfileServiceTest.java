package com.young04.lastproject.customerprofile.service;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepository;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {


    // =====================================================
    // Mock
    // =====================================================

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @Mock
    private CustomerProfileRepositoryCustom customerProfileRepositoryCustom;

    @Mock
    private CustomerGradeService customerGradeService;



    // =====================================================
    // 테스트 대상
    // =====================================================

    private CustomerProfileService customerProfileService;



    // =====================================================
    // 테스트용 고객 등급
    // =====================================================

    private CustomerGrade normalGrade;

    private CustomerGrade vipGrade;



    // =====================================================
    // 테스트 준비
    // =====================================================

    @BeforeEach
    void setUp() {

        customerProfileService =
                new CustomerProfileService(
                        customerProfileRepository,
                        customerProfileRepositoryCustom,
                        customerGradeService
                );


        // CustomerGrade는 테스트에서 DB를 사용하지 않고
        // Mockito 가짜 객체로 사용합니다.
        normalGrade =
                mock(
                        CustomerGrade.class
                );


        vipGrade =
                mock(
                        CustomerGrade.class
                );
    }



    // =====================================================
    // 테스트 고객 생성
    // =====================================================

    /**
     * 신규 비회원 고객을 생성합니다.
     *
     * 초기 상태:
     *
     * VISIT_COUNT = 0
     * TOTAL_PAYMENT = 0
     * GRADE_MANUAL_YN = N
     * GRADE = NORMAL
     */
    private CustomerProfile createCustomer() {

        return CustomerProfile.createGuestCustomer(
                "서비스테스트고객",
                "01099990000",
                normalGrade
        );
    }



    // =====================================================
    // 방문 완료 처리 테스트
    // =====================================================

    @Test
    @DisplayName(
            "방문 완료 처리 시 방문횟수, 최근방문일, 재방문 권장일이 갱신되고 자동등급을 계산한다"
    )
    void completeVisit() {

        // -------------------------------------------------
        // given
        // -------------------------------------------------

        Long customerId =
                1L;


        CustomerProfile customer =
                createCustomer();


        LocalDate visitDate =
                LocalDate.of(
                        2026,
                        8,
                        28
                );


        LocalDate revisitDate =
                LocalDate.of(
                        2026,
                        9,
                        28
                );


        when(
                customerProfileRepository.findById(
                        customerId
                )
        ).thenReturn(
                Optional.of(
                        customer
                )
        );


        // 방문 1회 / 결제 0원
        // → NORMAL 등급이라고 가정
        when(
                customerGradeService.calculateGradeCode(
                        1,
                        BigDecimal.ZERO
                )
        ).thenReturn(
                "NORMAL"
        );


        when(
                customerGradeService.findByGradeCode(
                        "NORMAL"
                )
        ).thenReturn(
                Optional.of(
                        normalGrade
                )
        );


        // -------------------------------------------------
        // when
        // -------------------------------------------------

        CustomerProfile result =
                customerProfileService.completeVisit(
                        customerId,
                        visitDate,
                        revisitDate
                );


        // -------------------------------------------------
        // then
        // -------------------------------------------------

        assertEquals(
                1,
                result.getVisitCount()
        );


        assertEquals(
                visitDate,
                result.getLastVisitDate()
        );


        assertEquals(
                revisitDate,
                result.getRevisitRecommendedDate()
        );


        assertSame(
                normalGrade,
                result.getCustomerGrade()
        );


        verify(
                customerGradeService
        ).calculateGradeCode(
                1,
                BigDecimal.ZERO
        );


        verify(
                customerGradeService
        ).findByGradeCode(
                "NORMAL"
        );
    }



    // =====================================================
    // 결제 누적 + 자동 등급 테스트
    // =====================================================

    @Test
    @DisplayName(
            "결제 완료 금액을 누적하고 실적에 따라 고객 등급을 자동 재계산한다"
    )
    void addCustomerPayment() {

        // -------------------------------------------------
        // given
        // -------------------------------------------------

        Long customerId =
                1L;


        CustomerProfile customer =
                createCustomer();


        BigDecimal paymentAmount =
                new BigDecimal(
                        "1000000"
                );


        when(
                customerProfileRepository.findById(
                        customerId
                )
        ).thenReturn(
                Optional.of(
                        customer
                )
        );


        // 방문 0회 / 누적결제 100만원
        // → VIP라고 가정
        when(
                customerGradeService.calculateGradeCode(
                        0,
                        paymentAmount
                )
        ).thenReturn(
                "VIP"
        );


        when(
                customerGradeService.findByGradeCode(
                        "VIP"
                )
        ).thenReturn(
                Optional.of(
                        vipGrade
                )
        );


        // -------------------------------------------------
        // when
        // -------------------------------------------------

        CustomerProfile result =
                customerProfileService.addCustomerPayment(
                        customerId,
                        paymentAmount
                );


        // -------------------------------------------------
        // then
        // -------------------------------------------------

        assertEquals(
                new BigDecimal(
                        "1000000"
                ),
                result.getTotalPayment()
        );


        assertSame(
                vipGrade,
                result.getCustomerGrade()
        );


        assertEquals(
                "N",
                result.getGradeManualYn()
        );


        verify(
                customerGradeService
        ).calculateGradeCode(
                0,
                paymentAmount
        );


        verify(
                customerGradeService
        ).findByGradeCode(
                "VIP"
        );
    }



    // =====================================================
    // 수동 등급 보호 테스트
    // =====================================================

    @Test
    @DisplayName(
            "관리자가 수동 지정한 등급은 방문과 결제가 추가되어도 자동등급으로 덮어쓰지 않는다"
    )
    void manualGradeIsNotAutomaticallyChanged() {

        // -------------------------------------------------
        // given
        // -------------------------------------------------

        Long customerId =
                1L;


        CustomerProfile customer =
                createCustomer();


        // 관리자가 VIP 등급으로 직접 지정
        customer.changeGradeManually(
                vipGrade
        );


        when(
                customerProfileRepository.findById(
                        customerId
                )
        ).thenReturn(
                Optional.of(
                        customer
                )
        );


        LocalDate visitDate =
                LocalDate.of(
                        2026,
                        8,
                        28
                );


        LocalDate revisitDate =
                LocalDate.of(
                        2026,
                        9,
                        28
                );


        // -------------------------------------------------
        // when - 방문 완료
        // -------------------------------------------------

        customerProfileService.completeVisit(
                customerId,
                visitDate,
                revisitDate
        );


        // -------------------------------------------------
        // when - 결제 완료
        // -------------------------------------------------

        customerProfileService.addCustomerPayment(
                customerId,
                new BigDecimal(
                        "50000"
                )
        );


        // -------------------------------------------------
        // then - CRM 데이터는 정상 갱신
        // -------------------------------------------------

        assertEquals(
                1,
                customer.getVisitCount()
        );


        assertEquals(
                visitDate,
                customer.getLastVisitDate()
        );


        assertEquals(
                revisitDate,
                customer.getRevisitRecommendedDate()
        );


        assertEquals(
                new BigDecimal(
                        "50000"
                ),
                customer.getTotalPayment()
        );


        // -------------------------------------------------
        // then - 수동 VIP 등급 유지
        // -------------------------------------------------

        assertSame(
                vipGrade,
                customer.getCustomerGrade()
        );


        assertEquals(
                "Y",
                customer.getGradeManualYn()
        );


        // -------------------------------------------------
        // 수동 등급 고객은 자동등급 계산 자체를
        // 호출하지 않아야 함
        // -------------------------------------------------

        verify(
                customerGradeService,
                never()
        ).calculateGradeCode(
                anyInt(),
                any(
                        BigDecimal.class
                )
        );
    }
}