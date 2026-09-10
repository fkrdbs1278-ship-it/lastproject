package com.young04.lastproject.servicematerial.dto;

import com.young04.lastproject.servicematerial.entity.ServiceMaterial;
import lombok.Getter;

import java.math.BigDecimal;

// 관리자 화면에 시술별 자재 설정 정보를 전달하는 DTO
@Getter
public class ServiceMaterialResponse {

    private final Long serviceMaterialNo;
    private final Long serviceMenuNo;
    private final Long materialNo;
    private final BigDecimal usageQuantity;


    public ServiceMaterialResponse(
            Long serviceMaterialNo,
            Long serviceMenuNo,
            Long materialNo,
            BigDecimal usageQuantity
    ) {
        this.serviceMaterialNo = serviceMaterialNo;
        this.serviceMenuNo = serviceMenuNo;
        this.materialNo = materialNo;
        this.usageQuantity = usageQuantity;
    }


    // Entity를 화면 전달용 DTO로 변환
    public static ServiceMaterialResponse from(
            ServiceMaterial serviceMaterial
    ) {

        return new ServiceMaterialResponse(
                serviceMaterial.getServiceMaterialNo(),
                serviceMaterial.getServiceMenuNo(),
                serviceMaterial.getMaterialNo(),
                serviceMaterial.getUsageQuantity()
        );
    }
}