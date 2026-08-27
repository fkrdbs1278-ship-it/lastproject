package com.young04.lastproject.customerprofile.service;

import com.young04.lastproject.customergrade.entity.CustomerGrade;
import com.young04.lastproject.customergrade.exception.CustomerGradeNotFoundException;
import com.young04.lastproject.customergrade.service.CustomerGradeService;
import com.young04.lastproject.customerprofile.dto.CustomerSearchCondition;
import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.exception.CustomerNotFoundException;
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

    // 기본 고객 조회 Repository
    private final CustomerProfileRepository customerProfileRepository;

    // 이름 / 전화번호 / 등급 / 미방문 기간 등
    // 복합 검색을 처리하는 Custom Repository
    private final CustomerProfileRepositoryCustom customerProfileRepositoryCustom;

    // 고객 등급 조회 및 자동 계산 Service
    private final CustomerGradeService customerGradeService;


    // =====================================================
    // 고객 전체 조회
    // =====================================================

    /**
     * 등록되어 있는 전체 고객을 조회합니다.
     */
    public List<CustomerProfile> findAllCustomers() {

        log.info("고객 전체 조회");

        return customerProfileRepository.findAll();
    }


    // =====================================================
    // 고객 번호로 조회 - 기존 Optional 방식
    // =====================================================

    /**
     * 기존 코드와의 호환성을 위해 유지하는 조회 메서드입니다.
     *
     * 아직 기존 Controller 일부가 Optional 방식으로
     * 고객 존재 여부를 확인하고 있기 때문에
     * 당장 삭제하지 않고 유지합니다.
     *
     * 이후 Controller를 Advice 방식으로 변경하면
     * getCustomerById()를 사용하게 됩니다.
     */
    public Optional<CustomerProfile> findByCustomerId(
            Long customerId
    ) {

        log.info(
                "고객 상세 조회 customerId={}",
                customerId
        );

        return customerProfileRepository.findById(customerId);
    }


    // =====================================================
    // 고객 번호로 조회 - Advice 예외 처리 방식
    // =====================================================

    /**
     * CUSTOMER_ID를 기준으로 고객 한 명을 조회합니다.
     *
     * 고객이 존재하지 않을 경우 Optional.empty()를 반환하지 않고
     * CustomerNotFoundException을 발생시킵니다.
     *
     * 발생한 예외는 CustomerCrmExceptionAdvice에서
     * 공통으로 처리합니다.
     */
    public CustomerProfile getCustomerById(
            Long customerId
    ) {

        log.info(
                "고객 조회 customerId={}",
                customerId
        );

        return customerProfileRepository
                .findById(customerId)
                .orElseThrow(() -> {

                    log.warn(
                            "고객을 찾을 수 없음 customerId={}",
                            customerId
                    );

                    return new CustomerNotFoundException(
                            customerId
                    );
                });
    }


    // =====================================================
    // 전화번호로 고객 조회
    // =====================================================

    /**
     * 고객 전화번호를 기준으로 고객을 조회합니다.
     *
     * 개인정보 보호를 위해 로그에는
     * 전화번호 전체를 출력하지 않고 마스킹합니다.
     */
    public Optional<CustomerProfile> findByPhone(
            String phone
    ) {

        log.info(
                "전화번호 기준 고객 조회 phone={}",
                maskPhone(phone)
        );

        return customerProfileRepository.findByPhone(phone);
    }


    // =====================================================
    // 회원 번호로 고객 조회
    // =====================================================

    /**
     * MEMBER 테이블의 회원 번호를 기준으로
     * CRM 고객 정보를 조회합니다.
     *
     * 회원 고객은 MEMBER_NO가 존재하고,
     * 비회원 / 전화예약 고객은 MEMBER_NO가 NULL일 수 있습니다.
     */
    public Optional<CustomerProfile> findByMemberNo(
            Long memberNo
    ) {

        log.info(
                "회원 번호 기준 고객 조회 memberNo={}",
                memberNo
        );

        return customerProfileRepository
                .findByMemberNo(memberNo);
    }


    // =====================================================
    // 복합 조건 고객 검색
    // =====================================================

    /**
     * 관리자 고객 CRM 목록 화면에서 사용하는 검색 기능입니다.
     *
     * 현재 검색 가능한 조건:
     *
     * - 이름 / 전화번호
     * - 회원 / 비회원
     * - 고객 등급
     * - 활성 여부
     * - 30일 / 60일 미방문 고객
     */
    public List<CustomerProfile> searchCustomers(
            CustomerSearchCondition condition
    ) {

        log.info("고객 CRM 조건 검색");

        return customerProfileRepositoryCustom
                .searchCustomers(condition);
    }


    // =====================================================
    // 전화번호 중복 여부
    // =====================================================

    /**
     * 동일한 전화번호로 등록된 고객이 있는지 확인합니다.
     *
     * 추후 전화예약 고객을 직접 등록할 때
     * 중복 고객 생성을 방지하는 용도로 사용할 수 있습니다.
     */
    public boolean existsByPhone(
            String phone
    ) {

        return customerProfileRepository
                .existsByPhone(phone);
    }


    // =====================================================
    // 고객 등급 자동 적용
    // =====================================================

    /**
     * 고객의 현재 방문 횟수와 누적 결제 금액을 기준으로
     * 고객 등급을 자동 계산하여 적용합니다.
     *
     * 등급 기준:
     *
     * NORMAL
     * - 방문 0 ~ 2회
     *
     * REGULAR
     * - 방문 3 ~ 9회
     *
     * VIP
     * - 방문 10회 이상
     * 또는
     * - 누적 결제 금액 1,000,000원 이상
     *
     * 단,
     * GRADE_MANUAL_YN = Y이면 관리자가 직접 지정한 등급이므로
     * 자동 등급 계산으로 덮어쓰지 않습니다.
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
        // 고객이 존재하지 않으면
        // CustomerNotFoundException 발생
        CustomerProfile customer =
                getCustomerById(customerId);


        // -------------------------------------------------
        // 2. 수동 등급 고객인지 확인
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
        // 3. 방문 횟수 + 누적 결제 금액으로
        //    적용해야 할 등급 코드 계산
        // -------------------------------------------------

        String gradeCode =
                customerGradeService
                        .calculateGradeCode(
                                customer.getVisitCount(),
                                customer.getTotalPayment()
                        );


        // -------------------------------------------------
        // 4. 계산된 등급 Entity 조회
        // -------------------------------------------------
        // CUSTOMER_GRADE 테이블에 필요한 등급 데이터가 없으면
        // CustomerGradeNotFoundException 발생
        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(gradeCode)
                        .orElseThrow(() -> {

                            log.error(
                                    "자동 등급 적용 실패 - 등급 정보 없음 gradeCode={}",
                                    gradeCode
                            );

                            return new CustomerGradeNotFoundException(
                                    gradeCode
                            );
                        });


        // -------------------------------------------------
        // 5. 자동 등급 적용
        // -------------------------------------------------

        customer.applyAutomaticGrade(grade);


        log.info(
                "고객 자동 등급 적용 완료 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        /*
         * customer는 현재 JPA 영속 상태입니다.
         *
         * @Transactional 안에서 Entity 값을 변경했기 때문에
         * 트랜잭션 종료 시 Dirty Checking으로 UPDATE가 실행됩니다.
         *
         * 따라서 아래처럼 별도의 save()는 필요하지 않습니다.
         *
         * customerProfileRepository.save(customer);
         */

        return customer;
    }


    // =====================================================
    // 관리자 고객 등급 수동 변경
    // =====================================================

    /**
     * 관리자가 고객의 등급을 직접 변경합니다.
     *
     * 예:
     *
     * NORMAL
     * REGULAR
     * VIP
     *
     * 관리자가 직접 변경하면:
     *
     * GRADE_MANUAL_YN = Y
     *
     * 로 저장됩니다.
     *
     * 이후 자동 등급 계산이 실행되어도
     * 관리자가 지정한 등급은 유지됩니다.
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
                getCustomerById(customerId);


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


        /*
         * 혹시 화면에서 normal / Normal처럼 전달되어도
         * DB의 NORMAL / REGULAR / VIP와 맞도록
         * 대문자로 통일합니다.
         */
        String normalizedGradeCode =
                gradeCode
                        .trim()
                        .toUpperCase();


        // -------------------------------------------------
        // 3. 변경할 고객 등급 조회
        // -------------------------------------------------

        CustomerGrade grade =
                customerGradeService
                        .findByGradeCode(
                                normalizedGradeCode
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "고객 등급 수동 변경 실패 - 존재하지 않는 등급 customerId={}, gradeCode={}",
                                    customerId,
                                    normalizedGradeCode
                            );

                            return new CustomerGradeNotFoundException(
                                    normalizedGradeCode
                            );
                        });


        // -------------------------------------------------
        // 4. 고객 등급 직접 변경
        // -------------------------------------------------

        customer.changeGradeManually(grade);


        log.info(
                "고객 등급 수동 변경 완료 customerId={}, gradeCode={}",
                customerId,
                normalizedGradeCode
        );


        /*
         * Entity 변경 감지(Dirty Checking)에 의해
         * 트랜잭션 종료 시 자동 UPDATE됩니다.
         */

        return customer;
    }


    // =====================================================
    // 수동 등급 해제 → 자동 등급으로 전환
    // =====================================================

    /**
     * 관리자가 직접 지정했던 등급을 해제하고
     * 다시 자동 등급 관리 상태로 변경합니다.
     *
     * 현재 고객의 방문 횟수와 누적 결제 금액을 기준으로
     * 새로운 등급을 즉시 계산합니다.
     *
     * 처리 후:
     *
     * GRADE_MANUAL_YN = N
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
                getCustomerById(customerId);


        // -------------------------------------------------
        // 2. 현재 실적으로 자동 등급 계산
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
                        .findByGradeCode(gradeCode)
                        .orElseThrow(() -> {

                            log.error(
                                    "자동 등급 전환 실패 - 등급 정보 없음 gradeCode={}",
                                    gradeCode
                            );

                            return new CustomerGradeNotFoundException(
                                    gradeCode
                            );
                        });


        // -------------------------------------------------
        // 4. 수동 여부를 N으로 바꾸고
        //    계산된 자동 등급 적용
        // -------------------------------------------------

        customer.changeGradeAutomatically(grade);


        log.info(
                "고객 자동 등급 전환 완료 customerId={}, gradeCode={}",
                customerId,
                gradeCode
        );


        return customer;
    }


    // =====================================================
    // 개인정보 로그 마스킹
    // =====================================================

    /**
     * 로그 파일에 전화번호 전체가 노출되지 않도록
     * 가운데 번호를 **** 형태로 처리합니다.
     *
     * 예:
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


        return phone.substring(0, 3)
                + "****"
                + phone.substring(
                phone.length() - 4
        );
    }
}