package com.young04.lastproject.member.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordMemberCheckRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String memberId;

    @NotBlank(message = "휴대전화번호를 입력해주세요.")
    private String phone;
}