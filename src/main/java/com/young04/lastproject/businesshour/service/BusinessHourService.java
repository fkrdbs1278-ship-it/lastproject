package com.young04.lastproject.businesshour.service;

import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessHourService {

    private final BusinessHourRepository businessHourRepository;

    public List<BusinessHour> getBusinessHours() {
        return businessHourRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BusinessHour::getDayOfWeek))
                .toList();
    }

    public BusinessHour getBusinessHour(Integer dayOfWeek) {
        return businessHourRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "영업시간 정보가 없습니다. dayOfWeek=" + dayOfWeek
                        )
                );
    }
}
