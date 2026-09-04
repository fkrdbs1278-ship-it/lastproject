package com.young04.lastproject.reservation.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNotificationListener {

    private final ReservationSmsSender smsSender;
    private final ReservationSmsMessageFactory messageFactory;

    /*
     * DB 트랜잭션이 성공한 뒤에만 문자 발송을 시도한다.
     * 문자 발송이 실패해도 이미 완료된 예약 트랜잭션은 되돌리지 않는다.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(
            ReservationNotificationEvent event
    ) {
        try {
            smsSender.send(
                    event.guestPhone(),
                    messageFactory.subject(event),
                    messageFactory.content(event)
            );
        } catch (Exception e) {
            log.error(
                    "예약 문자 발송 실패. reservationNo={}, type={}",
                    event.reservationNo(),
                    event.type(),
                    e
            );
        }
    }
}
