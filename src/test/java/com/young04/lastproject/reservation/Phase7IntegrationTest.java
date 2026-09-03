package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.dto.MemberReservationCreateRequest;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.service.AuthenticatedReservationService;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.exception.ReservationImageAccessDeniedException;
import com.young04.lastproject.reservationimage.service.ReservationImageContent;
import com.young04.lastproject.reservationimage.service.ReservationImageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(
        properties =
                "file.upload-dir=build/test-phase7-upload"
)
@Transactional
class Phase7IntegrationTest {

    @Autowired
    AuthenticatedReservationService authenticatedReservationService;

    @Autowired
    ReservationImageService reservationImageService;

    @Autowired
    ServiceMenuReader serviceMenuReader;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void 회원_참고이미지는_보호된_API_URL을_사용한다() {
        Long serviceMenuNo =
                findActiveServiceMenuNo("커트");

        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest();

        request.setServiceMenuNo(serviceMenuNo);

        request.setStartAt(
                LocalDateTime.of(
                        2099,
                        1,
                        5,
                        15,
                        0
                )
        );

        ReservationResponse reservation =
                authenticatedReservationService
                        .createMyReservation(
                                "phase2_test_member",
                                request
                        );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "reference.jpg",
                        "image/jpeg",
                        "phase7-image".getBytes()
                );

        ReservationImageResponse image =
                reservationImageService
                        .uploadForMember(
                                "phase2_test_member",
                                reservation.getReservationNo(),
                                file
                        );

        assertThat(image.getFileUrl())
                .startsWith(
                        "/api/reservations/me/"
                )
                .endsWith("/content");

        ReservationImageContent content =
                reservationImageService
                        .getMemberContent(
                                "phase2_test_member",
                                reservation.getReservationNo(),
                                image.getReservationImageNo()
                        );

        assertThat(content.resource().exists())
                .isTrue();

        reservationImageService.deleteForMember(
                "phase2_test_member",
                reservation.getReservationNo(),
                image.getReservationImageNo()
        );
    }

    @Test
    void 비회원_참고이미지는_전화번호가_일치해야_업로드할수있다() {
        /*
         * ownership 검증 자체를 확인한다.
         * 존재하지 않는 예약/전화번호 조합은 권한 오류여야 한다.
         */
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "reference.jpg",
                        "image/jpeg",
                        "phase7-image".getBytes()
                );

        assertThatThrownBy(
                () ->
                        reservationImageService
                                .uploadForGuest(
                                        999999999L,
                                        "010-1111-2222",
                                        file
                                )
        )
                .isInstanceOf(
                        ReservationImageAccessDeniedException.class
                );
    }

    @Test
    void 예약화면_시술목록에서는_PHASE_TEST_메뉴를_숨긴다() {
        assertThat(
                serviceMenuReader
                        .getActiveServiceMenus()
        )
                .extracting(menu -> menu.getName())
                .noneMatch(
                        name ->
                                name != null
                                        && name.toUpperCase()
                                                .matches(
                                                        "PHASE.*TEST.*"
                                                )
                );
    }

    private Long findActiveServiceMenuNo(
            String name
    ) {
        Number result =
                (Number) entityManager
                        .createNativeQuery("""
                            SELECT MIN(NO)
                            FROM SERVICE_MENU
                            WHERE NAME = :name
                              AND ACTIVE_YN = 'Y'
                            """)
                        .setParameter(
                                "name",
                                name
                        )
                        .getSingleResult();

        if (result == null) {
            throw new IllegalStateException(
                    "활성화된 시술 메뉴를 찾을 수 없습니다. name="
                            + name
            );
        }

        return result.longValue();
    }
}
