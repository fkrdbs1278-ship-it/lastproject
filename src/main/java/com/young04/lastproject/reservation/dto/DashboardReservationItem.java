package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DashboardReservationItem {

    private Long reservationNo;
    private String customerLabel;
    private String serviceName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;

    public static DashboardReservationItem from(
            Reservation reservation
    ) {
        String customerLabel;

        if (reservation.getCustomerType()
                == CustomerType.GUEST) {
            customerLabel =
                    reservation.getGuestName() == null
                            || reservation.getGuestName().isBlank()
                            ? "비회원"
                            : reservation.getGuestName();
        } else {
            customerLabel = "회원 예약";
        }

        return DashboardReservationItem.builder()
                .reservationNo(
                        reservation.getReservationNo()
                )
                .customerLabel(customerLabel)
                .serviceName(
                        reservation.getServiceNameSnapshot()
                )
                .startAt(
                        reservation.getStartAt()
                )
                .endAt(
                        reservation.getEndAt()
                )
                .status(
                        reservation.getStatus()
                )
                .build();
    }
}
