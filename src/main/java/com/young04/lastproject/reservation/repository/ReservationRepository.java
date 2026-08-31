package com.young04.lastproject.reservation.repository;

import com.young04.lastproject.reservation.entity.CustomerType;
import com.young04.lastproject.reservation.entity.Reservation;
import com.young04.lastproject.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
                ReservationRepositoryCustom {

    List<Reservation> findByMemberNoOrderByStartAtDesc(
            Long memberNo
    );

    Optional<Reservation> findByReservationNoAndMemberNo(
            Long reservationNo,
            Long memberNo
    );

    Optional<Reservation>
    findByReservationNoAndCustomerTypeAndGuestPhone(
            Long reservationNo,
            CustomerType customerType,
            String guestPhone
    );

    // JPQL: 기존 시작 < 신규 종료 AND 기존 종료 > 신규 시작
    @Query("""
            select count(r)
            from Reservation r
            where r.status in :activeStatuses
              and r.startAt < :requestedEnd
              and r.endAt > :requestedStart
            """)
    long countOverlappingReservations(
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses
    );

    // 예약 변경 시 자기 자신을 제외하고 시간 중복 검사
    @Query("""
            select count(r)
            from Reservation r
            where r.reservationNo <> :reservationNo
              and r.status in :activeStatuses
              and r.startAt < :requestedEnd
              and r.endAt > :requestedStart
            """)
    long countOverlappingReservationsExcludingSelf(
            @Param("reservationNo") Long reservationNo,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("activeStatuses") Collection<ReservationStatus> activeStatuses
    );

    @Query("""
            select r
            from Reservation r
            where r.status in :activeStatuses
              and r.startAt < :rangeEnd
              and r.endAt > :rangeStart
            order by r.startAt asc
            """)
    List<Reservation> findOverlappingReservations(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("activeStatuses")
            Collection<ReservationStatus> activeStatuses
    );
}
