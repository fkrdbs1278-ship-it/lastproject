package com.young04.lastproject.global.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "sms",
        name = "provider",
        havingValue = "log",
        matchIfMissing = true
)
public class LogSmsSender implements SmsSender {

    @Override
    public void send(
            String to,
            String subject,
            String content
    ) {
        log.info(
                """
                [SMS][LOG_ONLY]
                to={}
                subject={}
                content=
                {}
                """,
                maskPhone(to),
                subject,
                content
        );
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "***";
        }

        String digits =
                phone.replaceAll("\\D", "");

        if (digits.length() < 7) {
            return "***";
        }

        return digits.substring(0, 3)
                + "****"
                + digits.substring(
                        digits.length() - 4
                );
    }
}
