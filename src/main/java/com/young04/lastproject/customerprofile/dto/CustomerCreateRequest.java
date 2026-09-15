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
     * Bean Validation에서 기본 전화번호 형식을 검증하고,
     * CustomerProfileService에서 숫자 추출과 표준 포맷 변환을
     * 한 번 더 수행합니다.
     */
    @NotBlank(
            message = "전화번호를 입력해 주세요."
    )
    @Size(
            max = 20,
            message = "전화번호는 20자 이하로 입력해 주세요."
    )
    @Pattern(
            regexp = "^(?:02[-\\s]?\\d{3,4}[-\\s]?\\d{4}|0\\d{2}[-\\s]?\\d{3,4}[-\\s]?\\d{4})$",
            message = "전화번호 형식을 확인해 주세요. 예: 010-1234-5678"
    )
    private String phone;
}