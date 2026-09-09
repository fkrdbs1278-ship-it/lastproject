package com.young04.lastproject.material.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 자재 기본 정보와 재고 수량을 관리하는 Entity
@Getter
@Entity
@Table(name = "MATERIAL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material {

    // 자재 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MATERIAL_NO")
    private Long materialNo;

    // 자재명
    @Column(name = "MATERIAL_NAME", length = 100, nullable = false)
    private String materialName;

    // 자재 분류
    @Column(name = "CATEGORY_CODE", length = 30)
    private String categoryCode;

    // 구매 및 재고 관리 단위: BTL, TUB, BOX 등
    @Column(name = "UNIT_CODE", length = 20, nullable = false)
    private String unitCode;

    // 구매 단위 1개에 들어 있는 실제 내용량
    @Column(
            name = "CONTENT_QUANTITY",
            precision = 12,
            scale = 2
    )
    private BigDecimal contentQuantity;

    // 시술에서 사용하는 단위: ML, G, EA
    @Column(name = "USAGE_UNIT_CODE", length = 20)
    private String usageUnitCode;

    // 현재 개봉해서 사용 중인 자재의 남은 양
    @Column(
            name = "OPEN_REMAINING_QUANTITY",
            precision = 12,
            scale = 2
    )
    private BigDecimal openRemainingQuantity;

    // 현재 보유 중인 구매 단위 재고
    @Column(
            name = "CURRENT_STOCK",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal currentStock;

    // 안전 재고
    @Column(
            name = "SAFETY_STOCK",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal safetyStock;

    // 구매 단위당 가격
    @Column(
            name = "UNIT_PRICE",
            precision = 12,
            scale = 0,
            nullable = false
    )
    private BigDecimal unitPrice;

    // 업체명
    @Column(name = "SUPPLIER_NAME", length = 100)
    private String supplierName;

    // 사용 여부
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "USE_YN",
            length = 1,
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String useYn;

    // 등록일
    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;

    // 수정일
    @Column(name = "UPDATEDATE")
    private LocalDateTime updatedate;


    // DTO의 입력값으로 새로운 자재 생성
    public Material(
            String materialName,
            String categoryCode,
            String unitCode,
            BigDecimal currentStock,
            BigDecimal safetyStock,
            BigDecimal unitPrice,
            String supplierName,
            String useYn
    ) {
        this.materialName = materialName;
        this.categoryCode = categoryCode;
        this.unitCode = unitCode;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
        this.unitPrice = unitPrice;
        this.supplierName = supplierName;
        this.useYn = useYn;
    }


    // 재고를 제외한 자재 기본 정보 수정
    public void update(
            String materialName,
            String categoryCode,
            String unitCode,
            BigDecimal safetyStock,
            BigDecimal unitPrice,
            String supplierName,
            String useYn
    ) {
        this.materialName = materialName;
        this.categoryCode = categoryCode;
        this.unitCode = unitCode;
        this.safetyStock = safetyStock;
        this.unitPrice = unitPrice;
        this.supplierName = supplierName;
        this.useYn = useYn;
    }


    // 재고 이력 처리 시 현재 재고 변경
    public void changeStock(BigDecimal currentStock) {

        if (currentStock == null
                || currentStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "현재 재고는 0 이상이어야 합니다."
            );
        }

        this.currentStock = currentStock;
    }


    // 자재의 구매 단위당 내용량과 실제 사용 단위를 설정
    public void updateUsageInfo(
            BigDecimal contentQuantity,
            String usageUnitCode
    ) {

        if (contentQuantity == null
                || contentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "구매 단위당 내용량은 0보다 커야 합니다."
            );
        }

        validateUsageUnitCode(usageUnitCode);

        this.contentQuantity = contentQuantity;
        this.usageUnitCode = usageUnitCode;
    }


    // 구매 수량을 ml/g/개 기준 사용 수량으로 환산
    public BigDecimal convertToUsageQuantity(
            BigDecimal purchaseQuantity
    ) {

        if (purchaseQuantity == null
                || purchaseQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "구매 수량은 0 이상이어야 합니다."
            );
        }

        validateUsageInfo();

        BigDecimal usageQuantity =
                purchaseQuantity.multiply(contentQuantity);

        // DB NUMBER(12,2) 기준 소수점 두 자리까지만 허용
        if (usageQuantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "환산된 수량은 소수점 두 자리까지 저장할 수 있습니다."
            );
        }

        if (usageQuantity.compareTo(
                new BigDecimal("9999999999.99")
        ) > 0) {
            throw new IllegalArgumentException(
                    "환산된 수량이 저장 가능한 범위를 초과했습니다."
            );
        }

        return usageQuantity;
    }


    // 현재 보유한 자재를 실제 사용 단위(ml/g/개)로 환산
    public BigDecimal getTotalRemainingUsageQuantity() {

        validateUsageInfo();

        if (currentStock == null
                || currentStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    materialName + ": 현재 재고 정보가 올바르지 않습니다."
            );
        }

        BigDecimal openedRemaining =
                openRemainingQuantity == null
                        ? BigDecimal.ZERO
                        : openRemainingQuantity;

        // 개봉된 제품이 있는 경우
        if (openedRemaining.compareTo(BigDecimal.ZERO) > 0) {

            if (currentStock.compareTo(BigDecimal.ONE) < 0) {
                throw new IllegalStateException(
                        materialName + ": 개봉 재고 정보가 올바르지 않습니다."
                );
            }

            // 현재 재고에는 개봉 중인 제품도 1개 포함
            BigDecimal unopenedStock =
                    currentStock.subtract(BigDecimal.ONE);

            return openedRemaining.add(
                    unopenedStock.multiply(contentQuantity)
            );
        }

        // 개봉 제품이 없으면 전체 재고 × 내용량
        return currentStock.multiply(contentQuantity);
    }


    // 시술 완료 시 지정된 ml/g/개 만큼 자재 자동 차감
    public void useMaterial(BigDecimal usageQuantity) {

        if (usageQuantity == null
                || usageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "자재 사용량은 0보다 커야 합니다."
            );
        }

        validateUsageInfo();

        BigDecimal totalRemaining =
                getTotalRemainingUsageQuantity();

        // 전체 잔량보다 사용량이 많으면 차감 중지
        if (totalRemaining.compareTo(usageQuantity) < 0) {
            throw new IllegalStateException(
                    materialName + ": 자재 잔량이 부족합니다."
            );
        }

        BigDecimal remainingUsage = usageQuantity;

        while (remainingUsage.compareTo(BigDecimal.ZERO) > 0) {

            // 개봉된 제품이 없으면 새 제품 사용 시작
            if (openRemainingQuantity == null
                    || openRemainingQuantity.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                if (currentStock == null
                        || currentStock.compareTo(
                        BigDecimal.ONE
                ) < 0) {
                    throw new IllegalStateException(
                            materialName + ": 사용할 재고가 없습니다."
                    );
                }

                openRemainingQuantity = contentQuantity;
            }

            // 현재 개봉 제품으로 전부 처리 가능한 경우
            if (openRemainingQuantity.compareTo(
                    remainingUsage
            ) >= 0) {

                openRemainingQuantity =
                        openRemainingQuantity.subtract(
                                remainingUsage
                        );

                remainingUsage = BigDecimal.ZERO;

                // 한 제품을 정확히 모두 사용한 경우
                if (openRemainingQuantity.compareTo(
                        BigDecimal.ZERO
                ) == 0) {

                    currentStock =
                            currentStock.subtract(
                                    BigDecimal.ONE
                            );
                }

            } else {

                // 현재 개봉 제품을 전부 사용
                remainingUsage =
                        remainingUsage.subtract(
                                openRemainingQuantity
                        );

                openRemainingQuantity = BigDecimal.ZERO;

                currentStock =
                        currentStock.subtract(
                                BigDecimal.ONE
                        );
            }
        }
    }


    // 내용량과 실제 사용 단위가 등록되어 있는지 확인
    private void validateUsageInfo() {

        if (contentQuantity == null
                || contentQuantity.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            throw new IllegalStateException(
                    materialName
                            + ": 구매 단위당 내용량을 먼저 등록해 주세요."
            );
        }

        validateUsageUnitCode(usageUnitCode);
    }


    // 시술 자재에서 사용할 수 있는 단위인지 확인
    private void validateUsageUnitCode(
            String usageUnitCode
    ) {

        if (!"ML".equals(usageUnitCode)
                && !"G".equals(usageUnitCode)
                && !"EA".equals(usageUnitCode)) {
            throw new IllegalStateException(
                    materialName
                            + ": 사용 단위는 ML, G, EA 중 하나여야 합니다."
            );
        }
    }


    // 처음 저장하기 전에 기본값과 등록일 설정
    @PrePersist
    protected void onCreate() {

        if (currentStock == null) {
            currentStock = BigDecimal.ZERO;
        }

        if (safetyStock == null) {
            safetyStock = BigDecimal.ZERO;
        }

        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        if (useYn == null || useYn.isBlank()) {
            useYn = "Y";
        }

        regdate = LocalDateTime.now();
    }


    // 수정되기 전에 수정일 갱신
    @PreUpdate
    protected void onUpdate() {
        updatedate = LocalDateTime.now();
    }
}