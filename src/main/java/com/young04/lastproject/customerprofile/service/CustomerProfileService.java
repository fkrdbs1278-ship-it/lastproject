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

    private final CustomerProfileRepository
            customerProfileRepository;

    private final CustomerProfileRepositoryCustom
            customerProfileRepositoryCustom;

    private final CustomerGradeService
            customerGradeService;



    // =====================================================
    // 고객 전체 조회
    // =====================================================

    /**
     * 등록되어 있는 전체 고객을 조회합니다.
     *
     * 관리자 고객 목록에서는
     * searchCustomers()의 페이징 기능을 사용합니다.
     */
    public List<CustomerProfile> findAllCustomers() {

        log.info(
                "고객 전체 조회"
        );

        return customerProfileRepository
                .findAll();
    }



    // =====================================================
    // 고객 번호로 조회 - Optional 방식
    // =====================================================

    /**
     * 기존 코드와의 호환성을 위해 유지합니다.
     */
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
    // 고객 번호로 조회 - 필수 조회
    // =====================================================

    /**
     * 고객번호로 고객을 조회합니다.
     *
     * 고객이 존재하지 않으면
     * CustomerNotFoundException을 발생시킵니다.
     */
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
    // 전화번호로 고객 조회
    // =====================================================

    /**
     * 전화번호에서 하이픈 등을 제거한 후
     * 고객을 조회합니다.
     */
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
    // 회원 번호로 고객 조회 - Optional 방식
    // =====================================================

    /**
     * MEMBER 테이블의 회원 번호를 기준으로
     * CRM 고객을 조회합니다.
     *
     * 회원 고객:
     * MEMBER_NO 존재
     *
     * 전화예약 / 비회원 고객:
     * MEMBER_NO NULL 가능
     */
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
    // 회원 번호로 CRM 고객 필수 조회
    // =====================================================

    /**
     * 로그인 회원의 MEMBER_NO를 기준으로
     * CUSTOMER_PROFILE을 조회합니다.
     *
     * 향후 사용자 기능에서 사용합니다.
     *
     * 사용 예정:
     *
     * - 본인의 시술 이력 확인
     * - 본인의 예약 이력 확인
     * - 본인의 결제 이력 확인
     * - 고객 활동 연결
     *
     * 최종 구조:
     *
     * 로그인 사용자
     *      ↓
     * MEMBER.NO
     *      ↓
     * CUSTOMER_PROFILE.MEMBER_NO
     *      ↓
     * CUSTOMER_ID
     *      ↓
     * 시술 / 예약 / 결제 이력
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

                            /*
                             * 현재 프로젝트의 공통 고객 미존재 예외를
                             * 재사용합니다.
                             *
                             * 이후 MEMBER 연동 시 필요하면
                             * MemberCustomerProfileNotFoundException 같은
                             * 전용 예외로 분리할 수 있습니다.
                             */
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
     * 관리자 CRM 고객 목록의 조건 검색입니다.
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
     * 페이징:
     *
     * page = 현재 페이지
     * size = 한 페이지 고객 수
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
    // 전화번호 중복 여부
    // =====================================================

    /**
     * 입력된 전화번호를 숫자만 남도록 정규화한 후
     * 이미 등록된 고객인지 확인합니다.
     */
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
    // 전화예약 / 비회원 고객 직접 등록
    // =====================================================

    /**
     * 관리자가 전화 예약 고객을
     * CUSTOMER_PROFILE에 직접 등록합니다.
     *
     * 신규 전화예약 고객 초기값:
     *
     * MEMBER_NO       = NULL
     * CUSTOMER_TYPE   = GUEST
     * GRADE_CODE      = NORMAL
     * GRADE_MANUAL_YN = N
     * VISIT_COUNT     = 0
     * TOTAL_PAYMENT   = 0
     * ACTIVE_YN       = Y
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
        // 2. 전화번호 정규화
        // -------------------------------------------------

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


        // -------------------------------------------------
        // 3. 동일 전화번호 고객 존재 여부 확인
        // -------------------------------------------------

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


        // -------------------------------------------------
        // 4. 신규 고객 기본 등급 NORMAL 조회
        // -------------------------------------------------

        CustomerGrade normalGrade =
                customerGradeService
                        .findByGradeCode(
                                "NORMAL"
                        )
                        .orElseThrow(
                                () -> {

                                    log.error(
                                            "전화예약 고객 등록 실패 - NORMAL 등급 정보 없음"
                                    );

                                    return new CustomerGradeNotFoundException(
                                            "NORMAL"
                                    );
                                }
                        );


        // -------------------------------------------------
        // 5. 비회원 고객 Entity 생성
        // -------------------------------------------------

        CustomerProfile customer =
                CustomerProfile
                        .createGuestCustomer(
                                customerName,
                                normalizedPhone,
                                normalGrade
                        );


        // -------------------------------------------------
        // 6. CUSTOMER_PROFILE 저장
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
    // 고객 등급 자동 적용
    // =====================================================

    /**
     * 현재 방문 횟수와 누적 결제 금액으로
     * 고객 등급을 자동 계산합니다.
     *
     * 수동 지정 고객은 자동 변경하지 않습니다.
     */
    @Transactional
    public CustomerProfile applyAutomaticGrade(
            Long customerId
    ) {

        log.info(
                "고객 자동 등급 적용 시작 customerId={}",
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
        // 2. 수동 등급 여부 확인
        // -------------------------------------------------

        if ("Y".equals(
                customer.getGradeManualYn()
        )) {

            log.info(
                    "자동 등급 적용 제외 - 수동 등급 고객 customerId={}",
                    customerId
            );

            return customer;
        }


        // -------------------------------------------------
        // 3. 등급 코드 자동 계산
        // -------------------------------------------------

        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );


        // -------------------------------------------------
        // 4. 등급 Entity 조회
        // -------------------------------------------------

        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                gradeCode
                        )
                        .orElseThrow(
                                () -> {

                                    log.error(
                                            "자동 등급 적용 실패 - 등급 정보 없음 gradeCode={}",
                                            gradeCode
                                    );

                                    return new CustomerGradeNotFoundException(
                                            gradeCode
                                    );
                                }
                        );


        // -------------------------------------------------
        // 5. 등급 적용
        // -------------------------------------------------

        customer.applyAutomaticGrade(
                grade
        );


        log.info(
                "고객 자동 등급 적용 완료 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        return customer;
    }



    // =====================================================
    // 관리자 고객 등급 수동 변경
    // =====================================================

    /**
     * 관리자가 고객 등급을 직접 변경합니다.
     *
     * 변경 후 GRADE_MANUAL_YN = Y가 됩니다.
     */
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
                                            "고객 등급 수동 변경 실패 - 존재하지 않는 등급 customerId={}, gradeCode={}",
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
    // 수동 등급 해제 → 자동 등급 관리
    // =====================================================

    /**
     * 수동 지정한 고객 등급을 해제하고
     * 현재 실적 기준 자동 등급으로 돌아갑니다.
     *
     * 변경 후 GRADE_MANUAL_YN = N
     */
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
        // 3. 계산된 등급 Entity 조회
        // -------------------------------------------------

        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                gradeCode
                        )
                        .orElseThrow(
                                () -> {

                                    log.error(
                                            "자동 등급 전환 실패 - 등급 정보 없음 gradeCode={}",
                                            gradeCode
                                    );

                                    return new CustomerGradeNotFoundException(
                                            gradeCode
                                    );
                                }
                        );


        // -------------------------------------------------
        // 4. 자동 등급으로 전환
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
    // 전화번호 정규화
    // =====================================================

    /**
     * 전화번호에서 숫자가 아닌 문자를 모두 제거합니다.
     *
     * 010-1234-5678
     * →
     * 01012345678
     */
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

    /**
     * 로그에 전화번호 전체가 노출되지 않도록 처리합니다.
     *
     * 01012345678
     * →
     * 010****5678
     */
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