package com.young04.lastproject.member.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    /* 아이디 */

    @NotBlank(
            message = "아이디를 입력해주세요."
    )
    private String memberId;


    /* 휴대전화번호 */

    @NotBlank(
            message = "휴대전화번호를 입력해주세요."
    )
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화번호를 입력해주세요."
    )
    private String phone;


    /* 새 비밀번호 */

    @NotBlank(
            message = "새 비밀번호를 입력해주세요."
    )
    @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 이상이어야 합니다."
    )
    @Pattern(
            regexp =
                    "^(?=.*[A-Z])"
                            + "(?=.*[a-z])"
                            + "(?=.*\\d)"
                            + "(?=.*[^A-Za-z0-9])"
                            + "\\S{8,100}$",
            message =
                    "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String newPassword;


    /* 새 비밀번호 확인 */

    @NotBlank(
            message = "새 비밀번호 확인을 입력해주세요."
    )
    private String newPasswordCheck;
}
