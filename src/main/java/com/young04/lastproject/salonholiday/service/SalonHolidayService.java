package com.young04.lastproject.salonholiday.service;

import com.young04.lastproject.salonholiday.dto.SalonHolidayRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalonHolidayService {

    private final SalonHolidayRepository salonHolidayRepository;

    public List<SalonHolidayResponse> getHolidays() {
        return salonHolidayRepository
                .findAllByOrderByStartAtAsc()
                .stream()
                .map(SalonHolidayResponse::from)
                .toList();
    }

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

        SalonHoliday holiday =
                getHoliday(salonHolidayNo);

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
        salonHolidayRepository.delete(
                getHoliday(salonHolidayNo)
        );
    }

    private SalonHoliday getHoliday(Long salonHolidayNo) {
        return salonHolidayRepository
                .findById(salonHolidayNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "휴일 정보를 찾을 수 없습니다."
                        )
                );
    }

    private void validatePeriod(SalonHolidayRequest request) {
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new IllegalArgumentException(
                    "휴일 종료시간은 시작시간보다 뒤여야 합니다."
            );
        }
    }
}
