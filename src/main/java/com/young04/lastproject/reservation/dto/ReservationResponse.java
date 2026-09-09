package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    private Long reservationNo;
    private Long memberNo;
    private Long serviceMenuNo;
    private Long hairStyleNo;
    private CustomerType customerType;
    private String guestName;
    private String guestPhone;
    private ReservationSource reservationSource;
    private String serviceName;
    private Integer durationMinutes;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String requestMemo;
    private ReservationStatus status;

    private String cancelReason;
    private CanceledBy canceledBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     * 화면에서 상태별 버튼 노출을 결정할 수 있도록 서버 정책도 함께 전달한다.
     * REQUESTED / CONFIRMED만 변경·취소 가능.
     */
    private boolean modifiable;
    private boolean cancelable;

    public static ReservationResponse from(Reservation r) {
        boolean active =
                r.getStatus() == ReservationStatus.REQUESTED
                        || r.getStatus() == ReservationStatus.CONFIRMED;

        return ReservationResponse.builder()
                .reservationNo(r.getReservationNo())
                .memberNo(r.getMemberNo())
                .serviceMenuNo(r.getServiceMenuNo())
                .hairStyleNo(r.getHairStyleNo())
                .customerType(r.getCustomerType())
                .guestName(r.getGuestName())
                .guestPhone(r.getGuestPhone())
                .reservationSource(r.getReservationSource())
                .serviceName(r.getServiceNameSnapshot())
                .durationMinutes(r.getDurationMinutesSnapshot())
                .startAt(r.getStartAt())
                .endAt(r.getEndAt())
                .requestMemo(r.getRequestMemo())
                .status(r.getStatus())
                .cancelReason(r.getCancelReason())
                .canceledBy(r.getCanceledBy())
                .confirmedAt(r.getConfirmedAt())
                .completedAt(r.getCompletedAt())
                .canceledAt(r.getCanceledAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .modifiable(active)
                .cancelable(active)
                .build();
    }
}
