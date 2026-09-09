package com.young04.lastproject.loginhistory.service;

import com.young04.lastproject.loginhistory.entity.LoginHistory;
import com.young04.lastproject.loginhistory.repository.LoginHistoryRepository;
import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final MemberRepository memberRepository;


    /* =========================================================
       로그인 성공
    ========================================================= */

    public void recordSuccess(
            Long memberNo,
            String memberId,
            String ipAddress,
            String userAgent
    ) {

        Member member = memberRepository
                .findById(memberNo)
                .orElse(null);

        if (member != null) {

            // 로그인 실패 횟수 0
            // 마지막 로그인 시간 기록
            member.loginSuccess();
        }

        LoginHistory loginHistory =
                LoginHistory.success(
                        member,
                        cut(memberId, 100),
                        cut(ipAddress, 50),
                        cut(userAgent, 500)
                );

        loginHistoryRepository.save(loginHistory);
    }


    /* =========================================================
       로그인 실패
    ========================================================= */

    public void recordFailure(
            String memberId,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {

        Member member = null;

        /*
         * 존재하는 회원인지 확인
         *
         * 비밀번호만 틀린 경우라면 회원이 존재하므로
         * LOGIN_FAIL_COUNT 증가
         */
        if (memberId != null && !memberId.isBlank()) {

            member = memberRepository
                    .findByMemberId(memberId)
                    .orElse(null);
        }

        if (member != null) {
            member.loginFailure();
        }

        LoginHistory loginHistory =
                LoginHistory.failure(
                        member,
                        cut(memberId, 100),
                        cut(ipAddress, 50),
                        cut(userAgent, 500),
                        cut(failureReason, 500)
                );

        loginHistoryRepository.save(loginHistory);
    }


    /* =========================================================
       DB 컬럼 최대 길이 초과 방지
    ========================================================= */

    private String cut(
            String value,
            int maxLength
    ) {

        if (value == null) {
            return null;
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
