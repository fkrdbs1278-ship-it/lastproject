package com.young04.lastproject.purchaseorderitem.repository;

import com.young04.lastproject.purchaseorderitem.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 발주 품목의 저장과 조회를 담당하는 Repository
public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, Long> {

    // 발주서 번호에 포함된 품목을 등록 순서대로 조회
    List<PurchaseOrderItem>
    findByPurchaseOrder_PurchaseOrderNoOrderByPurchaseOrderItemNoAsc(
            Long purchaseOrderNo
    );

    // 대시보드용: 여러 발주서의 자재 이름을 한 번의 JOIN 쿼리로 조회
    @Query("""
            SELECT poi.purchaseOrder.purchaseOrderNo,
                   poi.material.materialName
            FROM PurchaseOrderItem poi
            WHERE poi.purchaseOrder.purchaseOrderNo IN :purchaseOrderNos
            ORDER BY poi.purchaseOrder.purchaseOrderNo ASC,
                     poi.purchaseOrderItemNo ASC
            """)
    List<Object[]> findMaterialNamesByPurchaseOrderNos(
            @Param("purchaseOrderNos") List<Long> purchaseOrderNos
    );

    // 한 발주서에 동일한 자재가 이미 등록되었는지 확인
    boolean existsByPurchaseOrder_PurchaseOrderNoAndMaterial_MaterialNo(
            Long purchaseOrderNo,
            Long materialNo
    );

    // 자재와 연결된 발주 품목 삭제
    long deleteByMaterial_MaterialNo(Long materialNo);
}
