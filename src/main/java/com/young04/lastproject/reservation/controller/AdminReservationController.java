package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.noshow.service.NoShowService;
import com.young04.lastproject.reservation.dto.*;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * 1번 담당자의 SecurityConfig가 /admin/** 를 ROLE_ADMIN으로 보호하므로
 * 다른 파트 코드를 수정하지 않고 그 정책을 그대로 사용하기 위해
 * 관리자 API도 /admin/api/** 아래에 둡니다.
 */
@RestController
@RequestMapping("/admin/api/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final AdminReservationService adminReservationService;
    private final NoShowService noShowService;

    @GetMapping
    public ResponseEntity<AdminReservationSearchResponse> search(
            ReservationSearchCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                adminReservationService.search(condition, page, size)
        );
    }

    @GetMapping("/{reservationNo}")
    public ResponseEntity<AdminReservationDetailResponse> detail(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                adminReservationService.detail(reservationNo)
        );
    }

    @PostMapping("/phone")
    public ResponseEntity<ReservationResponse> createPhoneReservation(
            @Valid @RequestBody AdminPhoneReservationRequest request
    ) {
        return ResponseEntity.ok(
                adminReservationService
                        .createPhoneReservation(request)
        );
    }

    @PostMapping("/{reservationNo}/confirm")
    public ResponseEntity<ReservationResponse> confirm(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationService.confirmReservation(reservationNo)
        );
    }

    @PostMapping("/{reservationNo}/complete")
    public ResponseEntity<ReservationResponse> complete(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationService.completeReservation(reservationNo)
        );
    }

    @PostMapping("/{reservationNo}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long reservationNo,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(
                reservationService.cancelReservation(
                        reservationNo,
                        reason,
                        CanceledBy.ADMIN
                )
        );
    }

    @PostMapping("/{reservationNo}/no-show")
    public ResponseEntity<Void> noShow(
            @PathVariable Long reservationNo,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String adminMemo
    ) {
        noShowService.markNoShow(
                reservationNo,
                reason,
                adminMemo
        );

        return ResponseEntity.noContent().build();
    }
}
