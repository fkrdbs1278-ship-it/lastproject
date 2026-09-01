package com.young04.lastproject.salonholiday.service;

import com.young04.lastproject.salonholiday.dto.OwnerAvailabilityBlockRequest;
import com.young04.lastproject.salonholiday.dto.SalonHolidayResponse;
import com.young04.lastproject.salonholiday.entity.HolidayType;
import com.young04.lastproject.salonholiday.entity.SalonHoliday;
import com.young04.lastproject.salonholiday.repository.SalonHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerAvailabilityService {

    private final SalonHolidayRepository salonHolidayRepository;

    public List<SalonHolidayResponse> getBlocks() {
        return salonHolidayRepository
                .findAllByHolidayTypeOrderByStartAtAsc(
                        HolidayType.PERSONAL
                )
                .stream()
                .map(SalonHolidayResponse::from)
                .toList();
    }

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

    @Transactional
    public void deleteBlock(Long salonHolidayNo) {
        salonHolidayRepository.delete(
                getPersonalBlock(salonHolidayNo)
        );
    }

    private SalonHoliday getPersonalBlock(
            Long salonHolidayNo
    ) {
        SalonHoliday block =
                salonHolidayRepository
                        .findById(salonHolidayNo)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "예약 차단 일정을 찾을 수 없습니다."
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

    private void validatePeriod(
            java.time.LocalDateTime startAt,
            java.time.LocalDateTime endAt
    ) {
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException(
                    "종료시간은 시작시간보다 뒤여야 합니다."
            );
        }
    }
}
