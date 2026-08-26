package com.young04.lastproject.stockhistory.entity;


import com.young04.lastproject.stockhistory.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHistoryRepository
        extends JpaRepository<StockHistory, Long> {

    // 전체 재고 변동 이력을 최신순으로 조회
    List<StockHistory> findAllByOrderByRegdateDesc();

    // 특정 자재의 변동 이력을 최신순으로 조회
    List<StockHistory> findByMaterialNoOrderByRegdateDesc(Long materialNo);
}
