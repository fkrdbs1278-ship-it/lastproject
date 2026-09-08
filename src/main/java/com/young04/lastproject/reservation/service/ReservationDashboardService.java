package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.DashboardReservationItem;
import com.young04.lastproject.reservation.dto.DashboardReservationSummary;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationDashboardService {

    private final ReservationRepository reservationRepository;

    public DashboardReservationSummary getSummary() {
        LocalDateTime now =
                LocalDateTime.now();

        LocalDate today =
                now.toLocalDate();

        LocalDate weekStart =
                today.with(
                        DayOfWeek.MONDAY
                );

        LocalDate weekEnd =
                weekStart.plusDays(6);

        LocalDateTime todayStart =
                today.atStartOfDay();

        LocalDateTime tomorrowStart =
                today.plusDays(1)
                        .atStartOfDay();

        LocalDateTime weekStartAt =
                weekStart.atStartOfDay();

        LocalDateTime weekEndExclusive =
                weekEnd.plusDays(1)
                        .atStartOfDay();

        List<Reservation> todayEntities =
                reservationRepository
                        .findByStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotOrderByStartAtAsc(
                                todayStart,
                                tomorrowStart,
                                ReservationStatus.CANCELED
                        );

        List<Reservation> weekEntities =
                reservationRepository
                        .findByStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotOrderByStartAtAsc(
                                weekStartAt,
                                weekEndExclusive,
                                ReservationStatus.CANCELED
                        );

        long todayRemainingCount =
                todayEntities.stream()
                        .filter(r ->
                                isActive(r.getStatus())
                                        && !r.getStartAt()
                                        .isBefore(now)
                        )
                        .count();

        return DashboardReservationSummary.builder()
                .todayCount(
                        todayEntities.size()
                )
                .todayRemainingCount(
                        todayRemainingCount
                )
                .weekCount(
                        weekEntities.size()
                )
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .todayReservations(
                        todayEntities.stream()
                                .map(
                                        DashboardReservationItem::from
                                )
                                .toList()
                )
                .weekReservations(
                        weekEntities.stream()
                                .map(
                                        DashboardReservationItem::from
                                )
                                .toList()
                )
                .build();
    }

    private boolean isActive(
            ReservationStatus status
    ) {
        return status
                == ReservationStatus.REQUESTED
                || status
                == ReservationStatus.CONFIRMED;
    }
}
