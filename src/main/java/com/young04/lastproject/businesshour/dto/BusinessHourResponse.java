package com.young04.lastproject.businesshour.dto;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessHourResponse {

    private Long businessHourNo;
    private Integer dayOfWeek;
    private boolean open;
    private String openTime;
    private String closeTime;

    public static BusinessHourResponse from(BusinessHour hour) {
        return BusinessHourResponse.builder()
                .businessHourNo(hour.getBusinessHourNo())
                .dayOfWeek(hour.getDayOfWeek())
                .open(hour.isOpenDay())
                .openTime(hour.getOpenTime())
                .closeTime(hour.getCloseTime())
                .build();
    }
}
