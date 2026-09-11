package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.*;
import com.young04.lastproject.reservation.exception.*;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import com.young04.lastproject.reservation.notification.ReservationNotificationPublisher;
import com.young04.lastproject.reservation.notification.ReservationNotificationType;
import com.young04.lastproject.servicematerial.service.MaterialUsageService;
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
    private final HairStyleReader hairStyleReader;
    private final BusinessHourRepository businessHourRepository;
    private final ReservationNotificationPublisher notificationPublisher;
    private final ReservationPricingService reservationPricingService;

    // 시술 완료 시 연결된 자재를 자동 차감
    private final MaterialUsageService materialUsageService;


    @Transactional
    public ReservationResponse createReservation(
            ReservationCreateRequest request
    ) {
        validateCustomer(request);

        var menu =
                serviceMenuReader.getActiveServiceMenu(
                        request.getServiceMenuNo()
                );

        validateHairStyle(
                request.getHairStyleNo(),
                request.getServiceMenuNo()
        );

        LocalDateTime start = request.getStartAt();
        LocalDateTime end =
                start.plusMinutes(menu.durationMin());

        lockReservationDay(start);

        if (!availableTimeService.isAvailable(start, end)) {
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
                            request.getHairStyleNo(),
                            menu.name(),
                            menu.durationMin(),
                            start,
                            end,
                            normalizeMemo(request.getRequestMemo()),
                            source
                    );
        } else {
            reservation =
                    Reservation.createGuestReservation(
                            normalizeGuestName(
                                    request.getGuestName()
                            ),
                            normalizeGuestPhone(
                                    request.getGuestPhone()
                            ),
                            request.getServiceMenuNo(),
                            request.getHairStyleNo(),
                            menu.name(),
                            menu.durationMin(),
                            start,
                            end,
                            normalizeMemo(request.getRequestMemo()),
                            source
                    );
        }

        var price = reservationPricingService.calculate(
                request.getMemberNo(),
                request.getGuestPhone(),
                request.getServiceMenuNo(),
                start
        );
        reservation.applyPricing(
                price.getOriginalPrice(),
                price.getEventNo(),
                price.getEventTitle(),
                price.getDiscountAmount(),
                price.getFinalPrice()
        );

        Reservation saved =
                reservationRepository.save(reservation);

        /*
         * 온라인 비회원은 예약번호를 잊어버리지 않도록
         * 예약 접수 직후 문자를 발송한다.
         *
         * 관리자 전화 예약(PHONE)은 생성 직후 CONFIRMED 처리되므로
         * 접수/확정 문자가 연속으로 2건 발송되지 않게 CREATED는 생략한다.
         */
        if (saved.getReservationSource()
                == ReservationSource.ONLINE) {
            notificationPublisher.publish(
                    ReservationNotificationType.CREATED,
                    saved
            );
        }

        return ReservationResponse.from(saved);
    }


    @Transactional
    public ReservationResponse updateReservation(
            Long reservationNo,
            ReservationUpdateRequest request
    ) {
        Reservation reservation =
                getReservationForUpdate(reservationNo);

        updateReservationEntity(
                reservation,
                reservationNo,
                request.getServiceMenuNo(),
                request.getHairStyleNo(),
                request.getStartAt(),
                request.getRequestMemo()
        );

        notificationPublisher.publish(
                ReservationNotificationType.UPDATED,
                reservation
        );

        return ReservationResponse.from(reservation);
    }


    @Transactional
    public ReservationResponse updateGuestReservation(
            GuestReservationUpdateRequest request
    ) {
        String phone =
                normalizeGuestPhone(
                        request.getGuestPhone()
                );

        Reservation reservation =
                reservationRepository
                        .findGuestForUpdate(
                                request.getReservationNo(),
                                CustomerType.GUEST,
                                phone
                        )
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                request.getReservationNo()
                                        )
                        );

        updateReservationEntity(
                reservation,
                reservation.getReservationNo(),
                request.getServiceMenuNo(),
                request.getHairStyleNo(),
                request.getStartAt(),
                request.getRequestMemo()
        );

        notificationPublisher.publish(
                ReservationNotificationType.UPDATED,
                reservation
        );

        return ReservationResponse.from(reservation);
    }


    @Transactional
    public ReservationResponse confirmReservation(
            Long reservationNo
    ) {
        Reservation reservation =
                getReservationForUpdate(reservationNo);

        if (reservation.getStatus()
                != ReservationStatus.REQUESTED) {
            throw new InvalidReservationStatusException(
                    "REQUESTED 상태의 예약만 확정할 수 있습니다."
            );
        }

        reservation.confirm();

        notificationPublisher.publish(
                ReservationNotificationType.CONFIRMED,
                reservation
        );

        return ReservationResponse.from(reservation);
    }


    // 확정된 예약을 시술 완료 처리하고 연결된 자재를 자동 차감
    @Transactional
    public ReservationResponse completeReservation(
            Long reservationNo
    ) {
        Reservation reservation =
                getReservationForUpdate(reservationNo);

        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStatusException(
                    "CONFIRMED 상태의 예약만 시술 완료 처리할 수 있습니다."
            );
        }

        if (reservation.getStartAt()
                .isAfter(LocalDateTime.now())) {
            throw new InvalidReservationStatusException(
                    "예약 시작 전에는 시술 완료 처리할 수 없습니다."
            );
        }

        // 예약 상태를 시술 완료로 변경
        reservation.complete();

        // 시술에 연결된 자재가 있으면 실제 사용량만큼 자동 차감
        materialUsageService.deductMaterialsForReservation(
                reservation.getReservationNo(),
                reservation.getServiceMenuNo()
        );

        notificationPublisher.publish(
                ReservationNotificationType.COMPLETED,
                reservation
        );

        return ReservationResponse.from(reservation);
    }


    @Transactional
    public ReservationResponse cancelReservation(
            Long reservationNo,
            String reason,
            CanceledBy canceledBy
    ) {
        Reservation reservation =
                getReservationForUpdate(reservationNo);

        validateCancelable(
                reservation,
                canceledBy
        );

        reservation.cancel(
                normalizeCancelReason(reason),
                canceledBy
        );

        notificationPublisher.publish(
                ReservationNotificationType.CANCELED,
                reservation
        );

        return ReservationResponse.from(reservation);
    }


    public ReservationResponse lookupGuestReservation(
            GuestReservationLookupRequest request
    ) {
        String phone =
                normalizeGuestPhone(
                        request.getGuestPhone()
                );

        Reservation reservation =
                reservationRepository
                        .findByReservationNoAndCustomerTypeAndGuestPhone(
                                request.getReservationNo(),
                                CustomerType.GUEST,
                                phone
                        )
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                request.getReservationNo()
                                        )
                        );

        return ReservationResponse.from(reservation);
    }


    @Transactional
    public ReservationResponse cancelGuestReservation(
            GuestReservationCancelRequest request
    ) {
        String phone =
                normalizeGuestPhone(
                        request.getGuestPhone()
                );

        Reservation reservation =
                reservationRepository
                        .findGuestForUpdate(
                                request.getReservationNo(),
                                CustomerType.GUEST,
                                phone
                        )
                        .orElseThrow(
                                () ->
                                        new ReservationNotFoundException(
                                                request.getReservationNo()
                                        )
                        );

        validateCancelable(
                reservation,
                CanceledBy.USER
        );

        reservation.cancel(
                normalizeCancelReason(
                        request.getReason()
                ),
                CanceledBy.USER
        );

        notificationPublisher.publish(
                ReservationNotificationType.CANCELED,
                reservation
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


    private void updateReservationEntity(
            Reservation reservation,
            Long reservationNo,
            Long serviceMenuNo,
            Long hairStyleNo,
            LocalDateTime start,
            String requestMemo
    ) {
        validateModifiable(reservation);

        var menu =
                serviceMenuReader.getActiveServiceMenu(
                        serviceMenuNo
                );

        validateHairStyle(
                hairStyleNo,
                serviceMenuNo
        );

        LocalDateTime end =
                start.plusMinutes(menu.durationMin());

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
                serviceMenuNo,
                hairStyleNo,
                menu.name(),
                menu.durationMin(),
                start,
                end,
                normalizeMemo(requestMemo)
        );

        var price = reservationPricingService.calculate(
                reservation.getMemberNo(),
                reservation.getGuestPhone(),
                serviceMenuNo, start
        );
        reservation.applyPricing(
                price.getOriginalPrice(),
                price.getEventNo(),
                price.getEventTitle(),
                price.getDiscountAmount(),
                price.getFinalPrice()
        );
    }


    private void validateCustomer(
            ReservationCreateRequest request
    ) {
        boolean member =
                request.getMemberNo() != null;

        boolean guest =
                hasText(request.getGuestName())
                        && hasText(request.getGuestPhone());

        if (member == guest) {
            throw new ReservationUnavailableException(
                    "회원 또는 비회원 정보 중 하나만 입력해야 합니다."
            );
        }
    }


    private void validateHairStyle(
            Long hairStyleNo,
            Long serviceMenuNo
    ) {
        if (!hairStyleReader.isStyleLinkedToService(
                hairStyleNo,
                serviceMenuNo
        )) {
            throw new ReservationUnavailableException(
                    "선택한 헤어스타일은 해당 시술 메뉴에서 사용할 수 없습니다."
            );
        }
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


    private Reservation getReservationForUpdate(
            Long reservationNo
    ) {
        return reservationRepository
                .findByIdForUpdate(reservationNo)
                .orElseThrow(
                        () ->
                                new ReservationNotFoundException(
                                        reservationNo
                                )
                );
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


    private void validateCancelable(
            Reservation reservation,
            CanceledBy canceledBy
    ) {
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

        if (canceledBy == CanceledBy.USER
                && !reservation.getStartAt()
                .isAfter(LocalDateTime.now())) {
            throw new InvalidReservationStatusException(
                    "예약 시간이 지난 후에는 고객이 직접 취소할 수 없습니다. 미용실에 문의해주세요."
            );
        }
    }


    private String normalizeGuestName(String value) {
        return value == null
                ? null
                : value.trim().replaceAll("\\s+", " ");
    }


    private String normalizeGuestPhone(String value) {
        return value == null
                ? null
                : value.replaceAll("\\D", "");
    }


    private String normalizeMemo(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    private String normalizeCancelReason(String value) {
        if (value == null || value.isBlank()) {
            return "사용자 요청";
        }

        return value.trim();
    }


    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}