package com.young04.lastproject.global.sms;

public class SmsSendException extends RuntimeException {

    public SmsSendException(String message) {
        super(message);
    }

    public SmsSendException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
