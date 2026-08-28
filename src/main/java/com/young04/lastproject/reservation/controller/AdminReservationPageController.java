package com.young04.lastproject.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminReservationPageController {

    @GetMapping("/admin/reservations")
    public String reservationList() {
        return "admin/reservation-list";
    }
}
