package com.young04.lastproject.member.service;

import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.exception.MemberRecoveryException;
import com.young04.lastproject.member.exception.PhoneVerificationException;
import com.young04.lastproject.member.repository.MemberRepository;
import com.young04.lastproject.member.verification.PhoneVerificationPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberRecoveryService {

    private final MemberRepository memberRepository;
    private final PhoneVerificationService
            phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    /* 아이디 찾기 */

    public List<String> findMemberIds(
            String name,
            String phone
    ) {

        /* 1. FIND_ID 휴대전화 인증 여부 확인 */

        if (
                !phoneVerificationService
                        .isVerified(
                                phone,
                                PhoneVerificationPurpose.FIND_ID
                        )
        ) {

            throw new PhoneVerificationException(
                    "휴대전화 인증을 완료해주세요."
            );
        }


        /* 2. 입력값 정리 */

        String normalizedName =
                name.trim();


        String phoneDigits =
                phone.replaceAll(
                        "\\D",
                        ""
                );


        /* 3. 회원 조회 */

        List<Member> members =
                memberRepository
                        .findAllByNameAndPhoneDigits(
                                normalizedName,
                                phoneDigits
                        );


        if (members.isEmpty()) {

            throw new MemberRecoveryException(
                    "입력한 정보와 일치하는 회원을 찾을 수 없습니다."
            );
        }


        /* 4. 아이디 마스킹

           tyrua1
              ↓
           tyr*** */

        List<String> maskedMemberIds =
                members.stream()

                        .map(
                                Member::getMemberId
                        )

                        /*
                         * 혹시 중복 결과가 있다면 제거
                         */
                        .distinct()

                        .map(
                                this::maskMemberId
                        )

                        .toList();


        /* 5. FIND_ID 인증정보 소비

           아이디 찾기에 한 번 사용한 인증은
           다시 사용할 수 없도록 제거한다. */

        boolean consumed =
                phoneVerificationService
                        .consumeVerification(
                                phone,
                                PhoneVerificationPurpose.FIND_ID
                        );


        if (!consumed) {

            throw new PhoneVerificationException(
                    "휴대전화 인증이 만료되었습니다. 다시 인증해주세요."
            );
        }


        return maskedMemberIds;
    }

    /*  비밀번호 재설정 */

    @Transactional
    public void resetPassword(
            String memberId,
            String phone,
            String newPassword,
            String newPasswordCheck
    ) {

    /* 1. 새 비밀번호 일치 확인 */

        if (
                !newPassword.equals(
                        newPasswordCheck
                )
        ) {

            throw new MemberRecoveryException(
                    "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }


    /* 2. RESET_PASSWORD 휴대전화 인증 확인 */

        if (
                !phoneVerificationService
                        .isVerified(
                                phone,
                                PhoneVerificationPurpose.RESET_PASSWORD
                        )
        ) {

            throw new PhoneVerificationException(
                    "휴대전화 인증을 완료해주세요."
            );
        }


    /* 3. 입력값 정리 */

        String normalizedMemberId =
                memberId.trim();


        String phoneDigits =
                phone.replaceAll(
                        "\\D",
                        ""
                );


    /* 4. 아이디 + 전화번호 회원 조회 */

        Member member =
                memberRepository
                        .findByMemberIdAndPhoneDigits(
                                normalizedMemberId,
                                phoneDigits
                        )
                        .orElseThrow(
                                () ->
                                        new MemberRecoveryException(
                                                "입력한 회원정보를 확인해주세요."
                                        )
                        );


    /* 5. 새 비밀번호 BCrypt 암호화 */

        String encodedPassword =
                passwordEncoder.encode(
                        newPassword
                );


    /* 6. Entity 비밀번호 변경

       @Transactional + Dirty Checking */

        member.changePassword(
                encodedPassword
        );


    /* 7. 인증정보 사용 처리 */

        boolean consumed =
                phoneVerificationService
                        .consumeVerification(
                                phone,
                                PhoneVerificationPurpose.RESET_PASSWORD
                        );


        if (!consumed) {

            throw new PhoneVerificationException(
                    "휴대전화 인증이 만료되었습니다. 다시 인증해주세요."
            );
        }
    }




    /* 아이디 마스킹 */

    private String maskMemberId(
            String memberId
    ) {

        if (
                memberId == null
                        || memberId.isBlank()
        ) {

            return "";
        }


        int length =
                memberId.length();


        /*
         * 한 글자인 경우
         */
        if (length == 1) {

            return "*";
        }


        /*
         * 2~3글자
         *
         * ab  → a*
         * abc → a**
         */
        if (length <= 3) {

            return memberId.substring(
                    0,
                    1
            )
                    + "*".repeat(
                    length - 1
            );
        }


        /*
         * 4글자 이상
         *
         * tyrua1
         * ↓
         * tyr***
         */
        return memberId.substring(
                0,
                3
        )
                + "*".repeat(
                length - 3
        );
    }
}
