package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.HairStyleOptionResponse;
import com.young04.lastproject.reservation.dto.ReservationDetailResponse;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.repository.ReservationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationDetailService {

    private final HairStyleReader hairStyleReader;
    private final ReservationImageRepository reservationImageRepository;

    /*
     * 로그인 회원 본인 상세 응답.
     * 고객 참고 이미지는 보호된 /api/reservations/me/** URL을 내려준다.
     */
    public ReservationDetailResponse toMemberDetail(
            Reservation reservation
    ) {
        HairStyleOptionResponse style =
                hairStyleReader
                        .findById(
                                reservation.getHairStyleNo()
                        )
                        .orElse(null);

        return ReservationDetailResponse.builder()
                .reservation(
                        ReservationResponse.from(reservation)
                )
                .hairStyleTitle(
                        style == null
                                ? null
                                : style.getTitle()
                )
                .hairStyleImageUrl(
                        style == null
                                ? null
                                : style.getImageUrl()
                )
                .images(
                        reservationImageRepository
                                .findByReservationReservationNoOrderBySortOrderAsc(
                                        reservation.getReservationNo()
                                )
                                .stream()
                                .map(
                                        ReservationImageResponse::forMember
                                )
                                .toList()
                )
                .build();
    }

    /*
     * 기존 호출부 호환용.
     */
    public ReservationDetailResponse toDetail(
            Reservation reservation
    ) {
        return toMemberDetail(reservation);
    }
}
