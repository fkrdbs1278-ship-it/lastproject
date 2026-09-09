package com.young04.lastproject.member.service;

import com.young04.lastproject.member.exception.PhoneVerificationException;
import com.young04.lastproject.member.verification.PhoneVerificationPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    /*
     * 인증번호 유효시간
     *
     * 문자 발송 후 3분이 지나면
     * 사용할 수 없다.
     */
    private static final Duration CODE_TTL =
            Duration.ofMinutes(3);


    /*
     * 인증번호 재발송 제한시간
     *
     * 한 번 발송하면 60초 동안
     * 다시 발송할 수 없다.
     */
    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);


    /*
     * 인증 성공 상태 유지시간
     *
     * 인증 성공 후 10분 동안
     * 회원가입 / 아이디 찾기 /
     * 비밀번호 재설정 작업을 완료할 수 있다.
     */
    private static final Duration VERIFIED_TTL =
            Duration.ofMinutes(10);


    /*
     * 인증번호 입력 최대 실패 횟수
     */
    private static final int MAX_VERIFY_ATTEMPTS =
            5;


    /*
     * 실제 문자 발송 담당
     */
    private final MemberSmsService memberSmsService;


    /*
     * 인증번호 생성용
     *
     * java.util.Random 대신
     * SecureRandom 사용
     */
    private final SecureRandom secureRandom =
            new SecureRandom();


    /*
     * 인증정보 임시 저장
     *
     * DB에는 저장하지 않는다.
     */
    private final Map<
            VerificationKey,
            VerificationState
            > verificationStore =
            new ConcurrentHashMap<>();


    /* 인증번호 발송 */

    public void sendVerificationCode(
            String phone,
            PhoneVerificationPurpose purpose
    ) {

        String normalizedPhone =
                normalizePhone(phone);


        validatePurpose(purpose);


        VerificationKey key =
                new VerificationKey(
                        normalizedPhone,
                        purpose
                );


        Instant now =
                Instant.now();


        VerificationState existing =
                verificationStore.get(key);


        /*
         * 재전송 제한 확인
         */
        if (
                existing != null
                        && existing.resendAvailableAt() != null
                        && now.isBefore(
                        existing.resendAvailableAt()
                )
        ) {

            long remainingSeconds =
                    Duration.between(
                            now,
                            existing.resendAvailableAt()
                    ).toSeconds() + 1;


            throw new PhoneVerificationException(
                    "인증번호는 "
                            + remainingSeconds
                            + "초 후 다시 요청할 수 있습니다."
            );
        }


        /*
         * 6자리 인증번호 생성
         */
        String verificationCode =
                generateVerificationCode();


        /*
         * SMS 발송
         *
         * 발송이 실패하면 아래 저장 코드는
         * 실행되지 않는다.
         */
        memberSmsService.sendVerificationCode(
                normalizedPhone,
                verificationCode
        );


        /*
         * 인증번호 저장
         */
        VerificationState state =
                new VerificationState(
                        verificationCode,

                        now.plus(
                                CODE_TTL
                        ),

                        now.plus(
                                RESEND_COOLDOWN
                        ),

                        0,

                        null
                );


        verificationStore.put(
                key,
                state
        );
    }


    /* 인증번호 확인 */

    public void verifyCode(
            String phone,
            PhoneVerificationPurpose purpose,
            String inputCode
    ) {

        String normalizedPhone =
                normalizePhone(phone);


        validatePurpose(purpose);


        if (
                inputCode == null
                        || !inputCode.matches("\\d{6}")
        ) {

            throw new PhoneVerificationException(
                    "6자리 인증번호를 입력해주세요."
            );
        }


        VerificationKey key =
                new VerificationKey(
                        normalizedPhone,
                        purpose
                );


        VerificationState state =
                verificationStore.get(key);


        if (state == null) {

            throw new PhoneVerificationException(
                    "인증번호를 먼저 요청해주세요."
            );
        }


        Instant now =
                Instant.now();


        /*
         * 이미 인증 완료된 상태
         */
        if (
                state.verifiedUntil() != null
                        && now.isBefore(
                        state.verifiedUntil()
                )
        ) {

            return;
        }


        /*
         * 인증번호 만료 확인
         */
        if (
                state.expiresAt() == null
                        || now.isAfter(
                        state.expiresAt()
                )
        ) {

            verificationStore.remove(
                    key
            );


            throw new PhoneVerificationException(
                    "인증번호가 만료되었습니다. "
                            + "다시 요청해주세요."
            );
        }


        /*
         * 인증번호가 틀린 경우
         */
        if (
                !state.code()
                        .equals(inputCode)
        ) {

            int failedAttempts =
                    state.failedAttempts() + 1;


            /*
             * 최대 횟수 초과
             */
            if (
                    failedAttempts
                            >= MAX_VERIFY_ATTEMPTS
            ) {

                verificationStore.remove(
                        key
                );


                throw new PhoneVerificationException(
                        "인증번호 입력 횟수를 초과했습니다. "
                                + "인증번호를 다시 요청해주세요."
                );
            }


            VerificationState failedState =
                    new VerificationState(
                            state.code(),

                            state.expiresAt(),

                            state.resendAvailableAt(),

                            failedAttempts,

                            null
                    );


            verificationStore.put(
                    key,
                    failedState
            );


            int remainingAttempts =
                    MAX_VERIFY_ATTEMPTS
                            - failedAttempts;


            throw new PhoneVerificationException(
                    "인증번호가 일치하지 않습니다. "
                            + "남은 횟수: "
                            + remainingAttempts
            );
        }


        /*
         * 인증 성공
         *
         * 인증번호 자체는 더 이상 사용할
         * 필요가 없으므로 제거하고
         *
         * 인증 완료 상태만 10분간 유지한다.
         */
        VerificationState verifiedState =
                new VerificationState(
                        null,
                        null,
                        now,
                        0,

                        now.plus(
                                VERIFIED_TTL
                        )
                );


        verificationStore.put(
                key,
                verifiedState
        );
    }


    /* 인증 완료 여부 확인 */

    public boolean isVerified(
            String phone,
            PhoneVerificationPurpose purpose
    ) {

        String normalizedPhone =
                normalizePhone(phone);


        validatePurpose(purpose);


        VerificationKey key =
                new VerificationKey(
                        normalizedPhone,
                        purpose
                );


        VerificationState state =
                verificationStore.get(key);


        if (
                state == null
                        || state.verifiedUntil() == null
        ) {

            return false;
        }


        Instant now =
                Instant.now();


        /*
         * 인증 완료 상태도 만료
         */
        if (
                !now.isBefore(
                        state.verifiedUntil()
                )
        ) {

            verificationStore.remove(
                    key
            );


            return false;
        }


        return true;
    }


    /* 인증 성공 상태 사용 후 제거

       회원가입 / 비밀번호 변경이
       최종 성공한 후 호출한다. */

    public boolean consumeVerification(
            String phone,
            PhoneVerificationPurpose purpose
    ) {

        String normalizedPhone =
                normalizePhone(phone);


        validatePurpose(purpose);


        VerificationKey key =
                new VerificationKey(
                        normalizedPhone,
                        purpose
                );


        VerificationState state =
                verificationStore.get(key);


        if (
                state == null
                        || state.verifiedUntil() == null
        ) {

            return false;
        }


        Instant now =
                Instant.now();


        if (
                !now.isBefore(
                        state.verifiedUntil()
                )
        ) {

            verificationStore.remove(
                    key
            );


            return false;
        }


        /*
         * 한 번 사용한 인증은 제거
         */
        return verificationStore.remove(
                key,
                state
        );
    }


    /* 6자리 인증번호 생성 */

    private String generateVerificationCode() {

        int number =
                100000
                        + secureRandom.nextInt(
                        900000
                );


        return String.valueOf(
                number
        );
    }


    /* 전화번호 정규화 */

    private String normalizePhone(
            String phone
    ) {

        if (
                phone == null
                        || phone.isBlank()
        ) {

            throw new PhoneVerificationException(
                    "휴대전화번호를 입력해주세요."
            );
        }


        /*
         * 010-1234-5678
         *
         * ↓
         *
         * 01012345678
         */
        String digits =
                phone.replaceAll(
                        "\\D",
                        ""
                );


        /*
         * 현재 회원가입 DTO에서 사용하는
         * 휴대전화 형식과 동일한 기준
         */
        if (
                !digits.matches(
                        "^01[016789]\\d{7,8}$"
                )
        ) {

            throw new PhoneVerificationException(
                    "올바른 휴대전화번호를 입력해주세요."
            );
        }


        return digits;
    }


    /* 인증 목적 확인 */

    private void validatePurpose(
            PhoneVerificationPurpose purpose
    ) {

        if (purpose == null) {

            throw new PhoneVerificationException(
                    "휴대전화 인증 목적이 없습니다."
            );
        }
    }


    /* Map Key */

    private record VerificationKey(

            String phone,

            PhoneVerificationPurpose purpose

    ) {
    }


    /* 인증 상태 */

    private record VerificationState(

            /*
             * 인증번호
             */
            String code,

            /*
             * 인증번호 만료시간
             */
            Instant expiresAt,

            /*
             * 다음 인증번호 발송 가능시간
             */
            Instant resendAvailableAt,

            /*
             * 인증번호 입력 실패횟수
             */
            int failedAttempts,

            /*
             * 인증 성공 상태 만료시간
             */
            Instant verifiedUntil

    ) {
    }
}