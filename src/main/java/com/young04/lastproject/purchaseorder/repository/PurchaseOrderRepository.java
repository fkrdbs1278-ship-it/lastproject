package com.young04.lastproject.purchaseorder.repository;

import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// PURCHASE_ORDER 테이블의 조회와 저장을 담당하는 Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // 전체 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder> findAllByOrderByOrderDateDesc();

    // 전체 발주서를 발주일 최신순으로 페이징 조회
    Page<PurchaseOrder> findAllByOrderByOrderDateDesc(
            Pageable pageable
    );

    // 선택한 발주 상태의 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder> findByOrderStatusOrderByOrderDateDesc(
            String orderStatus
    );

    // 선택한 발주 상태의 발주서를 발주일 최신순으로 페이징 조회
    Page<PurchaseOrder> findByOrderStatusOrderByOrderDateDesc(
            String orderStatus,
            Pageable pageable
    );

    // 선택한 발주 상태의 발주서 개수 조회
    long countByOrderStatus(String orderStatus);

    // 입고 예정일이 가장 가까운 발주서 한 건 조회
    Optional<PurchaseOrder>
    findFirstByOrderStatusOrderByExpectedDateAscOrderDateAsc(
            String orderStatus
    );

    // 업체명이 포함된 발주서를 발주일 최신순으로 조회
    List<PurchaseOrder>
    findBySupplierNameContainingIgnoreCaseOrderByOrderDateDesc(
            String supplierName
    );

    // 업체명이 포함된 발주서를 발주일 최신순으로 페이징 조회
    Page<PurchaseOrder>
    findBySupplierNameContainingIgnoreCaseOrderByOrderDateDesc(
            String supplierName,
            Pageable pageable
    );
}