package com.young04.lastproject.customerprofile.repository;

import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * =========================================================
 * 고객 CRM Custom Repository
 * =========================================================
 *
 * 고객 목록의 복합 조건 검색을 담당합니다.
 *
 * 검색 조건:
 *
 * - 이름 / 전화번호
 * - 회원 / 비회원
 * - 고객 등급
 * - 활성 여부
 * - 30일 / 60일 미방문
 * - 재방문 권장일 도래
 *
 * 고객 수가 많아질 경우를 대비하여
 * Page / Pageable 기반 페이징을 적용합니다.
 */
public interface CustomerProfileRepositoryCustom {


    // =====================================================
    // 고객 조건 검색 + 페이징
    // =====================================================

    /**
     * 검색 조건과 페이지 정보를 이용해
     * 고객 목록을 페이지 단위로 조회합니다.
     *
     * 예:
     *
     * page = 0
     * size = 10
     *
     * → 첫 번째 페이지의 고객 10명 조회
     */
    Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    );

}