package com.young04.lastproject.customerprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * =========================================================
 * 전화예약 고객 등록 Request DTO
 * =========================================================
 *
 * 관리자 고객관리 화면에서
 * 전화예약 / 비회원 고객을 직접 등록할 때 사용합니다.
 *
 *
 * 전화번호 입력 예:
 *
 * 01012345678
 * 010-1234-5678
 * 010 1234 5678
 *
 * 모두 입력 가능합니다.
 *
 *
 * 실제 DB 저장 전 CustomerProfileService에서
 *
 * 010-1234-5678
 *
 * 형식으로 자동 변환합니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CustomerCreateRequest {


    // =====================================================
    // 고객명
    // =====================================================

    @NotBlank(
            message = "고객명을 입력해 주세요."
    )
    @Size(
            max = 50,
            message = "고객명은 50자 이하로 입력해 주세요."
    )
    private String customerName;



    // =====================================================
    // 전화번호
    // =====================================================

    /**
     * 입력 단계에서는:
     *
     * 숫자
     * 하이픈(-)
     * 공백
     *
     * 을 허용합니다.
     *
     *
     * 예:
     *
     * 01012345678
     *
     * 010-1234-5678
     *
     * 010 1234 5678
     *
     *
     * 정확한 전화번호 자리수 검증과
     * 하이픈 자동 변환은
     *
     * CustomerProfileService
     *
     * 에서 처리합니다.
     */
    @NotBlank(
            message = "전화번호를 입력해 주세요."
    )
    @Size(
            max = 20,
            message = "전화번호는 20자 이하로 입력해 주세요."
    )
    @Pattern(
            regexp = "^[0-9\\-\\s]+$",
            message = "전화번호는 숫자, 하이픈(-), 공백만 입력할 수 있습니다."
    )
    private String phone;
}