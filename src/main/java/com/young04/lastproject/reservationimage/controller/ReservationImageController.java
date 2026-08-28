package com.young04.lastproject.reservationimage.controller;

import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.service.ReservationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reservations/{reservationNo}/images")
@RequiredArgsConstructor
public class ReservationImageController {

    private final ReservationImageService reservationImageService;

    @PostMapping
    public ResponseEntity<ReservationImageResponse> upload(
            @PathVariable Long reservationNo,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                reservationImageService.upload(
                        reservationNo,
                        file
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ReservationImageResponse>> list(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationImageService.getImages(
                        reservationNo
                )
        );
    }

    @DeleteMapping("/{reservationImageNo}")
    public ResponseEntity<Void> delete(
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo
    ) {

        reservationImageService.delete(
                reservationNo,
                reservationImageNo
        );

        return ResponseEntity.noContent()
                .build();
    }
}
