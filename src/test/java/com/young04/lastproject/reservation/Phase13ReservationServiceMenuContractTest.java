package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Phase13ReservationServiceMenuContractTest {

    @Test
    void 예약화면은_고정된_다섯개_시술카테고리를_제공한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        assertThat(html)
                .contains("1. 시술 카테고리 선택")
                .contains("value=\"CUT\"")
                .contains("value=\"PERM\"")
                .contains("value=\"COLOR\"")
                .contains("value=\"CLINIC\"")
                .contains("value=\"ETC\"")
                .contains(">커트<")
                .contains(">펌<")
                .contains(">컬러<")
                .contains(">클리닉<")
                .contains(">기타<");
    }

    @Test
    void 두번째단계는_DB의_상세시술메뉴를_카테고리별로_표시한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        assertThat(html)
                .contains("2. 시술 메뉴 선택")
                .contains("id=\"serviceMenuList\"")
                .contains("data-category=${menu.category}")
                .contains("data-service-menu-no=${menu.serviceMenuNo}")
                .contains("name=\"serviceMenu\"")
                .contains("${menu.serviceMenuNo}")
                .contains("#numbers.formatInteger(menu.price, 1, 'COMMA')")
                .contains("${menu.durationMin}");
    }

    @Test
    void 예약화면은_헤어스타일_이미지를_직접_선택하지않는다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        String js =
                resource(
                        "/static/js/reservation/reservation-form.js"
                );

        assertThat(html)
                .doesNotContain("hairStyleList")
                .doesNotContain("clearHairStyle")
                .doesNotContain("hair-style-radio")
                .contains("th:href=\"@{/hairstyles}\"")
                .contains("헤어스타일 보기");

        assertThat(js)
                .doesNotContain("loadHairStyles")
                .doesNotContain("/api/reservations/hair-styles")
                .contains("selectedHairStyleNo")
                .contains("hairStyleNo:")
                .contains("selectedHairStyleNo");
    }

    @Test
    void 카테고리_선택후_상세메뉴를_필터링한다()
            throws Exception {

        String js =
                resource(
                        "/static/js/reservation/reservation-form.js"
                );

        assertThat(js)
                .contains("renderServiceMenus")
                .contains("card.dataset.category")
                .contains("selectedCategory")
                .contains("selectedMenuNo")
                .contains("시술 카테고리, 상세 메뉴, 날짜, 시간을 선택해주세요.");
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
