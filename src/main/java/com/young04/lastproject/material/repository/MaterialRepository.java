package com.young04.lastproject.material.repository;

import com.young04.lastproject.material.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository <Material, Long>{

    // 전체 자재를 최근 등록순으로 조회
    List<Material> findAllByOrderByMaterialNoDesc();

    // 자동완성에서 사용할 전체 자재를 자재명순으로 조회
    List<Material> findAllByOrderByMaterialNameAsc();

    // 사용 여부에 따라 자재 조회
    List<Material> findByUseYnOrderByMaterialNoDesc(String useYn);

    // 자재명에 검색어가 포함된 자재 조회
    List<Material> findByMaterialNameContainingIgnoreCaseOrderByMaterialNoDesc(
            String keyword
    );

    // 자재 관리 화면의 전체 목록을 페이지 단위로 조회
    Page<Material> findAll(Pageable pageable);

    // 자재 관리 화면의 사용 상태별 목록을 페이지 단위로 조회
    Page<Material> findByUseYn(
            String useYn,
            Pageable pageable
    );

    // 자재명 검색 결과를 페이지 단위로 조회
    Page<Material> findByMaterialNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    // 입력 중인 글자가 포함된 자재명을 상태와 관계없이 모두 조회
    List<Material> findByMaterialNameContainingIgnoreCaseOrderByMaterialNameAsc(
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

    // 재고 부족 자재를 페이지 단위로 조회
    @Query("""
            SELECT m
            FROM Material m
            WHERE m.useYn = 'Y'
              AND m.currentStock <= m.safetyStock
            """)
    Page<Material> findLowStockMaterials(Pageable pageable);

    // 재고 부족 자재 개수 조회
    @Query("""
            SELECT COUNT(m)
            FROM Material m
            WHERE m.useYn = 'Y'
              AND m.currentStock <= m.safetyStock
            """)
    long countLowStockMaterials();
}
