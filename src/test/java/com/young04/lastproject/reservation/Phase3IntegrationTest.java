package com.young04.lastproject.reservation;

import com.young04.lastproject.reservation.dto.AdminReservationSearchResponse;
import com.young04.lastproject.reservation.dto.AvailableTimeResponse;
import com.young04.lastproject.reservation.dto.ReservationCreateRequest;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.AvailableTimeService;
import com.young04.lastproject.reservation.service.ReservationService;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.service.ReservationImageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "file.upload-dir=build/test-uploads"
        }
)
@SpringBootTest
@Transactional
class Phase3IntegrationTest {

    private static final LocalDate MONDAY =
            LocalDate.of(2099, 1, 5);

    @Autowired
    ReservationService reservationService;

    @Autowired
    AvailableTimeService availableTimeService;

    @Autowired
    AdminReservationService adminReservationService;

    @Autowired
    ReservationImageService reservationImageService;

    @PersistenceContext
    EntityManager entityManager;

    Long memberNo;
    Long serviceMenuNo;

    @BeforeEach
    void setUp() {
        memberNo =
                ((Number) entityManager
                        .createNativeQuery(
                                "SELECT NO FROM MEMBER "
                                        + "WHERE MEMBER_ID='phase2_test_member'"
                        )
                        .getSingleResult())
                        .longValue();

        serviceMenuNo =
                ((Number) entityManager
                        .createNativeQuery(
                                "SELECT NO FROM SERVICE_MENU "
                                        + "WHERE NAME='PHASE2_TEST_CUT_30'"
                        )
                        .getSingleResult())
                        .longValue();
    }

    @Test
    void 최적화된_예약가능시간_조회는_기존결과와_같다() {
        List<AvailableTimeResponse> result =
                availableTimeService
                        .getAvailableTimes(
                                MONDAY,
                                serviceMenuNo
                        );

        assertThat(result)
                .hasSize(20);

        assertThat(result.getFirst()
                .getStartTime())
                .isEqualTo(
                        LocalTime.of(10, 0)
                );

        assertThat(result.getLast()
                .getStartTime())
                .isEqualTo(
                        LocalTime.of(19, 30)
                );
    }

    @Test
    void 관리자_QueryDSL_검색이_서비스에서도_동작한다() {
        reservationService.createReservation(
                memberRequest(
                        MONDAY.atTime(11, 0)
                )
        );

        ReservationSearchCondition condition =
                new ReservationSearchCondition();

        condition.setStatus(
                ReservationStatus.REQUESTED
        );

        AdminReservationSearchResponse result =
                adminReservationService.search(
                        condition,
                        0,
                        20
                );

        assertThat(result.getContent())
                .isNotEmpty();

        assertThat(result.getContent())
                .allSatisfy(r ->
                        assertThat(r.getStatus())
                                .isEqualTo(
                                        ReservationStatus.REQUESTED
                                )
                );
    }

    @Test
    void 예약사진을_업로드하고_조회한다() {
        var reservation =
                reservationService.createReservation(
                        memberRequest(
                                MONDAY.atTime(12, 0)
                        )
                );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "style.jpg",
                        "image/jpeg",
                        new byte[]{
                                1, 2, 3, 4, 5
                        }
                );

        ReservationImageResponse uploaded =
                reservationImageService.upload(
                        reservation.getReservationNo(),
                        file
                );

        assertThat(
                uploaded.getReservationImageNo()
        ).isNotNull();

        assertThat(
                reservationImageService
                        .getImages(
                                reservation.getReservationNo()
                        )
        ).hasSize(1);
    }

    private ReservationCreateRequest memberRequest(
            java.time.LocalDateTime startAt
    ) {
        ReservationCreateRequest request =
                new ReservationCreateRequest();

        request.setMemberNo(memberNo);
        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(startAt);
        request.setRequestMemo(
                "3차 통합 테스트"
        );
        request.setReservationSource(
                ReservationSource.ONLINE
        );

        return request;
    }
}
