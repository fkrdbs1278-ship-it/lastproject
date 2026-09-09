package com.young04.lastproject.reservationimage.exception;

public class ReservationImageAccessDeniedException
        extends RuntimeException {

    public ReservationImageAccessDeniedException() {
        super("예약 이미지에 접근할 권한이 없습니다.");
    }
}
