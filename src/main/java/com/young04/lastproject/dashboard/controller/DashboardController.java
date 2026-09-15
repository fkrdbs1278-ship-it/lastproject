package com.young04.lastproject.dashboard.controller;

import com.young04.lastproject.material.service.MaterialService;
import com.young04.lastproject.payment.dto.PaymentTrendDto;
import com.young04.lastproject.payment.dto.PaymentTrendUnit;
import com.young04.lastproject.payment.service.PaymentService;
import com.young04.lastproject.reservation.service.ReservationDashboardService;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorderitem.service.PurchaseOrderItemService;
import com.young04.lastproject.salonevent.service.SalonEventService;
import com.young04.lastproject.dashboard.repository.DashboardStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 관리자 대시보드 화면과 요약 정보를 처리하는 Controller
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MaterialService materialService;

    // 발주 상태별 건수 조회를 담당하는 Repository
    private final PurchaseOrderRepository purchaseOrderRepository;

    // 발주서에 포함된 자재 품목 조회를 담당하는 Service
    private final PurchaseOrderItemService purchaseOrderItemService;

    // 대시보드에 표시할 진행 중 이벤트 조회를 담당하는 Service
    private final SalonEventService salonEventService;

    // 대시보드 통계 조회
    private final DashboardStatisticsRepository dashboardStatisticsRepository;

    // 예약 도메인의 실제 예약 현황 조회
    private final ReservationDashboardService reservationDashboardService;

    // 결제 완료(PAID) 기준 실제 매출 조회
    private final PaymentService paymentService;

    // 관리자 대시보드 조회
    @GetMapping("/admin/dashboard")
    public String dashboard(
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

        // 실제 예약 현황
        var reservationSummary =
                reservationDashboardService.getSummary();

        model.addAttribute(
                "reservationSummary",
                reservationSummary
        );

        // 대시보드 매출 그래프는 처음 접속하면 오늘 포함 최근 7일을 표시
        LocalDate today = LocalDate.now();

        if (endDate == null) {
            endDate = today;
        }

        if (startDate == null) {
            startDate = endDate.minusDays(6);
        }

        // 시작일과 종료일이 반대로 입력되면 자동 교환
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        PaymentTrendUnit trendUnit =
                PaymentTrendUnit.from(unit);

        // 일별 조회는 최대 7일로 유지
        // 종료일이 7일 범위를 넘으면 시작일을 종료일 기준 6일 전으로 자동 이동
        if (
                trendUnit == PaymentTrendUnit.DAY
                        && startDate.plusDays(6).isBefore(endDate)
        ) {
            startDate = endDate.minusDays(6);
        }

        var salesPaymentData = paymentService.getPaymentPage(
                startDate,
                endDate,
                trendUnit
        );

        // 상단 오늘 매출 / 오늘 결제 완료 건수
        model.addAttribute(
                "paymentSummary",
                salesPaymentData.getSummary()
        );

        // 조회 기간 총매출
        model.addAttribute(
                "salesPeriodTotal",
                salesPaymentData.getPeriodTotal()
        );

        // 조회 기간 그래프 라벨
        model.addAttribute(
                "salesTrendLabels",
                salesPaymentData.getTrend()
                        .stream()
                        .map(PaymentTrendDto::getLabel)
                        .toList()
        );

        // 조회 기간 그래프 실제 매출 금액
        model.addAttribute(
                "salesTrendAmounts",
                salesPaymentData.getTrend()
                        .stream()
                        .map(PaymentTrendDto::getAmount)
                        .toList()
        );

        model.addAttribute("salesStartDate", startDate);
        model.addAttribute("salesEndDate", endDate);
        model.addAttribute("salesTrendUnit", trendUnit.name());

        // 이번 달 결제 완료(PAID) 기준 인기 시술 TOP 5
        var monthlyPaymentData = paymentService.getPaymentPage(
                today.withDayOfMonth(1),
                today,
                PaymentTrendUnit.DAY
        );

        model.addAttribute(
                "popularServices",
                monthlyPaymentData.getPopularServices()
        );

        // 이번 달 방문 고객 수
        model.addAttribute(
                "monthlyVisitCount",
                dashboardStatisticsRepository.countMonthlyVisitCustomers()
        );

        // 재고 부족 자재 전체 개수
        model.addAttribute(
                "lowStockCount",
                materialService.countLowStockMaterials()
        );

        // 재고 부족 자재 중 최대 5개를 대시보드에 표시
        model.addAttribute(
                "lowStockMaterials",
                materialService.getLowStockMaterials()
                        .stream()
                        .limit(5)
                        .toList()
        );

        // 발주 완료 후 아직 입고되지 않은 발주서 개수
        model.addAttribute(
                "pendingReceiptCount",
                purchaseOrderRepository.countByOrderStatus("ORDERED")
        );

        // 입고 예정일이 가까운 발주서 중 최대 5개 조회
        List<PurchaseOrder> pendingReceiptOrders = purchaseOrderRepository
                .findTop5ByOrderStatusOrderByExpectedDateAscOrderDateAsc(
                        "ORDERED"
                );

        model.addAttribute("pendingReceiptOrders", pendingReceiptOrders);

        // 각 발주서에 포함된 대표 자재 이름을 화면으로 전달
        Map<Long, List<String>> pendingReceiptMaterialNames =
                new LinkedHashMap<>();

        for (PurchaseOrder order : pendingReceiptOrders) {
            pendingReceiptMaterialNames.put(
                    order.getPurchaseOrderNo(),
                    purchaseOrderItemService.getMaterialNames(
                            order.getPurchaseOrderNo()
                    )
            );
        }

        model.addAttribute(
                "pendingReceiptMaterialNames",
                pendingReceiptMaterialNames
        );

        // 현재 진행 중이며 노출 중인 이벤트를 종료일이 가까운 순서로 최대 2개 표시
        model.addAttribute(
                "activeEvents",
                salonEventService.getOngoingEventsForDashboard()
        );

        return "admin/dashboard";
    }
}