package com.young04.lastproject.treatmenthistory.service;

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

    private final TreatmentHistoryRepository treatmentHistoryRepository;


    // 고객별 시술 이력 조회
    public List<TreatmentHistory> findByCustomerId(Long customerId) {

        log.info(
                "고객 시술 이력 조회 customerId={}",
                customerId
        );

        return treatmentHistoryRepository
                .findByCustomer_CustomerIdOrderByTreatmentDateDescTreatmentIdDesc(
                        customerId
                );
    }


    // 시술 이력 번호로 상세 조회
    public Optional<TreatmentHistory> findByTreatmentId(
            Long treatmentId
    ) {

        log.info(
                "시술 이력 상세 조회 treatmentId={}",
                treatmentId
        );

        return treatmentHistoryRepository.findById(treatmentId);
    }


    // 예약 번호로 시술 이력 조회
    public Optional<TreatmentHistory> findByReservationNo(
            Long reservationNo
    ) {

        log.info(
                "예약 번호 기준 시술 이력 조회 reservationNo={}",
                reservationNo
        );

        return treatmentHistoryRepository
                .findByReservationNo(reservationNo);
    }


    // 고객별 시술 이력 건수
    public long countByCustomerId(Long customerId) {

        log.info(
                "고객 시술 이력 건수 조회 customerId={}",
                customerId
        );

        return treatmentHistoryRepository
                .countByCustomer_CustomerId(customerId);
    }
}