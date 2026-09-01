package com.young04.lastproject.reservation;

import com.young04.lastproject.businesshour.dto.BusinessHourResponse;
import com.young04.lastproject.businesshour.dto.BusinessHourUpdateRequest;
import com.young04.lastproject.businesshour.service.BusinessHourService;
import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.AuthenticatedReservationService;
import com.young04.lastproject.reservation.service.ReservationService;
import com.young04.lastproject.salonholiday.dto.OwnerAvailabilityBlockRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.service.OwnerAvailabilityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class Phase5IntegrationTest {

    @Autowired
    AuthenticatedReservationService authenticatedReservationService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    AdminReservationService adminReservationService;

    @Autowired
    BusinessHourService businessHourService;

    @Autowired
    OwnerAvailabilityService ownerAvailabilityService;

    @PersistenceContext
    EntityManager entityManager;

    Long serviceMenuNo;

    @BeforeEach
    void setUp() {
        serviceMenuNo =
                ((Number) entityManager.createNativeQuery("""
                        SELECT NO
                        FROM SERVICE_MENU
                        WHERE NAME = 'PHASE2_TEST_CUT_30'
                        """)
                        .getSingleResult())
                        .longValue();
    }

    @Test
    void 로그인회원은_memberNo를_요청하지않고_자기예약을_생성조회한다() {
        MemberReservationCreateRequest request =
                new MemberReservationCreateRequest();

        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(
                LocalDateTime.of(2099, 1, 5, 10, 0)
        );
        request.setRequestMemo("회원 예약 테스트");

        ReservationResponse created =
                authenticatedReservationService
                        .createMyReservation(
                                "phase2_test_member",
                                request
                        );

        assertThat(created.getMemberNo())
                .isNotNull();

        assertThat(created.getCustomerType().name())
                .isEqualTo("MEMBER");

        assertThat(
                authenticatedReservationService
                        .getMyReservations(
                                "phase2_test_member"
                        )
        )
                .extracting(
                        ReservationResponse::getReservationNo
                )
                .contains(created.getReservationNo());

        ReservationDetailResponse detail =
                authenticatedReservationService
                        .getMyReservationDetail(
                                "phase2_test_member",
                                created.getReservationNo()
                        );

        assertThat(
                detail.getReservation()
                        .getRequestMemo()
        ).isEqualTo("회원 예약 테스트");
    }

    @Test
    void 로그인회원은_자기예약을_변경하고_취소한다() {
        MemberReservationCreateRequest create =
                new MemberReservationCreateRequest();

        create.setServiceMenuNo(serviceMenuNo);
        create.setStartAt(
                LocalDateTime.of(2099, 1, 5, 11, 0)
        );

        ReservationResponse created =
                authenticatedReservationService
                        .createMyReservation(
                                "phase2_test_member",
                                create
                        );

        ReservationUpdateRequest update =
                new ReservationUpdateRequest();

        update.setServiceMenuNo(serviceMenuNo);
        update.setStartAt(
                LocalDateTime.of(2099, 1, 5, 11, 30)
        );
        update.setRequestMemo("회원 변경");

        ReservationResponse updated =
                authenticatedReservationService
                        .updateMyReservation(
                                "phase2_test_member",
                                created.getReservationNo(),
                                update
                        );

        assertThat(updated.getStartAt())
                .isEqualTo(
                        LocalDateTime.of(
                                2099, 1, 5, 11, 30
                        )
                );

        ReservationResponse canceled =
                authenticatedReservationService
                        .cancelMyReservation(
                                "phase2_test_member",
                                created.getReservationNo(),
                                "일정 변경"
                        );

        assertThat(canceled.getStatus())
                .isEqualTo(
                        ReservationStatus.CANCELED
                );
    }

    @Test
    void 비회원은_예약번호와_전화번호를_확인한뒤_예약을_변경한다() {
        ReservationCreateRequest create =
                new ReservationCreateRequest();

        create.setGuestName("김영준");
        create.setGuestPhone("010-5655-0100");
        create.setServiceMenuNo(serviceMenuNo);
        create.setStartAt(
                LocalDateTime.of(2099, 1, 5, 12, 0)
        );
        create.setReservationSource(
                ReservationSource.ONLINE
        );

        ReservationResponse created =
                reservationService.createReservation(create);

        GuestReservationUpdateRequest update =
                new GuestReservationUpdateRequest();

        update.setReservationNo(
                created.getReservationNo()
        );
        update.setGuestPhone(
                "01056550100"
        );
        update.setServiceMenuNo(serviceMenuNo);
        update.setStartAt(
                LocalDateTime.of(2099, 1, 5, 12, 30)
        );
        update.setRequestMemo(
                "비회원 변경 완료"
        );

        ReservationResponse updated =
                reservationService
                        .updateGuestReservation(update);

        assertThat(updated.getRequestMemo())
                .isEqualTo("비회원 변경 완료");

        assertThat(updated.getStartAt())
                .isEqualTo(
                        LocalDateTime.of(
                                2099, 1, 5, 12, 30
                        )
                );
    }

    @Test
    void 관리자는_전화예약을_직접등록하면_PHONE_CONFIRMED가_된다() {
        AdminPhoneReservationRequest request =
                new AdminPhoneReservationRequest();

        request.setGuestName("전화예약고객");
        request.setGuestPhone("010-1234-5678");
        request.setServiceMenuNo(serviceMenuNo);
        request.setStartAt(
                LocalDateTime.of(2099, 1, 5, 13, 0)
        );
        request.setRequestMemo(
                "전화로 예약 접수"
        );

        ReservationResponse result =
                adminReservationService
                        .createPhoneReservation(request);

        assertThat(result.getReservationSource())
                .isEqualTo(
                        ReservationSource.PHONE
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        ReservationStatus.CONFIRMED
                );
    }

    @Test
    void 관리자는_영업시간과_정기휴무를_변경할수있다() {
        BusinessHourUpdateRequest request =
                new BusinessHourUpdateRequest();

        request.setOpen(true);
        request.setOpenTime(
                LocalTime.of(9, 30)
        );
        request.setCloseTime(
                LocalTime.of(19, 30)
        );

        BusinessHourResponse changed =
                businessHourService
                        .updateBusinessHour(
                                1,
                                request
                        );

        assertThat(changed.isOpen())
                .isTrue();

        assertThat(changed.getOpenTime())
                .isEqualTo("09:30");

        BusinessHourUpdateRequest closed =
                new BusinessHourUpdateRequest();

        closed.setOpen(false);

        BusinessHourResponse sunday =
                businessHourService
                        .updateBusinessHour(
                                7,
                                closed
                        );

        assertThat(sunday.isOpen())
                .isFalse();

        assertThat(sunday.getOpenTime())
                .isNull();
    }

    @Test
    void 원장_개인일정은_PERSONAL_예약차단시간으로_관리한다() {
        OwnerAvailabilityBlockRequest request =
                new OwnerAvailabilityBlockRequest();

        request.setTitle("원장 외출");
        request.setStartAt(
                LocalDateTime.of(
                        2099, 1, 6, 14, 0
                )
        );
        request.setEndAt(
                LocalDateTime.of(
                        2099, 1, 6, 16, 0
                )
        );
        request.setAllDay(false);
        request.setMemo("예약 불가");

        SalonHolidayResponse created =
                ownerAvailabilityService
                        .createBlock(request);

        assertThat(
                created.getHolidayType().name()
        ).isEqualTo("PERSONAL");

        assertThat(
                ownerAvailabilityService
                        .getBlocks()
        )
                .extracting(
                        SalonHolidayResponse::getSalonHolidayNo
                )
                .contains(
                        created.getSalonHolidayNo()
                );
    }
}
