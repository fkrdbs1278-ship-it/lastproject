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

    // 재고 변동 당시 사용한 재고 단위
    private String unitCode;

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

                // 재고 단위 포함
                .unitCode(stockHistory.getUnitCode())

                .beforeStock(stockHistory.getBeforeStock())
                .afterStock(stockHistory.getAfterStock())
                .referenceType(stockHistory.getReferenceType())
                .referenceNo(stockHistory.getReferenceNo())
                .memo(stockHistory.getMemo())
                .regdate(stockHistory.getRegdate())
                .build();
    }
}
