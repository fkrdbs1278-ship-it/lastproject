package com.young04.lastproject.global.security;

import com.young04.lastproject.loginhistory.service.LoginHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "LOGIN_AUDIT")
public class LoginFailureHandler
        implements AuthenticationFailureHandler {

    private final LoginHistoryService loginHistoryService;


    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        /*
         * SecurityConfig에서 usernameParameter를
         * "memberId"로 설정했으므로
         * 여기서도 memberId로 가져온다.
         */
        String memberId =
                request.getParameter("memberId");

        String ipAddress =
                request.getRemoteAddr();

        String userAgent =
                request.getHeader("User-Agent");

        String failureReason =
                getFailureReason(exception);


        /* =====================================================
           DB 로그인 실패 이력
        ===================================================== */

        try {

            loginHistoryService.recordFailure(
                    memberId,
                    ipAddress,
                    userAgent,
                    failureReason
            );

        } catch (Exception e) {

            log.error(
                    "LOGIN_HISTORY_SAVE_ERROR type=FAILURE memberId={} ip={}",
                    memberId,
                    ipAddress
            );
        }


        /* =====================================================
           Logback 로그인 실패 로그
        ===================================================== */

        log.warn(
                "LOGIN_FAILURE memberId={} reason={} ip={}",
                memberId,
                failureReason,
                ipAddress
        );


        /* =====================================================
           로그인 화면으로 이동

           사용자에게는 구체적인 실패 원인을 보여주지 않는다.
        ===================================================== */

        response.sendRedirect(
                "/member/login?error"
        );
    }


    /* =========================================================
       로그인 실패 원인을 DB 내부 기록용으로 구분
    ========================================================= */

    private String getFailureReason(
            AuthenticationException exception
    ) {

        if (exception instanceof LockedException) {
            return "ACCOUNT_LOCKED";
        }

        if (exception instanceof DisabledException) {
            return "ACCOUNT_DISABLED";
        }

        if (exception instanceof BadCredentialsException) {
            return "BAD_CREDENTIALS";
        }

        return "AUTHENTICATION_FAILED";
    }
}
