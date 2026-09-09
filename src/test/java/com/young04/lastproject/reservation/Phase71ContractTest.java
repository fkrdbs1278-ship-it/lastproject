package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.notification.ReservationNotificationEvent;
import com.young04.lastproject.reservation.notification.ReservationNotificationType;
import com.young04.lastproject.reservation.notification.ReservationSmsMessageFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class Phase71ContractTest {

    @Test
    void 회원예약_JS는_me_API를_사용한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/my-reservations.js"
        );

        assertThat(js)
                .contains("/api/reservations/me")
                .doesNotContain(
                        "/api/reservations/member/"
                )
                .doesNotContain(
                        "`/api/reservations/${reservationNo}/cancel"
                );
    }

    @Test
    void 관리자예약_JS는_admin_api_경로를_사용한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/admin-reservations.js"
        );

        assertThat(js)
                .contains("/admin/api/reservations")
                .doesNotContain(
                        "/api/admin/reservations"
                );
    }

    @Test
    void 비회원_예약문자에는_예약번호와_예약정보가_포함된다() {
        ReservationNotificationEvent event =
                new ReservationNotificationEvent(
                        ReservationNotificationType.CREATED,
                        123L,
                        "01012345678",
                        "커트",
                        LocalDateTime.of(
                                2026,
                                9,
                                5,
                                14,
                                0
                        ),
                        ReservationStatus.REQUESTED,
                        null
                );

        ReservationSmsMessageFactory factory =
                new ReservationSmsMessageFactory();

        String content =
                factory.content(event);

        assertThat(content)
                .contains("예약번호: 123")
                .contains("시술: 커트")
                .contains("2026-09-05 14:00")
                .contains(
                        "예약번호와 휴대전화 번호"
                );
    }

    private String resource(
            String path
    ) throws Exception {
        try (InputStream input =
                     getClass()
                             .getResourceAsStream(
                                     path
                             )) {

            if (input == null) {
                throw new IllegalStateException(
                        "테스트 리소스를 찾을 수 없습니다: "
                                + path
                );
            }

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}
