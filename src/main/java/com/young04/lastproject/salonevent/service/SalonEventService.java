package com.young04.lastproject.salonevent.service;

import com.young04.lastproject.salonevent.dto.SalonEventRequest;
import com.young04.lastproject.salonevent.dto.SalonEventResponse;
import com.young04.lastproject.salonevent.entity.SalonEvent;
import com.young04.lastproject.salonevent.repository.SalonEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

// 이벤트 등록·조회·수정·상태 변경을 처리하는 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalonEventService {

    private final SalonEventRepository salonEventRepository;

    // 관리자 이벤트 목록 조회와 검색
    public List<SalonEventResponse> getEvents(
            String useYn,
            String keyword
    ) {
        // 사용 여부 필터와 검색어 입력 여부에 따라 조회 조건을 구분
        boolean hasUseYn =
                StringUtils.hasText(useYn)
                        && !"ALL".equalsIgnoreCase(useYn);

        boolean hasKeyword = StringUtils.hasText(keyword);

        List<SalonEvent> events;

        if (hasUseYn && hasKeyword) {
            events =
                    salonEventRepository
                            .findByUseYnAndEventTitleContainingIgnoreCaseOrderByRegdateDesc(
                                    useYn,
                                    keyword.trim()
                            );
        } else if (hasUseYn) {
            events =
                    salonEventRepository
                            .findByUseYnOrderByRegdateDesc(useYn);
        } else if (hasKeyword) {
            events =
                    salonEventRepository
                            .findByEventTitleContainingIgnoreCaseOrderByRegdateDesc(
                                    keyword.trim()
                            );
        } else {
            events =
                    salonEventRepository
                            .findAllByOrderByRegdateDesc();
        }

        return events.stream()
                .map(SalonEventResponse::from)
                .toList();
    }

    // 이벤트 검색 자동완성에서 사용할 이벤트명을 중복 없이 조회
    public List<String> getEventTitleSuggestions() {
        return salonEventRepository
                .findAllByOrderByEventTitleAsc()
                .stream()
                .map(SalonEvent::getEventTitle)
                .filter(eventTitle ->
                        eventTitle != null
                                && !eventTitle.isBlank()
                )
                .distinct()
                .toList();
    }

    // 이벤트 번호로 상세 정보 조회
    public SalonEventResponse getEvent(Long eventNo) {
        return SalonEventResponse.from(
                findEvent(eventNo)
        );
    }

    // 수정 화면에 표시할 기존 이벤트 정보 조회
    public SalonEventRequest getEventForEdit(Long eventNo) {
        SalonEvent event = findEvent(eventNo);

        SalonEventRequest request = new SalonEventRequest();

        request.setEventTitle(event.getEventTitle());
        request.setEventContent(event.getEventContent());
        request.setEventType(event.getEventType());
        request.setEventImageUrl(event.getEventImageUrl());
        request.setStartDate(event.getStartDate());
        request.setEndDate(event.getEndDate());
        request.setUseYn(event.getUseYn());

        return request;
    }

    // 사용자 화면에 노출할 진행 중 이벤트 조회
    public List<SalonEventResponse> getOngoingEvents() {
        LocalDateTime now = LocalDateTime.now();

        return salonEventRepository
                .findByUseYnAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
                        "Y",
                        now,
                        now
                )
                .stream()
                .map(SalonEventResponse::from)
                .toList();
    }

    // 관리자 대시보드에 표시할 진행 중 이벤트 최대 2개 조회
    public List<SalonEventResponse> getOngoingEventsForDashboard() {
        LocalDateTime now = LocalDateTime.now();

        return salonEventRepository
                .findTop2ByUseYnAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(
                        "Y",
                        now,
                        now
                )
                .stream()
                .map(SalonEventResponse::from)
                .toList();
    }

    // 신규 이벤트 등록
    @Transactional
    public Long createEvent(SalonEventRequest request) {
        validateEventPeriod(
                request.getStartDate(),
                request.getEndDate()
        );

        SalonEvent event = SalonEvent.builder()
                .eventTitle(request.getEventTitle().trim())
                .eventContent(
                        emptyToNull(request.getEventContent())
                )
                .eventType(request.getEventType())
                .eventImageUrl(
                        emptyToNull(request.getEventImageUrl())
                )
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .useYn(request.getUseYn())
                .build();

        SalonEvent savedEvent =
                salonEventRepository.save(event);

        return savedEvent.getEventNo();
    }

    // 기존 이벤트 수정
    @Transactional
    public void updateEvent(
            Long eventNo,
            SalonEventRequest request
    ) {
        validateEventPeriod(
                request.getStartDate(),
                request.getEndDate()
        );

        SalonEvent event = findEvent(eventNo);

        event.update(
                request.getEventTitle().trim(),
                emptyToNull(request.getEventContent()),
                request.getEventType(),
                emptyToNull(request.getEventImageUrl()),
                request.getStartDate(),
                request.getEndDate(),
                request.getUseYn()
        );
    }

    // 이벤트 사용 중지
    @Transactional
    public void stopEvent(Long eventNo) {
        SalonEvent event = findEvent(eventNo);

        if ("N".equals(event.getUseYn())) {
            throw new IllegalStateException(
                    "이미 사용 중지된 이벤트입니다."
            );
        }

        event.stopUsing();
    }

    // 사용 중지된 이벤트 다시 노출
    @Transactional
    public void resumeEvent(Long eventNo) {
        SalonEvent event = findEvent(eventNo);

        if ("Y".equals(event.getUseYn())) {
            throw new IllegalStateException(
                    "이미 사용 중인 이벤트입니다."
            );
        }

        event.resumeUsing();
    }

    // 이벤트 번호에 해당하는 Entity 조회
    private SalonEvent findEvent(Long eventNo) {
        return salonEventRepository.findById(eventNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이벤트 정보를 찾을 수 없습니다."
                        )
                );
    }

    // 종료일이 시작일보다 이전인지 검사
    private void validateEventPeriod(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate == null || endDate == null) {
            return;
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "이벤트 종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }

    // 공백 문자열을 null로 변환
    private String emptyToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
