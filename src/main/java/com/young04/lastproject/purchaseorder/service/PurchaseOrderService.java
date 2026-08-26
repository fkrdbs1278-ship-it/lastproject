package com.young04.lastproject.purchaseorder.service;

import com.young04.lastproject.purchaseorder.dto.PurchaseOrderRequest;
import com.young04.lastproject.purchaseorder.dto.PurchaseOrderResponse;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 발주서 조회와 등록 등의 업무 처리를 담당하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    // PURCHASE_ORDER 테이블에 접근하는 Repository
    private final PurchaseOrderRepository purchaseOrderRepository;

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