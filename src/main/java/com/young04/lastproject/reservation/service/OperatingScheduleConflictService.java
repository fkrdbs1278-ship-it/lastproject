package com.young04.lastproject.reservation.service;

import com.young04.lastproject.businesshour.repository.BusinessHourRepository;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import com.young04.lastproject.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperatingScheduleConflictService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(
                    ReservationStatus.REQUESTED,
                    ReservationStatus.CONFIRMED
            );

    private final ReservationRepository reservationRepository;
    private final BusinessHourRepository businessHourRepository;

    /**
     * 예약 생성 시 사용하는 BUSINESS_HOUR 행 잠금과 동일한 잠금을
     * 휴일/개인 일정 등록에서도 획득해, 두 작업이 동시에 진행될 때
     * 서로의 중복 검사를 비켜가는 것을 막는다.
     */
    @Transactional
    public void lockScheduleRange(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            return;
        }

        Set<Integer> dayOfWeeks = new TreeSet<>();

        LocalDate cursor = startAt.toLocalDate();
        LocalDate lastDate = endAt.minusNanos(1).toLocalDate();

        while (!cursor.isAfter(lastDate)) {
            dayOfWeeks.add(cursor.getDayOfWeek().getValue());
            cursor = cursor.plusDays(1);
        }

        for (Integer dayOfWeek : dayOfWeeks) {
            businessHourRepository
                    .findByDayOfWeekForUpdate(dayOfWeek)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "영업시간 정보가 없습니다. dayOfWeek="
                                            + dayOfWeek
                            )
                    );
        }
    }

    public void assertNoActiveReservationOverlap(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        long count = reservationRepository
                .countOverlappingReservations(
                        startAt,
                        endAt,
                        ACTIVE_STATUSES
                );

        if (count > 0) {
            throw new IllegalArgumentException(
                    "해당 시간에 접수 또는 확정된 예약이 있어 일정을 등록할 수 없습니다."
            );
        }
    }

    /**
     * 영업시간 축소/휴무 전환으로 이미 잡힌 미래 예약이
     * 영업시간 밖으로 밀려나는 것을 방지한다.
     */
    public void assertBusinessHourChangeSafe(
            Integer dayOfWeek,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        List<Reservation> futureReservations =
                reservationRepository
                        .findByStatusInAndStartAtGreaterThanEqualOrderByStartAtAsc(
                                ACTIVE_STATUSES,
                                LocalDateTime.now()
                        );

        List<Reservation> affected = futureReservations
                .stream()
                .filter(r -> r.getStartAt() != null)
                .filter(r -> r.getStartAt()
                        .getDayOfWeek()
                        .getValue() == dayOfWeek)
                .filter(r -> !fitsRequestedHours(
                        r,
                        open,
                        openTime,
                        closeTime
                ))
                .toList();

        if (!affected.isEmpty()) {
            Reservation first = affected.getFirst();
            throw new IllegalArgumentException(
                    "변경하려는 영업시간과 충돌하는 미래 예약이 있습니다. "
                            + "먼저 예약을 변경하거나 취소해주세요. 예약번호="
                            + first.getReservationNo()
            );
        }
    }

    private boolean fitsRequestedHours(
            Reservation reservation,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        if (!open || openTime == null || closeTime == null) {
            return false;
        }

        LocalTime reservationStart =
                reservation.getStartAt().toLocalTime();
        LocalTime reservationEnd =
                reservation.getEndAt().toLocalTime();

        return !reservationStart.isBefore(openTime)
                && !reservationEnd.isAfter(closeTime);
    }
}
