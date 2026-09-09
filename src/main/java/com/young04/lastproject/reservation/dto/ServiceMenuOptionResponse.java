package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceMenuOptionResponse {

    private Long serviceMenuNo;
    private String category;
    private String name;
    private Integer price;
    private Integer durationMin;
}
