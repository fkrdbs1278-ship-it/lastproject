package com.young04.lastproject.reservation;

import com.young04.lastproject.global.sms.sender.SmsSender;
import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.notification.ReservationNotificationEvent;
import com.young04.lastproject.reservation.notification.ReservationNotificationPublisher;
import com.young04.lastproject.reservation.notification.ReservationNotificationType;
import com.young04.lastproject.reservation.notification.ReservationSmsMessageFactory;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Phase9SolapiSmsContractTest {

    @Test
    void 공통_SMS_Sender가_존재한다() {
        assertThat(com.young04.lastproject.global.sms.sender.SmsSender.class)
                .isInterface();
    }

    @Test
    void 회원예약도_MEMBER의_전화번호로_알림이_발행된다() {
        ReservationMemberReader memberReader =
                mock(ReservationMemberReader.class);

        AtomicReference<Object> published =
                new AtomicReference<>();

        ApplicationEventPublisher eventPublisher =
                published::set;

        ReservationNotificationPublisher publisher =
                new ReservationNotificationPublisher(
                        eventPublisher,
                        memberReader
                );

        Reservation reservation =
                Reservation.createMemberReservation(
                        10L,
                        20L,
                        "커트",
                        30,
                        LocalDateTime.of(
                                2026, 9, 10, 14, 0
                        ),
                        LocalDateTime.of(
                                2026, 9, 10, 14, 30
                        ),
                        null,
                        null
                );

        MemberReservationInfo member =
                MemberReservationInfo.builder()
                        .memberNo(10L)
                        .phone("010-1234-5678")
                        .build();

        when(memberReader.findMemberInfoByMemberNo(10L))
                .thenReturn(Optional.of(member));

        publisher.publish(
                ReservationNotificationType.CREATED,
                reservation
        );

        assertThat(published.get())
                .isInstanceOf(
                        ReservationNotificationEvent.class
                );

        ReservationNotificationEvent event =
                (ReservationNotificationEvent)
                        published.get();

        assertThat(event.phone())
                .isEqualTo("010-1234-5678");
    }

    @Test
    void 시술완료_문자내용이_생성된다() {
        ReservationNotificationEvent event =
                new ReservationNotificationEvent(
                        ReservationNotificationType.COMPLETED,
                        321L,
                        "01012345678",
                        "커트",
                        LocalDateTime.of(
                                2026, 9, 10, 14, 0
                        ),
                        null,
                        null
                );

        ReservationSmsMessageFactory factory =
                new ReservationSmsMessageFactory();

        assertThat(factory.subject(event))
                .contains("시술 완료");

        assertThat(factory.content(event))
                .contains("시술이 완료되었습니다.")
                .contains("예약번호: 321");
    }
}
