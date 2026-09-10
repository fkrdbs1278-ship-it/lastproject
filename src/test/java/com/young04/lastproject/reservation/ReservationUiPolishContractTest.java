package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationUiPolishContractTest {

    @Test
    void 관리자_예약검색에_예약번호와_전화번호_입력제한이_있다() throws Exception {
        String html = Files.readString(Path.of(
                "src/main/resources/templates/admin/reservation-list.html"
        ));
        String dto = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/reservation/dto/ReservationSearchCondition.java"
        ));
        String repo = Files.readString(Path.of(
                "src/main/java/com/young04/lastproject/reservation/repository/ReservationRepositoryImpl.java"
        ));

        assertThat(html).contains("id=\"reservationNoSearch\"");
        assertThat(html).contains("maxlength=\"11\"");
        assertThat(html).contains("<span>이름</span>");
        assertThat(html).contains("<span>전화번호</span>");
        assertThat(dto).contains("private Long reservationNo;");
        assertThat(repo).contains("condition.getReservationNo()");
    }

    @Test
    void 내예약_취소는_browser_prompt가_아닌_프로젝트_모달을_사용한다() throws Exception {
        String html = Files.readString(Path.of(
                "src/main/resources/templates/reservation/my-reservations.html"
        ));
        String js = Files.readString(Path.of(
                "src/main/resources/static/js/reservation/my-reservations.js"
        ));

        assertThat(html).contains("id=\"memberCancelOverlay\"");
        assertThat(html).contains("id=\"cancelReason\"");
        assertThat(js).doesNotContain("prompt(\"취소 사유를 입력해주세요.\")");
        assertThat(js).contains("openCancelModal(reservation.reservationNo)");
    }

    @Test
    void 예약화면_시술메뉴_카드가_선택정보를_명확히_표현한다() throws Exception {
        String html = Files.readString(Path.of(
                "src/main/resources/templates/reservation/reservation-form.html"
        ));

        assertThat(html).contains("service-detail-check");
        assertThat(html).contains("service-detail-meta");
        assertThat(html).contains("service-detail-price");
        assertThat(html).contains("service-detail-duration");
    }

    @Test
    void 대시보드는_개인일정과_고객예약_건수를_구분해서_표현한다() throws Exception {
        String html = Files.readString(Path.of(
                "src/main/resources/templates/admin/dashboard.html"
        ));

        assertThat(html).contains("오늘 고객 예약");
        assertThat(html).contains("개인 일정·휴무는 예약 건수에 포함되지 않습니다.");
        assertThat(html).contains("personal-color");
        assertThat(html).contains("holiday-color");
    }
}
