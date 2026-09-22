package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class ReservationMemberReader {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Long> findMemberNoByMemberId(String memberId) {
        return findMemberInfoByMemberId(memberId)
                .map(MemberReservationInfo::getMemberNo);
    }

    public Optional<MemberReservationInfo> findMemberInfoByMemberId(
            String memberId
    ) {
        if (memberId == null || memberId.isBlank()) {
            return Optional.empty();
        }

        try {
            Object[] row = (Object[]) entityManager.createNativeQuery("""
                    SELECT NO, MEMBER_ID, NAME, PHONE
                    FROM MEMBER
                    WHERE MEMBER_ID = :memberId
                      AND STATUS = 'ACTIVE'
                    """)
                    .setParameter("memberId", memberId)
                    .getSingleResult();

            String phone = (String) row[3];

            return Optional.of(
                    MemberReservationInfo.builder()
                            .memberNo(((Number) row[0]).longValue())
                            .memberId((String) row[1])
                            .name((String) row[2])
                            .phone(phone)
                            .maskedPhone(maskPhone(phone))
                            .build()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * 대시보드처럼 여러 예약의 회원 이름이 한 번에 필요한 경우 사용합니다.
     * 회원 예약마다 개별 SELECT를 실행하지 않고 한 번의 IN 조회로 가져옵니다.
     */
    public Map<Long, String> findMemberNamesByMemberNos(
            Collection<Long> memberNos
    ) {
        if (memberNos == null || memberNos.isEmpty()) {
            return Map.of();
        }

        var rows = entityManager.createQuery(
                        """
                        SELECT m.no, m.name
                        FROM Member m
                        WHERE m.no IN :memberNos
                        """,
                        Object[].class
                )
                .setParameter("memberNos", memberNos)
                .getResultList();

        Map<Long, String> memberNames = new HashMap<>();

        for (Object[] row : rows) {
            memberNames.put(
                    ((Number) row[0]).longValue(),
                    (String) row[1]
            );
        }

        return memberNames;
    }

    public Optional<MemberReservationInfo> findMemberInfoByMemberNo(
            Long memberNo
    ) {
        if (memberNo == null) {
            return Optional.empty();
        }

        try {
            Object[] row = (Object[]) entityManager.createNativeQuery("""
                    SELECT NO, MEMBER_ID, NAME, PHONE
                    FROM MEMBER
                    WHERE NO = :memberNo
                    """)
                    .setParameter("memberNo", memberNo)
                    .getSingleResult();

            String phone = (String) row[3];

            return Optional.of(
                    MemberReservationInfo.builder()
                            .memberNo(((Number) row[0]).longValue())
                            .memberId((String) row[1])
                            .name((String) row[2])
                            .phone(phone)
                            .maskedPhone(maskPhone(phone))
                            .build()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() < 7) {
            return phone;
        }

        if (digits.length() == 10) {
            return digits.substring(0, 3)
                    + "-***-"
                    + digits.substring(6);
        }

        return digits.substring(0, 3)
                + "-****-"
                + digits.substring(digits.length() - 4);
    }
}
