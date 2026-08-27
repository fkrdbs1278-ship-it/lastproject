package com.young04.lastproject.salonholiday.dto;

import com.young04.lastproject.salonholiday.entity.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SalonHolidayRequest {

    @NotNull
    private HolidayType holidayType;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private boolean allDay;

    @Size(max = 500)
    private String memo;
}
