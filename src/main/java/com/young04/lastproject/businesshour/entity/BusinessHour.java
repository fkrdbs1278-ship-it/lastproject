package com.young04.lastproject.businesshour.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "BUSINESS_HOUR")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BUSINESS_HOUR_NO")
    private Long businessHourNo;

    @Column(name = "DAY_OF_WEEK", nullable = false, unique = true)
    private Integer dayOfWeek;

    @Column(name = "IS_OPEN", nullable = false)
    private Character isOpen;

    @Column(name = "OPEN_TIME", length = 5)
    private String openTime;

    @Column(name = "CLOSE_TIME", length = 5)
    private String closeTime;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (isOpen == null) {
            isOpen = 'Y';
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

    public boolean isOpenDay() {
        return Character.valueOf('Y').equals(isOpen);
    }

    public LocalTime getOpenLocalTime() {
        return openTime == null
                ? null
                : LocalTime.parse(openTime);
    }

    public LocalTime getCloseLocalTime() {
        return closeTime == null
                ? null
                : LocalTime.parse(closeTime);
    }

    public void changeBusinessHour(
            boolean open,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        this.isOpen = open ? 'Y' : 'N';

        if (open) {
            this.openTime = openTime.toString();
            this.closeTime = closeTime.toString();
        } else {
            this.openTime = null;
            this.closeTime = null;
        }
    }
}