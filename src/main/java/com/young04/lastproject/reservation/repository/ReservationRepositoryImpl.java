package com.young04.lastproject.reservation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.QReservation;
import com.young04.lastproject.reservation.entity.Reservation;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ReservationRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Reservation> search(
            ReservationSearchCondition condition,
            Pageable pageable
    ) {
        QReservation reservation = QReservation.reservation;
        BooleanBuilder builder = new BooleanBuilder();

        if (condition != null) {
            if (condition.getStatus() != null) {
                builder.and(reservation.status.eq(condition.getStatus()));
            }

            if (condition.getCustomerType() != null) {
                builder.and(reservation.customerType.eq(condition.getCustomerType()));
            }

            if (condition.getReservationSource() != null) {
                builder.and(
                        reservation.reservationSource.eq(
                                condition.getReservationSource()
                        )
                );
            }

            if (condition.getMemberNo() != null) {
                builder.and(reservation.memberNo.eq(condition.getMemberNo()));
            }

            if (condition.getServiceMenuNo() != null) {
                builder.and(
                        reservation.serviceMenuNo.eq(condition.getServiceMenuNo())
                );
            }

            if (condition.getGuestName() != null
                    && !condition.getGuestName().isBlank()) {
                builder.and(
                        reservation.guestName.containsIgnoreCase(
                                condition.getGuestName().trim()
                        )
                );
            }

            if (condition.getGuestPhone() != null
                    && !condition.getGuestPhone().isBlank()) {
                builder.and(
                        reservation.guestPhone.contains(
                                condition.getGuestPhone().trim()
                        )
                );
            }

            if (condition.getStartFrom() != null) {
                builder.and(
                        reservation.startAt.goe(condition.getStartFrom())
                );
            }

            if (condition.getStartTo() != null) {
                builder.and(
                        reservation.startAt.lt(condition.getStartTo())
                );
            }
        }

        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .where(builder)
                .orderBy(reservation.startAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0L : total
        );
    }
}
