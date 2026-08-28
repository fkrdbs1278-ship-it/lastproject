package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.AdminReservationSearchResponse;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;

    public AdminReservationSearchResponse search(
            ReservationSearchCondition condition,
            int page,
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(
                                Math.max(size, 1),
                                100
                        )
                );

        Page<?> result =
                reservationRepository.search(
                        condition,
                        pageable
                );

        @SuppressWarnings("unchecked")
        Page<com.young04.lastproject.reservation.entity.Reservation>
                reservationPage =
                (Page<com.young04.lastproject.reservation.entity.Reservation>)
                        result;

        return AdminReservationSearchResponse
                .builder()
                .content(
                        reservationPage
                                .getContent()
                                .stream()
                                .map(ReservationResponse::from)
                                .toList()
                )
                .page(
                        reservationPage.getNumber()
                )
                .size(
                        reservationPage.getSize()
                )
                .totalElements(
                        reservationPage.getTotalElements()
                )
                .totalPages(
                        reservationPage.getTotalPages()
                )
                .build();
    }
}
