package com.young04.lastproject.servicematerial.service;

import com.young04.lastproject.material.entity.Material;
import com.young04.lastproject.material.repository.MaterialRepository;
import com.young04.lastproject.servicematerial.entity.ServiceMaterial;
import com.young04.lastproject.stockhistory.entity.StockHistory;
import com.young04.lastproject.stockhistory.repository.StockHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// 시술 완료 시 설정된 자재 사용량만큼 자동 차감하는 Service
@Service
@RequiredArgsConstructor
public class MaterialUsageService {

    private final ServiceMaterialService serviceMaterialService;
    private final MaterialRepository materialRepository;
    private final StockHistoryRepository stockHistoryRepository;


    // 예약 시술에 연결된 자재를 자동 차감하고 사용 이력 저장
    @Transactional
    public void deductMaterialsForReservation(
            Long reservationNo,
            Long serviceMenuNo
    ) {

        if (reservationNo == null) {
            throw new IllegalArgumentException(
                    "예약 번호가 필요합니다."
            );
        }

        if (serviceMenuNo == null) {
            throw new IllegalArgumentException(
                    "시술 메뉴 번호가 필요합니다."
            );
        }

        // 같은 예약의 자재가 중복 차감되는 것을 방지
        boolean alreadyDeducted =
                stockHistoryRepository
                        .existsByReferenceTypeAndReferenceNoAndMovementType(
                                "RESERVATION",
                                reservationNo,
                                "USE"
                        );

        if (alreadyDeducted) {
            return;
        }

        // 선택한 시술에 연결된 자재와 기준 사용량 조회
        List<ServiceMaterial> serviceMaterials =
                serviceMaterialService
                        .getMaterialsByServiceMenuNo(
                                serviceMenuNo
                        );

        // 등록된 사용 자재가 없으면 차감 없이 종료
        if (serviceMaterials.isEmpty()) {
            return;
        }

        // 시술에 연결된 자재를 하나씩 자동 차감
        for (ServiceMaterial serviceMaterial : serviceMaterials) {

            Material material =
                    materialRepository
                            .findById(
                                    serviceMaterial.getMaterialNo()
                            )
                            .orElseThrow(() ->
                                    new EntityNotFoundException(
                                            "시술에 연결된 자재를 찾을 수 없습니다."
                                    )
                            );

            // 사용 중인 자재만 자동 차감
            if (!"Y".equals(material.getUseYn())) {
                throw new IllegalStateException(
                        material.getMaterialName()
                                + ": 현재 사용 중지된 자재입니다."
                );
            }

            // 시술 1회 기준 사용량
            BigDecimal usageQuantity =
                    serviceMaterial.getUsageQuantity();

            // 차감 전 실제 남은 ml/g/개 수량
            BigDecimal beforeQuantity =
                    material.getTotalRemainingUsageQuantity();

            // 실제 사용량만큼 자재 차감
            material.useMaterial(usageQuantity);

            // 차감 후 실제 남은 ml/g/개 수량
            BigDecimal afterQuantity =
                    material.getTotalRemainingUsageQuantity();

            // 자동 차감 내역 생성
            StockHistory stockHistory =
                    new StockHistory();

            stockHistory.setMaterialNo(
                    material.getMaterialNo()
            );

            stockHistory.setMovementType(
                    "USE"
            );

            stockHistory.setQuantity(
                    usageQuantity
            );

            // 자동 차감은 실제 사용 단위 ML/G/EA 기록
            stockHistory.setUnitCode(
                    material.getUsageUnitCode()
            );

            stockHistory.setBeforeStock(
                    beforeQuantity
            );

            stockHistory.setAfterStock(
                    afterQuantity
            );

            // 어떤 예약에서 사용된 자재인지 기록
            stockHistory.setReferenceType(
                    "RESERVATION"
            );

            stockHistory.setReferenceNo(
                    reservationNo
            );

            stockHistory.setMemo(
                    "예약 시술 완료에 따른 자재 자동 사용"
            );

            stockHistoryRepository.save(
                    stockHistory
            );
        }
    }
}