package com.young04.lastproject.payment.controller;

import com.young04.lastproject.payment.dto.PaymentPageDto;
import com.young04.lastproject.payment.dto.PaymentTrendUnit;
import com.young04.lastproject.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 관리자 결제 기반 매출 관리 화면
     */
    @GetMapping
    public String payment(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "DAY")
            String unit,

            Model model
    ) {

        LocalDate today = LocalDate.now();

        PaymentTrendUnit trendUnit =
                PaymentTrendUnit.from(unit);


        /*
         * 날짜가 둘 다 없으면
         * 오늘을 포함한 최근 7일을 기본 조회합니다.
         */
        if (startDate == null && endDate == null) {

            endDate = today;
            startDate = today.minusDays(6);
        }


        /*
         * 시작일만 전달된 경우
         */
        else if (startDate != null && endDate == null) {

            if (trendUnit == PaymentTrendUnit.DAY) {

                // 일별은 시작일 포함 7일
                endDate = startDate.plusDays(6);

            } else {

                endDate = today;
            }
        }


        /*
         * 종료일만 전달된 경우
         */
        else if (startDate == null) {

            if (trendUnit == PaymentTrendUnit.DAY) {

                // 일별은 종료일 포함 이전 7일
                startDate = endDate.minusDays(6);

            } else {

                startDate = endDate.minusDays(6);
            }
        }


        /*
         * 날짜 순서가 반대이면 교환
         */
        if (startDate.isAfter(endDate)) {

            LocalDate temp = startDate;

            startDate = endDate;
            endDate = temp;
        }


        /*
         * 일별 조회는 항상 정확히 7일로 맞춥니다.
         *
         * 정상 범위:
         * 9/13 ~ 9/19
         *
         * 사용자가 URL 등을 직접 수정해서
         * 10일, 20일 범위를 넘겨도 서버에서
         * 종료일 기준 최근 7일로 다시 맞춥니다.
         */
        if (trendUnit == PaymentTrendUnit.DAY) {

            long days =
                    ChronoUnit.DAYS.between(
                            startDate,
                            endDate
                    );

            if (days != 6) {

                startDate =
                        endDate.minusDays(6);
            }
        }


        PaymentPageDto paymentData =
                paymentService.getPaymentPage(
                        startDate,
                        endDate,
                        trendUnit
                );


        model.addAttribute(
                "paymentData",
                paymentData
        );

        model.addAttribute(
                "startDate",
                startDate
        );

        model.addAttribute(
                "endDate",
                endDate
        );

        model.addAttribute(
                "trendUnit",
                trendUnit.name()
        );


        /*
         * 실제 HTML 위치
         * templates/payment/list.html
         */
        return "payment/list";
    }
}