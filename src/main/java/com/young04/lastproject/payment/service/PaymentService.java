package com.young04.lastproject.payment.service;

import com.young04.lastproject.payment.dto.*;
import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentStatus;
import com.young04.lastproject.payment.repository.PaymentRepository;
import com.young04.lastproject.reservation.entity.Reservation;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 관리자 매출 관리 화면 전체 데이터 조회
     */
    public PaymentPageDto getPaymentPage(
            LocalDate startDate,
            LocalDate endDate,
            PaymentTrendUnit unit
    ) {
        PaymentSummaryDto summary = getSummary();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        long periodTotal = getSalesAmount(start, end);

        List<PaymentTrendDto> trend = getTrend(
                startDate,
                endDate,
                start,
                end,
                unit
        );

        List<PopularServiceDto> popularServices =
                paymentRepository.findPopularServices(
                        PaymentStatus.PAID,
                        start,
                        end,
                        PageRequest.of(0, 5)
                );

        List<PaymentMethodSalesDto> paymentMethods =
                getPaymentMethodSales(
                        start,
                        end,
                        periodTotal
                );

        List<RecentPaymentDto> recentPayments =
                getRecentPayments();

        return new PaymentPageDto(
                summary,
                trend,
                periodTotal,
                popularServices,
                paymentMethods,
                recentPayments
        );
    }

    /**
     * 상단 매출 요약
     */
    private PaymentSummaryDto getSummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        long todaySales = getSalesAmount(todayStart, tomorrowStart);
        long yesterdaySales = getSalesAmount(yesterdayStart, todayStart);

        long todayCount = paymentRepository.countPayments(
                PaymentStatus.PAID,
                todayStart,
                tomorrowStart
        );

        long averagePaymentAmount =
                todayCount == 0 ? 0L : todaySales / todayCount;

        YearMonth currentMonth = YearMonth.from(today);

        LocalDateTime monthStart =
                currentMonth.atDay(1).atStartOfDay();

        LocalDateTime nextMonthStart =
                currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        YearMonth previousMonth =
                currentMonth.minusMonths(1);

        LocalDateTime previousMonthStart =
                previousMonth.atDay(1).atStartOfDay();

        long monthSales =
                getSalesAmount(
                        monthStart,
                        nextMonthStart
                );

        long previousMonthSales =
                getSalesAmount(
                        previousMonthStart,
                        monthStart
                );

        Double todayChangeRate =
                calculateChangeRate(
                        todaySales,
                        yesterdaySales
                );

        Double monthChangeRate =
                calculateChangeRate(
                        monthSales,
                        previousMonthSales
                );

        return new PaymentSummaryDto(
                todaySales,
                monthSales,
                todayCount,
                averagePaymentAmount,
                todayChangeRate,
                monthChangeRate
        );
    }

    /**
     * 기간 매출 합계
     */
    private long getSalesAmount(
            LocalDateTime start,
            LocalDateTime end
    ) {
        Long amount =
                paymentRepository.sumPaymentAmount(
                        PaymentStatus.PAID,
                        start,
                        end
                );

        return amount == null ? 0L : amount;
    }

    /**
     * 매출 추이 조회
     */
    private List<PaymentTrendDto> getTrend(
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime start,
            LocalDateTime end,
            PaymentTrendUnit unit
    ) {
        List<PaymentTrendDto> dbResult;

        switch (unit) {
            case MONTH ->
                    dbResult =
                            paymentRepository.findMonthlyTrend(
                                    PaymentStatus.PAID,
                                    start,
                                    end
                            );

            case YEAR ->
                    dbResult =
                            paymentRepository.findYearlyTrend(
                                    PaymentStatus.PAID,
                                    start,
                                    end
                            );

            default ->
                    dbResult =
                            paymentRepository.findDailyTrend(
                                    PaymentStatus.PAID,
                                    start,
                                    end
                            );
        }

        Map<String, Long> salesMap =
                dbResult.stream()
                        .collect(
                                Collectors.toMap(
                                        PaymentTrendDto::getPeriodKey,
                                        PaymentTrendDto::getAmount
                                )
                        );

        return fillEmptyPeriods(
                startDate,
                endDate,
                unit,
                salesMap
        );
    }

    /**
     * 결제가 없는 날짜도 그래프에 0원으로 표시
     */
    private List<PaymentTrendDto> fillEmptyPeriods(
            LocalDate startDate,
            LocalDate endDate,
            PaymentTrendUnit unit,
            Map<String, Long> salesMap
    ) {
        List<PaymentTrendDto> result =
                new ArrayList<>();

        if (unit == PaymentTrendUnit.DAY) {
            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {
                String key = current.toString();

                result.add(
                        new PaymentTrendDto(
                                key,
                                salesMap.getOrDefault(
                                        key,
                                        0L
                                )
                        )
                );

                current = current.plusDays(1);
            }

            return result;
        }

        if (unit == PaymentTrendUnit.MONTH) {
            YearMonth current =
                    YearMonth.from(startDate);

            YearMonth last =
                    YearMonth.from(endDate);

            while (!current.isAfter(last)) {
                String key = current.toString();

                result.add(
                        new PaymentTrendDto(
                                key,
                                salesMap.getOrDefault(
                                        key,
                                        0L
                                )
                        )
                );

                current = current.plusMonths(1);
            }

            return result;
        }

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        for (
                int year = startYear;
                year <= endYear;
                year++
        ) {
            String key = String.valueOf(year);

            result.add(
                    new PaymentTrendDto(
                            key,
                            salesMap.getOrDefault(
                                    key,
                                    0L
                            )
                    )
            );
        }

        return result;
    }

    /**
     * 결제수단별 매출
     */
    private List<PaymentMethodSalesDto> getPaymentMethodSales(
            LocalDateTime start,
            LocalDateTime end,
            long totalSales
    ) {
        List<PaymentMethodSalesDto> rows =
                paymentRepository.findPaymentMethodSales(
                        PaymentStatus.PAID,
                        start,
                        end
                );

        List<PaymentMethodSalesDto> result =
                new ArrayList<>();

        for (PaymentMethodSalesDto row : rows) {
            double percentage = 0.0;

            if (totalSales > 0) {
                percentage =
                        row.getSalesAmount()
                                * 100.0
                                / totalSales;
            }

            result.add(
                    new PaymentMethodSalesDto(
                            row.getPaymentMethod(),
                            row.getSalesAmount(),
                            row.getPaymentCount(),
                            percentage
                    )
            );
        }

        return result;
    }

    /**
     * 최근 결제 내역 5건
     */
    private List<RecentPaymentDto> getRecentPayments() {
        List<Payment> payments =
                paymentRepository.findRecentPayments(
                        List.of(
                                PaymentStatus.PAID,
                                PaymentStatus.REFUNDED
                        ),
                        PageRequest.of(0, 5)
                );

        List<RecentPaymentDto> result =
                new ArrayList<>();

        for (Payment payment : payments) {
            Reservation reservation =
                    payment.getReservation();

            String customerName;

            if (payment.getMember() != null) {
                customerName =
                        payment
                                .getMember()
                                .getName();

            } else if (
                    reservation.getGuestName() != null
                    && !reservation.getGuestName().isBlank()
            ) {
                customerName =
                        reservation.getGuestName();

            } else {
                customerName = "고객";
            }

            result.add(
                    new RecentPaymentDto(
                            payment.getPaidAt(),
                            customerName,
                            reservation.getServiceNameSnapshot(),
                            payment.getPaymentMethod(),
                            payment.getPaymentAmount(),
                            payment.getPaymentStatus()
                    )
            );
        }

        return result;
    }

    /**
     * 이전 기간 대비 증감률
     */
    private Double calculateChangeRate(
            long currentAmount,
            long previousAmount
    ) {
        if (previousAmount == 0) {
            return null;
        }

        return (
                (currentAmount - previousAmount)
                        * 100.0
        ) / previousAmount;
    }
}
