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
import com.young04.lastproject.member.entity.Member;
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


    private final CustomerProfileRepository customerProfileRepository;

    private final CustomerProfileRepositoryCustom customerProfileRepositoryCustom;

    private final CustomerGradeService customerGradeService;



    // =====================================================
    // 고객 전체 조회
    // =====================================================

    public List<CustomerProfile> findAllCustomers() {

        return customerProfileRepository
                .findAll();
    }



    // =====================================================
    // 고객 번호 Optional 조회
    // =====================================================

    public Optional<CustomerProfile> findByCustomerId(
            Long customerId
    ) {

        return customerProfileRepository
                .findById(
                        customerId
                );
    }



    // =====================================================
    // 고객 번호 필수 조회
    // =====================================================

    public CustomerProfile getCustomerById(
            Long customerId
    ) {

        return customerProfileRepository
                .findById(
                        customerId
                )
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                customerId
                        )
                );
    }



    // =====================================================
    // 전화번호 조회
    // =====================================================

    public Optional<CustomerProfile> findByPhone(
            String phone
    ) {

        String phoneDigits =
                extractPhoneDigits(
                        phone
                );


        String formattedPhone =
                formatPhone(
                        phoneDigits
                );


        Optional<CustomerProfile> customer =
                customerProfileRepository
                        .findByPhone(
                                formattedPhone
                        );


        if (customer.isPresent()) {

            return customer;
        }


        return customerProfileRepository
                .findByPhone(
                        phoneDigits
                );
    }



    // =====================================================
    // 회원 번호 조회
    // =====================================================

    public Optional<CustomerProfile> findByMemberNo(
            Long memberNo
    ) {

        return customerProfileRepository
                .findByMemberNo(
                        memberNo
                );
    }



    // =====================================================
    // 회원 번호 필수 조회
    // =====================================================

    public CustomerProfile getCustomerByMemberNo(
            Long memberNo
    ) {

        return customerProfileRepository
                .findByMemberNo(
                        memberNo
                )
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                memberNo
                        )
                );
    }



    // =====================================================
    // 고객 검색 + 페이징
    // =====================================================

    public Page<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition,
            Pageable pageable
    ) {

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

        String digits =
                extractPhoneDigits(
                        phone
                );


        String formattedPhone =
                formatPhone(
                        digits
                );


        return customerProfileRepository
                .existsByPhone(
                        formattedPhone
                )
                ||
                customerProfileRepository
                        .existsByPhone(
                                digits
                        );
    }



    // =====================================================
    // 회원 고객 생성 / 동기화
    // =====================================================

    /**
     * MEMBER와 CUSTOMER_PROFILE을 연결합니다.
     *
     * 1. 이미 MEMBER_NO로 연결된 고객이 있으면
     *    회원 이름 / 전화번호를 동기화합니다.
     *
     * 2. 같은 전화번호의 GUEST 고객이 있으면
     *    기존 방문 / 결제 / 등급 정보는 유지한 채
     *    MEMBER 고객으로 전환합니다.
     *
     * 3. 아무 고객도 없으면 NORMAL 등급의
     *    새 MEMBER 고객을 생성합니다.
     */
    @Transactional
    public CustomerProfile syncMemberCustomer(
            Member member
    ) {

        if (member == null
                || member.getNo() == null) {

            throw new IllegalArgumentException(
                    "저장된 회원 정보가 필요합니다."
            );
        }

        String customerName =
                member.getName() == null
                        ? null
                        : member.getName().trim();

        String digits =
                extractPhoneDigits(
                        member.getPhone()
                );

        String formattedPhone =
                formatPhone(
                        digits
                );

        // -------------------------------------------------
        // 1. 이미 회원번호로 연결된 CRM 고객
        // -------------------------------------------------

        Optional<CustomerProfile> memberCustomer =
                customerProfileRepository
                        .findByMemberNo(
                                member.getNo()
                        );

        if (memberCustomer.isPresent()) {

            CustomerProfile customer =
                    memberCustomer.get();

            Optional<CustomerProfile> phoneOwner =
                    findByPhone(
                            formattedPhone
                    );

            if (phoneOwner.isPresent()
                    && !phoneOwner.get()
                    .getCustomerId()
                    .equals(
                            customer.getCustomerId()
                    )) {

                throw new IllegalStateException(
                        "해당 전화번호가 다른 CRM 고객과 연결되어 있습니다."
                );
            }

            customer.linkMember(
                    member.getNo(),
                    customerName,
                    formattedPhone
            );

            log.info(
                    "회원 CRM 동기화 완료 memberNo={}, customerId={}",
                    member.getNo(),
                    customer.getCustomerId()
            );

            return customer;
        }


        // -------------------------------------------------
        // 2. 같은 전화번호의 기존 GUEST 고객
        //    → 회원 고객으로 승격
        // -------------------------------------------------

        Optional<CustomerProfile> phoneCustomer =
                findByPhone(
                        formattedPhone
                );

        if (phoneCustomer.isPresent()) {

            CustomerProfile customer =
                    phoneCustomer.get();

            if (customer.getMemberNo() != null
                    && !customer.getMemberNo()
                    .equals(
                            member.getNo()
                    )) {

                throw new IllegalStateException(
                        "해당 전화번호가 이미 다른 회원 고객과 연결되어 있습니다."
                );
            }

            customer.linkMember(
                    member.getNo(),
                    customerName,
                    formattedPhone
            );

            log.info(
                    "기존 GUEST 고객 회원 연결 완료 memberNo={}, customerId={}",
                    member.getNo(),
                    customer.getCustomerId()
            );

            return customer;
        }


        // -------------------------------------------------
        // 3. 신규 MEMBER 고객 생성
        // -------------------------------------------------

        CustomerGrade normalGrade =
                customerGradeService
                        .findByGradeCode(
                                "NORMAL"
                        )
                        .orElseThrow(
                                () -> new CustomerGradeNotFoundException(
                                        "NORMAL"
                                )
                        );

        CustomerProfile customer =
                CustomerProfile
                        .createMemberCustomer(
                                member.getNo(),
                                customerName,
                                formattedPhone,
                                normalGrade
                        );

        CustomerProfile savedCustomer =
                customerProfileRepository
                        .save(
                                customer
                        );

        log.info(
                "회원 CRM 고객 생성 완료 memberNo={}, customerId={}",
                member.getNo(),
                savedCustomer.getCustomerId()
        );

        return savedCustomer;
    }



    // =====================================================
    // 전화예약 고객 등록
    // =====================================================

    @Transactional
    public CustomerProfile createGuestCustomer(
            CustomerCreateRequest request
    ) {

        String customerName =
                request
                        .getCustomerName()
                        .trim();


        String digits =
                extractPhoneDigits(
                        request.getPhone()
                );


        String formattedPhone =
                formatPhone(
                        digits
                );


        if (existsByPhone(
                formattedPhone
        )) {

            throw new DuplicateCustomerPhoneException();
        }


        CustomerGrade normalGrade =
                customerGradeService
                        .findByGradeCode(
                                "NORMAL"
                        )
                        .orElseThrow(
                                () -> new CustomerGradeNotFoundException(
                                        "NORMAL"
                                )
                        );


        CustomerProfile customer =
                CustomerProfile
                        .createGuestCustomer(
                                customerName,
                                formattedPhone,
                                normalGrade
                        );


        return customerProfileRepository
                .save(
                        customer
                );
    }



    // =====================================================
    // 고객 기본정보 수정
    // =====================================================

    @Transactional
    public CustomerProfile updateCustomer(
            Long customerId,
            String customerName,
            String phone
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        String digits =
                extractPhoneDigits(
                        phone
                );


        String formattedPhone =
                formatPhone(
                        digits
                );


        Optional<CustomerProfile> duplicated =
                findByPhone(
                        formattedPhone
                );


        if (duplicated.isPresent()
                &&
                !duplicated.get()
                        .getCustomerId()
                        .equals(
                                customerId
                        )) {

            throw new DuplicateCustomerPhoneException();
        }


        customer.updateBasicInfo(
                customerName,
                formattedPhone
        );


        log.info(
                "고객 기본정보 수정 완료 customerId={}, customerName={}, phone={}",
                customerId,
                customerName,
                maskPhone(
                        formattedPhone
                )
        );


        return customer;
    }



    // =====================================================
    // 고객 비활성 처리
    // =====================================================

    @Transactional
    public CustomerProfile deactivateCustomer(
            Long customerId
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        customer.deactivate();


        log.info(
                "고객 비활성 처리 완료 customerId={}",
                customerId
        );


        return customer;
    }



    // =====================================================
    // 고객 활성 처리
    // =====================================================

    @Transactional
    public CustomerProfile activateCustomer(
            Long customerId
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        customer.activate();


        log.info(
                "고객 활성 처리 완료 customerId={}",
                customerId
        );


        return customer;
    }



    // =====================================================
    // 고객 방문 완료
    // =====================================================

    @Transactional
    public CustomerProfile completeVisit(
            Long customerId,
            LocalDate visitDate
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        customer.recordVisit(
                visitDate
        );


        applyAutomaticGradeToCustomer(
                customer
        );


        return customer;
    }



    // =====================================================
    // 결제 금액 누적
    // =====================================================

    @Transactional
    public CustomerProfile addCustomerPayment(
            Long customerId,
            BigDecimal paymentAmount
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        customer.addPayment(
                paymentAmount
        );


        applyAutomaticGradeToCustomer(
                customer
        );


        return customer;
    }



    // =====================================================
    // 자동 등급 적용
    // =====================================================

    @Transactional
    public CustomerProfile applyAutomaticGrade(
            Long customerId
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        applyAutomaticGradeToCustomer(
                customer
        );


        return customer;
    }



    // =====================================================
    // 자동 등급 공통 처리
    // =====================================================

    private void applyAutomaticGradeToCustomer(
            CustomerProfile customer
    ) {

        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

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
                                () -> new CustomerGradeNotFoundException(
                                        gradeCode
                                )
                        );


        customer.applyAutomaticGrade(
                grade
        );
    }



    // =====================================================
    // 등급 수동 변경
    // =====================================================

    @Transactional
    public CustomerProfile changeGradeManually(
            Long customerId,
            String gradeCode
    ) {

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );


        if (gradeCode == null
                || gradeCode.isBlank()) {

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
                                () -> new CustomerGradeNotFoundException(
                                        normalizedGradeCode
                                )
                        );


        customer.changeGradeManually(
                grade
        );


        return customer;
    }



    // =====================================================
    // 자동 등급으로 복귀
    // =====================================================

    @Transactional
    public CustomerProfile changeToAutomaticGrade(
            Long customerId
    ) {

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


        return customer;
    }



    // =====================================================
    // 전화번호 숫자 추출
    // =====================================================

    private String extractPhoneDigits(
            String phone
    ) {

        if (phone == null
                || phone.isBlank()) {

            throw new IllegalArgumentException(
                    "전화번호는 필수입니다."
            );
        }


        String digits =
                phone.replaceAll(
                        "[^0-9]",
                        ""
                );


        if (digits.isBlank()) {

            throw new IllegalArgumentException(
                    "올바른 전화번호를 입력해주세요."
            );
        }


        return digits;
    }



    // =====================================================
    // 전화번호 표준 포맷
    // =====================================================

    private String formatPhone(
            String digits
    ) {

        if (digits.startsWith(
                "02"
        )) {

            if (digits.length() == 9) {

                return digits.substring(
                        0,
                        2
                )
                        + "-"
                        + digits.substring(
                        2,
                        5
                )
                        + "-"
                        + digits.substring(
                        5
                );
            }


            if (digits.length() == 10) {

                return digits.substring(
                        0,
                        2
                )
                        + "-"
                        + digits.substring(
                        2,
                        6
                )
                        + "-"
                        + digits.substring(
                        6
                );
            }
        }


        if (digits.length() == 11) {

            return digits.substring(
                    0,
                    3
            )
                    + "-"
                    + digits.substring(
                    3,
                    7
            )
                    + "-"
                    + digits.substring(
                    7
            );
        }


        if (digits.length() == 10) {

            return digits.substring(
                    0,
                    3
            )
                    + "-"
                    + digits.substring(
                    3,
                    6
            )
                    + "-"
                    + digits.substring(
                    6
            );
        }


        throw new IllegalArgumentException(
                "올바른 전화번호 형식이 아닙니다."
        );
    }



    // =====================================================
    // 전화번호 로그 마스킹
    // =====================================================

    private String maskPhone(
            String phone
    ) {

        if (phone == null
                || phone.isBlank()) {

            return "****";
        }


        String digits =
                phone.replaceAll(
                        "[^0-9]",
                        ""
                );


        if (digits.length() < 8) {

            return "****";
        }


        return digits.substring(
                0,
                3
        )
                + "-****-"
                + digits.substring(
                digits.length() - 4
        );
    }
}