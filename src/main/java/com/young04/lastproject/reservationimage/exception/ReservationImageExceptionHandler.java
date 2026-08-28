package com.young04.lastproject.reservationimage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        basePackages =
                "com.young04.lastproject.reservationimage"
)
public class ReservationImageExceptionHandler {

    @ExceptionHandler(
            ReservationImageException.class
    )
    public ResponseEntity<Map<String, Object>> handle(
            ReservationImageException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        Map.of(
                                "success", false,
                                "code", "RESERVATION_IMAGE_ERROR",
                                "message", e.getMessage()
                        )
                );
    }
}
