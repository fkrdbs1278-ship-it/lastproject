package com.young04.lastproject.customermemo.service;

import com.young04.lastproject.customermemo.dto.CustomerMemoRequest;
import com.young04.lastproject.customermemo.entity.CustomerMemo;
import com.young04.lastproject.customermemo.repository.CustomerMemoRepository;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerMemoService {

    private final CustomerMemoRepository customerMemoRepository;
    private final CustomerProfileRepository customerProfileRepository;


    // 고객별 전체 메모 조회
    public List<CustomerMemo> findByCustomerId(Long customerId) {

        log.info(
                "고객 상담 메모 전체 조회 customerId={}",
                customerId
        );

        return customerMemoRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDescMemoIdDesc(
                        customerId
                );
    }


    // 메모 번호로 상세 조회
    public Optional<CustomerMemo> findByMemoId(Long memoId) {

        log.info(
                "고객 상담 메모 상세 조회 memoId={}",
                memoId
        );

        return customerMemoRepository.findById(memoId);
    }


    // 고객 + 메모 유형별 조회
    public List<CustomerMemo> findByCustomerIdAndMemoType(
            Long customerId,
            String memoType
    ) {

        log.info(
                "고객 상담 메모 유형별 조회 customerId={}, memoType={}",
                customerId,
                memoType
        );

        return customerMemoRepository
                .findByCustomer_CustomerIdAndMemoTypeOrderByCreatedAtDescMemoIdDesc(
                        customerId,
                        memoType
                );
    }


    // 고객별 중요 메모 조회
    public List<CustomerMemo> findImportantMemos(
            Long customerId
    ) {

        log.info(
                "고객 중요 메모 조회 customerId={}",
                customerId
        );

        return customerMemoRepository
                .findByCustomer_CustomerIdAndImportantYnOrderByCreatedAtDescMemoIdDesc(
                        customerId,
                        "Y"
                );
    }


    // 고객별 메모 개수
    public long countByCustomerId(Long customerId) {

        return customerMemoRepository
                .countByCustomer_CustomerId(customerId);
    }


    // =====================================================
    // 메모 등록
    // =====================================================

    @Transactional
    public CustomerMemo createMemo(
            Long customerId,
            CustomerMemoRequest request
    ) {

        CustomerProfile customer = customerProfileRepository
                .findById(customerId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "고객을 찾을 수 없습니다. customerId="
                                        + customerId
                        )
                );

        CustomerMemo memo = CustomerMemo.create(
                customer,
                request.getMemoType(),
                request.getMemoContent(),
                request.getImportantYn()
        );

        CustomerMemo savedMemo =
                customerMemoRepository.save(memo);

        log.info(
                "고객 상담 메모 등록 customerId={}, memoId={}, memoType={}",
                customerId,
                savedMemo.getMemoId(),
                savedMemo.getMemoType()
        );

        return savedMemo;
    }


    // =====================================================
    // 메모 수정
    // =====================================================

    @Transactional
    public void updateMemo(
            Long customerId,
            Long memoId,
            CustomerMemoRequest request
    ) {

        CustomerMemo memo = customerMemoRepository
                .findById(memoId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "상담 메모를 찾을 수 없습니다. memoId="
                                        + memoId
                        )
                );

        // 다른 고객의 메모를 잘못 수정하는 것 방지
        if (!memo.getCustomer()
                .getCustomerId()
                .equals(customerId)) {

            throw new IllegalArgumentException(
                    "해당 고객의 상담 메모가 아닙니다."
            );
        }

        memo.update(
                request.getMemoType(),
                request.getMemoContent(),
                request.getImportantYn()
        );

        log.info(
                "고객 상담 메모 수정 customerId={}, memoId={}, memoType={}",
                customerId,
                memoId,
                request.getMemoType()
        );
    }


    // =====================================================
    // 메모 삭제
    // =====================================================

    @Transactional
    public void deleteMemo(
            Long customerId,
            Long memoId
    ) {

        CustomerMemo memo = customerMemoRepository
                .findById(memoId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "상담 메모를 찾을 수 없습니다. memoId="
                                        + memoId
                        )
                );

        // 다른 고객의 메모 삭제 방지
        if (!memo.getCustomer()
                .getCustomerId()
                .equals(customerId)) {

            throw new IllegalArgumentException(
                    "해당 고객의 상담 메모가 아닙니다."
            );
        }

        customerMemoRepository.delete(memo);

        log.info(
                "고객 상담 메모 삭제 customerId={}, memoId={}",
                customerId,
                memoId
        );
    }
}