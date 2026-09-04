package com.young04.lastproject.reservation.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "reservation.sms",
        name = "provider",
        havingValue = "log",
        matchIfMissing = true
)
public class LogReservationSmsSender
        implements ReservationSmsSender {

    @Override
    public void send(
            String to,
            String subject,
            String content
    ) {
        log.info(
                """
                [RESERVATION_SMS][LOG_ONLY]
                to={}
                subject={}
                content=
                {}
                """,
                maskPhone(to),
                subject,
                content
        );
    }

    private String maskPhone(
            String phone
    ) {
        if (phone == null
                || phone.length() < 7) {
            return "***";
        }

        return phone.substring(0, 3)
                + "****"
                + phone.substring(
                        phone.length() - 4
                );
    }
}
