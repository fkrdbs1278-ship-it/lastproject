package com.young04.lastproject.reservationimage.exception;

public class ReservationImageNotFoundException
        extends RuntimeException {

    public ReservationImageNotFoundException(Long reservationImageNo) {
        super(
                "예약 이미지를 찾을 수 없습니다. reservationImageNo="
                        + reservationImageNo
        );
    }
}
