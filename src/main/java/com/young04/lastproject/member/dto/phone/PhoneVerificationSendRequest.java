package com.young04.lastproject.member.dto.phone;

import com.young04.lastproject.member.verification.PhoneVerificationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneVerificationSendRequest {

    /*
     * 인증할 휴대전화번호
     */
    @NotBlank(
            message = "휴대전화번호를 입력해주세요."
    )
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화번호를 입력해주세요."
    )
    private String phone;


    /*
     * 인증 목적
     *
     * SIGNUP
     * FIND_ID
     * RESET_PASSWORD
     */
    @NotNull(
            message = "휴대전화 인증 목적이 없습니다."
    )
    private PhoneVerificationPurpose purpose;
}
