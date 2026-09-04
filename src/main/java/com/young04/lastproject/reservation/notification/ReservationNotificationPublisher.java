package com.young04.lastproject.reservation.notification;

import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishGuest(
            ReservationNotificationType type,
            Reservation reservation
    ) {
        if (reservation == null
                || reservation.getCustomerType()
                        != CustomerType.GUEST
                || reservation.getGuestPhone() == null
                || reservation.getGuestPhone()
                        .isBlank()) {
            return;
        }

        eventPublisher.publishEvent(
                ReservationNotificationEvent.from(
                        type,
                        reservation
                )
        );
    }
}
