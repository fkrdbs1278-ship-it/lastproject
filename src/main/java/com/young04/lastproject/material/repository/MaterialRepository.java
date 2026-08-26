package com.young04.lastproject.material.repository;

import com.young04.lastproject.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository <Material, Long>{

    // 전체 자재를 최근 등록순으로 조회
    List<Material> findAllByOrderByMaterialNoDesc();

    // 사용 여부에 따라 자재 조회
    List<Material> findByUseYnOrderByMaterialNoDesc(String useYn);

    // 자재명에 검색어가 포함된 자재 조회
    List<Material> findByMaterialNameContainingIgnoreCaseOrderByMaterialNoDesc(
            String keyword
    );


    // 현재 재고가 안전 재고 이하인 사용 중 자재 조회
    @Query("""
            SELECT m
            FROM Material m
            WHERE m.useYn = 'Y'
              AND m.currentStock <= m.safetyStock
            ORDER BY m.currentStock ASC
            """)
    List<Material> findLowStockMaterials();

    // 재고 부족 자재 개수 조회
    @Query("""
            SELECT COUNT(m)
            FROM Material m
            WHERE m.useYn = 'Y'
              AND m.currentStock <= m.safetyStock
            """)
    long countLowStockMaterials();
}
