package com.young04.lastproject.businesshour.controller;

import com.young04.lastproject.businesshour.dto.BusinessHourResponse;
import com.young04.lastproject.businesshour.dto.BusinessHourUpdateRequest;
import com.young04.lastproject.businesshour.service.BusinessHourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/business-hours")
@RequiredArgsConstructor
public class AdminBusinessHourController {

    private final BusinessHourService businessHourService;

    @GetMapping
    public ResponseEntity<List<BusinessHourResponse>> list() {
        return ResponseEntity.ok(
                businessHourService.getBusinessHourResponses()
        );
    }

    @PutMapping("/{dayOfWeek}")
    public ResponseEntity<BusinessHourResponse> update(
            @PathVariable Integer dayOfWeek,
            @Valid @RequestBody BusinessHourUpdateRequest request
    ) {
        return ResponseEntity.ok(
                businessHourService
                        .updateBusinessHour(
                                dayOfWeek,
                                request
                        )
        );
    }
}
