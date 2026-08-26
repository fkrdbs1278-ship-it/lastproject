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
