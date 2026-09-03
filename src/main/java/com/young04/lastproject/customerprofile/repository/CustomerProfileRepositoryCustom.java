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
 * 관리자 고객관리 목록
 *
 * /admin/customers
 *
 * 에서 사용하는
 * 복합 검색 + 페이징 Repository입니다.
 *
 *
 * 검색 조건
 *
 * - 고객명 / 전화번호
 * - 회원 / 비회원
 * - 고객 등급
 * - 활성 / 비활성
 * - 30일 / 60일 이상 미방문
 *
 *
 * 전화번호 검색은
 * 하이픈 입력 여부와 관계없이 처리합니다.
 *
 * 예:
 *
 * 010-1234-5678
 * 01012345678
 *
 * 모두 동일하게 검색됩니다.
 *
 *
 * 재방문 권장일 기능은 사용하지 않습니다.
 */
public interface CustomerProfileRepositoryCustom {


    // =====================================================
    // 고객 검색 + 페이징
    // =====================================================

    Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    );
}