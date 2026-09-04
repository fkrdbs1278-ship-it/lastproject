package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class AvailabilityNoticeItemResponse {

    /*
     * REGULAR_CLOSED / HOLIDAY / PERSONAL
     */
    private String noticeType;

    /*
     * 고객에게 보여줄 짧은 제목.
     * PERSONAL은 관리자 TITLE을 노출하지 않고 "내부 일정"로 치환한다.
     */
    private String title;

    private String message;

    private boolean allDay;

    /*
     * 하루 종일 일정이면 null일 수 있다.
     */
    private LocalTime startTime;
    private LocalTime endTime;
}
