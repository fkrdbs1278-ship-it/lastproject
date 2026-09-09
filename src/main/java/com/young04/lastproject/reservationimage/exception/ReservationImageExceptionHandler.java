package com.young04.lastproject.reservationimage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(
        basePackages =
                "com.young04.lastproject.reservationimage"
)
public class ReservationImageExceptionHandler {

    @ExceptionHandler(ReservationImageAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            ReservationImageAccessDeniedException e
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        body(
                                "RESERVATION_IMAGE_ACCESS_DENIED",
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(ReservationImageNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ReservationImageNotFoundException e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        body(
                                "RESERVATION_IMAGE_NOT_FOUND",
                                e.getMessage()
                        )
                );
    }

    @ExceptionHandler(ReservationImageException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            ReservationImageException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        body(
                                "RESERVATION_IMAGE_ERROR",
                                e.getMessage()
                        )
                );
    }

    private Map<String, Object> body(
            String code,
            String message
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("success", false);
        body.put("code", code);
        body.put("message", message);

        return body;
    }
}
