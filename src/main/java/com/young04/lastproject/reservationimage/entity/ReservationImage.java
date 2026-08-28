package com.young04.lastproject.reservationimage.entity;

import com.young04.lastproject.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "RESERVATION_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_IMAGE_NO")
    private Long reservationImageNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RESERVATION_NO", nullable = false)
    private Reservation reservation;

    @Column(name = "ORIGINAL_FILE_NAME", nullable = false)
    private String originalFileName;

    @Column(name = "STORED_FILE_NAME", nullable = false)
    private String storedFileName;

    @Column(name = "FILE_URL", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static ReservationImage create(
            Reservation reservation,
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String contentType,
            long fileSize,
            int sortOrder
    ) {
        ReservationImage image =
                new ReservationImage();

        image.reservation = reservation;
        image.originalFileName = originalFileName;
        image.storedFileName = storedFileName;
        image.fileUrl = fileUrl;
        image.contentType = contentType;
        image.fileSize = fileSize;
        image.sortOrder = sortOrder;

        return image;
    }
}
