package com.young04.lastproject.reservation.controller;

import com.young04.lastproject.reservation.service.ReservationMemberReader;
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
    private final ReservationMemberReader reservationMemberReader;

    @GetMapping("/reservation")
    public String reservationForm(
            Principal principal,
            Model model
    ) {
        Long memberNo = resolveMemberNo(principal);

        model.addAttribute(
                "serviceMenus",
                serviceMenuReader.getActiveServiceMenus()
        );
        model.addAttribute("memberNo", memberNo);
        model.addAttribute(
                "isLoggedIn",
                memberNo != null
        );

        return "reservation/reservation-form";
    }

    @GetMapping("/my-reservations")
    public String myReservations(
            Principal principal,
            Model model
    ) {
        Long memberNo = resolveMemberNo(principal);

        model.addAttribute("memberNo", memberNo);
        model.addAttribute(
                "isLoggedIn",
                memberNo != null
        );

        return "reservation/my-reservations";
    }

    private Long resolveMemberNo(Principal principal) {
        if (principal == null) {
            return null;
        }

        return reservationMemberReader
                .findMemberNoByMemberId(
                        principal.getName()
                )
                .orElse(null);
    }
}
