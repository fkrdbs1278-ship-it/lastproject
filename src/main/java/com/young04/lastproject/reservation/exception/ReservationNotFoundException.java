package com.young04.lastproject.reservation.exception;

public class ReservationNotFoundException extends ReservationException {
    public ReservationNotFoundException(Long reservationNo) {
        super("예약을 찾을 수 없습니다. reservationNo=" + reservationNo);
    }
}
