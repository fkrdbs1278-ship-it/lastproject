package com.young04.lastproject.reservationimage.controller;

import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.service.ReservationImageContent;
import com.young04.lastproject.reservationimage.service.ReservationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/reservations/{reservationNo}/images")
@RequiredArgsConstructor
public class AdminReservationImageController {

    private final ReservationImageService reservationImageService;

    @GetMapping
    public ResponseEntity<List<ReservationImageResponse>> list(
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationImageService.getAdminImages(
                        reservationNo
                )
        );
    }

    @GetMapping("/{reservationImageNo}/content")
    public ResponseEntity<Resource> content(
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo
    ) {
        ReservationImageContent content =
                reservationImageService.getAdminContent(
                        reservationNo,
                        reservationImageNo
                );

        return ResponseEntity.ok()
                .contentType(content.mediaType())
                .body(content.resource());
    }

    @DeleteMapping("/{reservationImageNo}")
    public ResponseEntity<Void> delete(
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo
    ) {
        reservationImageService.deleteForAdmin(
                reservationNo,
                reservationImageNo
        );

        return ResponseEntity.noContent().build();
    }
}
