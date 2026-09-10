package com.young04.lastproject.servicematerial.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "SERVICE_MATERIAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceMaterial {

    @Id
    // DB가 시술-자재 연결 번호를 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_MATERIAL_NO")
    private Long serviceMaterialNo;

    // 어떤 시술에서 사용하는 자재인지 저장
    @Column(name = "SERVICE_MENU_NO", nullable = false)
    private Long serviceMenuNo;

    // 실제로 차감할 자재 번호
    @Column(name = "MATERIAL_NO", nullable = false)
    private Long materialNo;

    // 해당 시술 1회 완료 시 사용할 기본 자재량
    @Column(
            name = "USAGE_QUANTITY",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal usageQuantity;


    // 새로운 시술별 기본 자재 설정 생성
    public ServiceMaterial(
            Long serviceMenuNo,
            Long materialNo,
            BigDecimal usageQuantity
    ) {
        validateUsageQuantity(usageQuantity);

        this.serviceMenuNo = serviceMenuNo;
        this.materialNo = materialNo;
        this.usageQuantity = usageQuantity;
    }


    // 기본으로 사용할 자재를 다른 제품으로 변경
    public void changeMaterial(Long materialNo) {
        this.materialNo = materialNo;
    }


    // 시술 1회당 기본 사용량 변경
    public void changeUsageQuantity(BigDecimal usageQuantity) {
        validateUsageQuantity(usageQuantity);

        this.usageQuantity = usageQuantity;
    }


    // DB 제약조건과 동일하게 사용량은 0보다 크게 제한
    private void validateUsageQuantity(BigDecimal usageQuantity) {

        if (usageQuantity == null
                || usageQuantity.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "자재 사용량은 0보다 커야 합니다."
            );
        }
    }
}