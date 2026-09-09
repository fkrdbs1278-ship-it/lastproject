package com.young04.lastproject.reservation.exception;

public class ReservationAccessDeniedException
        extends ReservationException {

    public ReservationAccessDeniedException() {
        super("해당 예약에 접근할 권한이 없습니다.");
    }
}
