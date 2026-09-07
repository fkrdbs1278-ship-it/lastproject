package com.young04.lastproject.member.controller;

import com.young04.lastproject.member.dto.recovery.FindIdRequest;
import com.young04.lastproject.member.dto.recovery.FindIdResponse;
import com.young04.lastproject.member.exception.MemberRecoveryException;
import com.young04.lastproject.member.exception.PhoneVerificationException;
import com.young04.lastproject.member.service.MemberRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
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


        try {

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


        } catch (
                PhoneVerificationException
                | MemberRecoveryException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new FindIdResponse(
                                    false,
                                    e.getMessage(),
                                    List.of()
                            )
                    );
        }
    }
}
