package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.DashboardCalendarBlock;
import com.young04.lastproject.reservation.dto.DashboardCalendarDay;
import com.young04.lastproject.reservation.dto.DashboardReservationItem;
import com.young04.lastproject.reservation.dto.DashboardReservationSummary;
import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationDashboardService {

    private static final int DEFAULT_START_HOUR = 10;
    private static final int DEFAULT_END_HOUR = 20;

    private final ReservationRepository reservationRepository;
    private final BusinessHourRepository businessHourRepository;
    private final SalonHolidayRepository salonHolidayRepository;
    private final ReservationMemberReader reservationMemberReader;

    public DashboardReservationSummary getSummary() {

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDate weekStart =
                today.with(DayOfWeek.MONDAY);

        LocalDate weekEnd =
                weekStart.plusDays(6);

        LocalDateTime todayStart =
                today.atStartOfDay();

        LocalDateTime tomorrowStart =
                today.plusDays(1).atStartOfDay();

        LocalDateTime weekStartAt =
                weekStart.atStartOfDay();

        LocalDateTime weekEndExclusive =
                weekEnd.plusDays(1).atStartOfDay();

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

        List<BusinessHour> businessHours =
                businessHourRepository.findAll();

        List<SalonHoliday> holidays =
                salonHolidayRepository
                        .findOverlappingHolidays(
                                weekStartAt,
                                weekEndExclusive
                        );

        int calendarStartHour =
                resolveCalendarStartHour(businessHours);

        int calendarEndHour =
                resolveCalendarEndHour(businessHours);

        List<DashboardCalendarDay> calendarDays =
                buildCalendarDays(
                        weekStart,
                        today,
                        weekEntities,
                        businessHours,
                        holidays
                );

        List<DashboardCalendarBlock> calendarBlocks =
                buildCalendarBlocks(
                        weekStart,
                        weekEnd,
                        weekEntities,
                        holidays,
                        calendarStartHour,
                        calendarEndHour
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
                .todayCount(todayEntities.size())
                .todayRemainingCount(todayRemainingCount)
                .weekCount(weekEntities.size())
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .todayReservations(
                        todayEntities.stream()
                                .map(DashboardReservationItem::from)
                                .toList()
                )
                .weekReservations(
                        weekEntities.stream()
                                .map(DashboardReservationItem::from)
                                .toList()
                )
                .calendarStartHour(calendarStartHour)
                .calendarEndHour(calendarEndHour)
                .calendarDays(calendarDays)
                .calendarBlocks(calendarBlocks)
                .build();
    }

    private List<DashboardCalendarDay> buildCalendarDays(
            LocalDate weekStart,
            LocalDate today,
            List<Reservation> reservations,
            List<BusinessHour> businessHours,
            List<SalonHoliday> holidays
    ) {
        Map<Integer, BusinessHour> businessHourMap =
                new HashMap<>();

        for (BusinessHour hour : businessHours) {
            businessHourMap.put(
                    hour.getDayOfWeek(),
                    hour
            );
        }

        List<DashboardCalendarDay> days =
                new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date =
                    weekStart.plusDays(i);

            int dayOfWeek =
                    date.getDayOfWeek().getValue();

            BusinessHour businessHour =
                    businessHourMap.get(dayOfWeek);

            SalonHoliday allDayHoliday =
                    findAllDayHoliday(
                            date,
                            holidays
                    );

            boolean regularClosed =
                    businessHour == null
                            || !businessHour.isOpenDay();

            boolean closed =
                    regularClosed
                            || allDayHoliday != null;

            String closedLabel = null;

            if (allDayHoliday != null) {
                closedLabel =
                        allDayHoliday.getHolidayType()
                                == HolidayType.PERSONAL
                                ? "예약 불가"
                                : safeTitle(
                                        allDayHoliday.getTitle(),
                                        "휴무"
                                );
            } else if (regularClosed) {
                closedLabel = "정기 휴무";
            }

            long reservationCount =
                    reservations.stream()
                            .filter(r ->
                                    r.getStartAt()
                                            .toLocalDate()
                                            .equals(date)
                            )
                            .count();

            days.add(
                    DashboardCalendarDay.builder()
                            .date(date)
                            .dayLabel(
                                    dayLabel(
                                            date.getDayOfWeek()
                                    )
                            )
                            .reservationCount(
                                    reservationCount
                            )
                            .today(date.equals(today))
                            .saturday(
                                    date.getDayOfWeek()
                                            == DayOfWeek.SATURDAY
                            )
                            .sunday(
                                    date.getDayOfWeek()
                                            == DayOfWeek.SUNDAY
                            )
                            .closed(closed)
                            .closedLabel(closedLabel)
                            .build()
            );
        }

        return days;
    }

    private List<DashboardCalendarBlock> buildCalendarBlocks(
            LocalDate weekStart,
            LocalDate weekEnd,
            List<Reservation> reservations,
            List<SalonHoliday> holidays,
            int calendarStartHour,
            int calendarEndHour
    ) {
        List<DashboardCalendarBlock> blocks =
                new ArrayList<>();

        int calendarStartMinute =
                calendarStartHour * 60;

        int calendarEndMinute =
                calendarEndHour * 60;

        for (Reservation reservation : reservations) {

            int dayIndex =
                    (int) ChronoUnit.DAYS.between(
                            weekStart,
                            reservation.getStartAt()
                                    .toLocalDate()
                    );

            if (dayIndex < 0 || dayIndex > 6) {
                continue;
            }

            int startMinute =
                    minuteOfDay(
                            reservation.getStartAt()
                                    .toLocalTime()
                    );

            int endMinute =
                    minuteOfDay(
                            reservation.getEndAt()
                                    .toLocalTime()
                    );

            int visibleStart =
                    Math.max(
                            startMinute,
                            calendarStartMinute
                    );

            int visibleEnd =
                    Math.min(
                            endMinute,
                            calendarEndMinute
                    );

            if (visibleStart >= visibleEnd) {
                continue;
            }

            blocks.add(
                    DashboardCalendarBlock.builder()
                            .type(
                                    DashboardCalendarBlock.BlockType.RESERVATION
                            )
                            .reservationNo(
                                    reservation.getReservationNo()
                            )
                            .dayIndex(dayIndex)
                            .startMinute(
                                    visibleStart
                                            - calendarStartMinute
                            )
                            .durationMinute(
                                    visibleEnd
                                            - visibleStart
                            )
                            .title(
                                    customerLabel(reservation)
                            )
                            .subtitle(
                                    reservation.getServiceNameSnapshot()
                            )
                            .timeLabel(
                                    reservation
                                            .getStartAt()
                                            .toLocalTime()
                                            + " · "
                                            + reservation
                                            .getDurationMinutesSnapshot()
                                            + "분"
                            )
                            .reservationStatus(
                                    reservation.getStatus()
                            )
                            .build()
            );
        }

        for (SalonHoliday holiday : holidays) {

            if (holiday.isAllDay()) {
                continue;
            }

            LocalDateTime rangeStart =
                    holiday.getStartAt();

            LocalDateTime rangeEnd =
                    holiday.getEndAt();

            LocalDate cursor =
                    rangeStart.toLocalDate()
                            .isBefore(weekStart)
                            ? weekStart
                            : rangeStart.toLocalDate();

            LocalDate lastDate =
                    rangeEnd.toLocalDate()
                            .isAfter(weekEnd)
                            ? weekEnd
                            : rangeEnd.toLocalDate();

            while (!cursor.isAfter(lastDate)) {

                LocalDateTime dayStart =
                        cursor.atStartOfDay();

                LocalDateTime dayEnd =
                        cursor.plusDays(1)
                                .atStartOfDay();

                LocalDateTime segmentStart =
                        rangeStart.isAfter(dayStart)
                                ? rangeStart
                                : dayStart;

                LocalDateTime segmentEnd =
                        rangeEnd.isBefore(dayEnd)
                                ? rangeEnd
                                : dayEnd;

                int startMinute =
                        minuteOfDay(
                                segmentStart.toLocalTime()
                        );

                int endMinute =
                        segmentEnd.equals(dayEnd)
                                ? 24 * 60
                                : minuteOfDay(
                                        segmentEnd.toLocalTime()
                                );

                int visibleStart =
                        Math.max(
                                startMinute,
                                calendarStartMinute
                        );

                int visibleEnd =
                        Math.min(
                                endMinute,
                                calendarEndMinute
                        );

                if (visibleStart < visibleEnd) {

                    int dayIndex =
                            (int) ChronoUnit.DAYS
                                    .between(
                                            weekStart,
                                            cursor
                                    );

                    String title =
                            holiday.getHolidayType()
                                    == HolidayType.PERSONAL
                                    ? "예약 불가"
                                    : safeTitle(
                                            holiday.getTitle(),
                                            "휴무"
                                    );

                    blocks.add(
                            DashboardCalendarBlock.builder()
                                    .type(
                                            holiday.getHolidayType()
                                                    == HolidayType.PERSONAL
                                                    ? DashboardCalendarBlock.BlockType.PERSONAL
                                                    : DashboardCalendarBlock.BlockType.HOLIDAY
                                    )
                                    .dayIndex(dayIndex)
                                    .startMinute(
                                            visibleStart
                                                    - calendarStartMinute
                                    )
                                    .durationMinute(
                                            visibleEnd
                                                    - visibleStart
                                    )
                                    .title(title)
                                    .subtitle(
                                            holiday.getHolidayType()
                                                    == HolidayType.PERSONAL
                                                    ? "개인 일정"
                                                    : "휴무 일정"
                                    )
                                    .timeLabel(
                                            segmentStart
                                                    .toLocalTime()
                                                    + " ~ "
                                                    + segmentEnd
                                                    .toLocalTime()
                                    )
                                    .build()
                    );
                }

                cursor = cursor.plusDays(1);
            }
        }

        blocks.sort(
                Comparator
                        .comparingInt(
                                DashboardCalendarBlock::getDayIndex
                        )
                        .thenComparingInt(
                                DashboardCalendarBlock::getStartMinute
                        )
        );

        return blocks;
    }

    private SalonHoliday findAllDayHoliday(
            LocalDate date,
            List<SalonHoliday> holidays
    ) {
        LocalDateTime dayStart =
                date.atStartOfDay();

        LocalDateTime dayEnd =
                date.plusDays(1)
                        .atStartOfDay();

        return holidays.stream()
                .filter(SalonHoliday::isAllDay)
                .filter(h ->
                        h.getStartAt()
                                .isBefore(dayEnd)
                                && h.getEndAt()
                                .isAfter(dayStart)
                )
                .findFirst()
                .orElse(null);
    }

    private int resolveCalendarStartHour(
            List<BusinessHour> businessHours
    ) {
        return businessHours.stream()
                .filter(BusinessHour::isOpenDay)
                .map(BusinessHour::getOpenLocalTime)
                .filter(time -> time != null)
                .mapToInt(LocalTime::getHour)
                .min()
                .orElse(DEFAULT_START_HOUR);
    }

    private int resolveCalendarEndHour(
            List<BusinessHour> businessHours
    ) {
        int resolved =
                businessHours.stream()
                        .filter(BusinessHour::isOpenDay)
                        .map(
                                BusinessHour::getCloseLocalTime
                        )
                        .filter(time -> time != null)
                        .mapToInt(time ->
                                time.getMinute() == 0
                                        ? time.getHour()
                                        : time.getHour() + 1
                        )
                        .max()
                        .orElse(DEFAULT_END_HOUR);

        return Math.max(
                resolved,
                resolveCalendarStartHour(
                        businessHours
                ) + 1
        );
    }

    private String customerLabel(
            Reservation reservation
    ) {
        String rawName;

        if (reservation.getCustomerType()
                == CustomerType.GUEST) {

            rawName =
                    reservation.getGuestName();

        } else {

            rawName =
                    reservationMemberReader
                            .findMemberInfoByMemberNo(
                                    reservation.getMemberNo()
                            )
                            .map(
                                    MemberReservationInfo::getName
                            )
                            .orElse("회원");
        }

        return maskName(rawName);
    }

    private String maskName(String value) {
        if (value == null || value.isBlank()) {
            return "고객";
        }

        String trimmed = value.trim();

        if (trimmed.length() == 1) {
            return trimmed + "○";
        }

        return trimmed.substring(0, 1)
                + "○".repeat(
                        Math.min(
                                2,
                                trimmed.length() - 1
                        )
                );
    }

    private String safeTitle(
            String value,
            String fallback
    ) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private String dayLabel(
            DayOfWeek dayOfWeek
    ) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    private int minuteOfDay(LocalTime time) {
        return time.getHour() * 60
                + time.getMinute();
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
