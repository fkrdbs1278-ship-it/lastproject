package com.young04.lastproject.stockhistory.dto;

import com.young04.lastproject.stockhistory.entity.StockHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StockHistoryResponse {

    private Long stockHistoryNo;
    private Long materialNo;
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal beforeStock;
    private BigDecimal afterStock;
    private String referenceType;
    private Long referenceNo;
    private String memo;
    private LocalDateTime regdate;

    public static StockHistoryResponse from(StockHistory stockHistory) {
        return StockHistoryResponse.builder()
                .stockHistoryNo(stockHistory.getStockHistoryNo())
                .materialNo(stockHistory.getMaterialNo())
                .movementType(stockHistory.getMovementType())
                .quantity(stockHistory.getQuantity())
                .beforeStock(stockHistory.getBeforeStock())
                .afterStock(stockHistory.getAfterStock())
                .referenceType(stockHistory.getReferenceType())
                .referenceNo(stockHistory.getReferenceNo())
                .memo(stockHistory.getMemo())
                .regdate(stockHistory.getRegdate())
                .build();
    }
}
