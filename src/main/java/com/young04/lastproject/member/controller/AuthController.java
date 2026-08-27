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

        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

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

        return "redirect:/member/login?signup=success";
    }

    /* =========================================================
    로그인 페이지
    ========================================================= */

    @GetMapping("/login")
    public String login() {

        return "member/login";
    }

}