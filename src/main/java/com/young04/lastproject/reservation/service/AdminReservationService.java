package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.exception.ReservationNotFoundException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.repository.ReservationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationImageRepository reservationImageRepository;
    private final ReservationMemberReader reservationMemberReader;
    private final HairStyleReader hairStyleReader;

    public AdminReservationSearchResponse search(
            ReservationSearchCondition condition,
            int page,
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        Math.clamp(size, 1, 100)
                );

        Page<Reservation> reservationPage =
                reservationRepository.search(condition, pageable);

        return AdminReservationSearchResponse.builder()
                .content(
                        reservationPage.getContent()
                                .stream()
                                .map(ReservationResponse::from)
                                .toList()
                )
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .build();
    }

    public AdminReservationDetailResponse detail(Long reservationNo) {
        Reservation reservation =
                reservationRepository.findById(reservationNo)
                        .orElseThrow(
                                () -> new ReservationNotFoundException(reservationNo)
                        );

        MemberReservationInfo member =
                reservationMemberReader
                        .findMemberInfoByMemberNo(reservation.getMemberNo())
                        .orElse(null);

        HairStyleOptionResponse style =
                hairStyleReader
                        .findById(reservation.getHairStyleNo())
                        .orElse(null);

        return AdminReservationDetailResponse.builder()
                .reservation(ReservationResponse.from(reservation))
                .memberName(member == null ? null : member.getName())
                .memberPhone(member == null ? null : member.getPhone())
                .hairStyleTitle(style == null ? null : style.getTitle())
                .hairStyleImageUrl(style == null ? null : style.getImageUrl())
                .images(
                        reservationImageRepository
                                .findByReservationReservationNoOrderBySortOrderAsc(
                                        reservationNo
                                )
                                .stream()
                                .map(ReservationImageResponse::from)
                                .toList()
                )
                .build();
    }
}
