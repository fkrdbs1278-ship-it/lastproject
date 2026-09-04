package com.young04.lastproject.reservation;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.service.AvailabilityNoticeService;
import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityNoticeServiceTest {

    @Mock
    BusinessHourRepository businessHourRepository;

    @Mock
    SalonHolidayRepository salonHolidayRepository;

    @Mock
    BusinessHour businessHour;

    @Mock
    SalonHoliday personal;

    @Mock
    SalonHoliday holiday;

    AvailabilityNoticeService service;

    @BeforeEach
    void setUp() {
        service = new AvailabilityNoticeService(
                businessHourRepository,
                salonHolidayRepository
        );
    }

    @Test
    void PERSONAL_관리자제목은_고객에게_노출하지않는다() {
        LocalDate date =
                LocalDate.of(2026, 9, 10);

        when(businessHourRepository.findByDayOfWeek(4))
                .thenReturn(Optional.of(businessHour));
        when(businessHour.isOpenDay()).thenReturn(true);
        when(businessHour.getOpenLocalTime())
                .thenReturn(LocalTime.of(10, 0));
        when(businessHour.getCloseLocalTime())
                .thenReturn(LocalTime.of(20, 0));

        when(personal.getHolidayType())
                .thenReturn(HolidayType.PERSONAL);
        when(personal.isAllDay()).thenReturn(false);
        when(personal.getStartAt())
                .thenReturn(LocalDateTime.of(2026, 9, 10, 14, 0));
        when(personal.getEndAt())
                .thenReturn(LocalDateTime.of(2026, 9, 10, 16, 0));

        when(salonHolidayRepository.findOverlappingHolidays(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(personal));

        var response = service.getNotice(date);
        var item = response.getNotices().getFirst();

        assertThat(item.getNoticeType()).isEqualTo("PERSONAL");
        assertThat(item.getTitle()).isEqualTo("내부 일정");
        assertThat(item.getMessage()).doesNotContain("병원");
        assertThat(item.getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(item.getEndTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    void 일반휴일은_기존_TITLE을_고객안내에_사용한다() {
        LocalDate date =
                LocalDate.of(2026, 9, 24);

        when(businessHourRepository.findByDayOfWeek(4))
                .thenReturn(Optional.of(businessHour));
        when(businessHour.isOpenDay()).thenReturn(true);
        when(businessHour.getOpenLocalTime())
                .thenReturn(LocalTime.of(10, 0));
        when(businessHour.getCloseLocalTime())
                .thenReturn(LocalTime.of(20, 0));

        when(holiday.getHolidayType())
                .thenReturn(HolidayType.TEMPORARY);
        when(holiday.getTitle())
                .thenReturn("추석 연휴");
        when(holiday.isAllDay()).thenReturn(true);

        when(salonHolidayRepository.findOverlappingHolidays(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(holiday));

        var response = service.getNotice(date);
        var item = response.getNotices().getFirst();

        assertThat(item.getNoticeType()).isEqualTo("HOLIDAY");
        assertThat(item.getTitle()).isEqualTo("추석 연휴");
        assertThat(item.getMessage()).contains("추석 연휴");
        assertThat(item.isAllDay()).isTrue();
    }

    @Test
    void 정기휴무일은_정기휴무_문구를_반환한다() {
        LocalDate date =
                LocalDate.of(2026, 9, 13);

        when(businessHourRepository.findByDayOfWeek(7))
                .thenReturn(Optional.empty());

        when(salonHolidayRepository.findOverlappingHolidays(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        )).thenReturn(List.of());

        var response = service.getNotice(date);

        assertThat(response.isOpenDay()).isFalse();
        assertThat(response.getDayMessage())
                .isEqualTo("정기 휴무일입니다.");
    }
}
