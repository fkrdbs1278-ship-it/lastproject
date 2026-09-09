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


    // =====================================================
    // PK
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TREATMENT_ID")
    private Long treatmentId;



    // =====================================================
    // 고객
    // =====================================================

    /*
     * 3번 담당 영역
     *
     * CUSTOMER_PROFILE과 직접 연관관계를 설정합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CUSTOMER_ID",
            nullable = false
    )
    private CustomerProfile customer;



    // =====================================================
    // 예약 번호
    // =====================================================

    /*
     * 2번 예약 파트와 연결되는 값입니다.
     *
     * 다른 작업자의 Reservation Entity를 직접 참조하지 않고
     * 예약 PK 값만 저장합니다.
     *
     * 이렇게 하면 3part 안에서 독립적으로 작업할 수 있습니다.
     */
    @Column(name = "RESERVATION_NO")
    private Long reservationNo;



    // =====================================================
    // 시술 메뉴 번호
    // =====================================================

    /*
     * 다른 담당자의 SERVICE_MENU와 연결되는 값입니다.
     *
     * ServiceMenu Entity를 직접 참조하지 않고
     * PK 값만 저장합니다.
     */
    @Column(name = "SERVICE_MENU_NO")
    private Long serviceMenuNo;



    // =====================================================
    // 시술명
    // =====================================================

    /*
     * 메뉴 이름이 나중에 변경되더라도
     * 실제 시술 당시 이름을 보존합니다.
     */
    @Column(
            name = "TREATMENT_NAME",
            nullable = false,
            length = 100
    )
    private String treatmentName;



    // =====================================================
    // 시술일
    // =====================================================

    @Column(
            name = "TREATMENT_DATE",
            nullable = false
    )
    private LocalDate treatmentDate;



    // =====================================================
    // 시술 금액
    // =====================================================

    @Column(
            name = "TREATMENT_PRICE",
            nullable = false,
            precision = 12,
            scale = 0
    )
    private BigDecimal treatmentPrice;



    // =====================================================
    // 시술 메모
    // =====================================================

    @Column(
            name = "TREATMENT_MEMO",
            length = 2000
    )
    private String treatmentMemo;



    // =====================================================
    // 다음 추천 방문일
    // =====================================================

    @Column(name = "NEXT_RECOMMENDED_DATE")
    private LocalDate nextRecommendedDate;



    // =====================================================
    // 등록 / 수정 시간
    // =====================================================

    /*
     * DB DEFAULT 값을 사용하므로
     * JPA에서 직접 INSERT / UPDATE 하지 않습니다.
     */

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
    // 생성자
    // =====================================================

    private TreatmentHistory(

            CustomerProfile customer,
            Long reservationNo,
            Long serviceMenuNo,
            String treatmentName,
            LocalDate treatmentDate,
            BigDecimal treatmentPrice,
            String treatmentMemo,
            LocalDate nextRecommendedDate

    ) {

        this.customer = customer;

        this.reservationNo = reservationNo;

        this.serviceMenuNo = serviceMenuNo;

        this.treatmentName = treatmentName;

        this.treatmentDate = treatmentDate;

        this.treatmentPrice = treatmentPrice;

        this.treatmentMemo = treatmentMemo;

        this.nextRecommendedDate =
                nextRecommendedDate;
    }



    // =====================================================
    // 시술이력 생성
    // =====================================================

    /**
     * 새로운 고객 시술이력을 생성합니다.
     *
     * 현재는 3part 안에서 독립적으로 사용할 수 있으며,
     * 나중에 2part 예약이 시술완료 처리될 때
     * Service를 통해 이 메서드를 사용할 수 있습니다.
     */
    public static TreatmentHistory create(

            CustomerProfile customer,
            Long reservationNo,
            Long serviceMenuNo,
            String treatmentName,
            LocalDate treatmentDate,
            BigDecimal treatmentPrice,
            String treatmentMemo,
            LocalDate nextRecommendedDate

    ) {


        // -------------------------------------------------
        // 고객 필수
        // -------------------------------------------------

        if (customer == null) {

            throw new IllegalArgumentException(
                    "고객 정보는 필수입니다."
            );
        }



        // -------------------------------------------------
        // 시술명 필수
        // -------------------------------------------------

        if (treatmentName == null
                || treatmentName.isBlank()) {

            throw new IllegalArgumentException(
                    "시술명은 필수입니다."
            );
        }



        // -------------------------------------------------
        // 시술일 필수
        // -------------------------------------------------

        if (treatmentDate == null) {

            throw new IllegalArgumentException(
                    "시술일은 필수입니다."
            );
        }



        // -------------------------------------------------
        // 시술금액 필수
        // -------------------------------------------------

        if (treatmentPrice == null) {

            throw new IllegalArgumentException(
                    "시술금액은 필수입니다."
            );
        }



        // -------------------------------------------------
        // 음수 금액 방지
        // -------------------------------------------------

        if (treatmentPrice.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "시술금액은 0원 이상이어야 합니다."
            );
        }



        // -------------------------------------------------
        // 메모 정리
        // -------------------------------------------------

        String normalizedMemo = null;


        if (treatmentMemo != null
                && !treatmentMemo.isBlank()) {

            normalizedMemo =
                    treatmentMemo.trim();
        }



        return new TreatmentHistory(

                customer,

                reservationNo,

                serviceMenuNo,

                treatmentName.trim(),

                treatmentDate,

                treatmentPrice,

                normalizedMemo,

                nextRecommendedDate
        );
    }



    // =====================================================
    // 시술 메모 수정
    // =====================================================

    public void updateTreatmentMemo(
            String treatmentMemo
    ) {

        if (treatmentMemo == null
                || treatmentMemo.isBlank()) {

            this.treatmentMemo = null;

            return;
        }


        this.treatmentMemo =
                treatmentMemo.trim();
    }



    // =====================================================
    // 다음 추천일 수정
    // =====================================================

    public void updateNextRecommendedDate(
            LocalDate nextRecommendedDate
    ) {

        this.nextRecommendedDate =
                nextRecommendedDate;
    }
}