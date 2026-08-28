package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.*;
import com.young04.lastproject.reservation.exception.*;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailableTimeService availableTimeService;
    private final ServiceMenuReader serviceMenuReader;
    private final BusinessHourRepository businessHourRepository;

    @Transactional
    public ReservationResponse createReservation(
            ReservationCreateRequest request
    ) {
        validateCustomer(request);

        var menu =
                serviceMenuReader.getActiveServiceMenu(
                        request.getServiceMenuNo()
                );

        LocalDateTime start =
                request.getStartAt();

        LocalDateTime end =
                start.plusMinutes(
                        menu.durationMin()
                );

        /*
         * 같은 요일의 BUSINESS_HOUR 행을 PESSIMISTIC_WRITE로 잠근 뒤
         * 예약 가능 여부를 최종 재검사합니다.
         *
         * 1인 미용실에서는 동시 예약량이 많지 않기 때문에,
         * 날짜 전용 lock table을 추가하지 않고도
         * 단순하고 안정적인 직렬화 효과를 얻을 수 있습니다.
         */
        lockReservationDay(start);

        if (!availableTimeService
                .isAvailable(start, end)) {
            throw new ReservationUnavailableException(
                    "선택한 시간에는 예약할 수 없습니다."
            );
        }

        ReservationSource source =
                request.getReservationSource() == null
                        ? ReservationSource.ONLINE
                        : request.getReservationSource();

        Reservation reservation;

        if (request.getMemberNo() != null) {
            reservation =
                    Reservation.createMemberReservation(
                            request.getMemberNo(),
                            request.getServiceMenuNo(),
                            menu.name(),
                            menu.durationMin(),
                            start,
                            end,
                            request.getRequestMemo(),
                            source
                    );
        } else {
            reservation =
                    Reservation.createGuestReservation(
                            request.getGuestName(),
                            request.getGuestPhone(),
                            request.getServiceMenuNo(),
                            menu.name(),
                            menu.durationMin(),
                            start,
                            end,
                            request.getRequestMemo(),
                            source
                    );
        }

        return ReservationResponse.from(
                reservationRepository.save(reservation)
        );
    }

    @Transactional
    public ReservationResponse updateReservation(
            Long reservationNo,
            ReservationUpdateRequest request
    ) {
        Reservation reservation =
                getReservation(reservationNo);

        validateModifiable(reservation);

        var menu =
                serviceMenuReader.getActiveServiceMenu(
                        request.getServiceMenuNo()
                );

        LocalDateTime start =
                request.getStartAt();

        LocalDateTime end =
                start.plusMinutes(
                        menu.durationMin()
                );

        lockReservationDay(start);

        if (!availableTimeService
                .isAvailableForUpdate(
                        reservationNo,
                        start,
                        end
                )) {
            throw new ReservationUnavailableException(
                    "변경하려는 시간에는 예약할 수 없습니다."
            );
        }

        reservation.changeSchedule(
                request.getServiceMenuNo(),
                menu.name(),
                menu.durationMin(),
                start,
                end,
                request.getRequestMemo()
        );

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse confirmReservation(
            Long reservationNo
    ) {
        Reservation reservation =
                getReservation(reservationNo);

        if (reservation.getStatus()
                != ReservationStatus.REQUESTED) {
            throw new InvalidReservationStatusException(
                    "REQUESTED 상태의 예약만 확정할 수 있습니다."
            );
        }

        reservation.confirm();

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse completeReservation(
            Long reservationNo
    ) {
        Reservation reservation =
                getReservation(reservationNo);

        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException(
                    "CONFIRMED 상태의 예약만 시술 완료 처리할 수 있습니다."
            );
        }

        reservation.complete();

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(
            Long reservationNo,
            String reason,
            CanceledBy canceledBy
    ) {
        Reservation reservation =
                getReservation(reservationNo);

        if (reservation.getStatus()
                == ReservationStatus.COMPLETED
                || reservation.getStatus()
                == ReservationStatus.CANCELED
                || reservation.getStatus()
                == ReservationStatus.NO_SHOW) {
            throw new InvalidReservationStatusException(
                    "현재 상태에서는 예약을 취소할 수 없습니다."
            );
        }

        reservation.cancel(
                reason,
                canceledBy
        );

        return ReservationResponse.from(reservation);
    }

    public ReservationResponse getReservationDetail(
            Long reservationNo
    ) {
        return ReservationResponse.from(
                getReservation(reservationNo)
        );
    }

    public List<ReservationResponse> getMemberReservations(
            Long memberNo
    ) {
        return reservationRepository
                .findByMemberNoOrderByStartAtDesc(memberNo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private void lockReservationDay(
            LocalDateTime startAt
    ) {
        int dayOfWeek =
                startAt.getDayOfWeek().getValue();

        businessHourRepository
                .findByDayOfWeekForUpdate(dayOfWeek)
                .orElseThrow(
                        () ->
                                new ReservationUnavailableException(
                                        "영업시간 정보가 없습니다."
                                )
                );
    }

    private Reservation getReservation(
            Long reservationNo
    ) {
        return reservationRepository
                .findById(reservationNo)
                .orElseThrow(
                        () ->
                                new ReservationNotFoundException(
                                        reservationNo
                                )
                );
    }

    private void validateCustomer(
            ReservationCreateRequest request
    ) {
        boolean member =
                request.getMemberNo() != null;

        boolean guest =
                hasText(request.getGuestName())
                        && hasText(
                                request.getGuestPhone()
                        );

        if (member == guest) {
            throw new ReservationUnavailableException(
                    "회원 또는 비회원 정보 중 하나만 입력해야 합니다."
            );
        }
    }

    private void validateModifiable(
            Reservation reservation
    ) {
        if (reservation.getStatus()
                != ReservationStatus.REQUESTED
                && reservation.getStatus()
                != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException(
                    "현재 상태에서는 예약을 변경할 수 없습니다."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null
                && !value.isBlank();
    }
}
