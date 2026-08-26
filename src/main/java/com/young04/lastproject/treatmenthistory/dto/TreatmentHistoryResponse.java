package com.young04.lastproject.treatmenthistory.dto;

import com.young04.lastproject.treatmenthistory.entity.TreatmentHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TreatmentHistoryResponse {

    private Long treatmentId;

    private Long customerId;

    private Long reservationNo;

    private Long serviceMenuNo;

    private String treatmentName;

    private LocalDate treatmentDate;

    private BigDecimal treatmentPrice;

    private String treatmentMemo;

    private LocalDate nextRecommendedDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public static TreatmentHistoryResponse from(
            TreatmentHistory treatmentHistory
    ) {

        return TreatmentHistoryResponse.builder()
                .treatmentId(
                        treatmentHistory.getTreatmentId()
                )
                .customerId(
                        treatmentHistory
                                .getCustomer()
                                .getCustomerId()
                )
                .reservationNo(
                        treatmentHistory.getReservationNo()
                )
                .serviceMenuNo(
                        treatmentHistory.getServiceMenuNo()
                )
                .treatmentName(
                        treatmentHistory.getTreatmentName()
                )
                .treatmentDate(
                        treatmentHistory.getTreatmentDate()
                )
                .treatmentPrice(
                        treatmentHistory.getTreatmentPrice()
                )
                .treatmentMemo(
                        treatmentHistory.getTreatmentMemo()
                )
                .nextRecommendedDate(
                        treatmentHistory.getNextRecommendedDate()
                )
                .createdAt(
                        treatmentHistory.getCreatedAt()
                )
                .updatedAt(
                        treatmentHistory.getUpdatedAt()
                )
                .build();
    }
}