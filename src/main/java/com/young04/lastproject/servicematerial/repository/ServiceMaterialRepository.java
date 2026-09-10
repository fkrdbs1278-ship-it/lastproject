package com.young04.lastproject.servicematerial.repository;

import com.young04.lastproject.servicematerial.entity.ServiceMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 시술별 자재 사용량 조회와 저장을 담당하는 Repository
public interface ServiceMaterialRepository
        extends JpaRepository<ServiceMaterial, Long> {

    // 특정 시술에 연결된 모든 자재 조회
    List<ServiceMaterial> findByServiceMenuNo(
            Long serviceMenuNo
    );

    // 특정 시술에 연결된 자재가 하나라도 있는지 확인
    boolean existsByServiceMenuNo(Long serviceMenuNo);

    // 특정 시술에 특정 자재가 이미 연결되어 있는지 확인
    boolean existsByServiceMenuNoAndMaterialNo(
            Long serviceMenuNo,
            Long materialNo
    );

    // 특정 시술과 자재의 연결 정보 조회
    Optional<ServiceMaterial> findByServiceMenuNoAndMaterialNo(
            Long serviceMenuNo,
            Long materialNo
    );

    // 특정 시술에 연결된 자재 전체 삭제
    void deleteByServiceMenuNo(
            Long serviceMenuNo
    );
}