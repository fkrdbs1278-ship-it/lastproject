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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String dashboard(Model model) {

        // 실제 예약 현황
        var reservationSummary =
                reservationDashboardService.getSummary();

        model.addAttribute(
                "reservationSummary",
                reservationSummary
        );

        // 오늘 포함 최근 7일 결제 완료(PAID) 매출
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        var weeklyPaymentData = paymentService.getPaymentPage(
                weekStart,
                today,
                PaymentTrendUnit.DAY
        );

        // 상단 오늘 매출 / 오늘 결제 완료 건수
        model.addAttribute(
                "paymentSummary",
                weeklyPaymentData.getSummary()
        );

        // 최근 7일 총매출
        model.addAttribute(
                "weeklySalesTotal",
                weeklyPaymentData.getPeriodTotal()
        );

        // 최근 7일 그래프 날짜 라벨
        model.addAttribute(
                "weeklySalesLabels",
                weeklyPaymentData.getTrend()
                        .stream()
                        .map(PaymentTrendDto::getLabel)
                        .toList()
        );

        // 최근 7일 그래프 실제 매출 금액
        model.addAttribute(
                "weeklySalesAmounts",
                weeklyPaymentData.getTrend()
                        .stream()
                        .map(PaymentTrendDto::getAmount)
                        .toList()
        );

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
