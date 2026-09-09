package com.young04.lastproject.reservation;

import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.noshow.repository.NoShowRepository;
import com.young04.lastproject.noshow.service.NoShowService;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.exception.InvalidReservationStatusException;
import com.young04.lastproject.reservation.notification.ReservationNotificationPublisher;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.service.AvailableTimeService;
import com.young04.lastproject.reservation.service.HairStyleReader;
import com.young04.lastproject.reservation.service.ReservationService;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import com.young04.lastproject.servicematerial.service.MaterialUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Phase101ReservationMutationSafetyTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    AvailableTimeService availableTimeService;

    @Mock
    ServiceMenuReader serviceMenuReader;

    @Mock
    HairStyleReader hairStyleReader;

    @Mock
    BusinessHourRepository businessHourRepository;

    @Mock
    ReservationNotificationPublisher notificationPublisher;

    @Mock
    MaterialUsageService materialUsageService;

    @Mock
    NoShowRepository noShowRepository;

    ReservationService reservationService;

    @BeforeEach
    void setUp() {

        reservationService =
                new ReservationService(
                        reservationRepository,
                        availableTimeService,
                        serviceMenuReader,
                        hairStyleReader,
                        businessHourRepository,
                        notificationPublisher,
                        materialUsageService
                );
    }

    @Test
    void 예약시작전에는_시술완료로_변경할수없다() {

        Reservation reservation =
                guestReservation(
                        LocalDateTime.now()
                                .plusDays(1)
                );

        reservation.confirm();

        when(
                reservationRepository
                        .findByIdForUpdate(400L)
        )
                .thenReturn(
                        Optional.of(reservation)
                );

        assertThatThrownBy(
                () ->
                        reservationService
                                .completeReservation(400L)
        )
                .isInstanceOf(
                        InvalidReservationStatusException.class
                )
                .hasMessageContaining(
                        "예약 시작 전"
                );

        verify(
                notificationPublisher,
                never()
        )
                .publish(
                        any(),
                        any()
                );
    }

    @Test
    void 지난예약은_고객이_직접취소할수없다() {

        Reservation reservation =
                guestReservation(
                        LocalDateTime.now()
                                .minusHours(1)
                );

        when(
                reservationRepository
                        .findByIdForUpdate(401L)
        )
                .thenReturn(
                        Optional.of(reservation)
                );

        assertThatThrownBy(
                () ->
                        reservationService
                                .cancelReservation(
                                        401L,
                                        "고객 요청",
                                        CanceledBy.USER
                                )
        )
                .isInstanceOf(
                        InvalidReservationStatusException.class
                )
                .hasMessageContaining(
                        "미용실에 문의"
                );
    }

    @Test
    void 예약시작전에는_노쇼처리할수없다() {

        Reservation reservation =
                guestReservation(
                        LocalDateTime.now()
                                .plusHours(2)
                );

        reservation.confirm();

        when(
                reservationRepository
                        .findByIdForUpdate(402L)
        )
                .thenReturn(
                        Optional.of(reservation)
                );

        NoShowService noShowService =
                new NoShowService(
                        reservationRepository,
                        noShowRepository
                );

        assertThatThrownBy(
                () ->
                        noShowService
                                .markNoShow(
                                        402L,
                                        "테스트",
                                        null
                                )
        )
                .isInstanceOf(
                        InvalidReservationStatusException.class
                )
                .hasMessageContaining(
                        "예약 시작 전"
                );
    }

    private Reservation guestReservation(
            LocalDateTime startAt
    ) {

        return Reservation
                .createGuestReservation(
                        "테스트고객",
                        "01012345678",
                        1L,
                        "커트",
                        30,
                        startAt,
                        startAt.plusMinutes(30),
                        null,
                        ReservationSource.ONLINE
                );
    }
}