package com.young04.lastproject.customerprofile.exception;

public class DuplicateCustomerPhoneException
        extends RuntimeException {

    public DuplicateCustomerPhoneException() {
        super("이미 등록된 전화번호입니다.");
    }

    public DuplicateCustomerPhoneException(
            String message
    ) {
        super(message);
    }
}