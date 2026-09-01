package com.young04.lastproject.salonholiday.controller;

import com.young04.lastproject.salonholiday.dto.OwnerAvailabilityBlockRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.service.OwnerAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * "원장 예약 가능 시간 관리"는 영업시간과 별개로
 * 예약을 받지 못하는 개인 일정/점심/외출 시간을 차단하는 기능입니다.
 *
 * 새 테이블을 만들지 않고 기존 SALON_HOLIDAY의 PERSONAL 유형을
 * 시간 단위 block으로 사용합니다.
 */
@RestController
@RequestMapping("/admin/api/availability-blocks")
@RequiredArgsConstructor
public class AdminOwnerAvailabilityController {

    private final OwnerAvailabilityService ownerAvailabilityService;

    @GetMapping
    public ResponseEntity<List<SalonHolidayResponse>> list() {
        return ResponseEntity.ok(
                ownerAvailabilityService.getBlocks()
        );
    }

    @PostMapping
    public ResponseEntity<SalonHolidayResponse> create(
            @Valid @RequestBody OwnerAvailabilityBlockRequest request
    ) {
        return ResponseEntity.ok(
                ownerAvailabilityService
                        .createBlock(request)
        );
    }

    @PutMapping("/{salonHolidayNo}")
    public ResponseEntity<SalonHolidayResponse> update(
            @PathVariable Long salonHolidayNo,
            @Valid @RequestBody OwnerAvailabilityBlockRequest request
    ) {
        return ResponseEntity.ok(
                ownerAvailabilityService
                        .updateBlock(
                                salonHolidayNo,
                                request
                        )
        );
    }

    @DeleteMapping("/{salonHolidayNo}")
    public ResponseEntity<Void> delete(
            @PathVariable Long salonHolidayNo
    ) {
        ownerAvailabilityService
                .deleteBlock(salonHolidayNo);

        return ResponseEntity.noContent().build();
    }
}
