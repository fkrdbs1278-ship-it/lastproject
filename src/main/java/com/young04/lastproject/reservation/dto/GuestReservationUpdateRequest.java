package com.young04.lastproject.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GuestReservationUpdateRequest {

    @NotNull(message = "예약번호를 입력해주세요.")
    private Long reservationNo;

    @NotBlank(message = "휴대전화 번호를 입력해주세요.")
    @Pattern(
            regexp = "^010-?\\d{4}-?\\d{4}$",
            message = "010으로 시작하는 휴대전화 번호 11자리를 입력해주세요."
    )
    private String guestPhone;

    @NotNull(message = "시술 메뉴를 선택해주세요.")
    private Long serviceMenuNo;

    private Long hairStyleNo;

    @NotNull(message = "예약 시간을 선택해주세요.")
    @Future(message = "예약 시간은 현재 이후여야 합니다.")
    private LocalDateTime startAt;

    @Size(max = 500, message = "요청사항은 500자 이하로 입력해주세요.")
    private String requestMemo;
}
