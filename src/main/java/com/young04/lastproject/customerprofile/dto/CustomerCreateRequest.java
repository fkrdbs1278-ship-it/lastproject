package com.young04.lastproject.customerprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerCreateRequest {

    // =====================================================
    // 고객명
    // =====================================================

    @NotBlank(message = "고객명을 입력해 주세요.")
    @Size(
            max = 50,
            message = "고객명은 50자 이하로 입력해 주세요."
    )
    private String customerName;


    // =====================================================
    // 전화번호
    // =====================================================

    /**
     * 입력 예:
     *
     * 01012345678
     * 010-1234-5678
     *
     * Service에서 최종적으로 숫자만 남도록
     * 정리한 뒤 저장할 예정입니다.
     */
    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Size(
            max = 20,
            message = "전화번호는 20자 이하로 입력해 주세요."
    )
    @Pattern(
            regexp = "^[0-9\\-\\s]+$",
            message = "전화번호는 숫자와 하이픈(-)만 입력할 수 있습니다."
    )
    private String phone;
}