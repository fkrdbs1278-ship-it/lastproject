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
    // 고객 번호 조회 - Optional
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
    // 고객 번호 필수 조회
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

    /**
     * 입력 예:
     *
     * 01012345678
     * 010-1234-5678
     * 010 1234 5678
     *
     * 모두
     *
     * 010-1234-5678
     *
     * 형식으로 변환하여 조회합니다.
     *
     *
     * 기존 QA 데이터처럼
     * DB에 숫자만 저장된 고객도
     * 당분간 조회할 수 있도록 fallback 조회를 유지합니다.
     */
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


        log.info(
                "전화번호 기준 고객 조회 phone={}",
                maskPhone(
                        formattedPhone
                )
        );


        // -------------------------------------------------
        // 1. 새로운 표준 형식 조회
        // -------------------------------------------------

        Optional<CustomerProfile> customer =
                customerProfileRepository
                        .findByPhone(
                                formattedPhone
                        );


        if (customer.isPresent()) {

            return customer;
        }


        // -------------------------------------------------
        // 2. 기존 숫자-only 데이터 호환 조회
        // -------------------------------------------------

        return customerProfileRepository
                .findByPhone(
                        phoneDigits
                );
    }



    // =====================================================
    // 회원 번호 조회 - Optional
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
     * 1part 로그인 기능과 통합할 때
     * 로그인 MEMBER.NO를 기준으로
     * CRM 고객을 조회하기 위한 메서드입니다.
     *
     * 1part 코드는 여기서 수정하지 않습니다.
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

    /**
     * 관리자 고객관리 목록
     *
     * /admin/customers
     *
     * 에서 사용합니다.
     *
     * 검색:
     *
     * - 이름 / 전화번호
     * - 회원 / 비회원
     * - 등급
     * - 활성 / 비활성
     * - 30일 / 60일 이상 미방문
     *
     * 재방문 권장일 조건은 사용하지 않습니다.
     */
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

        String phoneDigits =
                extractPhoneDigits(
                        phone
                );


        String formattedPhone =
                formatPhone(
                        phoneDigits
                );


        /*
         * 새 데이터:
         *
         * 010-1234-5678
         *
         *
         * 기존 테스트 데이터:
         *
         * 01012345678
         *
         *
         * 두 형식 모두 중복검사합니다.
         */
        return customerProfileRepository
                .existsByPhone(
                        formattedPhone
                )
                ||
                customerProfileRepository
                        .existsByPhone(
                                phoneDigits
                        );
    }



    // =====================================================
    // 전화예약 / 비회원 고객 등록
    // =====================================================

    /**
     * 관리자 고객관리 화면에서
     * 전화예약 고객을 직접 등록합니다.
     *
     *
     * 입력자가:
     *
     * 01012345678
     *
     * 로 입력해도
     *
     * DB에는:
     *
     * 010-1234-5678
     *
     * 로 저장합니다.
     */
    @Transactional
    public CustomerProfile createGuestCustomer(
            CustomerCreateRequest request
    ) {

        log.info(
                "전화예약 고객 등록 시작 customerName={}",
                request.getCustomerName()
        );


        // -------------------------------------------------
        // 1. 고객명 정리
        // -------------------------------------------------

        String customerName =
                request
                        .getCustomerName()
                        .trim();



        // -------------------------------------------------
        // 2. 전화번호 숫자 추출
        // -------------------------------------------------

        String phoneDigits =
                extractPhoneDigits(
                        request.getPhone()
                );



        // -------------------------------------------------
        // 3. 전화번호 표준 형식 변환
        // -------------------------------------------------
        //
        // 01012345678
        //
        // ↓
        //
        // 010-1234-5678
        //
        // -------------------------------------------------

        String formattedPhone =
                formatPhone(
                        phoneDigits
                );


        log.info(
                "전화예약 고객 전화번호 표준화 phone={}",
                maskPhone(
                        formattedPhone
                )
        );



        // -------------------------------------------------
        // 4. 전화번호 중복 확인
        // -------------------------------------------------

        if (existsByPhone(
                formattedPhone
        )) {

            log.warn(
                    "전화예약 고객 등록 실패 - 중복 전화번호 phone={}",
                    maskPhone(
                            formattedPhone
                    )
            );


            throw new DuplicateCustomerPhoneException();
        }



        // -------------------------------------------------
        // 5. 신규 고객 NORMAL 등급 조회
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 6. 비회원 고객 Entity 생성
        // -------------------------------------------------

        CustomerProfile customer =
                CustomerProfile
                        .createGuestCustomer(
                                customerName,
                                formattedPhone,
                                normalGrade
                        );



        // -------------------------------------------------
        // 7. 저장
        // -------------------------------------------------

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
     * 고객 방문이 완료되면:
     *
     * VISIT_COUNT + 1
     * LAST_VISIT_DATE 변경
     * 자동 등급 재계산
     *
     *
     * 재방문 권장일은 더 이상 사용하지 않습니다.
     *
     * 장기 미방문 여부는
     * LAST_VISIT_DATE를 기준으로
     * 30일 / 60일로 계산합니다.
     *
     *
     * 향후 2part 예약 기능과 통합 시
     * 예약 상태 COMPLETED에서 호출할 수 있도록
     * 3part에 준비해두는 메서드입니다.
     */
    @Transactional
    public CustomerProfile completeVisit(
            Long customerId,
            LocalDate visitDate
    ) {

        log.info(
                "고객 방문 완료 처리 시작 customerId={}, visitDate={}",
                customerId,
                visitDate
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
                visitDate
        );



        // -------------------------------------------------
        // 3. 자동 등급 재계산
        // -------------------------------------------------

        applyAutomaticGradeToCustomer(
                customer
        );



        log.info(
                "고객 방문 완료 처리 완료 customerId={}, visitCount={}, lastVisitDate={}, gradeCode={}",
                customerId,
                customer.getVisitCount(),
                customer.getLastVisitDate(),
                customer.getCustomerGrade()
                        .getGradeCode()
        );


        return customer;
    }



    // =====================================================
    // 고객 결제 금액 누적
    // =====================================================

    /**
     * 결제 완료 금액을
     * 고객 누적 결제액에 추가합니다.
     *
     * TOTAL_PAYMENT 증가
     * 자동 등급 재계산
     *
     * 향후 4part 결제 기능과 통합할 수 있도록
     * 3part에 준비해둡니다.
     *
     * 4part 코드는 수정하지 않습니다.
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
        // 2. 누적 결제금액 증가
        // -------------------------------------------------

        customer.addPayment(
                paymentAmount
        );



        // -------------------------------------------------
        // 3. 자동 등급 재계산
        // -------------------------------------------------

        applyAutomaticGradeToCustomer(
                customer
        );



        log.info(
                "고객 결제금액 누적 완료 customerId={}, totalPayment={}, gradeCode={}",
                customerId,
                customer.getTotalPayment(),
                customer.getCustomerGrade()
                        .getGradeCode()
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
                customer.getCustomerGrade()
                        .getGradeCode(),
                customer.getGradeManualYn()
        );


        return customer;
    }



    // =====================================================
    // 자동 등급 계산 공통 처리
    // =====================================================

    /**
     * 다음 기능에서 공통으로 사용합니다.
     *
     * - 방문 완료
     * - 결제 누적
     * - 관리자 자동등급 재계산
     */
    private void applyAutomaticGradeToCustomer(
            CustomerProfile customer
    ) {


        // -------------------------------------------------
        // 수동 등급 고객은 자동변경하지 않음
        // -------------------------------------------------

        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

            log.info(
                    "자동 등급 적용 제외 - 수동 등급 고객 customerId={}",
                    customer.getCustomerId()
            );


            return;
        }



        // -------------------------------------------------
        // 현재 방문 / 결제 실적으로 등급 계산
        // -------------------------------------------------

        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );



        // -------------------------------------------------
        // 등급 Entity 조회
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 등급 적용
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 1. 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );



        // -------------------------------------------------
        // 2. 등급 코드 검증
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 3. 등급 코드 정규화
        // -------------------------------------------------

        String normalizedGradeCode =
                gradeCode
                        .trim()
                        .toUpperCase();



        // -------------------------------------------------
        // 4. 변경할 등급 조회
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 5. 수동 등급 변경
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 1. 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                getCustomerById(
                        customerId
                );



        // -------------------------------------------------
        // 2. 현재 실적으로 등급 계산
        // -------------------------------------------------

        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );



        // -------------------------------------------------
        // 3. 등급 Entity 조회
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 4. 자동 등급 관리로 전환
        // -------------------------------------------------

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
    // 전화번호 숫자 추출
    // =====================================================

    /**
     * 아래 입력을 모두 숫자로 변환합니다.
     *
     * 010-1234-5678
     * 010 1234 5678
     * 01012345678
     *
     * ↓
     *
     * 01012345678
     */
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
    // 전화번호 하이픈 자동 포맷
    // =====================================================

    /**
     * 숫자만 입력된 전화번호를
     * 하이픈 포함 표준 형식으로 변경합니다.
     *
     *
     * 01012345678
     *
     * ↓
     *
     * 010-1234-5678
     *
     *
     * 일반 10자리 번호도 지원합니다.
     *
     * 0311234567
     *
     * ↓
     *
     * 031-123-4567
     */
    private String formatPhone(
            String digits
    ) {


        // -------------------------------------------------
        // 서울 지역번호 02
        // -------------------------------------------------

        if (digits.startsWith("02")) {


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



        // -------------------------------------------------
        // 휴대폰 / 일반 11자리
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 일반 10자리
        // -------------------------------------------------

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
    // 개인정보 로그 마스킹
    // =====================================================

    /**
     * 로그에 전화번호 전체가 노출되지 않게 합니다.
     *
     * 010-1234-5678
     *
     * ↓
     *
     * 010-****-5678
     */
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