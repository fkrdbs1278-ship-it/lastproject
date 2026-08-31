package com.young04.lastproject.businesshour.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BusinessHourUpdateRequest {

    private boolean open;
    private LocalTime openTime;
    private LocalTime closeTime;
}
