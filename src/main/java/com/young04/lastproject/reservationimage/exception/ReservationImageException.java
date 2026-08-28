package com.young04.lastproject.reservationimage.exception;

public class ReservationImageException extends RuntimeException {

    public ReservationImageException(String message) {
        super(message);
    }

    public ReservationImageException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
