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

    /*
     * 고객 예약 이미지는 DB의 raw /uploads/reservation/** 경로를
     * 그대로 노출하지 않고 접근 권한이 적용된 API URL을 내려준다.
     */
    private String fileUrl;

    private String contentType;
    private Long fileSize;
    private Integer sortOrder;

    public static ReservationImageResponse from(
            ReservationImage image
    ) {
        return from(
                image,
                image.getFileUrl()
        );
    }

    public static ReservationImageResponse from(
            ReservationImage image,
            String protectedFileUrl
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
                .fileUrl(protectedFileUrl)
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

    public static ReservationImageResponse forMember(
            ReservationImage image
    ) {
        Long reservationNo =
                image.getReservation()
                        .getReservationNo();

        return from(
                image,
                "/api/reservations/me/"
                        + reservationNo
                        + "/images/"
                        + image.getReservationImageNo()
                        + "/content"
        );
    }

    public static ReservationImageResponse forAdmin(
            ReservationImage image
    ) {
        Long reservationNo =
                image.getReservation()
                        .getReservationNo();

        return from(
                image,
                "/admin/api/reservations/"
                        + reservationNo
                        + "/images/"
                        + image.getReservationImageNo()
                        + "/content"
        );
    }
}
