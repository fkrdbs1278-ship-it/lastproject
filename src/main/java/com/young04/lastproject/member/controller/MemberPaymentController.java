package com.young04.lastproject.member.controller;

import com.young04.lastproject.global.security.CustomUserDetails;
import com.young04.lastproject.payment.dto.MemberPaymentHistoryResponse;
import com.young04.lastproject.payment.service.MemberPaymentHistoryService;
import com.young04.lastproject.payment.service.MemberPaymentHistoryService.PaymentHistorySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class MemberPaymentController {

    private final MemberPaymentHistoryService memberPaymentHistoryService;


    @GetMapping("/member/payments")
    public String paymentHistory(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {

        if (userDetails == null) {

            return "redirect:/member/login";
        }


        Long memberNo =
                userDetails.getMemberNo();


        Page<MemberPaymentHistoryResponse> paymentPage =
                memberPaymentHistoryService
                        .findMyPaymentHistory(
                                memberNo,
                                page,
                                10
                        );


        PaymentHistorySummary summary =
                memberPaymentHistoryService
                        .getSummary(
                                memberNo
                        );


        model.addAttribute(
                "payments",
                paymentPage.getContent()
        );

        model.addAttribute(
                "paymentPage",
                paymentPage
        );

        model.addAttribute(
                "paidCount",
                summary.paidCount()
        );

        model.addAttribute(
                "refundedCount",
                summary.refundedCount()
        );

        model.addAttribute(
                "totalPaidAmount",
                summary.totalPaidAmount()
        );


        return "member/payment-history";
    }
}
