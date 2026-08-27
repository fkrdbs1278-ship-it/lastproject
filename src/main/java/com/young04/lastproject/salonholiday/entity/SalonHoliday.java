package com.young04.lastproject.salonholiday.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "SALON_HOLIDAY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalonHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SALON_HOLIDAY_NO")
    private Long salonHolidayNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "HOLIDAY_TYPE", nullable = false, length = 20)
    private HolidayType holidayType;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "ALL_DAY_YN", nullable = false)
    private Character allDayYn;

    @Column(name = "MEMO", length = 500)
    private String memo;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (allDayYn == null) allDayYn = 'Y';
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static SalonHoliday create(
            HolidayType holidayType,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean allDay,
            String memo
    ) {
        SalonHoliday holiday = new SalonHoliday();
        holiday.holidayType = holidayType;
        holiday.title = title;
        holiday.startAt = startAt;
        holiday.endAt = endAt;
        holiday.allDayYn = allDay ? 'Y' : 'N';
        holiday.memo = memo;
        return holiday;
    }

    public void change(
            HolidayType holidayType,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean allDay,
            String memo
    ) {
        this.holidayType = holidayType;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDayYn = allDay ? 'Y' : 'N';
        this.memo = memo;
    }

    public boolean isAllDay() {
        return Character.valueOf('Y').equals(allDayYn);
    }
}
