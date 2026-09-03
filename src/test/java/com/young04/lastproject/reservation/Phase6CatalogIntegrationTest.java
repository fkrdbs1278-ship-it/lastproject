package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.dto.HairStyleOptionResponse;
import com.young04.lastproject.reservation.dto.SalonEventOptionResponse;
import com.young04.lastproject.reservation.dto.ServiceMenuOptionResponse;
import com.young04.lastproject.reservation.service.HairStyleReader;
import com.young04.lastproject.reservation.service.SalonEventReader;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class Phase6CatalogIntegrationTest {

    @Autowired
    ServiceMenuReader serviceMenuReader;

    @Autowired
    HairStyleReader hairStyleReader;

    @Autowired
    SalonEventReader salonEventReader;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void 예약용_시술메뉴는_커트_펌_컬러_클리닉을_조회한다() {
        List<ServiceMenuOptionResponse> menus =
                serviceMenuReader.getActiveServiceMenus();

        assertThat(menus)
                .extracting(ServiceMenuOptionResponse::getCategory)
                .contains(
                        "CUT",
                        "PERM",
                        "COLOR",
                        "CLINIC"
                );

        assertThat(menus)
                .extracting(ServiceMenuOptionResponse::getCategory)
                .doesNotContain("DRY", "EVENT");
    }

    @Test
    void 남녀_예시스타일과_업로드경로를_조회한다() {
        Long cutNo = findServiceMenuNo("커트");

        List<HairStyleOptionResponse> styles =
                hairStyleReader.getActiveStylesForService(cutNo);

        assertThat(styles)
                .extracting(HairStyleOptionResponse::getTitle)
                .contains(
                        "남자 댄디컷",
                        "여자 레이어드컷"
                );

        assertThat(styles)
                .allSatisfy(style ->
                        assertThat(style.getImageUrl())
                                .startsWith("/uploads/hairstyle/")
                );
    }

    @Test
    void 진행중인_4part_이벤트를_예약에서_조회한다() {
        entityManager.createNativeQuery("""
                INSERT INTO SALON_EVENT (
                    EVENT_TITLE,
                    EVENT_CONTENT,
                    EVENT_TYPE,
                    START_DATE,
                    END_DATE,
                    USE_YN
                )
                VALUES (
                    'PHASE6_TEST_EVENT',
                    '예약 화면 이벤트 조회 테스트',
                    'GENERAL',
                    SYSTIMESTAMP - INTERVAL '1' DAY,
                    SYSTIMESTAMP + INTERVAL '1' DAY,
                    'Y'
                )
                """)
                .executeUpdate();

        List<SalonEventOptionResponse> events =
                salonEventReader.getOngoingEvents();

        assertThat(events)
                .extracting(SalonEventOptionResponse::getTitle)
                .contains("PHASE6_TEST_EVENT");
    }

    private Long findServiceMenuNo(String name) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT MIN(NO)
                FROM SERVICE_MENU
                WHERE NAME = :name
                  AND ACTIVE_YN = 'Y'
                """)
                .setParameter("name", name)
                .getSingleResult();

        return result.longValue();
    }
}
