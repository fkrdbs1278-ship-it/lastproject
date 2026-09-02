package com.young04.lastproject.salonholiday.service;

import com.young04.lastproject.salonholiday.dto.OwnerAvailabilityBlockRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.exception.SalonHolidayNotFoundException;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerAvailabilityService {

    private final SalonHolidayRepository salonHolidayRepository;


    /*
     * =========================================================
     * 원장 예약 차단 일정 목록 조회
     * =========================================================
     *
     * SALON_HOLIDAY 중
     * PERSONAL 타입만 조회한다.
     *
     * 예:
     * - 점심시간
     * - 외출
     * - 개인 일정
     * - 병원
     * - 교육
     */
    public List<SalonHolidayResponse> getBlocks() {

        return salonHolidayRepository
                .findAllByHolidayTypeOrderByStartAtAsc(
                        HolidayType.PERSONAL
                )
                .stream()
                .map(SalonHolidayResponse::from)
                .toList();
    }


    /*
     * =========================================================
     * 원장 예약 차단 일정 등록
     * =========================================================
     */
    @Transactional
    public SalonHolidayResponse createBlock(
            OwnerAvailabilityBlockRequest request
    ) {

        validatePeriod(
                request.getStartAt(),
                request.getEndAt()
        );

        SalonHoliday saved =
                salonHolidayRepository.save(
                        SalonHoliday.create(
                                HolidayType.PERSONAL,
                                request.getTitle(),
                                request.getStartAt(),
                                request.getEndAt(),
                                request.isAllDay(),
                                request.getMemo()
                        )
                );

        return SalonHolidayResponse.from(saved);
    }


    /*
     * =========================================================
     * 원장 예약 차단 일정 수정
     * =========================================================
     */
    @Transactional
    public SalonHolidayResponse updateBlock(
            Long salonHolidayNo,
            OwnerAvailabilityBlockRequest request
    ) {

        validatePeriod(
                request.getStartAt(),
                request.getEndAt()
        );

        SalonHoliday block =
                getPersonalBlock(salonHolidayNo);

        block.change(
                HolidayType.PERSONAL,
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt(),
                request.isAllDay(),
                request.getMemo()
        );

        return SalonHolidayResponse.from(block);
    }


    /*
     * =========================================================
     * 원장 예약 차단 일정 삭제
     * =========================================================
     */
    @Transactional
    public void deleteBlock(Long salonHolidayNo) {
        SalonHoliday block =
                getPersonalBlock(salonHolidayNo);
        salonHolidayRepository.delete(block);
    }


    /*
     * =========================================================
     * 원장 예약 차단 일정 조회
     * =========================================================
     *
     * 존재하지 않으면
     * SalonHolidayNotFoundException 발생
     *
     * PERSONAL 타입이 아니면
     * 잘못된 요청으로 처리
     */
    private SalonHoliday getPersonalBlock(
            Long salonHolidayNo
    ) {
        SalonHoliday block =
                salonHolidayRepository
                        .findById(salonHolidayNo)
                        .orElseThrow(
                                () ->
                                        new SalonHolidayNotFoundException(
                                                salonHolidayNo
                                        )
                        );

        if (block.getHolidayType()
                != HolidayType.PERSONAL) {
            throw new IllegalArgumentException(
                    "원장 예약 차단 일정이 아닙니다."
            );
        }

        return block;
    }


    /*
     * =========================================================
     * 시작/종료 시간 검증
     * =========================================================
     */
    private void validatePeriod(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {

        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException(
                    "시작시간과 종료시간은 필수입니다."
            );
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException(
                    "종료시간은 시작시간보다 뒤여야 합니다."
            );
        }
    }
}