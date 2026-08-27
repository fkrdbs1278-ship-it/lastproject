package com.young04.lastproject.noshow.service;

import com.young04.lastproject.noshow.entity.NoShow;
import com.young04.lastproject.noshow.repository.NoShowRepository;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.exception.InvalidReservationStatusException;
import com.young04.lastproject.reservation.exception.ReservationNotFoundException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoShowService {

    private final ReservationRepository reservationRepository;
    private final NoShowRepository noShowRepository;

    @Transactional
    public NoShow markNoShow(
            Long reservationNo,
            String reason,
            String adminMemo
    ) {
        Reservation reservation = reservationRepository.findById(reservationNo)
                .orElseThrow(() ->
                        new ReservationNotFoundException(reservationNo)
                );

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException(
                    "CONFIRMED 상태의 예약만 노쇼 처리할 수 있습니다."
            );
        }

        if (noShowRepository.existsByReservationReservationNo(reservationNo)) {
            throw new InvalidReservationStatusException(
                    "이미 노쇼 처리된 예약입니다."
            );
        }

        reservation.markNoShow();

        return noShowRepository.save(
                NoShow.create(reservation, reason, adminMemo)
        );
    }
}
