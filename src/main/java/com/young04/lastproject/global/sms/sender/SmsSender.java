package com.young04.lastproject.global.sms.sender;

public interface SmsSender {

    void send(
            String to,
            String subject,
            String content
    );
}
