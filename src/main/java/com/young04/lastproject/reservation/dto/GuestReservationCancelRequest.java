package com.young04.lastproject.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestReservationCancelRequest {

    @NotNull(message = "예약번호를 입력해주세요.")
    private Long reservationNo;

    @NotBlank(message = "휴대전화 번호를 입력해주세요.")

    @Pattern(
            regexp = "^01[016789]-?\\d{4}-?\\d{4}$",
            message = "휴대전화 번호 형식이 올바르지 않습니다."
    )
    private String guestPhone;

    @NotBlank(message = "취소 사유를 입력해주세요.")
    @Size(max = 500, message = "취소 사유는 500자 이하로 입력해주세요.")
    private String reason;
}
