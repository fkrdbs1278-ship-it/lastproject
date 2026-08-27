package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.ReservationSource;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationCreateRequest {
    private Long memberNo;
    private String guestName;
    private String guestPhone;

    @NotNull
    private Long serviceMenuNo;

    @NotNull
    @Future
    private LocalDateTime startAt;

    @Size(max = 1000)
    private String requestMemo;

    private ReservationSource reservationSource;
}
