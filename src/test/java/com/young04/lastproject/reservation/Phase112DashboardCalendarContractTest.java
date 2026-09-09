package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase112DashboardCalendarContractTest {

    @Test
    void 관리자_대시보드는_주간_캘린더를_사용한다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .contains("class=\"week-calendar\"")
                .contains("reservationSummary.calendarDays")
                .contains("reservationSummary.calendarBlocks")
                .contains("reservationSummary.calendarStartHour")
                .contains("reservationSummary.calendarEndHour")
                .contains("전체 예약 보기");
    }

    @Test
    void 예약_블록은_예약관리_상세로_연결된다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .contains(
                        "@{/admin/reservations(reservationNo=${block.reservationNo})}"
                )
                .contains("reservation-schedule-block")
                .contains("#strings.toLowerCase(block.reservationStatus.name())")
                .contains("calendar-schedule-block")
                .contains("block.reservationNo");
    }

    @Test
    void 예약_상태_범례가_존재한다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .contains("requested-color")
                .contains("confirmed-color")
                .contains("completed-color")
                .contains("no-show-color")
                .contains("접수")
                .contains("확정")
                .contains("시술 완료")
                .contains("노쇼");
    }

    @Test
    void 기존_한줄형_주간목록은_대시보드에서_제거된다()
            throws Exception {

        String html =
                resource(
                        "/templates/admin/dashboard.html"
                );

        assertThat(html)
                .doesNotContain("dashboard-reservation-list")
                .doesNotContain("dashboard-reservation-item");
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
