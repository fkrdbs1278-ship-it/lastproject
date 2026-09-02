package com.young04.lastproject.purchaseorder.dto;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderRequest {

    // 발주를 요청할 공급업체 이름
    @NotBlank(message = "공급업체명을 입력해 주세요.")
    @Size(max = 100, message = "공급업체명은 100자 이내로 입력해 주세요.")
    private String supplierName;

    // 자재가 입고될 예정 날짜
    @FutureOrPresent(message = "입고 예정일은 오늘 이후의 날짜를 선택해 주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDate;

    // 발주와 관련된 추가 메모
    @Size(max = 500, message = "메모는 500자 이내로 입력해 주세요.")
    private String memo;
}
