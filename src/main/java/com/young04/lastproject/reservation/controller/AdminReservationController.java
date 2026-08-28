package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.dto.AdminReservationSearchResponse;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.service.AdminReservationService;
import com.young04.lastproject.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final AdminReservationService adminReservationService;

    @GetMapping
    public ResponseEntity<AdminReservationSearchResponse> search(
            ReservationSearchCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                adminReservationService.search(
                        condition,
                        page,
                        size
                )
        );
    }

    @PostMapping("/{reservationNo}/confirm")
    public ResponseEntity<ReservationResponse> confirm(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationService
                        .confirmReservation(
                                reservationNo
                        )
        );
    }

    @PostMapping("/{reservationNo}/complete")
    public ResponseEntity<ReservationResponse> complete(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationService
                        .completeReservation(
                                reservationNo
                        )
        );
    }

    @PostMapping("/{reservationNo}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long reservationNo,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(
                reservationService
                        .cancelReservation(
                                reservationNo,
                                reason,
                                CanceledBy.ADMIN
                        )
        );
    }
}
