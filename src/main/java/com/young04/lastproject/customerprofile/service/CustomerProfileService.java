package com.young04.lastproject.customerprofile.service;

import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepository;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepositoryCustom;
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
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerProfileRepositoryCustom customerProfileRepositoryCustom;

    // 고객 전체 조회
    public List<CustomerProfile> findAllCustomers() {

        log.info("고객 전체 조회");

        return customerProfileRepository.findAll();
    }

    // 고객 번호로 상세 조회
    public Optional<CustomerProfile> findByCustomerId(Long customerId) {

        log.info("고객 상세 조회 customerId={}", customerId);

        return customerProfileRepository.findById(customerId);
    }

    // 전화번호로 고객 조회
    public Optional<CustomerProfile> findByPhone(String phone) {

        log.info(
                "전화번호 기준 고객 조회 phone={}",
                maskPhone(phone)
        );

        return customerProfileRepository.findByPhone(phone);
    }

    // 회원 번호로 고객 조회
    public Optional<CustomerProfile> findByMemberNo(Long memberNo) {

        log.info("회원 번호 기준 고객 조회 memberNo={}", memberNo);

        return customerProfileRepository.findByMemberNo(memberNo);
    }

    // 복합 조건 고객 검색
    public List<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition
    ) {

        log.info("고객 CRM 조건 검색");

        return customerProfileRepositoryCustom.searchCustomers(condition);
    }

    // 전화번호 중복 여부
    public boolean existsByPhone(String phone) {

        return customerProfileRepository.existsByPhone(phone);
    }

    // 로그에 전화번호 전체가 노출되지 않도록 마스킹
    private String maskPhone(String phone) {

        if (phone == null || phone.length() < 8) {
            return "****";
        }

        return phone.substring(0, 3)
                + "****"
                + phone.substring(phone.length() - 4);
    }
}