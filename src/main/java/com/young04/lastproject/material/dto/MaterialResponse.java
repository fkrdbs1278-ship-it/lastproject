package com.young04.lastproject.material.dto;

import com.young04.lastproject.material.entity.Material;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MaterialResponse {

    private Long materialNo;
    private String materialName;
    private String categoryCode;
    private String unitCode;

    // 구매 단위 1개에 들어 있는 용량 또는 수량
    private BigDecimal contentQuantity;

    // 시술에서 사용하는 단위
    private String usageUnitCode;

    // 현재 개봉해서 사용 중인 자재의 남은 양
    private BigDecimal openRemainingQuantity;

    private BigDecimal currentStock;
    private BigDecimal safetyStock;
    private BigDecimal unitPrice;
    private String supplierName;
    private String useYn;
    private LocalDateTime regdate;
    private LocalDateTime updatedate;
    private boolean lowStock;


    public static MaterialResponse from(Material material) {

        return MaterialResponse.builder()

                .materialNo(material.getMaterialNo())
                .materialName(material.getMaterialName())
                .categoryCode(material.getCategoryCode())
                .unitCode(material.getUnitCode())

                // 자재의 용량과 사용 단위
                .contentQuantity(material.getContentQuantity())
                .usageUnitCode(material.getUsageUnitCode())

                // 현재 개봉 제품의 남은 양
                .openRemainingQuantity(material.getOpenRemainingQuantity())

                .currentStock(material.getCurrentStock())
                .safetyStock(material.getSafetyStock())
                .unitPrice(material.getUnitPrice())
                .supplierName(material.getSupplierName())
                .useYn(material.getUseYn())
                .regdate(material.getRegdate())
                .updatedate(material.getUpdatedate())

                .lowStock(
                        material.getCurrentStock()
                                .compareTo(material.getSafetyStock()) <= 0
                )

                .build();
    }
}