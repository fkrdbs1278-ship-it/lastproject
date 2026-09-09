package com.young04.lastproject.salonholiday.controller;

import com.young04.lastproject.salonholiday.dto.SalonHolidayRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.service.SalonHolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/holidays")
@RequiredArgsConstructor
public class AdminSalonHolidayController {

    private final SalonHolidayService salonHolidayService;

    @GetMapping
    public ResponseEntity<List<SalonHolidayResponse>> list() {
        return ResponseEntity.ok(
                salonHolidayService.getHolidays()
        );
    }

    @PostMapping
    public ResponseEntity<SalonHolidayResponse> create(
            @Valid @RequestBody SalonHolidayRequest request
    ) {
        return ResponseEntity.ok(
                SalonHolidayResponse.from(
                        salonHolidayService
                                .createHoliday(request)
                )
        );
    }

    @PutMapping("/{salonHolidayNo}")
    public ResponseEntity<SalonHolidayResponse> update(
            @PathVariable Long salonHolidayNo,
            @Valid @RequestBody SalonHolidayRequest request
    ) {
        return ResponseEntity.ok(
                SalonHolidayResponse.from(
                        salonHolidayService
                                .updateHoliday(
                                        salonHolidayNo,
                                        request
                                )
                )
        );
    }

    @DeleteMapping("/{salonHolidayNo}")
    public ResponseEntity<Void> delete(
            @PathVariable Long salonHolidayNo
    ) {
        salonHolidayService
                .deleteHoliday(salonHolidayNo);

        return ResponseEntity.noContent().build();
    }
}
