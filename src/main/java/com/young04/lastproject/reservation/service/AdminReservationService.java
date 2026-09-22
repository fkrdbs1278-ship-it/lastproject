package com.young04.lastproject.reservation.service;

import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentStatus;
import com.young04.lastproject.payment.repository.PaymentRepository;
import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.exception.ReservationNotFoundException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.repository.ReservationImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationImageRepository reservationImageRepository;
    private final ReservationMemberReader reservationMemberReader;
    private final HairStyleReader hairStyleReader;
    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;

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

        List<Reservation> reservations =
                reservationPage.getContent();

        Map<Long, PaymentStatus> paymentStatusByReservationNo =
                getPaymentStatusByReservationNo(reservations);

        return AdminReservationSearchResponse.builder()
                .content(
                        reservations
                                .stream()
                                .map(reservation ->
                                        toListItemResponse(
                                                reservation,
                                                paymentStatusByReservationNo.get(
                                                        reservation.getReservationNo()
                                                )
                                        )
                                )
                                .toList()
                )
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages())
                .build();
    }

    private AdminReservationListItemResponse toListItemResponse(
            Reservation reservation,
            PaymentStatus paymentStatus
    ) {

        MemberReservationInfo member =
                null;


        if (
                reservation.getCustomerType()
                        == CustomerType.MEMBER
        ) {

            member =
                    reservationMemberReader
                            .findMemberInfoByMemberNo(
                                    reservation.getMemberNo()
                            )
                            .orElse(null);
        }


        return AdminReservationListItemResponse.from(
                reservation,
                member,
                paymentStatus
        );
    }


    private Map<Long, PaymentStatus> getPaymentStatusByReservationNo(
            List<Reservation> reservations
    ) {

        List<Long> completedReservationNos =
                reservations.stream()
                        .filter(reservation ->
                                reservation.getStatus()
                                        == ReservationStatus.COMPLETED
                        )
                        .map(Reservation::getReservationNo)
                        .toList();

        if (completedReservationNos.isEmpty()) {
            return Map.of();
        }

        return paymentRepository
                .findByReservation_ReservationNoIn(
                        completedReservationNos
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                payment ->
                                        payment.getReservation()
                                                .getReservationNo(),
                                Payment::getPaymentStatus,
                                (first, ignored) -> first
                        )
                );
    }


    public AdminReservationDetailResponse detail(
            Long reservationNo
    ) {
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
                                .map(ReservationImageResponse::forAdmin)
                                .toList()
                )
                .build();
    }

    /*
     * 관리자가 직접 입력하는 전화 예약.
     * 전화로 이미 일정을 합의하고 관리자가 등록하는 흐름이므로
     * 생성 직후 CONFIRMED 상태로 확정합니다.
     */
    @Transactional
    public ReservationResponse createPhoneReservation(
            AdminPhoneReservationRequest request
    ) {
        ReservationCreateRequest create =
                new ReservationCreateRequest();

        create.setGuestName(request.getGuestName());
        create.setGuestPhone(request.getGuestPhone());
        create.setServiceMenuNo(request.getServiceMenuNo());
        create.setHairStyleNo(request.getHairStyleNo());
        create.setStartAt(request.getStartAt());
        create.setRequestMemo(request.getRequestMemo());
        create.setReservationSource(ReservationSource.PHONE);

        ReservationResponse created =
                reservationService.createReservation(create);

        return reservationService.confirmReservation(
                created.getReservationNo()
        );
    }
}
