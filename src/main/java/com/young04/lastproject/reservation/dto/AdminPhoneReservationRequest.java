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
public class AdminPhoneReservationRequest {

    @NotBlank(message = "예약자 이름을 입력해주세요.")
    @Size(min = 2, max = 50, message = "예약자 이름은 2~50자로 입력해주세요.")
    @Pattern(
            regexp = "^[\\p{L}][\\p{L}\\p{M} .'-]{0,48}[\\p{L}\\p{M}]$",
            message = "예약자 이름 형식이 올바르지 않습니다."
    )
    private String guestName;

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
