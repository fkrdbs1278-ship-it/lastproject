package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Phase14HairStyleReservationLinkContractTest {

    @Test
    void 헤어스타일_상세에서_선택한_스타일로_예약할수있다()
            throws Exception {

        String html =
                resource(
                        "/templates/hairstyle/detail.html"
                );

        assertThat(html)
                .contains("이 스타일로 예약하기")
                .contains(
                        "hairStyleNo=${detail.hairStyle.no}"
                )
                .contains(
                        "serviceMenuNo=${service.no}"
                )
                .contains("@{/reservation(");
    }

    @Test
    void 예약페이지는_헤어스타일_쿼리파라미터를_받는다()
            throws Exception {

        String java =
                source(
                        "src/main/java/com/young04/lastproject/reservation/controller/ReservationPageController.java"
                );

        assertThat(java)
                .contains("name = \"hairStyleNo\"")
                .contains("name = \"serviceMenuNo\"")
                .contains("findActiveById(hairStyleNo)")
                .contains("linkedServiceMenuNos")
                .contains("preferredServiceMenuNo");
    }

    @Test
    void 예약화면은_선택헤어스타일을_텍스트로_표시하고_연결메뉴를_전달한다()
            throws Exception {

        String html =
                resource(
                        "/templates/reservation/reservation-form.html"
                );

        assertThat(html)
                .contains("selectedHairStyleContext")
                .contains("SELECTED HAIR STYLE")
                .contains("${selectedHairStyle.title}")
                .contains("data-hair-style-no=")
                .contains("data-linked-service-menu-nos=")
                .doesNotContain("hair-style-radio");
    }

    @Test
    void 헤어스타일에서_진입한_예약은_hairStyleNo를_저장한다()
            throws Exception {

        String js =
                resource(
                        "/static/js/reservation/reservation-form.js"
                );

        assertThat(js)
                .contains("selectedHairStyleNo")
                .contains("linkedServiceMenuNos")
                .contains(
                        "initializeHairStyleReservationContext"
                )
                .contains("hairStyleNo:")
                .contains("card.dataset.serviceMenuNo");
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

    private String source(String path)
            throws Exception {

        return Files.readString(
                Path.of(path),
                StandardCharsets.UTF_8
        );
    }
}
