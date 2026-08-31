package com.young04.lastproject.reservation.exception;

public class ReservationAuthenticationRequiredException
        extends ReservationException {

    public ReservationAuthenticationRequiredException() {
        super("로그인이 필요한 기능입니다.");
    }
}
