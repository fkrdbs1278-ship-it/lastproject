package com.young04.lastproject.customerprofile.service;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import com.young04.lastproject.customergrade.exception.CustomerGradeNotFoundException;
import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.dto.CustomerCreateRequest;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.exception.CustomerNotFoundException;
import com.young04.lastproject.customerprofile.exception.DuplicateCustomerPhoneException;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepository;
import com.young04.lastproject.customerprofile.repository.CustomerProfileRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerProfileService {


    // =====================================================
    // Repository / Service
    // =====================================================

    private final CustomerProfileRepository customerProfileRepository;

    private final CustomerProfileRepositoryCustom customerProfileRepositoryCustom;

    private final CustomerGradeService customerGradeService;



    // =====================================================
    // 고객 전체 조회
    // =====================================================

    public List<CustomerProfile> findAllCustomers() {

        log.info(
                "고객 전체 조회"
        );

        return customerProfileRepository
                .findAll();
    }



    // =====================================================
    // 고객 번호로 조회 - Optional
    // =====================================================

    public Optional<CustomerProfile> findByCustomerId(
            Long customerId
    ) {

        log.info(
                "고객 상세 조회 customerId={}",
                customerId
        );

        return customerProfileRepository
                .findById(
                        customerId
                );
    }



    // =====================================================
    // 고객 번호로 필수 조회
    // =====================================================

    public CustomerProfile getCustomerById(
            Long customerId
    ) {

        log.info(
                "고객 조회 customerId={}",
                customerId
        );

        return customerProfileRepository
                .findById(
                        customerId
                )
                .orElseThrow(
                        () -> {

                            log.warn(
                                    "고객을 찾을 수 없음 customerId={}",
                                    customerId
                            );

                            return new CustomerNotFoundException(
                                    customerId
                            );
                        }
                );
    }



    // =====================================================
    // 전화번호 기준 고객 조회
    // =====================================================

    public Optional<CustomerProfile> findByPhone(
            String phone
    ) {

        String normalizedPhone =
                normalizePhone(
                        phone
                );

        log.info(
                "전화번호 기준 고객 조회 phone={}",
                maskPhone(
                        normalizedPhone
                )
        );

        return customerProfileRepository
                .findByPhone(
                        normalizedPhone
                );
    }



    // =====================================================
    // 회원 번호 기준 고객 조회 - Optional
    // =====================================================

    public Optional<CustomerProfile> findByMemberNo(
            Long memberNo
    ) {

        log.info(
                "회원 번호 기준 고객 조회 memberNo={}",
                memberNo
        );

        return customerProfileRepository
                .findByMemberNo(
                        memberNo
                );
    }



    // =====================================================
    // 회원 번호 기준 CRM 고객 필수 조회
    // =====================================================

    /**
     * 향후 1part 로그인 회원과 CRM 고객을
     * 연결할 때 사용합니다.
     */
    public CustomerProfile getCustomerByMemberNo(
            Long memberNo
    ) {

        log.info(
                "회원번호 기준 CRM 고객 필수 조회 memberNo={}",
                memberNo
        );

        return customerProfileRepository
                .findByMemberNo(
                        memberNo
                )
                .orElseThrow(
                        () -> {

                            log.warn(
                                    "회원번호와 연결된 CRM 고객 없음 memberNo={}",
                                    memberNo
                            );

                            return new CustomerNotFoundException(
                                    memberNo
                            );
                        }
                );
    }



    // =====================================================
    // 복합 조건 고객 검색 + 페이징
    // =====================================================

    public Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    ) {

        log.info(
                "고객 CRM 조건 검색 page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return customerProfileRepositoryCustom
                .searchCustomers(
                        condition,
                        pageable
                );
    }



    // =====================================================
    // 전화번호 중복 확인
    // =====================================================

    public boolean existsByPhone(
            String phone
    ) {

        String normalizedPhone =
                normalizePhone(
                        phone
                );

        return customerProfileRepository
                .existsByPhone(
                        normalizedPhone
                );
    }



    // =====================================================
    // 전화예약 / 비회원 고객 등록
    // =====================================================

    @Transactional
    public CustomerProfile createGuestCustomer(
            CustomerCreateRequest request
    ) {

        log.info(
                "전화예약 고객 등록 시작 customerName={}",
                request.getCustomerName()
        );


        String customerName =
                request
                        .getCustomerName()
                        .trim();


        String normalizedPhone =
                normalizePhone(
                        request.getPhone()
                );


        log.info(
                "전화예약 고객 전화번호 정규화 phone={}",
                maskPhone(
                        normalizedPhone
                )
        );


        if (customerProfileRepository
                .existsByPhone(
                        normalizedPhone
                )) {

            log.warn(
                    "전화예약 고객 등록 실패 - 중복 전화번호 phone={}",
                    maskPhone(
                            normalizedPhone
                    )
            );

            throw new DuplicateCustomerPhoneException();
        }


        CustomerGrade normalGrade =
                customerGradeService
                        .findByGradeCode(
                                "NORMAL"
                        )
                        .orElseThrow(
                                () -> {

                                    log.error(
                                            "전화예약 고객 등록 실패 - NORMAL 등급 없음"
                                    );

                                    return new CustomerGradeNotFoundException(
                                            "NORMAL"
                                    );
                                }
                        );


        CustomerProfile customer =
                CustomerProfile
                        .createGuestCustomer(
                                customerName,
                                normalizedPhone,
                                normalGrade
                        );


        CustomerProfile savedCustomer =
                customerProfileRepository
                        .save(
                                customer
                        );


        log.info(
                "전화예약 고객 등록 완료 customerId={}, customerName={}, phone={}",
                savedCustomer.getCustomerId(),
                savedCustomer.getCustomerName(),
                maskPhone(
                        savedCustomer.getPhone()
                )
        );


        return savedCustomer;
    }



    // =====================================================
    // 고객 방문 완료 처리
    // =====================================================

    /**
     * 실제 방문 및 시술 완료 후
     * CUSTOMER_PROFILE의 CRM 정보를 갱신합니다.
     *
     * VISIT_COUNT + 1
     * LAST_VISIT_DATE 변경
     * REVISIT_RECOMMENDED_DATE 변경
     * 자동 등급 재계산
     *
     * 향후 2part 예약 상태가 COMPLETED가 되었을 때
     * 이 메서드를 호출하면 됩니다.
     */
    @Transactional
    public CustomerProfile completeVisit(
            Long customerId,
            LocalDate visitDate,
            LocalDate revisitRecommendedDate
    ) {

        log.info(
                "고객 방문 완료 처리 시작 customerId={}, visitDate={}, revisitRecommendedDate={}",
                customerId,
                visitDate,
                revisitRecommendedDate
        );


        // -------------------------------------------------
        // 1. 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        // -------------------------------------------------
        // 2. 방문정보 갱신
        // -------------------------------------------------

        customer.recordVisit(
                visitDate,
                revisitRecommendedDate
        );


        // -------------------------------------------------
        // 3. 자동 등급 고객이면 등급 재계산
        // -------------------------------------------------

        applyAutomaticGradeToCustomer(
                customer
        );


        log.info(
                "고객 방문 완료 처리 완료 customerId={}, visitCount={}, lastVisitDate={}, revisitRecommendedDate={}, gradeCode={}",
                customerId,
                customer.getVisitCount(),
                customer.getLastVisitDate(),
                customer.getRevisitRecommendedDate(),
                customer.getCustomerGrade().getGradeCode()
        );


        return customer;
    }



    // =====================================================
    // 고객 결제 금액 누적
    // =====================================================

    /**
     * 결제 완료된 금액을
     * 고객 누적 결제액에 더합니다.
     *
     * TOTAL_PAYMENT 증가
     * 자동 등급 재계산
     *
     * 향후 4part 결제가 PAID 상태가 되었을 때
     * 이 메서드를 호출하면 됩니다.
     */
    @Transactional
    public CustomerProfile addCustomerPayment(
            Long customerId,
            BigDecimal paymentAmount
    ) {

        log.info(
                "고객 결제금액 누적 시작 customerId={}, paymentAmount={}",
                customerId,
                paymentAmount
        );


        // -------------------------------------------------
        // 1. 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        // -------------------------------------------------
        // 2. 누적 결제액 증가
        // -------------------------------------------------

        customer.addPayment(
                paymentAmount
        );


        // -------------------------------------------------
        // 3. 자동 등급 고객이면 등급 재계산
        // -------------------------------------------------

        applyAutomaticGradeToCustomer(
                customer
        );


        log.info(
                "고객 결제금액 누적 완료 customerId={}, totalPayment={}, gradeCode={}",
                customerId,
                customer.getTotalPayment(),
                customer.getCustomerGrade().getGradeCode()
        );


        return customer;
    }



    // =====================================================
    // 고객 등급 자동 적용
    // =====================================================

    @Transactional
    public CustomerProfile applyAutomaticGrade(
            Long customerId
    ) {

        log.info(
                "고객 자동 등급 적용 시작 customerId={}",
                customerId
        );


        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        applyAutomaticGradeToCustomer(
                customer
        );


        log.info(
                "고객 자동 등급 적용 완료 customerId={}, gradeCode={}, manualYn={}",
                customerId,
                customer.getCustomerGrade().getGradeCode(),
                customer.getGradeManualYn()
        );


        return customer;
    }



    // =====================================================
    // 자동 등급 계산 공통 처리
    // =====================================================

    /**
     * 방문 완료 / 결제 완료 / 관리자 재계산에서
     * 공통으로 사용하는 내부 메서드입니다.
     */
    private void applyAutomaticGradeToCustomer(
            CustomerProfile customer
    ) {


        // 수동 등급 고객은 자동 변경하지 않음
        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

            log.info(
                    "자동 등급 적용 제외 - 수동 등급 고객 customerId={}",
                    customer.getCustomerId()
            );

            return;
        }


        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );


        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                gradeCode
                        )
                        .orElseThrow(
                                () -> {

                                    log.error(
                                            "자동 등급 적용 실패 - 등급 없음 gradeCode={}",
                                            gradeCode
                                    );

                                    return new CustomerGradeNotFoundException(
                                            gradeCode
                                    );
                                }
                        );


        customer.applyAutomaticGrade(
                grade
        );
    }



    // =====================================================
    // 관리자 고객 등급 수동 변경
    // =====================================================

    @Transactional
    public CustomerProfile changeGradeManually(
            Long customerId,
            String gradeCode
    ) {

        log.info(
                "고객 등급 수동 변경 시작 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        if (gradeCode == null
                || gradeCode.isBlank()) {

            log.warn(
                    "고객 등급 수동 변경 실패 - gradeCode 없음 customerId={}",
                    customerId
            );

            throw new CustomerGradeNotFoundException(
                    "EMPTY"
            );
        }


        String normalizedGradeCode =
                gradeCode
                        .trim()
                        .toUpperCase();


        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                normalizedGradeCode
                        )
                        .orElseThrow(
                                () -> {

                                    log.warn(
                                            "고객 등급 수동 변경 실패 customerId={}, gradeCode={}",
                                            customerId,
                                            normalizedGradeCode
                                    );

                                    return new CustomerGradeNotFoundException(
                                            normalizedGradeCode
                                    );
                                }
                        );


        customer.changeGradeManually(
                grade
        );


        log.info(
                "고객 등급 수동 변경 완료 customerId={}, gradeCode={}",
                customerId,
                normalizedGradeCode
        );


        return customer;
    }



    // =====================================================
    // 수동 등급 해제 → 자동 등급
    // =====================================================

    @Transactional
    public CustomerProfile changeToAutomaticGrade(
            Long customerId
    ) {

        log.info(
                "고객 자동 등급 전환 시작 customerId={}",
                customerId
        );


        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );


        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                gradeCode
                        )
                        .orElseThrow(
                                () -> new CustomerGradeNotFoundException(
                                        gradeCode
                                )
                        );


        customer.changeGradeAutomatically(
                grade
        );


        log.info(
                "고객 자동 등급 전환 완료 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        return customer;
    }



    // =====================================================
    // 전화번호 정규화
    // =====================================================

    private String normalizePhone(
            String phone
    ) {

        if (phone == null) {

            return null;
        }


        return phone.replaceAll(
                "[^0-9]",
                ""
        );
    }



    // =====================================================
    // 개인정보 로그 마스킹
    // =====================================================

    private String maskPhone(
            String phone
    ) {

        if (phone == null
                || phone.length() < 8) {

            return "****";
        }


        return phone.substring(
                0,
                3
        )
                + "****"
                + phone.substring(
                phone.length() - 4
        );
    }
}