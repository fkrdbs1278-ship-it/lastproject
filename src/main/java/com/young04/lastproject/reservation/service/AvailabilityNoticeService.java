package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.AvailabilityNoticeItemResponse;
import com.young04.lastproject.reservation.dto.AvailabilityNoticeResponse;
import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityNoticeService {

    private final BusinessHourRepository businessHourRepository;
    private final SalonHolidayRepository salonHolidayRepository;

    public AvailabilityNoticeResponse getNotice(LocalDate date) {
        LocalDateTime dayStart =
                date.atStartOfDay();

        LocalDateTime dayEnd =
                date.plusDays(1)
                        .atStartOfDay();

        BusinessHour businessHour =
                businessHourRepository
                        .findByDayOfWeek(
                                date.getDayOfWeek().getValue()
                        )
                        .orElse(null);

        boolean openDay =
                businessHour != null
                        && businessHour.isOpenDay()
                        && businessHour.getOpenLocalTime() != null
                        && businessHour.getCloseLocalTime() != null;

        String dayMessage =
                openDay
                        ? null
                        : "정기 휴무일입니다.";

        List<AvailabilityNoticeItemResponse> notices =
                salonHolidayRepository
                        .findOverlappingHolidays(
                                dayStart,
                                dayEnd
                        )
                        .stream()
                        .map(holiday ->
                                toCustomerNotice(
                                        holiday,
                                        date
                                )
                        )
                        .toList();

        return AvailabilityNoticeResponse.builder()
                .date(date)
                .openDay(openDay)
                .dayMessage(dayMessage)
                .notices(notices)
                .build();
    }

    private AvailabilityNoticeItemResponse toCustomerNotice(
            SalonHoliday holiday,
            LocalDate requestedDate
    ) {
        boolean personal =
                holiday.getHolidayType()
                        == HolidayType.PERSONAL;

        String title =
                personal
                        ? "내부 일정"
                        : safeTitle(
                                holiday.getTitle()
                        );

        boolean allDay =
                holiday.isAllDay()
                        || coversWholeDate(
                                holiday,
                                requestedDate
                        );

        String message;

        if (personal) {
            message =
                    allDay
                            ? "내부 일정으로 해당 날짜 예약이 어렵습니다."
                            : "내부 일정으로 일부 시간 예약이 제한됩니다.";
        } else {
            message =
                    allDay
                            ? title + "로 휴무입니다."
                            : title + " 일정으로 일부 시간 예약이 제한됩니다.";
        }

        return AvailabilityNoticeItemResponse.builder()
                .noticeType(
                        personal
                                ? "PERSONAL"
                                : "HOLIDAY"
                )
                .title(title)
                .message(message)
                .allDay(allDay)
                .startTime(
                        allDay
                                ? null
                                : visibleStartTime(
                                        holiday,
                                        requestedDate
                                )
                )
                .endTime(
                        allDay
                                ? null
                                : visibleEndTime(
                                        holiday,
                                        requestedDate
                                )
                )
                .build();
    }

    private boolean coversWholeDate(
            SalonHoliday holiday,
            LocalDate date
    ) {
        LocalDateTime dayStart =
                date.atStartOfDay();

        LocalDateTime dayEnd =
                date.plusDays(1)
                        .atStartOfDay();

        return !holiday.getStartAt()
                .isAfter(dayStart)
                && !holiday.getEndAt()
                .isBefore(dayEnd);
    }

    private LocalTime visibleStartTime(
            SalonHoliday holiday,
            LocalDate date
    ) {
        if (holiday.getStartAt()
                .toLocalDate()
                .isBefore(date)) {
            return LocalTime.MIN;
        }

        return holiday.getStartAt()
                .toLocalTime();
    }

    private LocalTime visibleEndTime(
            SalonHoliday holiday,
            LocalDate date
    ) {
        if (holiday.getEndAt()
                .toLocalDate()
                .isAfter(date)) {
            return LocalTime.MAX;
        }

        return holiday.getEndAt()
                .toLocalTime();
    }

    private String safeTitle(String value) {
        if (value == null
                || value.isBlank()) {
            return "매장 일정";
        }

        return value.trim();
    }
}
