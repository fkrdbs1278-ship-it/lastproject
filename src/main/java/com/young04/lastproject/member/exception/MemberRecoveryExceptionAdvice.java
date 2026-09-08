package com.young04.lastproject.member.exception;

import com.young04.lastproject.member.controller.MemberRecoveryController;
import com.young04.lastproject.member.dto.recovery.FindIdResponse;
import com.young04.lastproject.member.dto.recovery.ResetPasswordResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice(
        assignableTypes = MemberRecoveryController.class
)
public class MemberRecoveryExceptionAdvice {


    /* 계정 복구 예외 처리

       아이디 찾기:
       FindIdResponse

       비밀번호 재설정:
       ResetPasswordResponse */

    @ExceptionHandler({
            PhoneVerificationException.class,
            MemberRecoveryException.class
    })
    public ResponseEntity<?> handleRecoveryException(
            RuntimeException e,
            HttpServletRequest request
    ) {

        String uri =
                request.getRequestURI();


        log.warn(
                "회원 계정 복구 실패 uri={}, message={}",
                uri,
                e.getMessage()
        );


        /* 아이디 찾기 */

        if (uri.endsWith(
                "/member/find-id/result"
        )) {

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


        /* 비밀번호 재설정 */

        return ResponseEntity
                .badRequest()
                .body(
                        new ResetPasswordResponse(
                                false,
                                e.getMessage()
                        )
                );
    }
}