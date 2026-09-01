package com.young04.lastproject.salonholiday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OwnerAvailabilityBlockRequest {

    @NotBlank(message = "일정 제목을 입력해주세요.")
    @Size(max = 100, message = "일정 제목은 100자 이하로 입력해주세요.")
    private String title;

    @NotNull(message = "시작시간을 입력해주세요.")
    private LocalDateTime startAt;

    @NotNull(message = "종료시간을 입력해주세요.")
    private LocalDateTime endAt;

    private boolean allDay;

    @Size(max = 500, message = "메모는 500자 이하로 입력해주세요.")
    private String memo;
}
