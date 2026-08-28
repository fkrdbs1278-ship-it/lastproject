package com.young04.lastproject.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminReservationSearchResponse {

    private List<ReservationResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
