package com.young04.lastproject.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    //사이드바의 매출 관리 주소로 결제 기반 매출 화면 조회
    @GetMapping("/admin/payment")
    public String payment() {
        return "payment/list";
    }
}
