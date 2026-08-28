package com.young04.lastproject.member.controller;

import com.young04.lastproject.global.exception.member.DuplicateEmailException;
import com.young04.lastproject.global.exception.member.DuplicateMemberIdException;
import com.young04.lastproject.global.exception.member.PasswordMismatchException;
import com.young04.lastproject.member.dto.SignupRequest;
import com.young04.lastproject.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Objects;


@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class AuthController {

    private final MemberService memberService;


    /* =========================================================
       회원가입 페이지
    ========================================================= */

    @GetMapping("/signup")
    public String signup(
            @ModelAttribute("signupRequest")
            SignupRequest signupRequest
    ) {

        return "member/signup";
    }


    /* =========================================================
       회원가입 처리
    ========================================================= */

    @PostMapping("/signup")
    public String signupProcess(

            @Valid
            @ModelAttribute("signupRequest")
            SignupRequest signupRequest,

            BindingResult bindingResult
    ) {

        /* =====================================================
           1. 비밀번호 / 비밀번호 확인 일치 검사
        ===================================================== */

        if (!Objects.equals(
                signupRequest.getPassword(),
                signupRequest.getPasswordCheck()
        )) {

            bindingResult.rejectValue(
                    "passwordCheck",
                    "mismatch",
                    "비밀번호가 일치하지 않습니다."
            );
        }


        /* =====================================================
           2. 생년월일 최소 날짜 검사

           1900-01-01부터 허용
           1899-12-31 이전은 가입 불가
        ===================================================== */

        LocalDate minimumBirthDate =
                LocalDate.of(
                        1900,
                        1,
                        1
                );


        if (signupRequest.getBirthDate() != null
                && signupRequest
                .getBirthDate()
                .isBefore(minimumBirthDate)) {

            bindingResult.rejectValue(
                    "birthDate",
                    "range",
                    "생년월일은 1900년 1월 1일 이후의 날짜를 선택해주세요."
            );
        }


        /* =====================================================
           3. Validation 오류 확인

           @NotBlank
           @Pattern
           @Past
           비밀번호 불일치
           생년월일 범위

           하나라도 문제가 있으면 회원가입 화면으로 돌아감
        ===================================================== */

        if (bindingResult.hasErrors()) {

            return "member/signup";
        }


        /* 4. 실제 회원가입 */

        try {

            memberService.signup(signupRequest);

        } catch (DuplicateMemberIdException e) {

            bindingResult.rejectValue(
                    "memberId",
                    "duplicate",
                    e.getMessage()
            );

            return "member/signup";

        } catch (DuplicateEmailException e) {

            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    e.getMessage()
            );

            return "member/signup";

        } catch (PasswordMismatchException e) {

            bindingResult.rejectValue(
                    "passwordCheck",
                    "mismatch",
                    e.getMessage()
            );

            return "member/signup";
        }


        /* 5. 회원가입 성공 */

        return "redirect:/member/login?signup=success";
    }


    /* 로그인 페이지 */

    @GetMapping("/login")
    public String login() {

        return "member/login";
    }
}