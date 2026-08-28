package com.young04.lastproject.stockhistory.service;

import com.young04.lastproject.stockhistory.dto.StockHistoryResponse;
import com.young04.lastproject.stockhistory.entity.StockHistory;
import com.young04.lastproject.stockhistory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

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
}
