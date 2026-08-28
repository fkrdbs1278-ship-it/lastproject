package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.dto.AvailableTimeResponse;
import com.young04.lastproject.reservation.dto.ReservationCreateRequest;
import com.young04.lastproject.reservation.dto.ReservationResponse;
import com.young04.lastproject.reservation.dto.ReservationUpdateRequest;
import com.young04.lastproject.reservation.dto.ServiceMenuOptionResponse;
import com.young04.lastproject.reservation.entity.CanceledBy;
import com.young04.lastproject.reservation.service.AvailableTimeService;
import com.young04.lastproject.reservation.service.ReservationService;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final AvailableTimeService availableTimeService;
    private final ServiceMenuReader serviceMenuReader;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.createReservation(request)
        );
    }

    @PutMapping("/{reservationNo}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long reservationNo,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(
                reservationService.updateReservation(
                        reservationNo,
                        request
                )
        );
    }

    @GetMapping("/{reservationNo}")
    public ResponseEntity<ReservationResponse> detail(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationDetail(reservationNo)
        );
    }

    @GetMapping("/member/{memberNo}")
    public ResponseEntity<List<ReservationResponse>> memberReservations(
            @PathVariable Long memberNo
    ) {
        return ResponseEntity.ok(
                reservationService.getMemberReservations(memberNo)
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
                availableTimeService.getAvailableTimes(date, serviceMenuNo)
        );
    }

    @GetMapping("/service-menus")
    public ResponseEntity<List<ServiceMenuOptionResponse>> serviceMenus() {
        return ResponseEntity.ok(
                serviceMenuReader.getActiveServiceMenus()
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
                        CanceledBy.USER
                )
        );
    }
}
