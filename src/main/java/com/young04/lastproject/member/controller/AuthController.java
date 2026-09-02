package com.young04.lastproject.member.controller;

import com.young04.lastproject.global.exception.member.DuplicateEmailException;
import com.young04.lastproject.global.exception.member.DuplicateMemberIdException;
import com.young04.lastproject.global.exception.member.InvalidBirthDateException;
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


@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class AuthController {

    private final MemberService memberService;


    /* 회원가입 페이지 */

    @GetMapping("/signup")
    public String signup(
            @ModelAttribute("signupRequest")
            SignupRequest signupRequest
    ) {

        return "member/signup";
    }


    /* 회원가입 처리 */

    @PostMapping("/signup")
    public String signupProcess(

            @Valid
            @ModelAttribute("signupRequest")
            SignupRequest signupRequest,

            BindingResult bindingResult
    ) {

        /* =====================================================
           DTO Validation

           @NotBlank
           @Size
           @Pattern
           @Email
           @Past

           등의 형식 오류가 있으면 Service까지 보내지 않는다.
        ===================================================== */

        if (bindingResult.hasErrors()) {

            return "member/signup";
        }


        try {

            /*
             * 비밀번호 일치
             * 생년월일 범위
             * 아이디 중복
             * 이메일 중복
             *
             * 등의 회원가입 검증은 Service에서 처리
             */
            memberService.signup(
                    signupRequest
            );

        }

        /* 아이디 중복 */

        catch (DuplicateMemberIdException e) {

            bindingResult.rejectValue(
                    "memberId",
                    "duplicate",
                    e.getMessage()
            );

            return "member/signup";
        }


        /* 이메일 중복 */

        catch (DuplicateEmailException e) {

            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    e.getMessage()
            );

            return "member/signup";
        }


        /* 비밀번호 불일치 */

        catch (PasswordMismatchException e) {

            bindingResult.rejectValue(
                    "passwordCheck",
                    "mismatch",
                    e.getMessage()
            );

            return "member/signup";
        }


        /* 잘못된 생년월일 */

        catch (InvalidBirthDateException e) {

            bindingResult.rejectValue(
                    "birthDate",
                    "range",
                    e.getMessage()
            );

            return "member/signup";
        }


        /* 회원가입 성공= */

        return "redirect:/member/login?signup=success";
    }


    /* 로그인 페이지 */

    @GetMapping("/login")
    public String login() {

        return "member/login";
    }
}