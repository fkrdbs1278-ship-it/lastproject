package com.young04.lastproject.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** API 인증/권한 실패는 리다이렉트 대신 상태 코드와 JSON으로 응답한다. */
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED,
                "로그인이 필요합니다. 다시 로그인해주세요.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, exception instanceof CsrfException
                ? "인증 정보가 만료되었거나 올바르지 않습니다. 페이지를 새로고침해주세요."
                : "요청한 작업에 접근할 권한이 없습니다.");
    }

    private void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        // message는 위의 고정 문구만 사용한다. 사용자 입력을 직접 이어붙이지 않는다.
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
