package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.HairStyleOptionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class HairStyleReader {

    @PersistenceContext
    private EntityManager entityManager;

    public List<HairStyleOptionResponse> getActiveStylesForService(
            Long serviceMenuNo
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT
                    H.NO,
                    H.TITLE,
                    H.CATEGORY,
                    H.DESCRIPTION,
                    H.IMAGE_URL
                FROM HAIR_STYLE H
                JOIN HAIR_STYLE_SERVICE HS
                  ON HS.HAIR_STYLE_NO = H.NO
                WHERE HS.SERVICE_MENU_NO = :serviceMenuNo
                  AND H.ACTIVE_YN = 'Y'
                ORDER BY H.DISPLAY_ORDER ASC, H.NO ASC
                """)
                .setParameter("serviceMenuNo", serviceMenuNo)
                .getResultList();

        return rows.stream()
                .map(row -> HairStyleOptionResponse.builder()
                        .hairStyleNo(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .category((String) row[2])
                        .description((String) row[3])
                        .imageUrl((String) row[4])
                        .build())
                .toList();
    }

    public boolean isStyleLinkedToService(
            Long hairStyleNo,
            Long serviceMenuNo
    ) {
        if (hairStyleNo == null) {
            return true;
        }

        Number count = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM HAIR_STYLE H
                JOIN HAIR_STYLE_SERVICE HS
                  ON HS.HAIR_STYLE_NO = H.NO
                WHERE H.NO = :hairStyleNo
                  AND HS.SERVICE_MENU_NO = :serviceMenuNo
                  AND H.ACTIVE_YN = 'Y'
                """)
                .setParameter("hairStyleNo", hairStyleNo)
                .setParameter("serviceMenuNo", serviceMenuNo)
                .getSingleResult();

        return count.longValue() > 0;
    }

    public Optional<HairStyleOptionResponse> findById(Long hairStyleNo) {
        if (hairStyleNo == null) {
            return Optional.empty();
        }

        try {
            Object[] row = (Object[]) entityManager.createNativeQuery("""
                    SELECT
                        NO,
                        TITLE,
                        CATEGORY,
                        DESCRIPTION,
                        IMAGE_URL
                    FROM HAIR_STYLE
                    WHERE NO = :hairStyleNo
                    """)
                    .setParameter("hairStyleNo", hairStyleNo)
                    .getSingleResult();

            return Optional.of(
                    HairStyleOptionResponse.builder()
                            .hairStyleNo(((Number) row[0]).longValue())
                            .title((String) row[1])
                            .category((String) row[2])
                            .description((String) row[3])
                            .imageUrl((String) row[4])
                            .build()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
