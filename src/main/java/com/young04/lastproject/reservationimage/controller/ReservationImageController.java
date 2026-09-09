package com.young04.lastproject.reservationimage.controller;

import com.young04.lastproject.reservationimage.dto.ReservationImageResponse;
import com.young04.lastproject.reservationimage.service.ReservationImageContent;
import com.young04.lastproject.reservationimage.service.ReservationImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationImageController {

    private final ReservationImageService reservationImageService;

    @PostMapping("/api/reservations/me/{reservationNo}/images")
    public ResponseEntity<ReservationImageResponse> uploadMember(
            Authentication authentication,
            @PathVariable Long reservationNo,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                reservationImageService.uploadForMember(
                        username(authentication),
                        reservationNo,
                        file
                )
        );
    }

    @GetMapping("/api/reservations/me/{reservationNo}/images")
    public ResponseEntity<List<ReservationImageResponse>> listMember(
            Authentication authentication,
            @PathVariable Long reservationNo
    ) {
        return ResponseEntity.ok(
                reservationImageService.getMemberImages(
                        username(authentication),
                        reservationNo
                )
        );
    }

    @GetMapping("/api/reservations/me/{reservationNo}/images/{reservationImageNo}/content")
    public ResponseEntity<Resource> memberContent(
            Authentication authentication,
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo
    ) {
        ReservationImageContent content =
                reservationImageService.getMemberContent(
                        username(authentication),
                        reservationNo,
                        reservationImageNo
                );

        return ResponseEntity.ok()
                .contentType(content.mediaType())
                .body(content.resource());
    }

    @DeleteMapping("/api/reservations/me/{reservationNo}/images/{reservationImageNo}")
    public ResponseEntity<Void> deleteMember(
            Authentication authentication,
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo
    ) {
        reservationImageService.deleteForMember(
                username(authentication),
                reservationNo,
                reservationImageNo
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/reservations/guest/{reservationNo}/images")
    public ResponseEntity<ReservationImageResponse> uploadGuest(
            @PathVariable Long reservationNo,
            @RequestParam("guestPhone") String guestPhone,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                reservationImageService.uploadForGuest(
                        reservationNo,
                        guestPhone,
                        file
                )
        );
    }

    @DeleteMapping("/api/reservations/guest/{reservationNo}/images/{reservationImageNo}")
    public ResponseEntity<Void> deleteGuest(
            @PathVariable Long reservationNo,
            @PathVariable Long reservationImageNo,
            @RequestParam("guestPhone") String guestPhone
    ) {
        reservationImageService.deleteForGuest(
                reservationNo,
                guestPhone,
                reservationImageNo
        );

        return ResponseEntity.noContent().build();
    }

    private String username(Authentication authentication) {
        return authentication == null
                ? null
                : authentication.getName();
    }
}
