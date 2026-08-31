package com.young04.lastproject.businesshour.service;

import com.young04.lastproject.businesshour.dto.BusinessHourResponse;
import com.young04.lastproject.businesshour.dto.BusinessHourUpdateRequest;
import com.young04.lastproject.businesshour.entity.BusinessHour;
import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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

    public List<BusinessHourResponse> getBusinessHourResponses() {
        return getBusinessHours()
                .stream()
                .map(BusinessHourResponse::from)
                .toList();
    }

    public BusinessHour getBusinessHour(Integer dayOfWeek) {
        validateDayOfWeek(dayOfWeek);

        return businessHourRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "영업시간 정보가 없습니다. dayOfWeek=" + dayOfWeek
                        )
                );
    }

    @Transactional
    public BusinessHourResponse updateBusinessHour(
            Integer dayOfWeek,
            BusinessHourUpdateRequest request
    ) {
        BusinessHour hour =
                getBusinessHour(dayOfWeek);

        validate(request);

        hour.changeBusinessHour(
                request.isOpen(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        return BusinessHourResponse.from(hour);
    }

    private void validateDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek == null
                || dayOfWeek < 1
                || dayOfWeek > 7) {
            throw new IllegalArgumentException(
                    "요일은 1(월)~7(일) 사이여야 합니다."
            );
        }
    }

    private void validate(BusinessHourUpdateRequest request) {
        if (!request.isOpen()) {
            return;
        }

        LocalTime openTime = request.getOpenTime();
        LocalTime closeTime = request.getCloseTime();

        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException(
                    "영업일에는 오픈시간과 마감시간이 필요합니다."
            );
        }

        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException(
                    "마감시간은 오픈시간보다 뒤여야 합니다."
            );
        }
    }
}
