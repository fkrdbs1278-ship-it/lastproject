package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationDetailResponse {

    private ReservationResponse reservation;

    private String hairStyleTitle;
    private String hairStyleImageUrl;

    private List<ReservationImageResponse> images;
}
