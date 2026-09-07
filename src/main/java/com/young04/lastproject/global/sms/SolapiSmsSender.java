package com.young04.lastproject.global.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "sms",
        name = "provider",
        havingValue = "solapi"
)
public class SolapiSmsSender implements SmsSender {

    private final String sender;
    private final DefaultMessageService messageService;

    public SolapiSmsSender(
            @Value("${sms.solapi.api-key:}")
            String apiKey,
            @Value("${sms.solapi.api-secret:}")
            String apiSecret,
            @Value("${sms.solapi.sender:}")
            String sender
    ) {
        validateConfiguration(
                apiKey,
                apiSecret,
                sender
        );

        this.sender =
                normalizePhone(sender);

        this.messageService =
                SolapiClient.INSTANCE
                        .createInstance(
                                apiKey,
                                apiSecret
                        );
    }

    @Override
    public void send(
            String to,
            String subject,
            String content
    ) {
        if (to == null || to.isBlank()) {
            throw new SmsSendException(
                    "문자 수신번호가 비어 있습니다."
            );
        }

        if (content == null || content.isBlank()) {
            throw new SmsSendException(
                    "문자 내용이 비어 있습니다."
            );
        }

        try {
            Message message =
                    new Message();

            message.setFrom(sender);
            message.setTo(
                    normalizePhone(to)
            );
            message.setText(content);

            if (subject != null
                    && !subject.isBlank()) {
                message.setSubject(subject);
            }

            messageService.send(
                    message,
                    null
            );
        } catch (Exception e) {
            throw new SmsSendException(
                    "SOLAPI 문자 발송에 실패했습니다.",
                    e
            );
        }
    }

    private void validateConfiguration(
            String apiKey,
            String apiSecret,
            String sender
    ) {
        if (isBlank(apiKey)
                || isBlank(apiSecret)
                || isBlank(sender)) {
            throw new SmsSendException(
                    "SOLAPI 설정값이 비어 있습니다. "
                            + "SOLAPI_API_KEY, "
                            + "SOLAPI_API_SECRET, "
                            + "SOLAPI_SENDER를 확인해주세요."
            );
        }
    }

    private String normalizePhone(
            String value
    ) {
        return value == null
                ? ""
                : value.replaceAll(
                        "\\D",
                        ""
                );
    }

    private boolean isBlank(String value) {
        return value == null
                || value.isBlank();
    }
}
