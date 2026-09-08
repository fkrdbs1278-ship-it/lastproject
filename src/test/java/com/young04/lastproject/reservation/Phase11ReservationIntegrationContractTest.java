package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase11ReservationIntegrationContractTest {

    @Test
    void 헤더의_시술메뉴는_예약으로_연결된다()
            throws Exception {

        String html =
                resource(
                        "/templates/layout/header.html"
                );

        assertThat(html)
                .contains("th:href=\"@{/reservation}\"")
                .contains("예약")
                .doesNotContain("th:href=\"@{/services}\"");
    }

    @Test
    void 마이페이지_예약내역은_내예약으로_연결된다()
            throws Exception {

        String html =
                resource(
                        "/templates/member/mypage.html"
                );

        assertThat(html)
                .contains("th:href=\"@{/my-reservations}\"")
                .contains("예약 내역")
                .contains("예약 상태, 일정, 변경 및 취소 내역");
    }

    @Test
    void 관리자_대시보드는_예약운영기능을_연결한다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .contains("reservationSummary.todayCount")
                .contains("reservationSummary.weekReservations")
                .contains("@{/admin/reservations}")
                .contains("tool='phone'")
                .contains("tool='business-hours'")
                .contains("tool='holidays'")
                .contains("tool='availability-blocks'");
    }

    @Test
    void 관리자_예약페이지는_대시보드_딥링크를_처리한다()
            throws Exception {

        String js =
                resource(
                        "/static/js/reservation/admin-reservations.js"
                );

        assertThat(js)
                .contains("initializeFromQuery")
                .contains("business-hours")
                .contains("availability-blocks")
                .contains("reservationNo")
                .contains("openDetail(reservationNo)");
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
