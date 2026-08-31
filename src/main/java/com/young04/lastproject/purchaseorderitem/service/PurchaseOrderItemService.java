package com.young04.lastproject.purchaseorderitem.service;

import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.material.repository.MaterialRepository;
import com.young04.lastproject.purchaseorder.entity.PurchaseOrder;
import com.young04.lastproject.purchaseorder.repository.PurchaseOrderRepository;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemCreateRequest;
import com.young04.lastproject.purchaseorderitem.dto.PurchaseOrderItemResponse;
import com.young04.lastproject.purchaseorderitem.entity.PurchaseOrderItem;
import com.young04.lastproject.purchaseorderitem.repository.PurchaseOrderItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

// 발주 품목 등록과 조회 기능을 처리하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRepository materialRepository;

    // 발주서에 새로운 자재 품목 등록
    @Transactional
    public PurchaseOrderItemResponse createItem(
            Long purchaseOrderNo,
            PurchaseOrderItemCreateRequest request
    ) {
        // 품목을 추가할 발주서 조회
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderNo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "발주서를 찾을 수 없습니다."
                ));

        // 발주할 자재 조회
        Material material = materialRepository
                .findById(request.getMaterialNo())
                .orElseThrow(() -> new EntityNotFoundException(
                        "자재를 찾을 수 없습니다."
                ));

        // 같은 발주서에 동일한 자재가 중복 등록되는 것을 방지
        boolean duplicateItem = purchaseOrderItemRepository
                .existsByPurchaseOrder_PurchaseOrderNoAndMaterial_MaterialNo(
                        purchaseOrderNo,
                        request.getMaterialNo()
                );

        if (duplicateItem) {
            throw new IllegalStateException(
                    "이미 발주서에 등록된 자재입니다."
            );
        }

        // 입력값으로 새로운 발주 품목 생성
        PurchaseOrderItem purchaseOrderItem =
                new PurchaseOrderItem(
                        purchaseOrder,
                        material,
                        request.getOrderQuantity(),
                        request.getUnitPrice()
                );

        // 발주 품목 저장
        PurchaseOrderItem savedItem =
                purchaseOrderItemRepository.save(purchaseOrderItem);

        return new PurchaseOrderItemResponse(savedItem);
    }

    // 발주서 번호에 포함된 전체 품목 조회
    public List<PurchaseOrderItemResponse> getItems(
            Long purchaseOrderNo
    ) {
        return purchaseOrderItemRepository
                .findByPurchaseOrder_PurchaseOrderNoOrderByPurchaseOrderItemNoAsc(
                        purchaseOrderNo
                )
                .stream()
                .map(PurchaseOrderItemResponse::new)
                .toList();
    }

    // 대시보드에 표시할 발주 품목 이름 조회
    public List<String> getMaterialNames(Long purchaseOrderNo) {
        return purchaseOrderItemRepository
                .findByPurchaseOrder_PurchaseOrderNoOrderByPurchaseOrderItemNoAsc(
                        purchaseOrderNo
                )
                .stream()
                .map(item -> item.getMaterial().getMaterialName())
                .toList();
    }
}
