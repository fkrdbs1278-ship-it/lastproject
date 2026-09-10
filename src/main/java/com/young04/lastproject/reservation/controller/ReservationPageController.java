package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import com.young04.lastproject.reservation.dto.HairStyleOptionResponse;
import com.young04.lastproject.reservation.service.HairStyleReader;
import com.young04.lastproject.reservation.service.ReservationMemberReader;
import com.young04.lastproject.reservation.service.SalonEventReader;
import com.young04.lastproject.reservation.service.ServiceMenuReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ReservationPageController {

    private final ServiceMenuReader serviceMenuReader;
    private final SalonEventReader salonEventReader;
    private final ReservationMemberReader reservationMemberReader;
    private final HairStyleReader hairStyleReader;

    @GetMapping("/reservation")
    public String reservationForm(
            @RequestParam(
                    name = "hairStyleNo",
                    required = false
            )
            Long hairStyleNo,

            @RequestParam(
                    name = "serviceMenuNo",
                    required = false
            )
            Long serviceMenuNo,

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

        HairStyleOptionResponse selectedHairStyle =
                hairStyleReader
                        .findActiveById(hairStyleNo)
                        .orElse(null);

        var linkedServiceMenuNos =
                selectedHairStyle == null
                        ? java.util.List.<Long>of()
                        : hairStyleReader
                                .getActiveServiceMenuNosForStyle(
                                        selectedHairStyle
                                                .getHairStyleNo()
                                );

        Long preferredServiceMenuNo =
                serviceMenuNo != null
                        && linkedServiceMenuNos
                                .contains(serviceMenuNo)
                        ? serviceMenuNo
                        : null;

        model.addAttribute(
                "selectedHairStyle",
                selectedHairStyle
        );

        model.addAttribute(
                "linkedServiceMenuNos",
                linkedServiceMenuNos
        );

        model.addAttribute(
                "preferredServiceMenuNo",
                preferredServiceMenuNo
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
