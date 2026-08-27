package com.young04.lastproject.member.controller;


import com.young04.lastproject.global.exception.member.DuplicateEmailException;
import com.young04.lastproject.global.security.CustomUserDetails;
import com.young04.lastproject.member.dto.MemberUpdateRequest;
import com.young04.lastproject.member.dto.PasswordConfirmRequest;
import com.young04.lastproject.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    /*회원 정보 수정 페이지*/

    @GetMapping("/edit")
    public String edit(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,
            Model model
    ){
        MemberUpdateRequest request =
                memberService
                        .getMemberUpdateRequest(
                                userDetails.getMemberNo()
                        );

        model.addAttribute(
                "memberUpdateRequest",
                request
        );

        return "member/edit";
    }

    /*회원 정보 수정 처리*/
    @PostMapping("/edit")
    public String editProcess(

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @ModelAttribute("memberUpdateRequest")
            MemberUpdateRequest request,

            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return "member/edit";
        }


        try {

            boolean success =
                    memberService.updateMember(
                            userDetails.getMemberNo(),
                            request
                    );


            /* 현재 비밀번호 불일치 */

            if (!success) {

                bindingResult.rejectValue(
                        "currentPassword",
                        "mismatch",
                        "현재 비밀번호가 일치하지 않습니다."
                );

                return "member/edit";
            }


        } catch (DuplicateEmailException e) {

            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    e.getMessage()
            );

            return "member/edit";
        }


        return "redirect:/member/edit?updated=success";
    }

    /*회원 탈퇴 페이지*/
    @GetMapping("/withdraw")
    public String withdraw(
            @ModelAttribute("passwordConfirmRequest")
            PasswordConfirmRequest request
    ) {

        return "member/withdraw";
    }

    /* 회원 탈퇴 처리*/
    @PostMapping("/withdraw")
    public String withdrawProcess(

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @ModelAttribute("passwordConfirmRequest")
            PasswordConfirmRequest request,

            BindingResult bindingResult,

            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Authentication authentication
    ) {

        if (bindingResult.hasErrors()) {
            return "member/withdraw";
        }


        boolean success =
                memberService.withdraw(
                        userDetails.getMemberNo(),
                        request
                );


        /* 비밀번호가 틀린 경우 */

        if (!success) {

            bindingResult.rejectValue(
                    "currentPassword",
                    "mismatch",
                    "현재 비밀번호가 일치하지 않습니다."
            );

            return "member/withdraw";
        }


        /* =====================================================
           탈퇴 성공 후 즉시 로그아웃

           WITHDRAWN 상태인데 기존 Session이 계속 살아 있는
           상황을 방지한다.
        ===================================================== */

        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();

        logoutHandler.logout(
                httpRequest,
                httpResponse,
                authentication
        );


        return "redirect:/member/login?withdraw=success";
    }


}
