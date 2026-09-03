package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.ReservationSource;
import com.young04.lastproject.reservation.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthenticatedReservationService authenticatedReservationService;
    private final AvailableTimeService availableTimeService;
    private final ServiceMenuReader serviceMenuReader;
    private final HairStyleReader hairStyleReader;
    private final SalonEventReader salonEventReader;

    /*
     * 공개 예약 생성은 비회원 전용입니다.
     * 클라이언트가 memberNo를 조작해 회원 예약을 만들 수 없도록
     * memberNo를 서버에서 강제로 제거합니다.
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createGuest(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        request.setMemberNo(null);
        request.setReservationSource(ReservationSource.ONLINE);

        return ResponseEntity.ok(
                reservationService.createReservation(request)
        );
    }

    /*
     * 로그인 회원 예약.
     * memberNo는 요청에서 받지 않고 로그인 ID를 통해 서버가 결정합니다.
     */
    @PostMapping("/me")
    public ResponseEntity<ReservationResponse> createMine(
            Authentication authentication,
            @Valid @RequestBody MemberReservationCreateRequest request
    ) {
        return ResponseEntity.ok(
                authenticatedReservationService
                        .createMyReservation(
                                username(authentication),
                                request
                        )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> myReservations(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                authenticatedReservationService
                        .getMyReservations(
                                username(authentication)
                        )
        );
    }

    @GetMapping("/me/{reservationNo}")
    public ResponseEntity<ReservationDetailResponse> myReservationDetail(
            Authentication authentication,
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                authenticatedReservationService
                        .getMyReservationDetail(
                                username(authentication),
                                reservationNo
                        )
        );
    }

    @PutMapping("/me/{reservationNo}")
    public ResponseEntity<ReservationResponse> updateMine(
            Authentication authentication,
            @PathVariable Long reservationNo,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(
                authenticatedReservationService
                        .updateMyReservation(
                                username(authentication),
                                reservationNo,
                                request
                        )
        );
    }

    @PostMapping("/me/{reservationNo}/cancel")
    public ResponseEntity<ReservationResponse> cancelMine(
            Authentication authentication,
            @PathVariable Long reservationNo,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(
                authenticatedReservationService
                        .cancelMyReservation(
                                username(authentication),
                                reservationNo,
                                reason
                        )
        );
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> availableTimes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam Long serviceMenuNo
    ) {
        return ResponseEntity.ok(
                availableTimeService.getAvailableTimes(
                        date,
                        serviceMenuNo
                )
        );
    }

    @GetMapping("/service-menus")
    public ResponseEntity<List<ServiceMenuOptionResponse>> serviceMenus() {
        return ResponseEntity.ok(
                serviceMenuReader.getActiveServiceMenus()
        );
    }

    @GetMapping("/hair-styles")
    public ResponseEntity<List<HairStyleOptionResponse>> hairStyles(
            @RequestParam Long serviceMenuNo
    ) {
        return ResponseEntity.ok(
                hairStyleReader.getActiveStylesForService(
                        serviceMenuNo
                )
        );
    }

    @GetMapping("/events")
    public ResponseEntity<List<SalonEventOptionResponse>> events() {
        return ResponseEntity.ok(
                salonEventReader.getOngoingEvents()
        );
    }

    @PostMapping("/guest/lookup")
    public ResponseEntity<ReservationResponse> lookupGuest(
            @Valid @RequestBody GuestReservationLookupRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.lookupGuestReservation(request)
        );
    }

    @PutMapping("/guest")
    public ResponseEntity<ReservationResponse> updateGuest(
            @Valid @RequestBody GuestReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.updateGuestReservation(request)
        );
    }

    @PostMapping("/guest/cancel")
    public ResponseEntity<ReservationResponse> cancelGuest(
            @Valid @RequestBody GuestReservationCancelRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.cancelGuestReservation(request)
        );
    }

    private String username(Authentication authentication) {
        return authentication == null
                ? null
                : authentication.getName();
    }
}
