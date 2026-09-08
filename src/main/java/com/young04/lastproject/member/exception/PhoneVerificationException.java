package com.young04.lastproject.member.exception;

public class PhoneVerificationException
        extends RuntimeException {

    public PhoneVerificationException(
            String message
    ) {
        super(message);
    }
}