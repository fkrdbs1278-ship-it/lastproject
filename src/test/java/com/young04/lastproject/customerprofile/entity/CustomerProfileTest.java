package com.young04.lastproject.customerprofile.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CustomerProfileTest {


    // =====================================================
    // 테스트 고객 생성
    // =====================================================

    /**
     * Entity의 방문 / 결제 로직만 테스트하므로
     * CustomerGrade는 null로 생성합니다.
     *
     * DB에 저장하는 테스트가 아니기 때문에
     * 실제 CUSTOMER_GRADE 데이터에는 영향을 주지 않습니다.
     */
    private CustomerProfile createCustomer() {

        return CustomerProfile.createGuestCustomer(
                "테스트고객",
                "01012345678",
                null
        );
    }



    // =====================================================
    // 방문 완료 테스트
    // =====================================================

    @Test
    @DisplayName("방문 완료 시 방문횟수와 최근방문일, 재방문 권장일이 갱신된다")
    void recordVisit() {

        // given
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


        // when
        customer.recordVisit(
                visitDate,
                revisitDate
        );


        // then
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
    }



    // =====================================================
    // 방문 횟수 누적 테스트
    // =====================================================

    @Test
    @DisplayName("여러 번 방문하면 방문횟수가 정상적으로 누적된다")
    void recordVisitMultipleTimes() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when
        customer.recordVisit(
                LocalDate.of(
                        2026,
                        8,
                        1
                ),
                LocalDate.of(
                        2026,
                        9,
                        1
                )
        );

        customer.recordVisit(
                LocalDate.of(
                        2026,
                        8,
                        28
                ),
                LocalDate.of(
                        2026,
                        9,
                        28
                )
        );


        // then
        assertEquals(
                2,
                customer.getVisitCount()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        8,
                        28
                ),
                customer.getLastVisitDate()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        9,
                        28
                ),
                customer.getRevisitRecommendedDate()
        );
    }



    // =====================================================
    // 결제 누적 테스트
    // =====================================================

    @Test
    @DisplayName("결제 완료 금액이 누적 결제액에 반영된다")
    void addPayment() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when
        customer.addPayment(
                new BigDecimal(
                        "35000"
                )
        );


        // then
        assertEquals(
                new BigDecimal(
                        "35000"
                ),
                customer.getTotalPayment()
        );
    }



    // =====================================================
    // 여러 번 결제 누적 테스트
    // =====================================================

    @Test
    @DisplayName("여러 번 결제하면 누적 결제액이 합산된다")
    void addPaymentMultipleTimes() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when
        customer.addPayment(
                new BigDecimal(
                        "35000"
                )
        );

        customer.addPayment(
                new BigDecimal(
                        "50000"
                )
        );


        // then
        assertEquals(
                new BigDecimal(
                        "85000"
                ),
                customer.getTotalPayment()
        );
    }



    // =====================================================
    // 0원 결제 방어 테스트
    // =====================================================

    @Test
    @DisplayName("0원 결제는 예외가 발생한다")
    void addPaymentZero() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when / then
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.addPayment(
                        BigDecimal.ZERO
                )
        );
    }



    // =====================================================
    // 음수 결제 방어 테스트
    // =====================================================

    @Test
    @DisplayName("음수 결제는 예외가 발생한다")
    void addPaymentNegative() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when / then
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.addPayment(
                        new BigDecimal(
                                "-10000"
                        )
                )
        );
    }



    // =====================================================
    // NULL 결제 방어 테스트
    // =====================================================

    @Test
    @DisplayName("결제 금액이 null이면 예외가 발생한다")
    void addPaymentNull() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when / then
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.addPayment(
                        null
                )
        );
    }



    // =====================================================
    // 방문일 NULL 방어 테스트
    // =====================================================

    @Test
    @DisplayName("방문일이 null이면 예외가 발생한다")
    void recordVisitNullDate() {

        // given
        CustomerProfile customer =
                createCustomer();


        // when / then
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.recordVisit(
                        null,
                        LocalDate.of(
                                2026,
                                9,
                                28
                        )
                )
        );
    }
}