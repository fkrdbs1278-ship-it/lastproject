package com.young04.lastproject.salonholiday.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.young04.lastproject.salonholiday"
)
public class SalonHolidayExceptionHandler {

    @ExceptionHandler(SalonHolidayNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            SalonHolidayNotFoundException e
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("success", false);
        body.put(
                "code",
                "SALON_HOLIDAY_NOT_FOUND"
        );
        body.put(
                "message",
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException e
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("success", false);
        body.put(
                "code",
                "SALON_HOLIDAY_BAD_REQUEST"
        );
        body.put(
                "message",
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}