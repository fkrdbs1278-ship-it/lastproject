package com.young04.lastproject.reservation;

import com.young04.lastproject.noshow.entity.NoShow;
import com.young04.lastproject.noshow.service.NoShowService;
import com.young04.lastproject.reservation.dto.AvailableTimeResponse;
import com.young04.lastproject.reservation.dto.ReservationCreateRequest;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.exception.ReservationUnavailableException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.service.AvailableTimeService;
import com.young04.lastproject.reservation.service.ReservationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class Phase2ServiceIntegrationTest {

    private static final LocalDate MONDAY = LocalDate.of(2099, 1, 5);
    private static final LocalDate SUNDAY = LocalDate.of(2099, 1, 4);

    @Autowired ReservationService reservationService;
    @Autowired AvailableTimeService availableTimeService;
    @Autowired NoShowService noShowService;
    @Autowired ReservationRepository reservationRepository;

    @PersistenceContext
    EntityManager entityManager;

    Long memberNo;
    Long serviceMenuNo;

    @BeforeEach
    void setUp() {
        memberNo = ((Number) entityManager.createNativeQuery(
                "SELECT NO FROM MEMBER WHERE MEMBER_ID = 'phase2_test_member'"
        ).getSingleResult()).longValue();

        serviceMenuNo = ((Number) entityManager.createNativeQuery(
                "SELECT NO FROM SERVICE_MENU WHERE NAME = 'PHASE2_TEST_CUT_30'"
        ).getSingleResult()).longValue();

        System.out.println("테스트 MEMBER.NO = " + memberNo);
        System.out.println("테스트 SERVICE_MENU.NO = " + serviceMenuNo);
    }

    @Test
    void 예약_가능시간을_조회한다() {
        List<AvailableTimeResponse> result =
                availableTimeService.getAvailableTimes(MONDAY, serviceMenuNo);

        System.out.println("예약 가능 슬롯 수 = " + result.size());

        assertThat(result).isNotEmpty();

        assertThat(result).anySatisfy(time -> {
            assertThat(time.getStartTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(time.getEndTime()).isEqualTo(LocalTime.of(10, 30));
        });

        assertThat(result).anySatisfy(time -> {
            assertThat(time.getStartTime()).isEqualTo(LocalTime.of(19, 30));
            assertThat(time.getEndTime()).isEqualTo(LocalTime.of(20, 0));
        });
    }

    @Test
    void 영업시간_밖의_예약은_차단한다() {
        ReservationCreateRequest request = memberRequest(MONDAY.atTime(9, 30));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ReservationUnavailableException.class)
                .hasMessageContaining("예약할 수 없습니다");
    }

    @Test
    void 일요일_정기휴무에는_예약할수없다() {
        ReservationCreateRequest request = memberRequest(SUNDAY.atTime(11, 0));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void 기존예약과_겹치는_시간은_차단한다() {
        Reservation existing =
                Reservation.createMemberReservation(
                        memberNo,
                        serviceMenuNo,
                        "PHASE2_TEST_CUT_30",
                        30,
                        MONDAY.atTime(14, 0),
                        MONDAY.atTime(14, 30),
                        "기존 예약",
                        ReservationSource.ONLINE
                );

        reservationRepository.saveAndFlush(existing);

        ReservationCreateRequest request = memberRequest(MONDAY.atTime(14, 10));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ReservationUnavailableException.class);
    }

    @Test
    void 정상_회원예약을_생성한다() {
        ReservationResponse result =
                reservationService.createReservation(
                        memberRequest(MONDAY.atTime(11, 0))
                );

        assertThat(result.getReservationNo()).isNotNull();
        assertThat(result.getMemberNo()).isEqualTo(memberNo);
        assertThat(result.getCustomerType()).isEqualTo(CustomerType.MEMBER);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        assertThat(result.getStartAt()).isEqualTo(MONDAY.atTime(11, 0));
        assertThat(result.getEndAt()).isEqualTo(MONDAY.atTime(11, 30));
    }

    @Test
    void 비회원_예약을_생성한다() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setGuestName("비회원테스트");
        request.setGuestPhone("01090000002");
        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(MONDAY.atTime(12, 0));
        request.setRequestMemo("비회원 예약 테스트");
        request.setReservationSource(ReservationSource.ONLINE);

        ReservationResponse result =
                reservationService.createReservation(request);

        assertThat(result.getReservationNo()).isNotNull();
        assertThat(result.getMemberNo()).isNull();
        assertThat(result.getCustomerType()).isEqualTo(CustomerType.GUEST);
        assertThat(result.getGuestName()).isEqualTo("비회원테스트");
        assertThat(result.getGuestPhone()).isEqualTo("01090000002");
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
    }

    @Test
    void 예약신청을_예약확정으로_변경한다() {
        ReservationResponse created =
                reservationService.createReservation(
                        memberRequest(MONDAY.atTime(13, 0))
                );

        ReservationResponse confirmed =
                reservationService.confirmReservation(created.getReservationNo());

        assertThat(confirmed.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 확정예약을_시술완료로_변경한다() {
        ReservationResponse created =
                reservationService.createReservation(
                        memberRequest(MONDAY.atTime(13, 30))
                );

        reservationService.confirmReservation(created.getReservationNo());

        ReservationResponse completed =
                reservationService.completeReservation(created.getReservationNo());

        assertThat(completed.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    void 예약을_취소한다() {
        ReservationResponse created =
                reservationService.createReservation(
                        memberRequest(MONDAY.atTime(15, 0))
                );

        ReservationResponse canceled =
                reservationService.cancelReservation(
                        created.getReservationNo(),
                        "개인 일정으로 취소",
                        CanceledBy.USER
                );

        assertThat(canceled.getStatus())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    void 확정예약을_노쇼처리한다() {
        ReservationResponse created =
                reservationService.createReservation(
                        memberRequest(MONDAY.atTime(16, 0))
                );

        reservationService.confirmReservation(created.getReservationNo());

        NoShow noShow =
                noShowService.markNoShow(
                        created.getReservationNo(),
                        "예약시간 미방문",
                        "테스트 노쇼 처리"
                );

        entityManager.flush();

        Reservation reservation =
                reservationRepository.findById(created.getReservationNo())
                        .orElseThrow();

        assertThat(noShow.getNoShowNo()).isNotNull();
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.NO_SHOW);
    }

    private ReservationCreateRequest memberRequest(LocalDateTime startAt) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setMemberNo(memberNo);
        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(startAt);
        request.setRequestMemo("2차 Service 통합 테스트");
        request.setReservationSource(ReservationSource.ONLINE);
        return request;
    }
}
