package com.young04.lastproject.salonholiday.service;

import com.young04.lastproject.salonholiday.dto.SalonHolidayRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.exception.SalonHolidayNotFoundException;
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


    /*
     * =========================================================
     * 휴일 목록 조회
     * =========================================================
     */
    public List<SalonHolidayResponse> getHolidays() {
        return salonHolidayRepository
                .findAllByOrderByStartAtAsc()
                .stream()
                .map(SalonHolidayResponse::from)
                .toList();
    }


    /*
     * =========================================================
     * 휴일 등록
     * =========================================================
     */
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


    /*
     * =========================================================
     * 휴일 수정
     * =========================================================
     */
    @Transactional
    public SalonHoliday updateHoliday(
            Long salonHolidayNo,
            SalonHolidayRequest request
    ) {

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


    /*
     * =========================================================
     * 휴일 삭제
     * =========================================================
     */
    @Transactional
    public void deleteHoliday(Long salonHolidayNo) {
        SalonHoliday holiday =
                getHoliday(salonHolidayNo);
        salonHolidayRepository.delete(holiday);
    }


    /*
     * =========================================================
     * 휴일 단건 조회
     * =========================================================
     */
    private SalonHoliday getHoliday(
            Long salonHolidayNo
    ) {

        return salonHolidayRepository
                .findById(salonHolidayNo)
                .orElseThrow(
                        () ->
                                new SalonHolidayNotFoundException(
                                        salonHolidayNo
                                )
                );
    }


    /*
     * =========================================================
     * 휴일 기간 검증
     * =========================================================
     */
    private void validatePeriod(
            SalonHolidayRequest request
    ) {

        if (request.getStartAt() == null
                || request.getEndAt() == null) {

            throw new IllegalArgumentException(
                    "시작시간과 종료시간은 필수입니다."
            );
        }

        if (!request.getStartAt()
                .isBefore(request.getEndAt())) {

            throw new IllegalArgumentException(
                    "휴일 종료시간은 시작시간보다 뒤여야 합니다."
            );
        }
    }
}