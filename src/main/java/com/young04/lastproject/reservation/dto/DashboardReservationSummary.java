package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardReservationSummary {

    private long todayCount;
    private long todayRemainingCount;
    private long weekCount;

    private LocalDate weekStart;
    private LocalDate weekEnd;

    private List<DashboardReservationItem> todayReservations;
    private List<DashboardReservationItem> weekReservations;

    private int calendarStartHour;
    private int calendarEndHour;

    private List<DashboardCalendarDay> calendarDays;
    private List<DashboardCalendarBlock> calendarBlocks;
}
