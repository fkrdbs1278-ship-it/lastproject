package com.young04.lastproject.reservation.dto;

import com.young04.lastproject.payment.entity.PaymentStatus;
import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReservationListItemResponse {

    private ReservationResponse reservation;

    private String customerName;

    private String maskedPhone;

    private PaymentStatus paymentStatus;


    public static AdminReservationListItemResponse from(
            Reservation reservation,
            MemberReservationInfo member,
            PaymentStatus paymentStatus
    ) {

        String customerName =
                resolveCustomerName(
                        reservation,
                        member
                );

        String maskedPhone =
                resolveMaskedPhone(
                        reservation,
                        member
                );


        return AdminReservationListItemResponse
                .builder()
                .reservation(
                        ReservationResponse.from(
                                reservation
                        )
                )
                .customerName(
                        customerName
                )
                .maskedPhone(
                        maskedPhone
                )
                .paymentStatus(
                        paymentStatus
                )
                .build();
    }


    private static String resolveCustomerName(
            Reservation reservation,
            MemberReservationInfo member
    ) {

        if (reservation.getCustomerType() == CustomerType.MEMBER) {

            if (
                    member != null
                    && member.getName() != null
                    && !member.getName().isBlank()
            ) {

                return member.getName();
            }


            if (reservation.getMemberNo() != null) {

                return "회원 #"
                        + reservation.getMemberNo();
            }


            return "회원";
        }


        if (
                reservation.getGuestName() != null
                && !reservation.getGuestName().isBlank()
        ) {

            return reservation.getGuestName();
        }


        return "비회원";
    }


    private static String resolveMaskedPhone(
            Reservation reservation,
            MemberReservationInfo member
    ) {

        if (reservation.getCustomerType() == CustomerType.MEMBER) {

            if (
                    member != null
                    && member.getMaskedPhone() != null
                    && !member.getMaskedPhone().isBlank()
            ) {

                return member.getMaskedPhone();
            }


            return "-";
        }


        return maskPhone(
                reservation.getGuestPhone()
        );
    }


    private static String maskPhone(
            String phone
    ) {

        if (
                phone == null
                || phone.isBlank()
        ) {

            return "-";
        }


        String digits =
                phone.replaceAll(
                        "\\D",
                        ""
                );


        if (digits.length() == 11) {

            return digits.substring(
                    0,
                    3
            )
                    + "-****-"
                    + digits.substring(7);
        }


        if (digits.length() == 10) {

            return digits.substring(
                    0,
                    3
            )
                    + "-***-"
                    + digits.substring(6);
        }


        return phone;
    }
}
