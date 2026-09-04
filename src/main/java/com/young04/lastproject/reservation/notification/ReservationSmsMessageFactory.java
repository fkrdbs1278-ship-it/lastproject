package com.young04.lastproject.reservation.notification;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ReservationSmsMessageFactory {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String subject(
            ReservationNotificationEvent event
    ) {
        return switch (event.type()) {
            case CREATED -> "미용실 예약 접수";
            case CONFIRMED -> "미용실 예약 확정";
            case UPDATED -> "미용실 예약 변경";
            case CANCELED -> "미용실 예약 취소";
        };
    }

    public String content(
            ReservationNotificationEvent event
    ) {
        String reservationTime =
                event.startAt() == null
                        ? "-"
                        : event.startAt().format(DATE_TIME_FORMAT);

        StringBuilder message =
                new StringBuilder()
                        .append("[미용실 예약]\n")
                        .append(statusLine(event.type()))
                        .append("\n")
                        .append("예약번호: ")
                        .append(event.reservationNo())
                        .append("\n")
                        .append("시술: ")
                        .append(nullToDash(event.serviceName()))
                        .append("\n")
                        .append("예약일시: ")
                        .append(reservationTime);

        if (event.type()
                == ReservationNotificationType.CANCELED) {
            message.append("\n취소사유: ")
                    .append(
                            nullToDash(
                                    event.cancelReason()
                            )
                    );
        }

        message.append("\n예약 조회 시 예약번호와 휴대전화 번호를 입력해주세요.");

        return message.toString();
    }

    private String statusLine(
            ReservationNotificationType type
    ) {
        return switch (type) {
            case CREATED -> "예약이 접수되었습니다.";
            case CONFIRMED -> "예약이 확정되었습니다.";
            case UPDATED -> "예약 정보가 변경되었습니다.";
            case CANCELED -> "예약이 취소되었습니다.";
        };
    }

    private String nullToDash(
            String value
    ) {
        return value == null
                || value.isBlank()
                ? "-"
                : value;
    }
}
