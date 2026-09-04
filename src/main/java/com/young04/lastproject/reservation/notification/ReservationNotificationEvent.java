package com.young04.lastproject.reservation.notification;

import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationNotificationEvent(
        ReservationNotificationType type,
        Long reservationNo,
        String guestPhone,
        String serviceName,
        LocalDateTime startAt,
        ReservationStatus status,
        String cancelReason
) {

    public static ReservationNotificationEvent from(
            ReservationNotificationType type,
            Reservation reservation
    ) {
        return new ReservationNotificationEvent(
                type,
                reservation.getReservationNo(),
                reservation.getGuestPhone(),
                reservation.getServiceNameSnapshot(),
                reservation.getStartAt(),
                reservation.getStatus(),
                reservation.getCancelReason()
        );
    }
}
