package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.exception.ServiceMenuNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ServiceMenuReader {

    @PersistenceContext
    private EntityManager entityManager;

    public ServiceMenuSnapshot getActiveServiceMenu(Long serviceMenuNo) {
        try {
            Object[] row = (Object[]) entityManager.createNativeQuery("""
                    SELECT NAME, DURATION_MIN
                    FROM SERVICE_MENU
                    WHERE NO = :serviceMenuNo
                      AND ACTIVE_YN = 'Y'
                    """)
                    .setParameter("serviceMenuNo", serviceMenuNo)
                    .getSingleResult();

            return new ServiceMenuSnapshot(
                    serviceMenuNo,
                    (String) row[0],
                    ((Number) row[1]).intValue()
            );
        } catch (NoResultException e) {
            throw new ServiceMenuNotFoundException(serviceMenuNo);
        }
    }

    public record ServiceMenuSnapshot(
            Long serviceMenuNo,
            String name,
            Integer durationMin
    ) {}
}
