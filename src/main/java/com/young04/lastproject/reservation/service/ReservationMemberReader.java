package com.young04.lastproject.reservation.service;

import com.young04.lastproject.reservation.dto.MemberReservationInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
