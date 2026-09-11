package com.young04.lastproject.member.controller;

import com.young04.lastproject.global.security.CustomUserDetails;
import com.young04.lastproject.payment.dto.MemberPaymentHistoryResponse;
import com.young04.lastproject.payment.service.MemberPaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberPaymentController {

    private final MemberPaymentHistoryService memberPaymentHistoryService;


    @GetMapping("/member/payments")
    public String paymentHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/member/login";
        }


        List<MemberPaymentHistoryResponse> payments =
                memberPaymentHistoryService
                        .findMyPaymentHistory(
                                userDetails.getMemberNo()
                        );


        long paidCount =
                payments.stream()
                        .filter(payment ->
                                "PAID".equals(
                                        payment.getPaymentStatus()
                                )
                        )
                        .count();


        long refundedCount =
                payments.stream()
                        .filter(payment ->
                                "REFUNDED".equals(
                                        payment.getPaymentStatus()
                                )
                        )
                        .count();


        long totalPaidAmount =
                memberPaymentHistoryService
                        .calculateTotalPaidAmount(
                                payments
                        );


        model.addAttribute(
                "payments",
                payments
        );

        model.addAttribute(
                "paidCount",
                paidCount
        );

        model.addAttribute(
                "refundedCount",
                refundedCount
        );

        model.addAttribute(
                "totalPaidAmount",
                totalPaidAmount
        );


        return "member/payment-history";
    }
}
