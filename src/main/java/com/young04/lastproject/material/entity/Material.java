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
}
