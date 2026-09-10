package com.young04.lastproject.servicematerial.service;

import com.young04.lastproject.servicematerial.entity.ServiceMaterial;
import com.young04.lastproject.servicematerial.repository.ServiceMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// 시술별 자재와 기준 사용량을 관리하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceMaterialService {

    private final ServiceMaterialRepository serviceMaterialRepository;


    // 특정 시술에 연결된 자재 전체 조회
    public List<ServiceMaterial> getMaterialsByServiceMenuNo(
            Long serviceMenuNo
    ) {
        return serviceMaterialRepository
                .findByServiceMenuNo(serviceMenuNo);
    }


    // 시술에 자재를 새로 연결하거나 기존 사용량 수정
    @Transactional
    public ServiceMaterial saveOrUpdate(
            Long serviceMenuNo,
            Long materialNo,
            BigDecimal usageQuantity
    ) {

        validateUsageQuantity(usageQuantity);

        return serviceMaterialRepository
                .findByServiceMenuNoAndMaterialNo(
                        serviceMenuNo,
                        materialNo
                )
                .map(serviceMaterial -> {
                    serviceMaterial.changeUsageQuantity(
                            usageQuantity
                    );

                    return serviceMaterial;
                })
                .orElseGet(() ->
                        serviceMaterialRepository.save(
                                new ServiceMaterial(
                                        serviceMenuNo,
                                        materialNo,
                                        usageQuantity
                                )
                        )
                );
    }


    // 기존에 설정된 기본 자재를 다른 제품으로 교체
    @Transactional
    public ServiceMaterial changeMaterial(
            Long serviceMaterialNo,
            Long newMaterialNo
    ) {

        ServiceMaterial serviceMaterial =
                serviceMaterialRepository
                        .findById(serviceMaterialNo)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "시술 자재 설정을 찾을 수 없습니다."
                                )
                        );

        // 같은 시술에 새 자재가 이미 연결되어 있으면 중복 등록 방지
        boolean duplicate =
                serviceMaterialRepository
                        .existsByServiceMenuNoAndMaterialNo(
                                serviceMaterial.getServiceMenuNo(),
                                newMaterialNo
                        );

        if (duplicate
                && !serviceMaterial.getMaterialNo()
                .equals(newMaterialNo)) {

            throw new IllegalStateException(
                    "해당 자재는 이미 이 시술에 등록되어 있습니다."
            );
        }

        serviceMaterial.changeMaterial(newMaterialNo);

        return serviceMaterial;
    }


    // 시술에 연결된 특정 자재 삭제
    @Transactional
    public void deleteMaterial(
            Long serviceMenuNo,
            Long materialNo
    ) {

        ServiceMaterial serviceMaterial =
                serviceMaterialRepository
                        .findByServiceMenuNoAndMaterialNo(
                                serviceMenuNo,
                                materialNo
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 시술에 연결된 자재가 없습니다."
                                )
                        );

        serviceMaterialRepository.delete(serviceMaterial);
    }


    // 특정 시술의 자재 설정 전체 삭제
    @Transactional
    public void deleteAllByServiceMenuNo(
            Long serviceMenuNo
    ) {

        serviceMaterialRepository
                .deleteByServiceMenuNo(serviceMenuNo);
    }


    // 자재 사용량 검증
    private void validateUsageQuantity(
            BigDecimal usageQuantity
    ) {

        if (usageQuantity == null
                || usageQuantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "자재 사용량은 0보다 커야 합니다."
            );
        }
    }
}