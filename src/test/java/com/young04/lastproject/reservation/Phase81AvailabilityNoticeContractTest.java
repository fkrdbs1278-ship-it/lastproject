package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase81AvailabilityNoticeContractTest {

    @Test
    void 예약화면은_예약불가사유_API를_조회한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/reservation-form.js"
        );

        assertThat(js)
                .contains("/api/reservations/availability-notices")
                .contains("bestUnavailableMessage")
                .contains("renderAvailabilityNotice");
    }

    @Test
    void 회원예약변경화면도_예약불가사유를_조회한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/my-reservations.js"
        );

        assertThat(js)
                .contains("/api/reservations/availability-notices")
                .contains("editAvailabilityNotice");
    }

    @Test
    void 관리자전화예약도_예약불가사유를_조회한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/admin-reservations.js"
        );

        assertThat(js)
                .contains("/api/reservations/availability-notices")
                .contains("phoneAvailabilityNotice");
    }

    private String resource(String path)
            throws Exception {
        try (InputStream input =
                     getClass().getResourceAsStream(path)) {

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
