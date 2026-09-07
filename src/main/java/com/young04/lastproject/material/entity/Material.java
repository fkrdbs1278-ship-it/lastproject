package com.young04.lastproject.material.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
// JPA Entity로 등록하고 MATERIAL 테이블과 연결
@Entity
@Table(name = "MATERIAL")
// JPA가 사용할 protected 기본 생성자를 자동으로 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material {
    @Id
    // DB가 기본키 값을 자동 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MATERIAL_NO")
    private Long materialNo;

    @Column(name = "MATERIAL_NAME", length = 100, nullable = false)
    private String materialName;

    @Column(name = "CATEGORY_CODE", length = 30)
    private String categoryCode;

    @Column(name = "UNIT_CODE", length = 20, nullable = false)
    private String unitCode;

    // 구매 단위 1개에 들어 있는 용량 또는 수량
    @Column(
            name = "CONTENT_QUANTITY",
            precision = 12,
            scale = 2
    )
    private BigDecimal contentQuantity;

    // 시술에서 사용하는 단위: ML(ml), G(g), EA(개)
    @Column(name = "USAGE_UNIT_CODE", length = 20)
    private String usageUnitCode;

    // 현재 개봉해서 사용 중인 자재의 남은 양
    @Column(
            name = "OPEN_REMAINING_QUANTITY",
            precision = 12,
            scale = 2
    )
    private BigDecimal openRemainingQuantity;

    @Column(
            name = "CURRENT_STOCK",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal currentStock;

    @Column(
            name = "SAFETY_STOCK",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal safetyStock;

    @Column(
            name = "UNIT_PRICE",
            precision = 12,
            scale = 0,
            nullable = false
    )
    private BigDecimal unitPrice;

    @Column(name = "SUPPLIER_NAME", length = 100)
    private String supplierName;

    // DB의 CHAR(1) 자료형으로 매핑
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "USE_YN",
            length = 1,
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String useYn;

    @Column(name = "REGDATE", nullable = false, updatable = false)
    private LocalDateTime regdate;

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
        this.currentStock = currentStock;
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

    // 자재의 구매 단위당 내용량과 사용 단위를 설정
    public void updateUsageInfo(
            BigDecimal contentQuantity,
            String usageUnitCode
    ) {
        this.contentQuantity = contentQuantity;
        this.usageUnitCode = usageUnitCode;
    }

    // 구매 수량을 등록된 내용량 기준의 사용 수량으로 환산
    public BigDecimal convertToUsageQuantity(BigDecimal purchaseQuantity) {
        if (purchaseQuantity == null
                || purchaseQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "구매 수량은 0 이상이어야 합니다."
            );
        }

        if (contentQuantity == null
                || contentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    materialName + ": 구매 단위당 내용량을 먼저 등록해 주세요."
            );
        }

        if (!"ML".equals(usageUnitCode)
                && !"G".equals(usageUnitCode)
                && !"EA".equals(usageUnitCode)) {
            throw new IllegalStateException(
                    materialName + ": 사용 단위를 먼저 등록해 주세요."
            );
        }

        BigDecimal usageQuantity = purchaseQuantity.multiply(contentQuantity);

        // 소수점 두 자리를 넘는 수량은 반올림하지 않고 입력 확인 요청
        if (usageQuantity.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "환산된 수량은 소수점 두 자리까지 저장할 수 있습니다."
            );
        }

        if (usageQuantity.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "환산된 수량이 저장 가능한 범위를 초과했습니다."
            );
        }

        return usageQuantity;
    }
}
