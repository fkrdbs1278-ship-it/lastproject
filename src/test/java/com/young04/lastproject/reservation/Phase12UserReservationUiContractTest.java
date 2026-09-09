package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase12UserReservationUiContractTest {

    @Test
    void 예약하기는_메인사이트_공통레이아웃을_사용한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        assertThat(html)
                .contains("layout/header :: header")
                .contains("layout/footer :: footer")
                .contains("/css/common.css")
                .contains("/css/layout/header.css")
                .contains("/css/layout/footer.css")
                .contains("/css/reservation/reservation-user.css")
                .contains("th:href=\"@{/}\"")
                .contains("메인페이지");
    }

    @Test
    void 내예약은_마이페이지_스타일과_메인_마이페이지_이동을_제공한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/my-reservations.html"
                );

        assertThat(html)
                .contains("layout/header :: header")
                .contains("layout/footer :: footer")
                .contains("/css/member/mypage.css")
                .contains("class=\"mypage-section\"")
                .contains("th:href=\"@{/}\"")
                .contains("th:href=\"@{/member/mypage}\"")
                .contains("메인페이지")
                .contains("마이페이지");
    }

    @Test
    void 비회원예약조회는_메인사이트_공통레이아웃을_사용한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/guest-reservation.html"
                );

        assertThat(html)
                .contains("layout/header :: header")
                .contains("layout/footer :: footer")
                .contains("/css/common.css")
                .contains("/css/reservation/reservation-user.css")
                .contains("th:href=\"@{/}\"")
                .contains("메인페이지")
                .contains("비회원 예약 조회");
    }

    @Test
    void 기존_예약_자바스크립트용_id는_유지된다()
            throws Exception {

        String reservation =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        String mine =
                resource(
                        "/templates/reservation/my-reservations.html"
                );

        String guest =
                resource(
                        "/templates/reservation/guest-reservation.html"
                );

        assertThat(reservation)
                .contains("id=\"reservationPage\"")
                .contains("id=\"submitReservation\"")
                .contains("id=\"reservationDate\"")
                .contains("id=\"timeSlots\"");

        assertThat(mine)
                .contains("id=\"myReservationPage\"")
                .contains("id=\"reservationList\"")
                .contains("id=\"memberDetailOverlay\"")
                .contains("id=\"memberEditOverlay\"");

        assertThat(guest)
                .contains("id=\"lookupReservationNo\"")
                .contains("id=\"lookupGuestPhone\"")
                .contains("id=\"lookupButton\"")
                .contains("id=\"lookupResult\"");
    }

    private String resource(String path)
            throws Exception {

        try (InputStream input =
                     getClass()
                             .getResourceAsStream(path)) {

            if (input == null) {
                throw new IllegalStateException(
                        "리소스를 찾을 수 없습니다: "
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
