package com.young04.lastproject.member.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindIdRequest {

    /* 이름 */

    @NotBlank(
            message = "이름을 입력해주세요."
    )
    @Size(
            max = 50,
            message = "이름은 50자 이하여야 합니다."
    )
    private String name;


    /* 휴대전화번호 */

    @NotBlank(
            message = "휴대전화번호를 입력해주세요."
    )
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화번호를 입력해주세요."
    )
    private String phone;
}
