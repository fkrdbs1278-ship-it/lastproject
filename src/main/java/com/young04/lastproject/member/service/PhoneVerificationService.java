package com.young04.lastproject.member.service;

import com.young04.lastproject.member.exception.PhoneVerificationException;
import com.young04.lastproject.member.verification.PhoneVerificationPurpose;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PhoneVerificationService {
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final MemberSmsService memberSmsService;
    private final HttpServletRequest request;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    // 인증 증명은 현재 HTTP 세션에만 속한다. API와 기존 호출 메서드명은 유지한다.
    private final Map<VerificationKey, VerificationState> verificationStore = new ConcurrentHashMap<>();
    // 새 세션을 만들어도 전화번호·용도별 60초 재발송 제한은 유지한다.
    private final Map<PhonePurposeKey, Instant> resendAvailableAt = new ConcurrentHashMap<>();

    public PhoneVerificationService(MemberSmsService memberSmsService,
                                    HttpServletRequest request, Clock clock) {
        this.memberSmsService = memberSmsService;
        this.request = request;
        this.clock = clock;
    }

    public void sendVerificationCode(String phone, PhoneVerificationPurpose purpose) {
        String normalizedPhone = normalizePhone(phone);
        validatePurpose(purpose);
        VerificationKey key = key(normalizedPhone, purpose);
        PhonePurposeKey phoneKey = new PhonePurposeKey(normalizedPhone, purpose);
        Instant now = clock.instant();
        removeExpiredStates(now);
        Instant nextSendAt = now.plus(RESEND_COOLDOWN);

        // 확인과 갱신을 원자적으로 수행해 동시 재발송도 차단한다.
        resendAvailableAt.compute(phoneKey, (ignored, existing) -> {
            if (existing != null && now.isBefore(existing)) {
                long seconds = Duration.between(now, existing).toSeconds() + 1;
                throw new PhoneVerificationException("인증번호는 " + seconds + "초 후 다시 요청할 수 있습니다.");
            }
            return nextSendAt;
        });

        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        try {
            memberSmsService.sendVerificationCode(normalizedPhone, code);
            verificationStore.put(key, new VerificationState(code, now.plus(CODE_TTL), 0, null));
        } catch (RuntimeException exception) {
            resendAvailableAt.remove(phoneKey, nextSendAt);
            throw exception;
        }
    }

    public void verifyCode(String phone, PhoneVerificationPurpose purpose, String inputCode) {
        String normalizedPhone = normalizePhone(phone);
        validatePurpose(purpose);
        if (inputCode == null || !inputCode.matches("\\d{6}")) {
            throw new PhoneVerificationException("6자리 인증번호를 입력해주세요.");
        }
        VerificationKey key = key(normalizedPhone, purpose);
        Instant now = clock.instant();
        AtomicReference<String> failure = new AtomicReference<>();

        // 실패 횟수 증가도 원자적으로 처리한다. compute 안에서 예외를 던지면
        // 상태 변경이 취소되므로, 변경을 반영한 다음 아래에서 예외를 던진다.
        verificationStore.compute(key, (ignored, state) -> {
            if (state == null) {
                failure.set("인증번호를 먼저 요청해주세요.");
                return null;
            }
            if (state.verifiedUntil() != null && now.isBefore(state.verifiedUntil())) {
                return state;
            }
            if (state.expiresAt() == null || !now.isBefore(state.expiresAt())) {
                failure.set("인증번호가 만료되었습니다. 다시 요청해주세요.");
                return null;
            }
            if (!state.code().equals(inputCode)) {
                int attempts = state.failedAttempts() + 1;
                if (attempts >= MAX_VERIFY_ATTEMPTS) {
                    failure.set("인증번호 입력 횟수를 초과했습니다. 인증번호를 다시 요청해주세요.");
                    return null;
                }
                failure.set("인증번호가 일치하지 않습니다. 남은 횟수: " + (MAX_VERIFY_ATTEMPTS - attempts));
                return new VerificationState(state.code(), state.expiresAt(), attempts, null);
            }
            return new VerificationState(null, null, 0, now.plus(VERIFIED_TTL));
        });
        if (failure.get() != null) throw new PhoneVerificationException(failure.get());
    }

    public boolean isVerified(String phone, PhoneVerificationPurpose purpose) {
        String normalizedPhone = normalizePhone(phone);
        validatePurpose(purpose);
        VerificationKey key = key(normalizedPhone, purpose);
        VerificationState state = verificationStore.get(key);
        if (state == null || state.verifiedUntil() == null) return false;
        if (!clock.instant().isBefore(state.verifiedUntil())) {
            verificationStore.remove(key, state);
            return false;
        }
        return true;
    }

    public boolean consumeVerification(String phone, PhoneVerificationPurpose purpose) {
        String normalizedPhone = normalizePhone(phone);
        validatePurpose(purpose);
        VerificationKey key = key(normalizedPhone, purpose);
        Instant now = clock.instant();
        AtomicBoolean consumed = new AtomicBoolean();
        verificationStore.computeIfPresent(key, (ignored, state) -> {
            if (state.verifiedUntil() == null) return state;
            if (now.isBefore(state.verifiedUntil())) consumed.set(true);
            return null;
        });
        return consumed.get();
    }

    private VerificationKey key(String phone, PhoneVerificationPurpose purpose) {
        // Spring이 현재 요청을 가리키는 HttpServletRequest 프록시를 주입한다.
        // 브라우저가 보낸 임의의 sessionId 파라미터는 신뢰하지 않는다.
        return new VerificationKey(phone, purpose, request.getSession().getId());
    }

    private void removeExpiredStates(Instant now) {
        verificationStore.entrySet().removeIf(entry -> {
            VerificationState state = entry.getValue();
            Instant expiry = state.verifiedUntil() != null ? state.verifiedUntil() : state.expiresAt();
            return expiry == null || !now.isBefore(expiry);
        });
        resendAvailableAt.entrySet().removeIf(entry -> !now.isBefore(entry.getValue()));
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new PhoneVerificationException("휴대전화번호를 입력해주세요.");
        }
        String digits = phone.replaceAll("\\D", "");
        if (!digits.matches("^01[016789]\\d{7,8}$")) {
            throw new PhoneVerificationException("올바른 휴대전화번호를 입력해주세요.");
        }
        return digits;
    }

    private void validatePurpose(PhoneVerificationPurpose purpose) {
        if (purpose == null) throw new PhoneVerificationException("휴대전화 인증 목적이 없습니다.");
    }

    private record PhonePurposeKey(String phone, PhoneVerificationPurpose purpose) {}
    private record VerificationKey(String phone, PhoneVerificationPurpose purpose, String sessionId) {}
    private record VerificationState(String code, Instant expiresAt, int failedAttempts, Instant verifiedUntil) {}
}
