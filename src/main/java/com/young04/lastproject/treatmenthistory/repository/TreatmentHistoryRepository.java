package com.young04.lastproject.treatmenthistory.repository;

import com.young04.lastproject.treatmenthistory.entity.TreatmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentHistoryRepository
        extends JpaRepository<TreatmentHistory, Long> {

    // 특정 고객의 시술 이력을 최신순으로 조회
    List<TreatmentHistory>
    findByCustomer_CustomerIdOrderByTreatmentDateDescTreatmentIdDesc(
            Long customerId
    );

    // 예약 번호로 시술 이력 조회
    Optional<TreatmentHistory> findByReservationNo(Long reservationNo);

    // 특정 고객의 시술 이력 개수
    long countByCustomer_CustomerId(Long customerId);
}