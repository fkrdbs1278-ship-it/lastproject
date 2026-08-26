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

    @Column(name = "ORIGINAL_FILE_NAME", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "STORED_FILE_NAME", nullable = false, length = 255)
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
}
