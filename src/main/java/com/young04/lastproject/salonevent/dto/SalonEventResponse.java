package com.young04.lastproject.salonevent.dto;

import com.young04.lastproject.salonevent.entity.SalonEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 이벤트 조회 결과를 화면으로 전달하는 DTO
@Getter
@Builder
public class SalonEventResponse {

    private Long eventNo;
    private String eventTitle;
    private String eventContent;
    private String eventType;
    private String eventImageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String useYn;
    private LocalDateTime regdate;
    private LocalDateTime updatedate;

    // 현재 날짜를 기준으로 계산한 이벤트 상태
    private String eventStatus;

    // Entity를 조회 응답 DTO로 변환
    public static SalonEventResponse from(SalonEvent event) {
        return SalonEventResponse.builder()
                .eventNo(event.getEventNo())
                .eventTitle(event.getEventTitle())
                .eventContent(event.getEventContent())
                .eventType(event.getEventType())
                .eventImageUrl(event.getEventImageUrl())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .useYn(event.getUseYn())
                .regdate(event.getRegdate())
                .updatedate(event.getUpdatedate())
                .eventStatus(calculateStatus(event))
                .build();
    }

    // 이벤트 노출 여부와 기간을 기준으로 상태 계산
    private static String calculateStatus(SalonEvent event) {
        LocalDateTime now = LocalDateTime.now();

        if ("N".equals(event.getUseYn())) {
            return "STOPPED";
        }

        if (now.isBefore(event.getStartDate())) {
            return "SCHEDULED";
        }

        if (now.isAfter(event.getEndDate())) {
            return "ENDED";
        }

        return "ONGOING";
    }

    // 이벤트 유형의 한글 명칭 반환
    public String getEventTypeName() {
        return switch (eventType) {
            case "FIRST_VISIT" -> "첫 방문";
            case "SEASON" -> "시즌";
            case "PARTNERSHIP" -> "제휴";
            case "PREPAID" -> "선불권";
            case "GENERAL" -> "일반";
            default -> eventType;
        };
    }

    // 이벤트 상태의 한글 명칭 반환
    public String getEventStatusName() {
        return switch (eventStatus) {
            case "SCHEDULED" -> "진행 예정";
            case "ONGOING" -> "진행 중";
            case "ENDED" -> "종료";
            case "STOPPED" -> "사용 중지";
            default -> eventStatus;
        };
    }
}