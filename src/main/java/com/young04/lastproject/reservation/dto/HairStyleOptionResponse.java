package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HairStyleOptionResponse {

    private Long hairStyleNo;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
}
