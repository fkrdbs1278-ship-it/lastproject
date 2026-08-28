package com.young04.lastproject.servicemenu.exception;

public class ServiceMenuNotFoundException
        extends RuntimeException {

    public ServiceMenuNotFoundException() {

        super("시술 메뉴를 찾을 수 없습니다.");
    }
}
