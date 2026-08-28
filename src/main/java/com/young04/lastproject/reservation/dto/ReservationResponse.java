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

    public static ReservationResponse from(Reservation r) {
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
                .build();
    }
}
