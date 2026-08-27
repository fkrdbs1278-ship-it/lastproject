package com.young04.lastproject.noshow.entity;

import com.young04.lastproject.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "NO_SHOW")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO_SHOW_NO")
    private Long noShowNo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RESERVATION_NO", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Column(name = "ADMIN_MEMO", length = 1000)
    private String adminMemo;

    @Column(name = "PROCESSED_AT", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (processedAt == null) processedAt = now;
        if (createdAt == null) createdAt = now;
    }

    public static NoShow create(
            Reservation reservation,
            String reason,
            String adminMemo
    ) {
        NoShow noShow = new NoShow();
        noShow.reservation = reservation;
        noShow.reason = reason;
        noShow.adminMemo = adminMemo;
        return noShow;
    }
}
