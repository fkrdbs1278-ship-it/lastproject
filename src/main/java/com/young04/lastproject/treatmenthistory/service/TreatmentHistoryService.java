package com.young04.lastproject.treatmenthistory.service;

import com.young04.lastproject.customerprofile.entity.CustomerProfile;
import com.young04.lastproject.customerprofile.service.CustomerProfileService;
import com.young04.lastproject.treatmenthistory.entity.TreatmentHistory;
import com.young04.lastproject.treatmenthistory.repository.TreatmentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class TreatmentHistoryService {


    // =====================================================
    // Repository / Service
    // =====================================================

    private final TreatmentHistoryRepository treatmentHistoryRepository;

    private final CustomerProfileService customerProfileService;



    // =====================================================
    // 고객별 시술 이력 조회
    // =====================================================

    /**
     * 특정 고객의 시술이력을
     * 최근 시술일 순으로 조회합니다.
     */
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
    // 시술 이력 번호 조회
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
    // 시술 이력 번호 필수 조회
    // =====================================================

    public TreatmentHistory getByTreatmentId(
            Long treatmentId
    ) {

        log.info(
                "시술 이력 필수 조회 treatmentId={}",
                treatmentId
        );


        return treatmentHistoryRepository
                .findById(
                        treatmentId
                )
                .orElseThrow(
                        () -> {

                            log.warn(
                                    "시술 이력을 찾을 수 없음 treatmentId={}",
                                    treatmentId
                            );


                            return new IllegalArgumentException(
                                    "존재하지 않는 시술 이력입니다."
                            );
                        }
                );
    }



    // =====================================================
    // 예약 번호로 조회
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



    // =====================================================
    // 시술 이력 등록
    // =====================================================

    /**
     * 고객 시술이력을 저장합니다.
     *
     * 현재 3part 내부에서 독립적으로 사용 가능합니다.
     *
     * 추후 2part 예약 기능에서
     * 예약 상태가 시술 완료로 변경될 때
     * 이 메서드를 호출하는 방식으로 연동할 수 있습니다.
     *
     *
     * reservationNo
     *     → 아직 예약 연동이 없으면 null 가능
     *
     * serviceMenuNo
     *     → 아직 시술메뉴 연동이 없으면 null 가능
     */
    @Transactional
    public TreatmentHistory createTreatmentHistory(

            Long customerId,

            Long reservationNo,

            Long serviceMenuNo,

            String treatmentName,

            LocalDate treatmentDate,

            BigDecimal treatmentPrice,

            String treatmentMemo,

            LocalDate nextRecommendedDate

    ) {

        log.info(
                "시술 이력 등록 시작 customerId={}, reservationNo={}, treatmentName={}",
                customerId,
                reservationNo,
                treatmentName
        );



        // -------------------------------------------------
        // 1. 고객번호 확인
        // -------------------------------------------------

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "고객번호는 필수입니다."
            );
        }



        // -------------------------------------------------
        // 2. 고객 조회
        // -------------------------------------------------

        CustomerProfile customer =
                customerProfileService
                        .getCustomerById(
                                customerId
                        );



        // -------------------------------------------------
        // 3. 예약번호 중복 방지
        // -------------------------------------------------
        //
        // reservationNo가 null이면
        // 아직 예약 파트와 연결되지 않은 독립 시술이므로
        // 중복검사를 하지 않습니다.
        //
        // 실제 예약번호가 존재하면
        // 같은 예약으로 시술이력이 두 번 생성되지 않도록 합니다.
        // -------------------------------------------------

        if (reservationNo != null) {


            boolean duplicated =
                    treatmentHistoryRepository
                            .findByReservationNo(
                                    reservationNo
                            )
                            .isPresent();


            if (duplicated) {

                log.warn(
                        "시술 이력 등록 실패 - 예약번호 중복 reservationNo={}",
                        reservationNo
                );


                throw new IllegalArgumentException(
                        "이미 시술 이력이 등록된 예약입니다."
                );
            }
        }



        // -------------------------------------------------
        // 4. Entity 생성
        // -------------------------------------------------

        TreatmentHistory treatmentHistory =
                TreatmentHistory.create(

                        customer,

                        reservationNo,

                        serviceMenuNo,

                        treatmentName,

                        treatmentDate,

                        treatmentPrice,

                        treatmentMemo,

                        nextRecommendedDate
                );



        // -------------------------------------------------
        // 5. 저장
        // -------------------------------------------------

        TreatmentHistory savedTreatment =
                treatmentHistoryRepository
                        .save(
                                treatmentHistory
                        );



        log.info(
                "시술 이력 등록 완료 treatmentId={}, customerId={}, reservationNo={}, treatmentName={}",
                savedTreatment.getTreatmentId(),
                customerId,
                reservationNo,
                savedTreatment.getTreatmentName()
        );


        return savedTreatment;
    }



    // =====================================================
    // 시술 메모 수정
    // =====================================================

    /**
     * 이미 저장된 시술이력의
     * 시술 후 메모만 수정합니다.
     */
    @Transactional
    public TreatmentHistory updateTreatmentMemo(

            Long treatmentId,

            String treatmentMemo

    ) {

        log.info(
                "시술 메모 수정 시작 treatmentId={}",
                treatmentId
        );


        TreatmentHistory treatmentHistory =
                getByTreatmentId(
                        treatmentId
                );


        treatmentHistory
                .updateTreatmentMemo(
                        treatmentMemo
                );


        log.info(
                "시술 메모 수정 완료 treatmentId={}",
                treatmentId
        );


        /*
         * JPA Dirty Checking으로 UPDATE되므로
         * 별도의 save() 호출은 필요하지 않습니다.
         */
        return treatmentHistory;
    }



    // =====================================================
    // 다음 추천 방문일 수정
    // =====================================================

    /**
     * 시술이력에 저장된
     * 다음 추천 방문일을 변경합니다.
     */
    @Transactional
    public TreatmentHistory updateNextRecommendedDate(

            Long treatmentId,

            LocalDate nextRecommendedDate

    ) {

        log.info(
                "시술 다음 추천일 수정 시작 treatmentId={}, nextRecommendedDate={}",
                treatmentId,
                nextRecommendedDate
        );


        TreatmentHistory treatmentHistory =
                getByTreatmentId(
                        treatmentId
                );


        treatmentHistory
                .updateNextRecommendedDate(
                        nextRecommendedDate
                );


        log.info(
                "시술 다음 추천일 수정 완료 treatmentId={}, nextRecommendedDate={}",
                treatmentId,
                nextRecommendedDate
        );


        return treatmentHistory;
    }
}