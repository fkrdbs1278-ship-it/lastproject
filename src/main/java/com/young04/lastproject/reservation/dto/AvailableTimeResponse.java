package com.young04.lastproject.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class AvailableTimeResponse {
    private LocalTime startTime;
    private LocalTime endTime;
}
