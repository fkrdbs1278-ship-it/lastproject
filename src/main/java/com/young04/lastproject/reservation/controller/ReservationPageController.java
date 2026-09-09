package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import com.young04.lastproject.reservation.service.SalonEventReader;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ReservationPageController {

    private final ServiceMenuReader serviceMenuReader;
    private final SalonEventReader salonEventReader;
    private final ReservationMemberReader reservationMemberReader;

    @GetMapping("/reservation")
    public String reservationForm(
            Principal principal,
            Model model
    ) {
        MemberReservationInfo member =
                resolveMember(principal);

        model.addAttribute(
                "ongoingEvents",
                salonEventReader.getOngoingEvents()
        );

        model.addAttribute(
                "serviceMenus",
                serviceMenuReader.getActiveServiceMenus()
        );

        model.addAttribute(
                "memberNo",
                member == null ? null : member.getMemberNo()
        );

        model.addAttribute(
                "memberName",
                member == null ? null : member.getName()
        );

        model.addAttribute(
                "memberPhoneMasked",
                member == null ? null : member.getMaskedPhone()
        );

        model.addAttribute(
                "isLoggedIn",
                member != null
        );

        return "reservation/reservation-form";
    }

    @GetMapping("/my-reservations")
    public String myReservations(
            Principal principal,
            Model model
    ) {
        MemberReservationInfo member =
                resolveMember(principal);

        model.addAttribute(
                "memberNo",
                member == null ? null : member.getMemberNo()
        );

        model.addAttribute(
                "isLoggedIn",
                member != null
        );

        return "reservation/my-reservations";
    }

    @GetMapping("/guest-reservation")
    public String guestReservationLookup() {
        return "reservation/guest-reservation";
    }

    private MemberReservationInfo resolveMember(
            Principal principal
    ) {
        if (principal == null) {
            return null;
        }

        return reservationMemberReader
                .findMemberInfoByMemberId(
                        principal.getName()
                )
                .orElse(null);
    }
}
