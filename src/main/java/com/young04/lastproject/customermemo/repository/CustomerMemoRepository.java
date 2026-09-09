package com.young04.lastproject.customermemo.repository;

import com.young04.lastproject.customermemo.entity.CustomerMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerMemoRepository
        extends JpaRepository<CustomerMemo, Long> {

    // 고객별 전체 메모 최신순 조회
    List<CustomerMemo>
    findByCustomer_CustomerIdOrderByCreatedAtDescMemoIdDesc(
            Long customerId
    );


    // 고객별 + 메모 유형별 조회
    List<CustomerMemo>
    findByCustomer_CustomerIdAndMemoTypeOrderByCreatedAtDescMemoIdDesc(
            Long customerId,
            String memoType
    );


    // 고객별 메모 개수
    long countByCustomer_CustomerId(
            Long customerId
    );


    // 고객별 중요 메모 조회
    List<CustomerMemo>
    findByCustomer_CustomerIdAndImportantYnOrderByCreatedAtDescMemoIdDesc(
            Long customerId,
            String importantYn
    );
}