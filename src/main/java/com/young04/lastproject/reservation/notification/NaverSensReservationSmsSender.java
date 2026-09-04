package com.young04.lastproject.reservation.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "reservation.sms",
        name = "provider",
        havingValue = "naver-sens"
)
public class NaverSensReservationSmsSender
        implements ReservationSmsSender {

    private static final String HOST =
            "https://sens.apigw.ntruss.com";

    private final ObjectMapper objectMapper;

    @Value("${reservation.sms.naver-sens.service-id:}")
    private String serviceId;

    @Value("${reservation.sms.naver-sens.access-key:}")
    private String accessKey;

    @Value("${reservation.sms.naver-sens.secret-key:}")
    private String secretKey;

    @Value("${reservation.sms.naver-sens.sender:}")
    private String sender;

    @Override
    public void send(
            String to,
            String subject,
            String content
    ) {
        validateConfiguration();

        try {
            String normalizedTo =
                    normalizePhone(to);

            String normalizedSender =
                    normalizePhone(sender);

            String uri =
                    "/sms/v2/services/"
                            + serviceId
                            + "/messages";

            String timestamp =
                    String.valueOf(
                            System.currentTimeMillis()
                    );

            String signature =
                    createSignature(
                            "POST",
                            uri,
                            timestamp
                    );

            Map<String, Object> requestBody =
                    Map.of(
                            "type", "LMS",
                            "contentType", "COMM",
                            "countryCode", "82",
                            "from", normalizedSender,
                            "subject", subject,
                            "content", content,
                            "messages",
                            List.of(
                                    Map.of(
                                            "to",
                                            normalizedTo
                                    )
                            )
                    );

            String json =
                    objectMapper
                            .writeValueAsString(
                                    requestBody
                            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            HOST + uri
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(10)
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "x-ncp-apigw-timestamp",
                                    timestamp
                            )
                            .header(
                                    "x-ncp-iam-access-key",
                                    accessKey
                            )
                            .header(
                                    "x-ncp-apigw-signature-v2",
                                    signature
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    json,
                                                    StandardCharsets.UTF_8
                                            )
                            )
                            .build();

            HttpResponse<String> response =
                    HttpClient.newBuilder()
                            .connectTimeout(
                                    Duration.ofSeconds(5)
                            )
                            .build()
                            .send(
                                    request,
                                    HttpResponse.BodyHandlers
                                            .ofString(
                                                    StandardCharsets.UTF_8
                                            )
                            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "NAVER SENS 문자 발송 실패. status="
                                + response.statusCode()
                                + ", body="
                                + response.body()
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "NAVER SENS 문자 발송 중 인터럽트가 발생했습니다.",
                    e
            );
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }

            throw new IllegalStateException(
                    "NAVER SENS 문자 발송에 실패했습니다.",
                    e
            );
        }
    }

    private String createSignature(
            String method,
            String uri,
            String timestamp
    ) throws Exception {
        String message =
                method
                        + " "
                        + uri
                        + "\n"
                        + timestamp
                        + "\n"
                        + accessKey;

        SecretKeySpec signingKey =
                new SecretKeySpec(
                        secretKey.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );

        Mac mac =
                Mac.getInstance(
                        "HmacSHA256"
                );

        mac.init(signingKey);

        byte[] rawHmac =
                mac.doFinal(
                        message.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return Base64.getEncoder()
                .encodeToString(rawHmac);
    }

    private void validateConfiguration() {
        if (isBlank(serviceId)
                || isBlank(accessKey)
                || isBlank(secretKey)
                || isBlank(sender)) {
            throw new IllegalStateException(
                    "NAVER SENS 설정값이 비어 있습니다. "
                            + "SERVICE_ID, ACCESS_KEY, SECRET_KEY, SENDER를 확인해주세요."
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

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.isBlank();
    }
}
