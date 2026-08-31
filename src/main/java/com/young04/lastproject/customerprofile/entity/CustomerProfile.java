package com.young04.lastproject.customerprofile.entity;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Entity
@Table(name = "CUSTOMER_PROFILE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerProfile {


    // =====================================================
    // 기본 정보
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long customerId;


    /**
     * 회원 고객이면 MEMBER.NO 저장
     *
     * 비회원 / 전화예약 고객이면 NULL
     *
     * 실제 MEMBER 연동은 1part 통합 시 처리합니다.
     */
    @Column(name = "MEMBER_NO")
    private Long memberNo;


    @Column(
            name = "CUSTOMER_NAME",
            nullable = false,
            length = 50
    )
    private String customerName;


    /**
     * CRM에서는 전화번호를 숫자만 저장합니다.
     *
     * 예:
     * 010-1234-5678
     * →
     * 01012345678
     */
    @Column(
            name = "PHONE",
            nullable = false,
            unique = true,
            length = 20
    )
    private String phone;


    /**
     * MEMBER
     * GUEST
     */
    @Column(
            name = "CUSTOMER_TYPE",
            nullable = false,
            length = 20
    )
    private String customerType;



    // =====================================================
    // 고객 등급
    // =====================================================

    /**
     * NORMAL
     * REGULAR
     * VIP
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "GRADE_CODE",
            nullable = false
    )
    private CustomerGrade customerGrade;


    /**
     * 관리자가 고객 등급을 직접 변경했는지 여부
     *
     * Y = 수동 등급
     * N = 자동 등급
     */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "GRADE_MANUAL_YN",
            nullable = false,
            length = 1,
            columnDefinition = "CHAR(1)"
    )
    private String gradeManualYn;



    // =====================================================
    // CRM 방문 / 결제 정보
    // =====================================================

    /**
     * 최근 방문일
     *
     * 장기 미방문 고객 조회 시
     * 이 값을 기준으로 30일 / 60일을 계산합니다.
     */
    @Column(name = "LAST_VISIT_DATE")
    private LocalDate lastVisitDate;


    /**
     * 누적 방문 횟수
     */
    @Column(
            name = "VISIT_COUNT",
            nullable = false
    )
    private Integer visitCount;


    /**
     * 누적 결제 금액
     */
    @Column(
            name = "TOTAL_PAYMENT",
            nullable = false,
            precision = 14,
            scale = 0
    )
    private BigDecimal totalPayment;



    // =====================================================
    // 고객 상태
    // =====================================================

    /**
     * Y = 활성
     * N = 비활성
     */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "ACTIVE_YN",
            nullable = false,
            length = 1,
            columnDefinition = "CHAR(1)"
    )
    private String activeYn;



    // =====================================================
    // 생성 / 수정 시간
    // =====================================================

    @Column(
            name = "CREATED_AT",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "UPDATED_AT",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;



    // =====================================================
    // 전화예약 / 비회원 고객 생성
    // =====================================================

    /**
     * 관리자 고객관리 화면에서
     * 전화예약 고객을 처음 등록할 때 사용합니다.
     *
     * 초기값:
     *
     * MEMBER_NO       = null
     * CUSTOMER_TYPE   = GUEST
     * GRADE_CODE      = NORMAL
     * GRADE_MANUAL_YN = N
     * LAST_VISIT_DATE = null
     * VISIT_COUNT     = 0
     * TOTAL_PAYMENT   = 0
     * ACTIVE_YN       = Y
     */
    public static CustomerProfile createGuestCustomer(
            String customerName,
            String phone,
            CustomerGrade normalGrade
    ) {

        CustomerProfile customer =
                new CustomerProfile();


        customer.memberNo =
                null;


        customer.customerName =
                customerName;


        customer.phone =
                phone;


        customer.customerType =
                "GUEST";


        customer.customerGrade =
                normalGrade;


        customer.gradeManualYn =
                "N";


        customer.lastVisitDate =
                null;


        customer.visitCount =
                0;


        customer.totalPayment =
                BigDecimal.ZERO;


        customer.activeYn =
                "Y";


        return customer;
    }



    // =====================================================
    // 방문 완료 처리
    // =====================================================

    /**
     * 고객이 실제 방문하여 시술을 완료했을 때
     * CRM 방문 정보를 갱신합니다.
     *
     * 처리 내용:
     *
     * VISIT_COUNT + 1
     * LAST_VISIT_DATE 변경
     *
     * 재방문 관리는 별도의 권장일을 사용하지 않고
     * LAST_VISIT_DATE 기준으로
     * 30일 / 60일 미방문 여부를 계산합니다.
     *
     * 향후 2part 예약 기능과 통합할 경우
     * 예약 상태 COMPLETED 시 호출할 수 있도록
     * 3part에서 제공하는 도메인 메서드입니다.
     */
    public void recordVisit(
            LocalDate visitDate
    ) {

        if (visitDate == null) {

            throw new IllegalArgumentException(
                    "방문일은 필수입니다."
            );
        }


        // 기존 데이터 NULL 방어
        if (this.visitCount == null) {

            this.visitCount =
                    0;
        }


        this.visitCount =
                this.visitCount + 1;


        this.lastVisitDate =
                visitDate;
    }



    // =====================================================
    // 결제 금액 누적
    // =====================================================

    /**
     * 결제 완료 금액을 고객의
     * 누적 결제액에 더합니다.
     *
     * 향후 4part 결제 기능과 통합할 경우
     * 결제 완료 시 호출할 수 있도록
     * 3part에서 제공하는 도메인 메서드입니다.
     *
     * 4part 코드는 여기서 수정하지 않습니다.
     */
    public void addPayment(
            BigDecimal paymentAmount
    ) {

        if (paymentAmount == null
                || paymentAmount.signum() <= 0) {

            throw new IllegalArgumentException(
                    "결제 금액은 0보다 커야 합니다."
            );
        }


        // 기존 데이터 NULL 방어
        if (this.totalPayment == null) {

            this.totalPayment =
                    BigDecimal.ZERO;
        }


        this.totalPayment =
                this.totalPayment.add(
                        paymentAmount
                );
    }



    // =====================================================
    // 고객 등급 자동 적용
    // =====================================================

    /**
     * 자동으로 계산된 고객 등급을 적용합니다.
     *
     * 수동 등급 고객은
     * 자동 계산 결과가 기존 등급을 덮어쓰지 않습니다.
     */
    public void applyAutomaticGrade(
            CustomerGrade customerGrade
    ) {

        if ("Y".equals(
                this.gradeManualYn
        )) {

            return;
        }


        this.customerGrade =
                customerGrade;
    }



    // =====================================================
    // 고객 등급 수동 변경
    // =====================================================

    /**
     * 관리자가 고객 등급을 직접 변경합니다.
     *
     * 수동 변경 여부를 Y로 설정하여
     * 이후 자동 등급 계산 대상에서 제외합니다.
     */
    public void changeGradeManually(
            CustomerGrade customerGrade
    ) {

        this.customerGrade =
                customerGrade;


        this.gradeManualYn =
                "Y";
    }



    // =====================================================
    // 자동 등급으로 복귀
    // =====================================================

    /**
     * 수동 등급 설정을 해제하고
     * 자동 등급 관리 상태로 변경합니다.
     */
    public void changeGradeAutomatically(
            CustomerGrade customerGrade
    ) {

        this.customerGrade =
                customerGrade;


        this.gradeManualYn =
                "N";
    }
}