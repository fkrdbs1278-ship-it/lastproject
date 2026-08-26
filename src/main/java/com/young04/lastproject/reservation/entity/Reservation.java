package com.young04.lastproject.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "RESERVATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_NO")
    private Long reservationNo;

    @Column(name = "MEMBER_NO")
    private Long memberNo;

    @Column(name = "SERVICE_MENU_NO", nullable = false)
    private Long serviceMenuNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_TYPE", nullable = false, length = 10)
    private CustomerType customerType;

    @Column(name = "GUEST_NAME", length = 50)
    private String guestName;

    @Column(name = "GUEST_PHONE", length = 20)
    private String guestPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESERVATION_SOURCE", nullable = false, length = 10)
    private ReservationSource reservationSource;

    @Column(name = "SERVICE_NAME_SNAPSHOT", nullable = false, length = 100)
    private String serviceNameSnapshot;

    @Column(name = "DURATION_MINUTES_SNAPSHOT", nullable = false)
    private Integer durationMinutesSnapshot;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "REQUEST_MEMO", length = 1000)
    private String requestMemo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "CANCEL_REASON", length = 500)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "CANCELED_BY", length = 10)
    private CanceledBy canceledBy;

    @Column(name = "CONFIRMED_AT")
    private LocalDateTime confirmedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "CANCELED_AT")
    private LocalDateTime canceledAt;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = ReservationStatus.REQUESTED;
        }

        if (reservationSource == null) {
            reservationSource = ReservationSource.ONLINE;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Reservation createMemberReservation(
            Long memberNo,
            Long serviceMenuNo,
            String serviceNameSnapshot,
            Integer durationMinutesSnapshot,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String requestMemo,
            ReservationSource source
    ) {
        Reservation reservation = new Reservation();
        reservation.memberNo = memberNo;
        reservation.serviceMenuNo = serviceMenuNo;
        reservation.customerType = CustomerType.MEMBER;
        reservation.reservationSource =
                source == null ? ReservationSource.ONLINE : source;
        reservation.serviceNameSnapshot = serviceNameSnapshot;
        reservation.durationMinutesSnapshot = durationMinutesSnapshot;
        reservation.startAt = startAt;
        reservation.endAt = endAt;
        reservation.requestMemo = requestMemo;
        reservation.status = ReservationStatus.REQUESTED;
        return reservation;
    }

    public static Reservation createGuestReservation(
            String guestName,
            String guestPhone,
            Long serviceMenuNo,
            String serviceNameSnapshot,
            Integer durationMinutesSnapshot,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String requestMemo,
            ReservationSource source
    ) {
        Reservation reservation = new Reservation();
        reservation.customerType = CustomerType.GUEST;
        reservation.guestName = guestName;
        reservation.guestPhone = guestPhone;
        reservation.serviceMenuNo = serviceMenuNo;
        reservation.reservationSource =
                source == null ? ReservationSource.ONLINE : source;
        reservation.serviceNameSnapshot = serviceNameSnapshot;
        reservation.durationMinutesSnapshot = durationMinutesSnapshot;
        reservation.startAt = startAt;
        reservation.endAt = endAt;
        reservation.requestMemo = requestMemo;
        reservation.status = ReservationStatus.REQUESTED;
        return reservation;
    }

    public void changeSchedule(
            Long serviceMenuNo,
            String serviceNameSnapshot,
            Integer durationMinutesSnapshot,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String requestMemo
    ) {
        this.serviceMenuNo = serviceMenuNo;
        this.serviceNameSnapshot = serviceNameSnapshot;
        this.durationMinutesSnapshot = durationMinutesSnapshot;
        this.startAt = startAt;
        this.endAt = endAt;
        this.requestMemo = requestMemo;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel(String reason, CanceledBy canceledBy) {
        this.status = ReservationStatus.CANCELED;
        this.cancelReason = reason;
        this.canceledBy = canceledBy;
        this.canceledAt = LocalDateTime.now();
    }

    public void markNoShow() {
        this.status = ReservationStatus.NO_SHOW;
    }
}
