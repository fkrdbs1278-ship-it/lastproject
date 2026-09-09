package com.young04.lastproject.member.exception;

import com.young04.lastproject.member.dto.phone.PhoneVerificationResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j

@RestControllerAdvice(
        assignableTypes =
                com.young04.lastproject.member.controller
                        .PhoneVerificationController.class
)
public class MemberExceptionAdvice {


    /* 휴대전화 인증 예외 */

    @ExceptionHandler(
            PhoneVerificationException.class
    )
    public ResponseEntity<PhoneVerificationResponse>
    handlePhoneVerificationException(

            PhoneVerificationException e

    ) {

        log.warn(
                "휴대전화 인증 실패 message={}",
                e.getMessage()
        );


        return ResponseEntity
                .badRequest()
                .body(
                        new PhoneVerificationResponse(
                                false,
                                e.getMessage()
                        )
                );
    }
}
