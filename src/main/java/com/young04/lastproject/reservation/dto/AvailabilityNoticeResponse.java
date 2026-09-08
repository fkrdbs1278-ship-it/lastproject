package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class AvailabilityNoticeResponse {

    private LocalDate date;

    /*
     * 정기 영업일인지 여부.
     * false이면 dayMessage에 정기 휴무 안내가 들어간다.
     */
    private boolean openDay;

    private String dayMessage;

    /*
     * SALON_HOLIDAY에서 고객에게 안전하게 가공한 일정.
     */
    private List<AvailabilityNoticeItemResponse> notices;
}
