package com.young04.lastproject.salonevent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// 이벤트 등록과 수정 화면의 입력값을 전달하는 DTO
@Getter
@Setter
public class SalonEventRequest {

    // 이벤트 제목
    @NotBlank(message = "이벤트 제목을 입력해 주세요.")
    @Size(max = 200, message = "이벤트 제목은 200자 이하로 입력해 주세요.")
    private String eventTitle;

    // 이벤트 상세 내용
    @Size(max = 2000, message = "이벤트 내용은 2,000자 이하로 입력해 주세요.")
    private String eventContent;

    // 이벤트 유형
    @NotBlank(message = "이벤트 유형을 선택해 주세요.")
    @Pattern(
            regexp = "FIRST_VISIT|SEASON|PARTNERSHIP|PREPAID|GENERAL",
            message = "올바른 이벤트 유형을 선택해 주세요."
    )
    private String eventType;

    // 이벤트 이미지 주소
    @Size(max = 500, message = "이미지 주소는 500자 이하로 입력해 주세요.")
    private String eventImageUrl;

    // 이벤트 시작일
    @NotNull(message = "이벤트 시작일을 입력해 주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    // 이벤트 종료일
    @NotNull(message = "이벤트 종료일을 입력해 주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    // 이벤트 사용 여부
    @NotBlank(message = "이벤트 사용 여부를 선택해 주세요.")
    @Pattern(
            regexp = "Y|N",
            message = "사용 여부는 Y 또는 N이어야 합니다."
    )
    private String useYn = "Y";
}