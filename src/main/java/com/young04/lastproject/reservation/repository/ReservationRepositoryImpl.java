package com.young04.lastproject.reservation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.young04.lastproject.member.entity.QMember;
import com.young04.lastproject.reservation.dto.ReservationSearchCondition;
import com.young04.lastproject.reservation.entity.CustomerType;
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
        QReservation reservation =
                QReservation.reservation;

        QMember member =
                QMember.member;

        BooleanBuilder builder =
                new BooleanBuilder();

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

            if (condition.getReservationNo() != null) {
                builder.and(
                        reservation.reservationNo.eq(
                                condition.getReservationNo()
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

            if (
                    condition.getGuestName() != null
                    && !condition.getGuestName().isBlank()
            ) {

                builder.and(
                        customerNameContains(
                                reservation,
                                member,
                                condition
                                        .getGuestName()
                                        .trim()
                        )
                );
            }


            if (
                    condition.getGuestPhone() != null
                    && !condition.getGuestPhone().isBlank()
            ) {

                String normalizedPhone =
                        condition
                                .getGuestPhone()
                                .replaceAll(
                                        "\\D",
                                        ""
                                );


                if (!normalizedPhone.isBlank()) {

                    builder.and(
                            customerPhoneContains(
                                    reservation,
                                    member,
                                    normalizedPhone
                            )
                    );
                }
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

        List<Reservation> content =
                queryFactory
                        .selectFrom(reservation)
                        .leftJoin(member)
                        .on(
                                reservation.memberNo.eq(
                                        member.no
                                )
                        )
                        .where(builder)
                .orderBy(reservation.startAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total =
                queryFactory
                        .select(
                                reservation.count()
                        )
                        .from(reservation)
                        .leftJoin(member)
                        .on(
                                reservation.memberNo.eq(
                                        member.no
                                )
                        )
                        .where(builder)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total == null ? 0L : total
        );
    }


    private BooleanExpression customerNameContains(
            QReservation reservation,
            QMember member,
            String customerName
    ) {

        BooleanExpression memberCondition =
                reservation.customerType
                        .eq(CustomerType.MEMBER)
                        .and(
                                member.name
                                        .containsIgnoreCase(
                                                customerName
                                        )
                        );


        BooleanExpression guestCondition =
                reservation.customerType
                        .eq(CustomerType.GUEST)
                        .and(
                                reservation.guestName
                                        .containsIgnoreCase(
                                                customerName
                                        )
                        );


        return memberCondition.or(
                guestCondition
        );
    }


    private BooleanExpression customerPhoneContains(
            QReservation reservation,
            QMember member,
            String normalizedPhone
    ) {

        StringExpression normalizedMemberPhone =
                normalizePhone(
                        member.phone
                );

        StringExpression normalizedGuestPhone =
                normalizePhone(
                        reservation.guestPhone
                );


        BooleanExpression memberCondition =
                reservation.customerType
                        .eq(CustomerType.MEMBER)
                        .and(
                                normalizedMemberPhone.contains(
                                        normalizedPhone
                                )
                        );


        BooleanExpression guestCondition =
                reservation.customerType
                        .eq(CustomerType.GUEST)
                        .and(
                                normalizedGuestPhone.contains(
                                        normalizedPhone
                                )
                        );


        return memberCondition.or(
                guestCondition
        );
    }


    private StringExpression normalizePhone(
            StringExpression phone
    ) {

        return Expressions.stringTemplate(
                "replace(replace({0}, '-', ''), ' ', '')",
                phone
        );
    }
}
