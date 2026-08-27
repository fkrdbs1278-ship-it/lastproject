package com.young04.lastproject.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationUpdateRequest {
    @NotNull
    private Long serviceMenuNo;

    @NotNull
    @Future
    private LocalDateTime startAt;

    @Size(max = 1000)
    private String requestMemo;
}
