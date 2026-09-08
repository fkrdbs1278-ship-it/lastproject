package com.young04.lastproject.material.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class MaterialRequest {

    @NotBlank(message = "자재명을 입력해 주세요.")
    @Size(max = 100, message = "자재명은 100자 이하로 입력해 주세요.")
    private String materialName;

    @Size(max = 30, message = "카테고리 코드는 30자 이하로 입력해 주세요.")
    private String categoryCode;

    @NotBlank(message = "단위 코드를 입력해 주세요.")
    @Size(max = 20, message = "단위 코드는 20자 이하로 입력해 주세요.")
    private String unitCode;

    // 구매 단위 1개에 들어 있는 용량 또는 수량
    @NotNull(message = "구매 단위당 내용량을 입력해 주세요.")
    @DecimalMin(
            value = "0",
            inclusive = false,
            message = "내용량은 0보다 커야 합니다."
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "내용량은 정수 10자리, 소수점 2자리까지 입력할 수 있습니다."
    )
    private BigDecimal contentQuantity;

    // 시술에서 자재를 사용할 때 적용할 단위
    @NotBlank(message = "사용 단위를 선택해 주세요.")
    @Pattern(
            regexp = "ML|G|EA",
            message = "사용 단위는 ml, g, 개 중에서 선택해 주세요."
    )
    private String usageUnitCode;

    @NotNull(message = "현재 재고를 입력해 주세요.")
    @DecimalMin(value = "0.0", message = "현재 재고는 0 이상이어야 합니다.")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "현재 재고는 정수 10자리, 소수점 2자리까지 입력할 수 있습니다."
    )
    private BigDecimal currentStock;

    @NotNull(message = "안전 재고를 입력해 주세요.")
    @DecimalMin(value = "0.0", message = "안전 재고는 0 이상이어야 합니다.")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "안전 재고는 정수 10자리, 소수점 2자리까지 입력할 수 있습니다."
    )
    private BigDecimal safetyStock;

    @NotNull(message = "단가를 입력해 주세요.")
    @DecimalMin(value = "0", message = "단가는 0 이상이어야 합니다.")
    @Digits(
            integer = 12,
            fraction = 0,
            message = "단가는 12자리 이하의 정수로 입력해 주세요."
    )
    private BigDecimal unitPrice;

    @Size(max = 100, message = "공급업체명은 100자 이하로 입력해 주세요.")
    private String supplierName;

    @NotBlank(message = "사용 여부를 선택해 주세요.")
    @Pattern(regexp = "Y|N", message = "사용 여부는 Y 또는 N이어야 합니다.")
    private String useYn = "Y";
}