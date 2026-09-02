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


    @Column(name = "MEMBER_NO")
    private Long memberNo;


    @Column(
            name = "CUSTOMER_NAME",
            nullable = false,
            length = 50
    )
    private String customerName;


    @Column(
            name = "PHONE",
            nullable = false,
            unique = true,
            length = 20
    )
    private String phone;


    @Column(
            name = "CUSTOMER_TYPE",
            nullable = false,
            length = 20
    )
    private String customerType;



    // =====================================================
    // 고객 등급
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "GRADE_CODE",
            nullable = false
    )
    private CustomerGrade customerGrade;


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

    @Column(name = "LAST_VISIT_DATE")
    private LocalDate lastVisitDate;


    @Column(
            name = "VISIT_COUNT",
            nullable = false
    )
    private Integer visitCount;


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
    // 고객 기본정보 수정
    // =====================================================

    public void updateBasicInfo(
            String customerName,
            String phone
    ) {

        if (customerName == null
                || customerName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "고객명은 필수입니다."
            );
        }


        if (phone == null
                || phone.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "전화번호는 필수입니다."
            );
        }


        this.customerName =
                customerName.trim();


        this.phone =
                phone.trim();
    }



    // =====================================================
    // 활성 여부 확인
    // =====================================================

    public boolean isActive() {

        return "Y".equals(
                this.activeYn
        );
    }



    // =====================================================
    // 활성 처리
    // =====================================================

    public void activate() {

        this.activeYn =
                "Y";
    }



    // =====================================================
    // 비활성 처리
    // =====================================================

    public void deactivate() {

        this.activeYn =
                "N";
    }



    // =====================================================
    // 방문 완료 처리
    // =====================================================

    public void recordVisit(
            LocalDate visitDate
    ) {

        if (visitDate == null) {

            throw new IllegalArgumentException(
                    "방문일은 필수입니다."
            );
        }


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

    public void addPayment(
            BigDecimal paymentAmount
    ) {

        if (paymentAmount == null
                || paymentAmount.signum() <= 0) {

            throw new IllegalArgumentException(
                    "결제 금액은 0보다 커야 합니다."
            );
        }


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

    public void changeGradeManually(
            CustomerGrade customerGrade
    ) {

        this.customerGrade =
                customerGrade;


        this.gradeManualYn =
                "Y";
    }



    // =====================================================
    // 자동 등급 복귀
    // =====================================================

    public void changeGradeAutomatically(
            CustomerGrade customerGrade
    ) {

        this.customerGrade =
                customerGrade;


        this.gradeManualYn =
                "N";
    }
}