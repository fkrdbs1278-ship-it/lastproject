package com.young04.lastproject.stockhistory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 재고 조정 화면에서 입력한 값을 전달하는 DTO
@Getter
@Setter
public class StockAdjustmentRequest {

    // 재고를 변경할 자재 번호
    @NotNull(message = "자재를 선택해 주세요.")
    private Long materialNo;

    // 사용, 폐기, 수동 증가·감소 중 하나를 선택
    @NotBlank(message = "변동 구분을 선택해 주세요.")
    @Pattern(
            regexp = "USE|DISCARD|MANUAL_IN|MANUAL_OUT",
            message = "올바른 변동 구분을 선택해 주세요."
    )
    private String movementType;

    // 실제로 증가하거나 감소할 재고 수량
    @NotNull(message = "수량을 입력해 주세요.")
    @DecimalMin(
            value = "1",
            message = "수량은 0보다 커야 합니다."
    )
    private BigDecimal quantity;

    // 재고를 변경한 이유 또는 참고 내용
    @Size(max = 500, message = "메모는 500자 이하로 입력해 주세요.")
    private String memo;
}