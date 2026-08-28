package com.young04.lastproject.reservationimage.dto;

import com.young04.lastproject.reservationimage.entity.ReservationImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationImageResponse {

    private Long reservationImageNo;
    private Long reservationNo;
    private String originalFileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private Integer sortOrder;

    public static ReservationImageResponse from(
            ReservationImage image
    ) {
        return ReservationImageResponse.builder()
                .reservationImageNo(
                        image.getReservationImageNo()
                )
                .reservationNo(
                        image.getReservation()
                                .getReservationNo()
                )
                .originalFileName(
                        image.getOriginalFileName()
                )
                .fileUrl(
                        image.getFileUrl()
                )
                .contentType(
                        image.getContentType()
                )
                .fileSize(
                        image.getFileSize()
                )
                .sortOrder(
                        image.getSortOrder()
                )
                .build();
    }
}
