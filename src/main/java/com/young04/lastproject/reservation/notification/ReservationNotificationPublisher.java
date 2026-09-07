package com.young04.lastproject.reservation.notification;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final ReservationMemberReader reservationMemberReader;

    public void publish(
            ReservationNotificationType type,
            Reservation reservation
    ) {
        if (reservation == null) {
            return;
        }

        String phone =
                resolvePhone(reservation);

        if (phone == null
                || phone.isBlank()) {
            return;
        }

        eventPublisher.publishEvent(
                ReservationNotificationEvent.from(
                        type,
                        reservation,
                        phone
                )
        );
    }

    /*
     * Phase 7.1 이전 호출부와 테스트의 호환성을 위해 남겨둡니다.
     * 실제 동작은 회원/비회원 공통 publish()로 위임합니다.
     */
    @Deprecated
    public void publishGuest(
            ReservationNotificationType type,
            Reservation reservation
    ) {
        publish(type, reservation);
    }

    private String resolvePhone(
            Reservation reservation
    ) {
        if (reservation.getCustomerType()
                == CustomerType.GUEST) {
            return reservation.getGuestPhone();
        }

        if (reservation.getCustomerType()
                == CustomerType.MEMBER
                && reservation.getMemberNo() != null) {
            return reservationMemberReader
                    .findMemberInfoByMemberNo(
                            reservation.getMemberNo()
                    )
                    .map(
                            MemberReservationInfo::getPhone
                    )
                    .orElse(null);
        }

        return null;
    }
}
