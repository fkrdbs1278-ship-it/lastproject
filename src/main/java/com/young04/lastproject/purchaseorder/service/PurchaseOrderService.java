package com.young04.lastproject.purchaseorder.service;

import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderReceiveRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import com.young04.lastproject.purchaseorderitem.entity.PurchaseOrderItem;
import com.young04.lastproject.purchaseorderitem.repository.PurchaseOrderItemRepository;
import com.young04.lastproject.stockhistory.entity.StockHistory;
import com.young04.lastproject.stockhistory.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 발주서 조회와 등록 등의 업무 처리를 담당하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    // PURCHASE_ORDER 테이블에 접근하는 Repository
    private final PurchaseOrderRepository purchaseOrderRepository;

    // 발주서에 품목이 등록되어 있는지 확인하는 Repository
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    // 발주 입고에 따른 재고 변동 이력을 저장하는 Service
    private final StockHistoryService stockHistoryService;

    // 전체 발주서를 발주일 최신순으로 조회
    public List<PurchaseOrderResponse> getAllOrders() {
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    // 선택한 발주 상태의 발주서를 최신순으로 조회
    public List<PurchaseOrderResponse> getOrdersByStatus(
            String orderStatus
    ) {
        return purchaseOrderRepository
                .findByOrderStatusOrderByOrderDateDesc(orderStatus)
                .stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    // 공급업체 이름으로 발주서를 검색
    public List<PurchaseOrderResponse> searchOrders(
            String supplierName
    ) {
        return purchaseOrderRepository
                .findBySupplierNameContainingIgnoreCaseOrderByOrderDateDesc(
                        supplierName
                )
                .stream()
                .map(PurchaseOrderResponse::from)
                .toList();
    }

    // 검색 조건에 맞는 관리자 발주서 목록을 페이징 조회
    public Page<PurchaseOrderResponse> getOrderPage(
            String orderStatus,
            String supplierName,
            Pageable pageable
    ) {
        // 업체명이 입력되면 업체명으로 검색
        if (supplierName != null && !supplierName.isBlank()) {
            return purchaseOrderRepository
                    .findBySupplierNameContainingIgnoreCaseOrderByOrderDateDesc(
                            supplierName.trim(),
                            pageable
                    )
                    .map(PurchaseOrderResponse::from);
        }

        // 발주 상태가 선택되면 해당 상태만 조회
        if (orderStatus != null && !orderStatus.isBlank()) {
            return purchaseOrderRepository
                    .findByOrderStatusOrderByOrderDateDesc(
                            orderStatus,
                            pageable
                    )
                    .map(PurchaseOrderResponse::from);
        }

        // 검색 조건이 없으면 전체 발주서를 조회
        return purchaseOrderRepository
                .findAllByOrderByOrderDateDesc(pageable)
                .map(PurchaseOrderResponse::from);
    }

    // 발주 검색 자동완성에서 사용할 업체명을 중복 없이 조회
    public List<String> getSupplierNameSuggestions() {
        return purchaseOrderRepository
                .findAllByOrderBySupplierNameAsc()
                .stream()
                .map(PurchaseOrder::getSupplierName)
                .filter(supplierName ->
                        supplierName != null
                                && !supplierName.isBlank()
                )
                .distinct()
                .toList();
    }

    // 발주서 번호로 발주서 상세 정보 조회
    public PurchaseOrderResponse getOrder(Long purchaseOrderNo) {
        PurchaseOrder purchaseOrder = findOrder(purchaseOrderNo);

        return PurchaseOrderResponse.from(purchaseOrder);
    }

    // 새로운 발주서 등록
    @Transactional
    public Long createOrder(PurchaseOrderRequest request) {

        // 화면에서 받은 값으로 새로운 발주서 Entity 생성
        PurchaseOrder purchaseOrder = new PurchaseOrder();

        // 공급업체 이름 저장
        purchaseOrder.setSupplierName(request.getSupplierName());

        // 입고 예정일 저장
        purchaseOrder.setExpectedDate(request.getExpectedDate());

        // 발주 관련 메모 저장
        purchaseOrder.setMemo(request.getMemo());

        // 발주 상태를 발주 요청 상태로 지정
        purchaseOrder.setOrderStatus("REQUESTED");

        // 아직 발주 품목이 없으므로 총금액을 0원으로 지정
        purchaseOrder.setTotalAmount(0L);

        // 완성된 발주서 Entity를 DB에 저장
        PurchaseOrder savedOrder =
                purchaseOrderRepository.save(purchaseOrder);

        // 저장 후 생성된 발주서 번호 반환
        return savedOrder.getPurchaseOrderNo();
    }

    // 공급업체가 발주 요청을 확인하고 출고 완료 상태로 변경
    @Transactional
    public void processShipment(Long purchaseOrderNo) {

        // 상태를 변경할 발주서 조회
        PurchaseOrder purchaseOrder = findOrder(purchaseOrderNo);

        // 발주 요청 상태에서만 출고 처리 가능
        if (!"REQUESTED".equals(purchaseOrder.getOrderStatus())) {
            throw new IllegalStateException(
                    "발주 요청 상태에서만 출고 처리할 수 있습니다."
            );
        }

        // 해당 발주서에 등록된 품목 조회
        boolean hasItems = !purchaseOrderItemRepository
                .findByPurchaseOrder_PurchaseOrderNoOrderByPurchaseOrderItemNoAsc(
                        purchaseOrderNo
                )
                .isEmpty();

        // 품목이 없는 빈 발주서는 출고할 수 없음
        if (!hasItems) {
            throw new IllegalStateException(
                    "발주 품목이 없는 발주서는 출고 처리할 수 없습니다."
            );
        }

        // 공급업체 출고가 완료된 상태로 변경
        purchaseOrder.setOrderStatus("ORDERED");
    }

    // 공급업체가 발주 요청을 거절 상태로 변경
    @Transactional
    public void rejectOrder(Long purchaseOrderNo) {

        // 상태를 변경할 발주서 조회
        PurchaseOrder purchaseOrder = findOrder(purchaseOrderNo);

        // 아직 처리되지 않은 발주 요청만 거절 가능
        if (!"REQUESTED".equals(purchaseOrder.getOrderStatus())) {
            throw new IllegalStateException(
                    "발주 요청 상태에서만 거절할 수 있습니다."
            );
        }

        // 기존 취소 상태를 공급업체 거절 상태로 함께 사용
        purchaseOrder.setOrderStatus("CANCELED");
    }

    // 출고된 발주 품목을 입고하고 자재 재고와 이력을 함께 반영
    @Transactional
    public void receiveOrder(
            Long purchaseOrderNo,
            PurchaseOrderReceiveRequest request
    ) {

        // 입고 처리할 발주서 조회
        PurchaseOrder purchaseOrder = findOrder(purchaseOrderNo);

        // 공급업체에서 출고 완료된 발주서만 입고 가능
        if (!"ORDERED".equals(purchaseOrder.getOrderStatus())) {
            throw new IllegalStateException(
                    "발주 완료 상태에서만 입고 처리할 수 있습니다."
            );
        }

        // 발주서에 등록된 전체 품목 조회
        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository
                        .findByPurchaseOrder_PurchaseOrderNoOrderByPurchaseOrderItemNoAsc(
                                purchaseOrderNo
                        );

        // 품목이 없는 발주서는 입고 처리 불가
        if (items.isEmpty()) {
            throw new IllegalStateException(
                    "발주 품목이 없어 입고 처리할 수 없습니다."
            );
        }

        // 발주 품목별 입고 수량과 자재 재고 반영
        for (PurchaseOrderItem item : items) {

            // 발주 품목과 연결된 자재
            Material material = item.getMaterial();

            // 화면에서 입력한 품목별 실제 입고 수량
            BigDecimal receivedQuantity = request.getReceivedQuantities()
                    .get(item.getPurchaseOrderItemNo());

            if (receivedQuantity == null
                    || receivedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "실제 입고 수량은 0 이상으로 입력해 주세요."
                );
            }

            if (receivedQuantity.compareTo(item.getOrderQuantity()) > 0) {
                throw new IllegalArgumentException(
                        "실제 입고 수량은 발주 수량을 넘을 수 없습니다."
                );
            }

            // 입고 전과 입고 후 재고 계산
            BigDecimal beforeStock = material.getCurrentStock();
            BigDecimal afterStock = beforeStock.add(receivedQuantity);

            // 발주 품목의 실제 입고 수량 반영
            item.changeReceivedQuantity(receivedQuantity);

            // 자재의 현재 재고 증가
            material.changeStock(afterStock);

            // 실제 입고된 수량이 있을 때만 재고 변동 이력을 저장
            if (receivedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                StockHistory stockHistory = new StockHistory();
                stockHistory.setMaterialNo(material.getMaterialNo());
                stockHistory.setMovementType("PURCHASE_IN");
                stockHistory.setQuantity(receivedQuantity);
                stockHistory.setBeforeStock(beforeStock);
                stockHistory.setAfterStock(afterStock);
                stockHistory.setReferenceType("PURCHASE_ORDER");
                stockHistory.setReferenceNo(purchaseOrderNo);
                stockHistory.setMemo("발주서 입고 완료");

                stockHistoryService.saveHistory(stockHistory);
            }
        }

        // 발주 상태와 실제 입고 날짜 변경
        purchaseOrder.setOrderStatus("RECEIVED");
        purchaseOrder.setReceivedDate(LocalDateTime.now());
    }

    // 발주서 번호로 Entity를 조회하고 없으면 오류 발생
    private PurchaseOrder findOrder(Long purchaseOrderNo) {
        return purchaseOrderRepository.findById(purchaseOrderNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 발주서입니다."
                        )
                );
    }
}
