package com.young04.lastproject.salonholiday.dto;

import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SalonHolidayResponse {

    private Long salonHolidayNo;
    private HolidayType holidayType;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private String memo;

    public static SalonHolidayResponse from(SalonHoliday holiday) {
        return SalonHolidayResponse.builder()
                .salonHolidayNo(holiday.getSalonHolidayNo())
                .holidayType(holiday.getHolidayType())
                .title(holiday.getTitle())
                .startAt(holiday.getStartAt())
                .endAt(holiday.getEndAt())
                .allDay(holiday.isAllDay())
                .memo(holiday.getMemo())
                .build();
    }
}
