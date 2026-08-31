package com.young04.lastproject.member.dto;


import com.young04.lastproject.member.entity.Member;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
public class MemberUpdateRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;

    @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.")
    private String nickname;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;

    @NotBlank(
            message = "전화번호를 입력해주세요."
    )
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화 번호를 입력해주세요."
    )
    private String phone;

    @Past(
            message = "생년월일은 오늘보다 이전 날짜만 선택할 수 있습니다."
    )
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate birthDate;

    private String gender;

    /*
     * 회원정보를 실제로 수정하기 전에
     * 현재 비밀번호를 다시 확인한다.
     */
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;


    /* =========================================================
       Entity → 수정 Form DTO
    ========================================================= */

    public static MemberUpdateRequest from(Member member) {

        MemberUpdateRequest request =
                new MemberUpdateRequest();

        request.setName(member.getName());
        request.setNickname(member.getNickname());
        request.setEmail(member.getEmail());
        request.setPhone(member.getPhone());
        request.setBirthDate(member.getBirthDate());
        request.setGender(member.getGender());

        return request;
    }
}
