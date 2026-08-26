package com.young04.lastproject.treatmenthistory.entity;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "TREATMENT_HISTORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TREATMENT_ID")
    private Long treatmentId;

    // 3번 담당 영역이므로 CustomerProfile Entity와 직접 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CUSTOMER_ID",
            nullable = false
    )
    private CustomerProfile customer;

    // 2번 담당자의 RESERVATION과 연결되는 FK
    // Git 충돌을 줄이기 위해 Reservation Entity를 직접 참조하지 않음
    @Column(name = "RESERVATION_NO")
    private Long reservationNo;

    // 다른 담당자의 SERVICE_MENU와 연결되는 FK
    // ServiceMenu Entity를 직접 참조하지 않음
    @Column(name = "SERVICE_MENU_NO")
    private Long serviceMenuNo;

    // 시술 당시 이름을 그대로 보존
    @Column(
            name = "TREATMENT_NAME",
            nullable = false,
            length = 100
    )
    private String treatmentName;

    @Column(
            name = "TREATMENT_DATE",
            nullable = false
    )
    private LocalDate treatmentDate;

    // 시술 당시 가격
    @Column(
            name = "TREATMENT_PRICE",
            nullable = false,
            precision = 12,
            scale = 0
    )
    private BigDecimal treatmentPrice;

    // 시술 후 기록
    @Column(
            name = "TREATMENT_MEMO",
            length = 2000
    )
    private String treatmentMemo;

    // 다음 추천 방문일
    @Column(name = "NEXT_RECOMMENDED_DATE")
    private LocalDate nextRecommendedDate;

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
}