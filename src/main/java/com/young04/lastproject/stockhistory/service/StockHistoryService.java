package com.young04.lastproject.stockhistory.service;

import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.material.repository.MaterialRepository;
import com.young04.lastproject.stockhistory.dto.StockAdjustmentRequest;
import com.young04.lastproject.stockhistory.dto.StockHistoryResponse;
import com.young04.lastproject.stockhistory.entity.StockHistory;
import com.young04.lastproject.stockhistory.repository.StockHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;
    private final MaterialRepository materialRepository;

    // 전체 재고 변동 이력 조회
    public List<StockHistoryResponse> getAllHistories() {
        return stockHistoryRepository.findAllByOrderByRegdateDesc()
                .stream()
                .map(StockHistoryResponse::from)
                .toList();
    }

    // 특정 자재의 재고 변동 이력 조회
    public List<StockHistoryResponse> getHistoriesByMaterialNo(Long materialNo) {
        return stockHistoryRepository
                .findByMaterialNoOrderByRegdateDesc(materialNo)
                .stream()
                .map(StockHistoryResponse::from)
                .toList();
    }

    //재고 변동 이력 저장
    @Transactional
    public StockHistory saveHistory(StockHistory stockHistory) {
        return stockHistoryRepository.save(stockHistory);
    }

    // 사용·폐기·수동 조정에 따라 현재 재고와 변동 이력을 함께 반영
    @Transactional
    public void adjustStock(StockAdjustmentRequest request) {
        Material material = materialRepository
                .findById(request.getMaterialNo())
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 자재를 찾을 수 없습니다."
                ));

        if (!"Y".equals(material.getUseYn())) {
            throw new IllegalStateException(
                    "사용 중인 자재만 재고를 조정할 수 있습니다."
            );
        }

        BigDecimal quantity = request.getQuantity();

        if (quantity == null
                || quantity.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException(
                    "수량은 1 이상 입력해 주세요."
            );
        }

        String movementType = request.getMovementType();
        BigDecimal beforeStock = material.getCurrentStock();
        BigDecimal afterStock;

        if ("MANUAL_IN".equals(movementType)) {
            afterStock = beforeStock.add(quantity);

        } else if ("USE".equals(movementType)
                || "DISCARD".equals(movementType)
                || "MANUAL_OUT".equals(movementType)) {
            afterStock = beforeStock.subtract(quantity);

        } else {
            throw new IllegalArgumentException(
                    "올바른 변동 구분을 선택해 주세요."
            );
        }

        if (afterStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "현재 재고보다 많은 수량은 차감할 수 없습니다."
            );
        }

        material.changeStock(afterStock);

        StockHistory stockHistory = new StockHistory();
        stockHistory.setMaterialNo(material.getMaterialNo());
        stockHistory.setMovementType(movementType);
        stockHistory.setQuantity(quantity);
        stockHistory.setBeforeStock(beforeStock);
        stockHistory.setAfterStock(afterStock);
        stockHistory.setReferenceType("MANUAL");
        stockHistory.setReferenceNo(null);
        stockHistory.setMemo(request.getMemo());

        stockHistoryRepository.save(stockHistory);
    }
}
