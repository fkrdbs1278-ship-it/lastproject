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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    // 회원 고객이면 MEMBER.NO 저장
    // 비회원/전화예약 고객이면 NULL
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

    // MEMBER / GUEST
    @Column(
            name = "CUSTOMER_TYPE",
            nullable = false,
            length = 20
    )
    private String customerType;

    // 고객 등급
    // NORMAL / REGULAR / VIP
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "GRADE_CODE",
            nullable = false
    )
    private CustomerGrade customerGrade;

    // 관리자가 고객 등급을 직접 변경했는지 여부
    // Y / N
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "GRADE_MANUAL_YN",
            nullable = false,
            length = 1,
            columnDefinition = "CHAR(1)"
    )
    private String gradeManualYn;

    // 최근 방문일
    @Column(name = "LAST_VISIT_DATE")
    private LocalDate lastVisitDate;

    // 누적 방문 횟수
    @Column(
            name = "VISIT_COUNT",
            nullable = false
    )
    private Integer visitCount;

    // 누적 결제 금액
    @Column(
            name = "TOTAL_PAYMENT",
            nullable = false,
            precision = 14,
            scale = 0
    )
    private BigDecimal totalPayment;

    // 재방문 권장일
    @Column(name = "REVISIT_RECOMMENDED_DATE")
    private LocalDate revisitRecommendedDate;

    // 고객 활성 여부
    // Y / N
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "ACTIVE_YN",
            nullable = false,
            length = 1,
            columnDefinition = "CHAR(1)"
    )
    private String activeYn;

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
    // 고객 등급 관리
    // =====================================================

    /**
     * 자동으로 계산된 고객 등급을 적용합니다.
     *
     * 관리자가 수동으로 등급을 지정한 고객은
     * 자동 계산 결과가 기존 등급을 덮어쓰지 않습니다.
     */
    public void applyAutomaticGrade(CustomerGrade customerGrade) {

        if ("Y".equals(this.gradeManualYn)) {
            return;
        }

        this.customerGrade = customerGrade;
    }


    /**
     * 관리자가 고객 등급을 직접 변경합니다.
     *
     * 수동 변경 여부를 Y로 설정하여
     * 이후 자동 등급 계산에서 제외합니다.
     */
    public void changeGradeManually(CustomerGrade customerGrade) {

        this.customerGrade = customerGrade;
        this.gradeManualYn = "Y";
    }


    /**
     * 수동 등급 설정을 해제하고
     * 다시 자동 등급 관리 상태로 변경합니다.
     */
    public void changeGradeAutomatically(CustomerGrade customerGrade) {

        this.customerGrade = customerGrade;
        this.gradeManualYn = "N";
    }
}