package com.young04.lastproject.reservation.repository;

import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationRepositoryCustom {

    Page<Reservation> search(
            ReservationSearchCondition condition,
            Pageable pageable
    );
}
