package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.ReservationSource;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationCreateRequest {

    private Long memberNo;

    @Size(
            min = 2,
            max = 50,
            message = "예약자 이름은 2~50자로 입력해주세요."
    )
    @Pattern(
            regexp = "^[\\p{L}][\\p{L}\\p{M} .'-]{0,48}[\\p{L}\\p{M}]$",
            message = "예약자 이름은 한글/영문 등 문자와 공백, ., ', -만 사용할 수 있습니다."
    )
    private String guestName;

    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "휴대전화 번호 형식이 올바르지 않습니다."
    )
    private String guestPhone;

    @NotNull(message = "시술 메뉴를 선택해주세요.")
    private Long serviceMenuNo;

    // 미용실에서 제공하는 예시 헤어스타일. 선택사항.
    private Long hairStyleNo;

    @NotNull(message = "예약 시간을 선택해주세요.")
    @Future(message = "예약 시간은 현재 이후여야 합니다.")
    private LocalDateTime startAt;

    @Size(
            max = 500,
            message = "요청사항은 500자 이하로 입력해주세요."
    )
    private String requestMemo;

    private ReservationSource reservationSource;
}
