package com.young04.lastproject.reservation.notification;

public interface ReservationSmsSender {

    void send(
            String to,
            String subject,
            String content
    );
}
