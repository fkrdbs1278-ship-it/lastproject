package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.dto.ServiceMenuOptionResponse;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
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
class Phase4IntegrationTest {

    @Autowired
    ServiceMenuReader serviceMenuReader;

    @Autowired
    ReservationMemberReader reservationMemberReader;

    @Test
    void 예약화면용_활성시술메뉴를_조회한다() {
        List<ServiceMenuOptionResponse> menus =
                serviceMenuReader.getActiveServiceMenus();

        assertThat(menus)
                .isNotEmpty();

        assertThat(menus)
                .anySatisfy(menu -> {
                    assertThat(menu.getName())
                            .isEqualTo("PHASE2_TEST_CUT_30");

                    assertThat(menu.getDurationMin())
                            .isEqualTo(30);
                });
    }

    @Test
    void 로그인아이디로_회원번호를_조회한다() {
        Long memberNo =
                reservationMemberReader
                        .findMemberNoByMemberId(
                                "phase2_test_member"
                        )
                        .orElseThrow();

        assertThat(memberNo)
                .isPositive();
    }

    @Test
    void 존재하지않는_회원아이디는_empty를_반환한다() {
        assertThat(
                reservationMemberReader
                        .findMemberNoByMemberId(
                                "not_exists_phase4_member"
                        )
        ).isEmpty();
    }
}
