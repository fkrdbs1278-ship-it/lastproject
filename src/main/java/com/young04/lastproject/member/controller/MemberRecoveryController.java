package com.young04.lastproject.member.controller;

import com.young04.lastproject.member.dto.recovery.FindIdRequest;
import com.young04.lastproject.member.dto.recovery.FindIdResponse;
import com.young04.lastproject.member.dto.recovery.ResetPasswordRequest;
import com.young04.lastproject.member.dto.recovery.ResetPasswordResponse;
import com.young04.lastproject.member.service.MemberRecoveryService;
import com.young04.lastproject.member.dto.recovery.ResetPasswordMemberCheckRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberRecoveryController {

    private final MemberRecoveryService
            memberRecoveryService;


    /* 아이디 찾기 화면 */

    @GetMapping("/find-id")
    public String findIdPage() {

        return "member/find-id";
    }


    /* 아이디 찾기 결과 */

    @PostMapping("/find-id/result")
    @ResponseBody
    public ResponseEntity<FindIdResponse>
    findId(

            @Valid
            @RequestBody
            FindIdRequest request,

            BindingResult bindingResult

    ) {

        /* DTO 검증 오류 */

        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            return ResponseEntity
                    .badRequest()
                    .body(
                            new FindIdResponse(
                                    false,
                                    message,
                                    List.of()
                            )
                    );
        }


        List<String> memberIds =
                memberRecoveryService
                        .findMemberIds(
                                request.getName(),
                                request.getPhone()
                        );


        return ResponseEntity.ok(
                new FindIdResponse(
                        true,
                        "아이디를 찾았습니다.",
                        memberIds
                )
        );
    }


    /* 비밀번호 재설정 화면 */

    @GetMapping("/reset-password")
    public String resetPasswordPage() {

        return "member/reset-password";
    }


    /* 비밀번호 재설정 전 회원정보 확인 */

    @PostMapping("/reset-password/validate-member")
    @ResponseBody
    public ResponseEntity<ResetPasswordResponse>
    validateResetPasswordMember(

            @Valid
            @RequestBody
            ResetPasswordMemberCheckRequest request,

            BindingResult bindingResult

    ) {

        /* DTO 검증 실패 */

        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            return ResponseEntity
                    .badRequest()
                    .body(
                            new ResetPasswordResponse(
                                    false,
                                    message
                            )
                    );
        }


        memberRecoveryService
                .validateResetPasswordMember(
                        request.getMemberId(),
                        request.getPhone()
                );


        return ResponseEntity.ok(
                new ResetPasswordResponse(
                        true,
                        "회원정보가 확인되었습니다."
                )
        );
    }





    /* 비밀번호 재설정 처리 */

    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<ResetPasswordResponse>
    resetPassword(

            @Valid
            @RequestBody
            ResetPasswordRequest request,

            BindingResult bindingResult

    ) {

        /* DTO 검증 실패 */

        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            return ResponseEntity
                    .badRequest()
                    .body(
                            new ResetPasswordResponse(
                                    false,
                                    message
                            )
                    );
        }


        memberRecoveryService
                .resetPassword(
                        request.getMemberId(),
                        request.getPhone(),
                        request.getNewPassword(),
                        request.getNewPasswordCheck()
                );


        return ResponseEntity.ok(
                new ResetPasswordResponse(
                        true,
                        "비밀번호가 변경되었습니다."
                )
        );
    }
}
