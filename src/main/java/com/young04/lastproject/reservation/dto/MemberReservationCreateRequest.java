package com.young04.lastproject.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberReservationCreateRequest {

    @NotNull(message = "시술 메뉴를 선택해주세요.")
    private Long serviceMenuNo;

    private Long hairStyleNo;

    @NotNull(message = "예약 시간을 선택해주세요.")
    @Future(message = "예약 시간은 현재 이후여야 합니다.")
    private LocalDateTime startAt;

    @Size(max = 500, message = "요청사항은 500자 이하로 입력해주세요.")
    private String requestMemo;
}
