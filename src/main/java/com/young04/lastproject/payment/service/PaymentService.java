package com.young04.lastproject.payment.service;

import com.young04.lastproject.customerprofile.service.CustomerCrmSyncService;
import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import com.young04.lastproject.payment.dto.*;
import com.young04.lastproject.payment.entity.Payment;
import com.young04.lastproject.payment.entity.PaymentMethod;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final MemberRepository memberRepository;

    private final CustomerCrmSyncService customerCrmSyncService;

    /**
     * 시술 완료 시 해당 예약의 PAYMENT가 없으면 UNPAID 1건을 생성합니다.
     * 이벤트를 다시 계산하지 않고 Reservation의 가격 snapshot을 그대로 사용합니다.
     */
    @Transactional
    public Payment createUnpaidPaymentIfAbsent(Reservation reservation) {
        if (reservation == null || reservation.getReservationNo() == null) {
            throw new IllegalArgumentException("저장된 예약 정보가 필요합니다.");
        }

        Optional<Payment> existing =
                paymentRepository.findByReservation_ReservationNo(
                        reservation.getReservationNo()
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        if (reservation.getOriginalPriceSnapshot() == null
                || reservation.getFinalPriceSnapshot() == null) {
            throw new IllegalStateException("예약 가격 snapshot이 없습니다.");
        }

        Member member = null;

        if (reservation.getMemberNo() != null) {
            member = memberRepository
                    .findById(reservation.getMemberNo())
                    .orElseThrow(
                            () -> new IllegalStateException(
                                    "예약에 연결된 회원을 찾을 수 없습니다."
                            )
                    );
        }

        Long originalAmount =
                reservation.getOriginalPriceSnapshot().longValue();

        Long discountAmount =
                reservation.getDiscountAmountSnapshot() == null
                        ? 0L
                        : reservation.getDiscountAmountSnapshot().longValue();

        Long paymentAmount =
                reservation.getFinalPriceSnapshot().longValue();

        Payment payment = Payment.createUnpaid(
                reservation,
                member,
                originalAmount,
                discountAmount,
                paymentAmount,
                null
        );

        return paymentRepository.save(payment);
    }

    /**
     * 예약 번호로 결제 정보를 조회합니다.
     */
    public Optional<ReservationPaymentResponse> findReservationPayment(
            Long reservationNo
    ) {
        return paymentRepository
                .findByReservation_ReservationNo(reservationNo)
                .map(ReservationPaymentResponse::from);
    }

    /**
     * 관리자 예약 화면에서 예약 번호 기준으로 결제 완료 처리합니다.
     */
    @Transactional
    public ReservationPaymentResponse completePaymentByReservationNo(
            Long reservationNo,
            PaymentMethod paymentMethod
    ) {
        if (paymentMethod == PaymentMethod.PREPAID) {
            throw new IllegalArgumentException(
                    "관리자 현장 결제는 카드, 현금, 계좌이체만 선택할 수 있습니다."
            );
        }

        Payment payment = paymentRepository
                .findByReservationNoForUpdate(reservationNo)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "해당 예약의 결제 대기 정보를 찾을 수 없습니다."
                        )
                );

        payment.pay(paymentMethod);

        customerCrmSyncService.synchronizePayment(
                payment
        );

        return ReservationPaymentResponse.from(payment);
    }

    /**
     * 관리자 결제 완료 처리
     */
    @Transactional
    public Payment completePayment(
            Long paymentNo,
            PaymentMethod paymentMethod
    ) {
        Payment payment = paymentRepository
                .findByIdForUpdate(paymentNo)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "결제 정보를 찾을 수 없습니다. paymentNo=" + paymentNo
                        )
                );

        payment.pay(paymentMethod);

        customerCrmSyncService.synchronizePayment(
                payment
        );

        return payment;
    }

    /**
     * 관리자 환불 처리
     */
    @Transactional
    public Payment refundPayment(Long paymentNo) {
        Payment payment = paymentRepository
                .findByIdForUpdate(paymentNo)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "결제 정보를 찾을 수 없습니다. paymentNo=" + paymentNo
                        )
                );

        payment.refund();

        customerCrmSyncService.synchronizePayment(
                payment
        );

        return payment;
    }

    /**
     * 관리자 매출 관리 화면 전체 데이터 조회
     */
    public PaymentPageDto getPaymentPage(
            LocalDate startDate,
            LocalDate endDate,
            PaymentTrendUnit unit
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 기간이 필요합니다.");
        }

        if (unit == null) {
            unit = PaymentTrendUnit.DAY;
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        List<Payment> periodPayments = findPaidPayments(start, endExclusive);
        long periodTotal = sumAmount(periodPayments);

        PaymentSummaryDto summary = getSummary();

        List<PaymentTrendDto> trend =
                buildTrend(startDate, endDate, unit, periodPayments);

        List<PopularServiceDto> popularServices =
                buildPopularServices(periodPayments);

        List<PaymentMethodSalesDto> paymentMethods =
                buildPaymentMethodSales(periodPayments, periodTotal);

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
     * 대시보드 인기 시술 TOP 5 전용 조회
     * 전체 결제 관리 화면 데이터를 만들지 않고 필요한 기간의 결제만 조회합니다.
     */
    public List<PopularServiceDto> getPopularServices(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 기간이 필요합니다.");
        }

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        return buildPopularServices(
                findPaidPayments(start, endExclusive)
        );
    }

    /**
     * 상단 요약 카드
     */
    private PaymentSummaryDto getSummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        List<Payment> todayPayments =
                findPaidPayments(todayStart, tomorrowStart);

        List<Payment> yesterdayPayments =
                findPaidPayments(yesterdayStart, todayStart);

        long todaySales = sumAmount(todayPayments);
        long yesterdaySales = sumAmount(yesterdayPayments);
        long todayPaymentCount = todayPayments.size();

        long averagePaymentAmount =
                todayPaymentCount == 0
                        ? 0L
                        : todaySales / todayPaymentCount;

        YearMonth currentMonth = YearMonth.from(today);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime currentMonthStart =
                currentMonth.atDay(1).atStartOfDay();

        LocalDateTime nextMonthStart =
                currentMonth.plusMonths(1)
                        .atDay(1)
                        .atStartOfDay();

        LocalDateTime previousMonthStart =
                previousMonth.atDay(1).atStartOfDay();

        long monthSales =
                sumAmount(
                        findPaidPayments(
                                currentMonthStart,
                                nextMonthStart
                        )
                );

        long previousMonthSales =
                sumAmount(
                        findPaidPayments(
                                previousMonthStart,
                                currentMonthStart
                        )
                );

        return new PaymentSummaryDto(
                todaySales,
                monthSales,
                todayPaymentCount,
                averagePaymentAmount,
                calculateChangeRate(todaySales, yesterdaySales),
                calculateChangeRate(monthSales, previousMonthSales)
        );
    }

    /**
     * PAID 상태만 매출로 조회
     */
    private List<Payment> findPaidPayments(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return paymentRepository
                .findByPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtAsc(
                        PaymentStatus.PAID,
                        start,
                        end
                );
    }

    /**
     * 결제 금액 합계
     */
    private long sumAmount(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getPaymentAmount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * 일/월/연도별 그래프 데이터 생성
     */
    private List<PaymentTrendDto> buildTrend(
            LocalDate startDate,
            LocalDate endDate,
            PaymentTrendUnit unit,
            List<Payment> payments
    ) {
        Map<String, Long> grouped = new HashMap<>();

        for (Payment payment : payments) {
            if (payment.getPaidAt() == null) {
                continue;
            }

            String key = toPeriodKey(payment.getPaidAt(), unit);

            grouped.merge(
                    key,
                    payment.getPaymentAmount() == null
                            ? 0L
                            : payment.getPaymentAmount(),
                    Long::sum
            );
        }

        return fillEmptyPeriods(
                startDate,
                endDate,
                unit,
                grouped
        );
    }

    /**
     * 그래프 묶음 기준 키
     */
    private String toPeriodKey(
            LocalDateTime paidAt,
            PaymentTrendUnit unit
    ) {
        return switch (unit) {
            case MONTH ->
                    YearMonth.from(paidAt).toString();

            case YEAR ->
                    String.valueOf(paidAt.getYear());

            case DAY ->
                    paidAt.toLocalDate().toString();
        };
    }

    /**
     * 매출이 없는 기간도 0원으로 채우기
     */
    private List<PaymentTrendDto> fillEmptyPeriods(
            LocalDate startDate,
            LocalDate endDate,
            PaymentTrendUnit unit,
            Map<String, Long> grouped
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
                                grouped.getOrDefault(key, 0L)
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
                                grouped.getOrDefault(key, 0L)
                        )
                );

                current = current.plusMonths(1);
            }

            return result;
        }

        for (
                int year = startDate.getYear();
                year <= endDate.getYear();
                year++
        ) {
            String key = String.valueOf(year);

            result.add(
                    new PaymentTrendDto(
                            key,
                            grouped.getOrDefault(key, 0L)
                    )
            );
        }

        return result;
    }

    /**
     * 인기 시술 TOP 5
     */
    private List<PopularServiceDto> buildPopularServices(
            List<Payment> payments
    ) {
        class ServiceStat {
            long count;
            long amount;
        }

        Map<String, ServiceStat> stats =
                new HashMap<>();

        for (Payment payment : payments) {
            Reservation reservation =
                    payment.getReservation();

            if (reservation == null) {
                continue;
            }

            String serviceName =
                    reservation.getServiceNameSnapshot();

            if (serviceName == null || serviceName.isBlank()) {
                serviceName = "시술명 없음";
            }

            ServiceStat stat =
                    stats.computeIfAbsent(
                            serviceName,
                            key -> new ServiceStat()
                    );

            stat.count++;

            if (payment.getPaymentAmount() != null) {
                stat.amount += payment.getPaymentAmount();
            }
        }

        return stats.entrySet()
                .stream()
                .map(entry ->
                        new PopularServiceDto(
                                entry.getKey(),
                                entry.getValue().count,
                                entry.getValue().amount
                        )
                )
                .sorted(
                        Comparator.comparingLong(
                                        PopularServiceDto::getPaymentCount
                                )
                                .reversed()
                                .thenComparing(
                                        Comparator.comparingLong(
                                                        PopularServiceDto::getSalesAmount
                                                )
                                                .reversed()
                                )
                )
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 결제 수단별 매출
     */
    private List<PaymentMethodSalesDto> buildPaymentMethodSales(
            List<Payment> payments,
            long periodTotal
    ) {
        class MethodStat {
            long count;
            long amount;
        }

        Map<PaymentMethod, MethodStat> stats =
                new EnumMap<>(PaymentMethod.class);

        for (Payment payment : payments) {
            PaymentMethod method =
                    payment.getPaymentMethod();

            if (method == null) {
                continue;
            }

            MethodStat stat =
                    stats.computeIfAbsent(
                            method,
                            key -> new MethodStat()
                    );

            stat.count++;

            if (payment.getPaymentAmount() != null) {
                stat.amount += payment.getPaymentAmount();
            }
        }

        return stats.entrySet()
                .stream()
                .map(entry -> {
                    long amount =
                            entry.getValue().amount;

                    double percentage =
                            periodTotal == 0
                                    ? 0.0
                                    : amount * 100.0 / periodTotal;

                    return new PaymentMethodSalesDto(
                            entry.getKey(),
                            amount,
                            entry.getValue().count,
                            percentage
                    );
                })
                .sorted(
                        Comparator.comparingLong(
                                        PaymentMethodSalesDto::getSalesAmount
                                )
                                .reversed()
                )
                .collect(Collectors.toList());
    }

    /**
     * 최근 결제/환불 5건
     */
    private List<RecentPaymentDto> getRecentPayments() {
        List<Payment> payments =
                paymentRepository
                        .findByPaymentStatusInOrderByPaidAtDesc(
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

            String customerName = "고객";
            String serviceName = "-";

            if (
                    payment.getMember() != null
                    && payment.getMember().getName() != null
                    && !payment.getMember().getName().isBlank()
            ) {
                customerName =
                        payment.getMember().getName();

            } else if (
                    reservation != null
                    && reservation.getGuestName() != null
                    && !reservation.getGuestName().isBlank()
            ) {
                customerName =
                        reservation.getGuestName();
            }

            if (
                    reservation != null
                    && reservation.getServiceNameSnapshot() != null
                    && !reservation.getServiceNameSnapshot().isBlank()
            ) {
                serviceName =
                        reservation.getServiceNameSnapshot();
            }

            result.add(
                    new RecentPaymentDto(
                            payment.getPaidAt(),
                            customerName,
                            serviceName,
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
