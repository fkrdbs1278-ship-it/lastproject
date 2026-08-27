package com.young04.lastproject.salonholiday.service;

import com.young04.lastproject.salonholiday.dto.SalonHolidayRequest;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalonHolidayService {

    private final SalonHolidayRepository salonHolidayRepository;

    @Transactional
    public SalonHoliday createHoliday(SalonHolidayRequest request) {
        validatePeriod(request);

        return salonHolidayRepository.save(
                SalonHoliday.create(
                        request.getHolidayType(),
                        request.getTitle(),
                        request.getStartAt(),
                        request.getEndAt(),
                        request.isAllDay(),
                        request.getMemo()
                )
        );
    }

    @Transactional
    public SalonHoliday updateHoliday(Long salonHolidayNo, SalonHolidayRequest request) {
        validatePeriod(request);

        SalonHoliday holiday = salonHolidayRepository.findById(salonHolidayNo)
                .orElseThrow(() ->
                        new IllegalArgumentException("휴일 정보를 찾을 수 없습니다.")
                );

        holiday.change(
                request.getHolidayType(),
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.isAllDay(),
                request.getMemo()
        );

        return holiday;
    }

    @Transactional
    public void deleteHoliday(Long salonHolidayNo) {
        SalonHoliday holiday = salonHolidayRepository.findById(salonHolidayNo)
                .orElseThrow(() ->
                        new IllegalArgumentException("휴일 정보를 찾을 수 없습니다.")
                );

        salonHolidayRepository.delete(holiday);
    }

    private void validatePeriod(SalonHolidayRequest request) {
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new IllegalArgumentException(
                    "휴일 종료시간은 시작시간보다 뒤여야 합니다."
            );
        }
    }
}
