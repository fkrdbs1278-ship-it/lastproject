package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.AvailableTimeResponse;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailableTimeService {

    private static final int SLOT_UNIT_MINUTES = 30;
    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(
                    ReservationStatus.REQUESTED,
                    ReservationStatus.CONFIRMED
            );

    private final BusinessHourRepository businessHourRepository;
    private final SalonHolidayRepository salonHolidayRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceMenuReader serviceMenuReader;

    public List<AvailableTimeResponse> getAvailableTimes(
            LocalDate date,
            Long serviceMenuNo
    ) {
        var menu =
                serviceMenuReader.getActiveServiceMenu(serviceMenuNo);

        BusinessHour businessHour =
                businessHourRepository
                        .findByDayOfWeek(
                                toBusinessDayOfWeek(
                                        date.getDayOfWeek()
                                )
                        )
                        .orElse(null);

        if (!isValidOpenDay(businessHour)) {
            return List.of();
        }

        LocalDateTime businessStart =
                LocalDateTime.of(
                        date,
                        businessHour.getOpenLocalTime()
                );

        LocalDateTime businessEnd =
                LocalDateTime.of(
                        date,
                        businessHour.getCloseLocalTime()
                );

        // 하루치 휴일/예약을 각각 한 번만 조회합니다.
        List<SalonHoliday> holidays =
                salonHolidayRepository.findOverlappingHolidays(
                        businessStart,
                        businessEnd
                );

        List<Reservation> reservations =
                reservationRepository.findOverlappingReservations(
                        businessStart,
                        businessEnd,
                        ACTIVE_STATUSES
                );

        List<AvailableTimeResponse> result =
                new ArrayList<>();

        LocalDateTime cursor = businessStart;

        while (!cursor.plusMinutes(menu.durationMin())
                .isAfter(businessEnd)) {

            LocalDateTime slotEnd =
                    cursor.plusMinutes(menu.durationMin());

            if (!overlapsHoliday(
                    cursor,
                    slotEnd,
                    holidays
            ) && !overlapsReservation(
                    cursor,
                    slotEnd,
                    reservations
            )) {
                result.add(
                        new AvailableTimeResponse(
                                cursor.toLocalTime(),
                                slotEnd.toLocalTime()
                        )
                );
            }

            cursor =
                    cursor.plusMinutes(
                            SLOT_UNIT_MINUTES
                    );
        }

        return result;
    }

    public boolean isAvailable(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (!isInsideBusinessHours(start, end)) {
            return false;
        }

        if (salonHolidayRepository
                .countOverlappingHolidays(start, end) > 0) {
            return false;
        }

        return reservationRepository
                .countOverlappingReservations(
                        start,
                        end,
                        ACTIVE_STATUSES
                ) == 0;
    }

    public boolean isAvailableForUpdate(
            Long reservationNo,
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (!isInsideBusinessHours(start, end)) {
            return false;
        }

        if (salonHolidayRepository
                .countOverlappingHolidays(start, end) > 0) {
            return false;
        }

        return reservationRepository
                .countOverlappingReservationsExcludingSelf(
                        reservationNo,
                        start,
                        end,
                        ACTIVE_STATUSES
                ) == 0;
    }

    private boolean isInsideBusinessHours(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null
                || end == null
                || !start.isBefore(end)) {
            return false;
        }

        if (!start.toLocalDate()
                .equals(end.toLocalDate())) {
            return false;
        }

        BusinessHour businessHour =
                businessHourRepository
                        .findByDayOfWeek(
                                toBusinessDayOfWeek(
                                        start.getDayOfWeek()
                                )
                        )
                        .orElse(null);

        if (!isValidOpenDay(businessHour)) {
            return false;
        }

        LocalDateTime businessStart =
                LocalDateTime.of(
                        start.toLocalDate(),
                        businessHour.getOpenLocalTime()
                );

        LocalDateTime businessEnd =
                LocalDateTime.of(
                        start.toLocalDate(),
                        businessHour.getCloseLocalTime()
                );

        return !start.isBefore(businessStart)
                && !end.isAfter(businessEnd);
    }

    private boolean overlapsHoliday(
            LocalDateTime start,
            LocalDateTime end,
            List<SalonHoliday> holidays
    ) {
        return holidays.stream()
                .anyMatch(h ->
                        h.getStartAt().isBefore(end)
                                && h.getEndAt().isAfter(start)
                );
    }

    private boolean overlapsReservation(
            LocalDateTime start,
            LocalDateTime end,
            List<Reservation> reservations
    ) {
        return reservations.stream()
                .anyMatch(r ->
                        r.getStartAt().isBefore(end)
                                && r.getEndAt().isAfter(start)
                );
    }

    private boolean isValidOpenDay(
            BusinessHour businessHour
    ) {
        return businessHour != null
                && businessHour.isOpenDay()
                && businessHour.getOpenLocalTime() != null
                && businessHour.getCloseLocalTime() != null;
    }

    private int toBusinessDayOfWeek(
            DayOfWeek dayOfWeek
    ) {
        return dayOfWeek.getValue();
    }
}
