package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DashboardCalendarDay {

    private LocalDate date;
    private String dayLabel;
    private long reservationCount;

    private boolean today;
    private boolean saturday;
    private boolean sunday;
    private boolean closed;

    private String closedLabel;
}
