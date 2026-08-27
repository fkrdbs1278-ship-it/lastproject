package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.AvailableTimeResponse;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
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

    private final BusinessHourRepository businessHourRepository;
    private final SalonHolidayRepository salonHolidayRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceMenuReader serviceMenuReader;

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long serviceMenuNo) {
        ServiceMenuReader.ServiceMenuSnapshot menu = serviceMenuReader.getActiveServiceMenu(serviceMenuNo);
        BusinessHour bh = businessHourRepository.findByDayOfWeek(toBusinessDayOfWeek(date.getDayOfWeek())).orElse(null);

        if (bh == null || !bh.isOpenDay() || bh.getOpenLocalTime() == null || bh.getCloseLocalTime() == null) {
            return List.of();
        }

        List<AvailableTimeResponse> result = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.of(date, bh.getOpenLocalTime());
        LocalDateTime close = LocalDateTime.of(date, bh.getCloseLocalTime());

        while (!cursor.plusMinutes(menu.durationMin()).isAfter(close)) {
            LocalDateTime end = cursor.plusMinutes(menu.durationMin());
            if (isAvailable(cursor, end)) {
                result.add(new AvailableTimeResponse(cursor.toLocalTime(), end.toLocalTime()));
            }
            cursor = cursor.plusMinutes(SLOT_UNIT_MINUTES);
        }

        return result;
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        if (!isInsideBusinessHours(start, end)) return false;

        if (salonHolidayRepository.countOverlappingHolidays(start, end) > 0) {
            return false;
        }

        long count = reservationRepository.countOverlappingReservations(
                start,
                end,
                List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)
        );

        return count == 0;
    }

    public boolean isAvailableForUpdate(Long reservationNo, LocalDateTime start, LocalDateTime end) {
        if (!isInsideBusinessHours(start, end)) return false;

        if (salonHolidayRepository.countOverlappingHolidays(start, end) > 0) {
            return false;
        }

        long count = reservationRepository.countOverlappingReservationsExcludingSelf(
                reservationNo,
                start,
                end,
                List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)
        );

        return count == 0;
    }

    private boolean isInsideBusinessHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) return false;
        if (!start.toLocalDate().equals(end.toLocalDate())) return false;

        BusinessHour bh = businessHourRepository
                .findByDayOfWeek(toBusinessDayOfWeek(start.getDayOfWeek()))
                .orElse(null);

        if (bh == null || !bh.isOpenDay()) return false;

        LocalTime open = bh.getOpenLocalTime();
        LocalTime close = bh.getCloseLocalTime();

        if (open == null || close == null) return false;

        LocalDateTime businessStart = LocalDateTime.of(start.toLocalDate(), open);
        LocalDateTime businessEnd = LocalDateTime.of(start.toLocalDate(), close);

        return !start.isBefore(businessStart) && !end.isAfter(businessEnd);
    }

    private int toBusinessDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue();
    }
}
