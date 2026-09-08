package com.young04.lastproject.reservation;

import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.service.OperatingScheduleConflictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Phase10OperatingSafetyTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    BusinessHourRepository businessHourRepository;

    OperatingScheduleConflictService service;

    @BeforeEach
    void setUp() {
        service = new OperatingScheduleConflictService(
                reservationRepository,
                businessHourRepository
        );
    }

    @Test
    void 휴일시간과_활성예약이_겹치면_일정등록을_차단한다() {
        LocalDateTime start =
                LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime end =
                LocalDateTime.of(2026, 9, 10, 18, 0);

        when(reservationRepository.countOverlappingReservations(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList()
        )).thenReturn(1L);

        assertThatThrownBy(() ->
                service.assertNoActiveReservationOverlap(
                        start,
                        end
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약");
    }

    @Test
    void 영업시간_축소로_미래예약이_범위를_벗어나면_변경을_차단한다() {
        Reservation reservation = mock(Reservation.class);
        LocalDateTime monday =
                LocalDateTime.of(2026, 9, 14, 19, 30);

        when(reservation.getReservationNo()).thenReturn(300L);
        when(reservation.getStartAt()).thenReturn(monday);
        when(reservation.getEndAt()).thenReturn(monday.plusMinutes(30));

        when(reservationRepository
                .findByStatusInAndStartAtGreaterThanEqualOrderByStartAtAsc(
                        anyList(),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(reservation));

        assertThatThrownBy(() ->
                service.assertBusinessHourChangeSafe(
                        1,
                        true,
                        LocalTime.of(10, 0),
                        LocalTime.of(19, 0)
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약번호=300");
    }

    @Test
    void 영업시간_확장에는_기존예약이_있어도_문제가없다() {
        Reservation reservation = mock(Reservation.class);
        LocalDateTime monday =
                LocalDateTime.of(2026, 9, 14, 19, 30);

        when(reservation.getStartAt()).thenReturn(monday);
        when(reservation.getEndAt()).thenReturn(monday.plusMinutes(30));

        when(reservationRepository
                .findByStatusInAndStartAtGreaterThanEqualOrderByStartAtAsc(
                        anyList(),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(reservation));

        assertThatCode(() ->
                service.assertBusinessHourChangeSafe(
                        1,
                        true,
                        LocalTime.of(9, 0),
                        LocalTime.of(21, 0)
                )
        ).doesNotThrowAnyException();
    }
}
