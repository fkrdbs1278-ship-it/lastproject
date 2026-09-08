package com.young04.lastproject.member.controller;

import com.young04.lastproject.member.dto.phone.PhoneVerificationCheckRequest;
import com.young04.lastproject.member.dto.phone.PhoneVerificationResponse;
import com.young04.lastproject.member.dto.phone.PhoneVerificationSendRequest;
import com.young04.lastproject.member.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member/phone-verification")
public class PhoneVerificationController {

    private final PhoneVerificationService
            phoneVerificationService;


    /* 인증번호 발송 */

    @PostMapping("/send")
    public ResponseEntity<PhoneVerificationResponse>
    sendVerificationCode(

            @Valid
            @RequestBody
            PhoneVerificationSendRequest request,

            BindingResult bindingResult

    ) {

        /*
         * DTO 검증 실패
         */
        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            return ResponseEntity
                    .badRequest()
                    .body(
                            new PhoneVerificationResponse(
                                    false,
                                    message
                            )
                    );
        }


        phoneVerificationService
                .sendVerificationCode(
                        request.getPhone(),
                        request.getPurpose()
                );


        return ResponseEntity.ok(
                new PhoneVerificationResponse(
                        true,
                        "인증번호를 발송했습니다."
                )
        );
    }


    /* 인증번호 확인 */

    @PostMapping("/verify")
    public ResponseEntity<PhoneVerificationResponse>
    verifyCode(

            @Valid
            @RequestBody
            PhoneVerificationCheckRequest request,

            BindingResult bindingResult

    ) {

        /*
         * DTO 검증 실패
         */
        if (bindingResult.hasErrors()) {

            String message =
                    bindingResult
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            return ResponseEntity
                    .badRequest()
                    .body(
                            new PhoneVerificationResponse(
                                    false,
                                    message
                            )
                    );
        }


        phoneVerificationService
                .verifyCode(
                        request.getPhone(),
                        request.getPurpose(),
                        request.getCode()
                );


        return ResponseEntity.ok(
                new PhoneVerificationResponse(
                        true,
                        "휴대전화 인증이 완료되었습니다."
                )
        );
    }
}