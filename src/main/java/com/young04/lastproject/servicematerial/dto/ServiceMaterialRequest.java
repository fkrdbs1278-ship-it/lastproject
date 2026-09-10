package com.young04.lastproject.servicematerial.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// 관리자 화면에서 시술별 기본 자재 설정값을 받는 DTO
@Getter
@Setter
@NoArgsConstructor
public class ServiceMaterialRequest {

    // 설정할 시술 메뉴 번호
    @NotNull(message = "시술 메뉴를 선택해 주세요.")
    private Long serviceMenuNo;

    // 기본으로 사용할 자재 번호
    @NotNull(message = "자재를 선택해 주세요.")
    private Long materialNo;

    // 시술 1회 기준 자재 사용량
    @NotNull(message = "자재 사용량을 입력해 주세요.")
    @DecimalMin(
            value = "0.01",
            message = "자재 사용량은 0보다 커야 합니다."
    )
    private BigDecimal usageQuantity;
}