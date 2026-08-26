package com.young04.lastproject.purchaseorder.repository;

import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// PURCHASE_ORDER 테이블의 조회와 저장을 담당하는 Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // 전체 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder> findAllByOrderByOrderDateDesc();

    // 선택한 발주 상태의 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder> findByOrderStatusOrderByOrderDateDesc(
            String orderStatus
    );

    // 공급업체 이름이 포함된 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder> findBySupplierNameContainingIgnoreCaseOrderByOrderDateDesc(
            String supplierName
    );
}