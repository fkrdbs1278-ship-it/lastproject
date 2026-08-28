package com.young04.lastproject.treatmenthistory.service;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import com.young04.lastproject.treatmenthistory.entity.TreatmentHistory;
import com.young04.lastproject.treatmenthistory.repository.TreatmentHistoryRepository;
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
public class TreatmentHistoryService {


    // =====================================================
    // Repository / Service
    // =====================================================

    private final TreatmentHistoryRepository treatmentHistoryRepository;

    // MEMBER_NO → CUSTOMER_PROFILE 연결
    private final CustomerProfileService customerProfileService;



    // =====================================================
    // 고객별 시술 이력 조회
    // =====================================================

    public List<TreatmentHistory> findByCustomerId(
            Long customerId
    ) {

        log.info(
                "고객 시술 이력 조회 customerId={}",
                customerId
        );

        return treatmentHistoryRepository
                .findByCustomer_CustomerIdOrderByTreatmentDateDescTreatmentIdDesc(
                        customerId
                );
    }



    // =====================================================
    // 회원 번호 기준 본인 시술 이력 조회
    // =====================================================

    /**
     * 로그인 회원의 MEMBER_NO를 기준으로
     * CRM 고객을 찾은 뒤 본인의 시술 이력을 조회합니다.
     *
     * 처리 흐름:
     *
     * MEMBER_NO
     *      ↓
     * CUSTOMER_PROFILE
     *      ↓
     * CUSTOMER_ID
     *      ↓
     * TREATMENT_HISTORY
     *
     * 향후 사용자 마이페이지의
     * "내 시술 이력" 기능에서 사용합니다.
     */
    public List<TreatmentHistory> findMyTreatmentsByMemberNo(
            Long memberNo
    ) {

        log.info(
                "회원번호 기준 본인 시술 이력 조회 시작 memberNo={}",
                memberNo
        );


        // -------------------------------------------------
        // 1. MEMBER_NO로 CRM 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerByMemberNo(
                                memberNo
                        );


        // -------------------------------------------------
        // 2. CUSTOMER_ID 확인
        // -------------------------------------------------

        Long customerId =
                customer.getCustomerId();


        // -------------------------------------------------
        // 3. 본인 시술 이력 조회
        // -------------------------------------------------

        List<TreatmentHistory> treatments =
                treatmentHistoryRepository
                        .findByCustomer_CustomerIdOrderByTreatmentDateDescTreatmentIdDesc(
                                customerId
                        );


        log.info(
                "회원번호 기준 본인 시술 이력 조회 완료 memberNo={}, customerId={}, count={}",
                memberNo,
                customerId,
                treatments.size()
        );


        return treatments;
    }



    // =====================================================
    // 시술 이력 번호로 상세 조회
    // =====================================================

    public Optional<TreatmentHistory> findByTreatmentId(
            Long treatmentId
    ) {

        log.info(
                "시술 이력 상세 조회 treatmentId={}",
                treatmentId
        );

        return treatmentHistoryRepository
                .findById(
                        treatmentId
                );
    }



    // =====================================================
    // 예약 번호로 시술 이력 조회
    // =====================================================

    public Optional<TreatmentHistory> findByReservationNo(
            Long reservationNo
    ) {

        log.info(
                "예약 번호 기준 시술 이력 조회 reservationNo={}",
                reservationNo
        );

        return treatmentHistoryRepository
                .findByReservationNo(
                        reservationNo
                );
    }



    // =====================================================
    // 고객별 시술 이력 건수
    // =====================================================

    public long countByCustomerId(
            Long customerId
    ) {

        log.info(
                "고객 시술 이력 건수 조회 customerId={}",
                customerId
        );

        return treatmentHistoryRepository
                .countByCustomer_CustomerId(
                        customerId
                );
    }
}