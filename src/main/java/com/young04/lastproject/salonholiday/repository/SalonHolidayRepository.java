package com.young04.lastproject.salonholiday.repository;

import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalonHolidayRepository
        extends JpaRepository<SalonHoliday, Long> {

    @Query("""
            select count(h)
            from SalonHoliday h
            where h.startAt < :requestedEnd
              and h.endAt > :requestedStart
            """)
    long countOverlappingHolidays(
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd
    );

    @Query("""
            select h
            from SalonHoliday h
            where h.startAt < :rangeEnd
              and h.endAt > :rangeStart
            order by h.startAt asc
            """)
    List<SalonHoliday> findOverlappingHolidays(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );
}
