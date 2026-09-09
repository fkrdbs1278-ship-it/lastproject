package com.young04.lastproject.salonholiday.exception;

public class SalonHolidayNotFoundException
        extends RuntimeException {

    public SalonHolidayNotFoundException(Long salonHolidayNo) {
        super(
                "일정 정보를 찾을 수 없습니다. salonHolidayNo="
                        + salonHolidayNo
        );
    }
}