package com.young04.lastproject.servicematerial.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 시술별로 사용하는 자재와 기준 사용량을 저장하는 Entity
@Entity
@Table(name = "SERVICE_MATERIAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceMaterial {

    // 시술-자재 연결 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_MATERIAL_NO")
    private Long serviceMaterialNo;

    // 시술 메뉴 번호
    @Column(name = "SERVICE_MENU_NO", nullable = false)
    private Long serviceMenuNo;

    // 자재 번호
    @Column(name = "MATERIAL_NO", nullable = false)
    private Long materialNo;

    // 시술 1회 기준 자재 사용량
    @Column(
            name = "USAGE_QUANTITY",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal usageQuantity;

    // 시술별 자재 사용량 등록
    public ServiceMaterial(
            Long serviceMenuNo,
            Long materialNo,
            BigDecimal usageQuantity
    ) {
        this.serviceMenuNo = serviceMenuNo;
        this.materialNo = materialNo;
        this.usageQuantity = usageQuantity;
    }

    // 자재 기준 사용량 변경
    public void changeUsageQuantity(BigDecimal usageQuantity) {

        if (usageQuantity == null
                || usageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "자재 사용량은 0보다 커야 합니다."
            );
        }

        this.usageQuantity = usageQuantity;
    }
}