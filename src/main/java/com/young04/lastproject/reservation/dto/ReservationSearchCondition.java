package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationSearchCondition {

    private ReservationStatus status;
    private CustomerType customerType;
    private ReservationSource reservationSource;

    private Long memberNo;
    private Long serviceMenuNo;

    private String guestName;
    private String guestPhone;

    private LocalDateTime startFrom;
    private LocalDateTime startTo;
}
