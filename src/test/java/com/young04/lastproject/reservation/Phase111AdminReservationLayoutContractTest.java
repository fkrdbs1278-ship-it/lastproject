package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase111AdminReservationLayoutContractTest {

    @Test
    void 예약관리_화면은_관리자_공통_사이드바를_사용한다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/reservation-list.html"
                );

        assertThat(html)
                .contains(
                        "admin/dashboard-sidebar :: sidebar('reservation')"
                )
                .contains("class=\"admin-layout\"")
                .contains("dashboard-content reservation-admin-content")
                .contains("/css/admin/dashboard-sidebar.css")
                .contains("/css/admin/dashboard.css")
                .contains("/css/admin/reservation-admin.css");
    }

    @Test
    void 예약_운영_기능은_예약관리_화면에_존재한다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/reservation-list.html"
                );

        assertThat(html)
                .contains("id=\"openPhoneReservation\"")
                .contains("id=\"openBusinessHours\"")
                .contains("id=\"openHolidays\"")
                .contains("id=\"openAvailabilityBlocks\"")
                .contains("예약 관리")
                .contains("전화 예약")
                .contains("영업시간")
                .contains("휴일 관리")
                .contains("개인 일정");
    }

    @Test
    void 대시보드에는_예약_운영_바로가기_블록이_남아있지않는다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .doesNotContain("reservation-quick-actions")
                .doesNotContain("tool='phone'")
                .doesNotContain("tool='business-hours'")
                .doesNotContain("tool='holidays'")
                .doesNotContain("tool='availability-blocks'");
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
