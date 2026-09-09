package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardCalendarBlock {

    public enum BlockType {
        RESERVATION,
        PERSONAL,
        HOLIDAY
    }

    private BlockType type;

    private Long reservationNo;
    private int dayIndex;

    private int startMinute;
    private int durationMinute;

    private String title;
    private String subtitle;
    private String timeLabel;

    private ReservationStatus reservationStatus;
}
