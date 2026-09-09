package com.young04.lastproject.global.security;

import com.young04.lastproject.loginhistory.service.LoginHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "LOGIN_AUDIT")
public class LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final LoginHistoryService loginHistoryService;


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String memberId =
                userDetails.getUsername();

        Long memberNo =
                userDetails.getMemberNo();

        String ipAddress =
                request.getRemoteAddr();

        String userAgent =
                request.getHeader("User-Agent");


        /* =====================================================
           DB 로그인 성공 이력
        ===================================================== */

        try {

            loginHistoryService.recordSuccess(
                    memberNo,
                    memberId,
                    ipAddress,
                    userAgent
            );

        } catch (Exception e) {

            /*
             * 로그인 이력 저장에 문제가 생겨도
             * 로그인 자체를 막지는 않는다.
             */
            log.error(
                    "LOGIN_HISTORY_SAVE_ERROR type=SUCCESS memberId={} ip={}",
                    memberId,
                    ipAddress
            );
        }


        /* =====================================================
           Logback 로그인 성공 로그
        ===================================================== */

        log.info(
                "LOGIN_SUCCESS memberId={} memberNo={} ip={}",
                memberId,
                memberNo,
                ipAddress
        );


        /* =====================================================
           로그인 성공 후 메인으로
        ===================================================== */

        response.sendRedirect("/");
    }
}