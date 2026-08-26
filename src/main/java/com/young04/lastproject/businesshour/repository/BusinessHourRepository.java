package com.young04.lastproject.businesshour.repository;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessHourRepository
        extends JpaRepository<BusinessHour, Long> {

    Optional<BusinessHour> findByDayOfWeek(Integer dayOfWeek);
}
