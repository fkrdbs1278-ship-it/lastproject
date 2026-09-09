package com.young04.lastproject.reservation.exception;

public class ServiceMenuNotFoundException extends ReservationException {
    public ServiceMenuNotFoundException(Long serviceMenuNo) {
        super("시술 메뉴를 찾을 수 없습니다. serviceMenuNo=" + serviceMenuNo);
    }
}
