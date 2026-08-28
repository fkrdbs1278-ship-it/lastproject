package com.young04.lastproject.material.service;

import com.young04.lastproject.material.dto.MaterialRequest;
import com.young04.lastproject.material.dto.MaterialResponse;
import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.material.repository.MaterialRepository;
import com.young04.lastproject.purchaseorderitem.repository.PurchaseOrderItemRepository;
import com.young04.lastproject.stockhistory.repository.StockHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// final 필드의 생성자를 자동으로 생성
@RequiredArgsConstructor
@Service

// 조회 메서드는 기본적으로 읽기 전용 처리
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockHistoryRepository stockHistoryRepository;

    // 전체 자재 목록 조회
    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAllByOrderByMaterialNoDesc()
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    // 자재 번호로 상세 조회
    public MaterialResponse getMaterial(Long materialNo) {
        Material material = findMaterial(materialNo);

        return MaterialResponse.from(material);
    }

    // 사용 여부에 따라 자재 조회
    public List<MaterialResponse> getMaterialsByUseYn(String useYn) {
        return materialRepository
                .findByUseYnOrderByMaterialNoDesc(useYn)
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    // 자재명으로 검색
    public List<MaterialResponse> searchMaterials(String keyword) {
        return materialRepository
                .findByMaterialNameContainingIgnoreCaseOrderByMaterialNoDesc(
                        keyword
                )
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    // 재고 부족 자재 조회
    public List<MaterialResponse> getLowStockMaterials() {
        return materialRepository.findLowStockMaterials()
                .stream()
                .map(MaterialResponse::from)
                .toList();
    }

    // 재고 부족 자재 개수 조회
    public long countLowStockMaterials() {
        return materialRepository.countLowStockMaterials();
    }

    // 자재 신규 등록
    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        Material material = new Material(
                request.getMaterialName(),
                request.getCategoryCode(),
                request.getUnitCode(),
                request.getCurrentStock(),
                request.getSafetyStock(),
                request.getUnitPrice(),
                request.getSupplierName(),
                request.getUseYn()
        );

        Material savedMaterial = materialRepository.save(material);

        return MaterialResponse.from(savedMaterial);
    }

    // 현재 재고를 제외한 자재 기본 정보 수정
    @Transactional
    public MaterialResponse updateMaterial(
            Long materialNo,
            MaterialRequest request
    ) {
        Material material = findMaterial(materialNo);

        material.update(
                request.getMaterialName(),
                request.getCategoryCode(),
                request.getUnitCode(),
                request.getSafetyStock(),
                request.getUnitPrice(),
                request.getSupplierName(),
                request.getUseYn()
        );

        return MaterialResponse.from(material);
    }

    // 사용 중지된 자재와 연결된 이력을 함께 삭제
    @Transactional
    public void deleteMaterial(Long materialNo) {
        Material material = findMaterial(materialNo);

        if (!"N".equals(material.getUseYn())) {
            throw new IllegalStateException(
                    "사용 중지된 자재만 삭제할 수 있습니다."
            );
        }

        try {
            // 외래키로 연결된 재고 이력과 발주 품목을 먼저 삭제
            stockHistoryRepository.deleteByMaterialNo(materialNo);
            purchaseOrderItemRepository
                    .deleteByMaterial_MaterialNo(materialNo);

            materialRepository.delete(material);
            materialRepository.flush();

        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "다른 업무 내역에서 사용 중인 자재는 삭제할 수 없습니다.",
                    exception
            );
        }
    }

    // 존재하지 않는 자재 번호이면 예외 발생
    private Material findMaterial(Long materialNo) {
        return materialRepository.findById(materialNo)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "해당 자재를 찾을 수 없습니다. 번호: "
                                        + materialNo
                        )
                );
    }
}
