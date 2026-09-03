package com.young04.lastproject.member.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequest {

     /*  아이디

       - 4 ~ 20자
       - 영문 소문자
       - 숫자
       - _
       - - */

    @NotBlank(
            message = "아이디를 입력해주세요."
    )
    @Pattern(
            regexp = "^[a-z0-9_-]{4,20}$",
            message = "아이디는 영문 소문자, 숫자, _, -를 사용하여 4~20자로 입력해주세요."
    )
    private String memberId;


    /* 비밀번호

       조건
       - 8 ~ 100자
       - 영문 대문자 1개 이상
       - 영문 소문자 1개 이상
       - 숫자 1개 이상
       - 특수문자 1개 이상
       - 공백 사용 불가 */

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 이상 100자 이하로 입력해주세요."
    )
    @Pattern(
            regexp = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$",
            message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String password;


    /* 비밀번호 확인
       실제 일치 여부는 Controller에서도 다시 확인한다. */

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordCheck;


    /* 이름 */

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(
            max = 50,
            message = "이름은 50자 이하로 입력해주세요."
    )
    private String name;


    /* 닉네임
       선택사항 */

    @Size(
            max = 50,
            message = "닉네임은 50자 이하로 입력해주세요."
    )
    private String nickname;


    /* 이메일
       선택사항 */

    @Email(
            message = "올바른 이메일 형식이 아닙니다."
    )
    @Pattern(
            regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "이메일 도메인을 올바르게 입력해주세요."
    )
    @Size(
            max = 100,
            message = "이메일은 100자 이하로 입력해주세요."
    )
    private String email;


    /* 전화번호 */

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "올바른 휴대전화 번호를 입력해주세요."
    )
    private String phone;


    /* 생년월일

       - 미래 날짜 불가
       - 오늘 날짜 불가
       - 1900년 이전 여부는 Controller에서 추가 검사 */

    @Past(
            message = "생년월일은 오늘보다 이전 날짜만 선택할 수 있습니다."
    )
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate birthDate;


    /* 성별
       M / F / NULL */

    private String gender;


    /* 약관 */

    @AssertTrue(
            message = "이용약관에 동의해주세요."
    )
    private boolean agreeTerms;


    @AssertTrue(
            message = "개인정보 수집 및 이용에 동의해주세요."
    )
    private boolean agreePrivacy;
}