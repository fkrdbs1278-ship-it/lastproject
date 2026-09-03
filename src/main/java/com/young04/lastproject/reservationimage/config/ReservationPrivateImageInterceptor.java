package com.young04.lastproject.reservationimage.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ReservationPrivateImageInterceptor
        implements HandlerInterceptor {

    /*
     * 고객이 올린 예약 참고 이미지는 /uploads/reservation/** 로
     * 직접 공개하지 않는다.
     *
     * 실제 조회는 회원/관리자 전용 API를 통해서만 허용한다.
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        response.sendError(
                HttpServletResponse.SC_NOT_FOUND
        );
        return false;
    }
}
