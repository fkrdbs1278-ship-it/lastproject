package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase8UiContractTest {

    @Test
    void 회원예약화면은_상세_변경_취소_API를_사용한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/my-reservations.js"
        );

        assertThat(js)
                .contains("/api/reservations/me/${reservationNo}")
                .contains("method: \"PUT\"")
                .contains("/cancel?")
                .contains("예약 변경")
                .doesNotContain("location.reload(")
                .doesNotContain("window.location.reload(");
    }

    @Test
    void 관리자화면은_운영관리_API를_한화면에서_사용한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/admin-reservations.js"
        );

        assertThat(js)
                .contains("/admin/api/reservations/phone")
                .contains("/admin/api/business-hours")
                .contains("/admin/api/holidays")
                .contains("/admin/api/availability-blocks")
                .doesNotContain("location.reload(")
                .doesNotContain("window.location.reload(");
    }

    @Test
    void 관리자템플릿에_운영도구_버튼이_존재한다()
            throws Exception {
        String html = resource(
                "/templates/admin/reservation-list.html"
        );

        assertThat(html)
                .contains("openPhoneReservation")
                .contains("openBusinessHours")
                .contains("openHolidays")
                .contains("openAvailabilityBlocks");
    }

    @Test
    void 회원템플릿에_상세_변경_모달이_존재한다()
            throws Exception {
        String html = resource(
                "/templates/reservation/my-reservations.html"
        );

        assertThat(html)
                .contains("memberDetailOverlay")
                .contains("memberEditOverlay")
                .contains("memberEditForm");
    }

    @Test
    void 비회원전화번호는_010_11자리만_허용한다()
            throws Exception {
        String js = resource(
                "/static/js/reservation/guest-reservation.js"
        );

        assertThat(js)
                .contains("^010-?\\d{4}-?\\d{4}$")
                .doesNotContain("^01[016789]");
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
