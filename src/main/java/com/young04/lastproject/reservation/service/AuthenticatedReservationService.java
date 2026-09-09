package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.exception.ReservationAccessDeniedException;
import com.young04.lastproject.reservation.exception.ReservationAuthenticationRequiredException;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticatedReservationService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationMemberReader reservationMemberReader;
    private final ReservationDetailService reservationDetailService;

    @Transactional
    public ReservationResponse createMyReservation(
            String memberId,
            MemberReservationCreateRequest request
    ) {
        MemberReservationInfo member =
                requireMember(memberId);

        ReservationCreateRequest create =
                new ReservationCreateRequest();

        create.setMemberNo(member.getMemberNo());
        create.setServiceMenuNo(request.getServiceMenuNo());
        create.setHairStyleNo(request.getHairStyleNo());
        create.setStartAt(request.getStartAt());
        create.setRequestMemo(request.getRequestMemo());
        create.setReservationSource(ReservationSource.ONLINE);

        return reservationService.createReservation(create);
    }

    public List<ReservationResponse> getMyReservations(
            String memberId
    ) {
        Long memberNo =
                requireMember(memberId).getMemberNo();

        return reservationService
                .getMemberReservations(memberNo);
    }

    public ReservationDetailResponse getMyReservationDetail(
            String memberId,
            Long reservationNo
    ) {
        return reservationDetailService.toMemberDetail(
                requireOwnedReservation(
                        memberId,
                        reservationNo
                )
        );
    }

    @Transactional
    public ReservationResponse updateMyReservation(
            String memberId,
            Long reservationNo,
            ReservationUpdateRequest request
    ) {
        requireOwnedReservation(
                memberId,
                reservationNo
        );

        return reservationService.updateReservation(
                reservationNo,
                request
        );
    }

    @Transactional
    public ReservationResponse cancelMyReservation(
            String memberId,
            Long reservationNo,
            String reason
    ) {
        requireOwnedReservation(
                memberId,
                reservationNo
        );

        return reservationService.cancelReservation(
                reservationNo,
                reason,
                CanceledBy.USER
        );
    }

    private Reservation requireOwnedReservation(
            String memberId,
            Long reservationNo
    ) {
        Long memberNo =
                requireMember(memberId).getMemberNo();

        return reservationRepository
                .findByReservationNoAndMemberNo(
                        reservationNo,
                        memberNo
                )
                .orElseThrow(
                        ReservationAccessDeniedException::new
                );
    }

    private MemberReservationInfo requireMember(
            String memberId
    ) {
        if (memberId == null
                || memberId.isBlank()
                || "anonymousUser".equals(memberId)) {
            throw new ReservationAuthenticationRequiredException();
        }

        return reservationMemberReader
                .findMemberInfoByMemberId(memberId)
                .orElseThrow(
                        ReservationAuthenticationRequiredException::new
                );
    }
}
