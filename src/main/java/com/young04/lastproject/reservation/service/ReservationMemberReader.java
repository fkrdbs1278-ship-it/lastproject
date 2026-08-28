package com.young04.lastproject.reservation.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.NoResultException;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ReservationMemberReader {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Long> findMemberNoByMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return Optional.empty();
        }

        try {
            Number result = (Number) entityManager.createNativeQuery("""
                    SELECT NO
                    FROM MEMBER
                    WHERE MEMBER_ID = :memberId
                      AND STATUS = 'ACTIVE'
                    """)
                    .setParameter("memberId", memberId)
                    .getSingleResult();

            return Optional.of(result.longValue());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
