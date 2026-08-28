package com.young04.lastproject.businesshour.repository;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessHourRepository
        extends JpaRepository<BusinessHour, Long> {

    Optional<BusinessHour> findByDayOfWeek(Integer dayOfWeek);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from BusinessHour b
            where b.dayOfWeek = :dayOfWeek
            """)
    Optional<BusinessHour> findByDayOfWeekForUpdate(
            @Param("dayOfWeek") Integer dayOfWeek
    );
}
