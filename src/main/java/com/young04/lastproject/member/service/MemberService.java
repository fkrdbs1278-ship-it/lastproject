package com.young04.lastproject.member.service;

import com.young04.lastproject.global.exception.member.DuplicateEmailException;
import com.young04.lastproject.global.exception.member.DuplicateMemberIdException;
import com.young04.lastproject.global.exception.member.InvalidBirthDateException;
import com.young04.lastproject.global.exception.member.MemberNotFoundException;
import com.young04.lastproject.global.exception.member.PasswordMismatchException;
import com.young04.lastproject.member.dto.MemberResponse;
import com.young04.lastproject.member.dto.MemberUpdateRequest;
import com.young04.lastproject.member.dto.PasswordConfirmRequest;
import com.young04.lastproject.member.dto.SignupRequest;
import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /* =========================================================
       회원가입
    ========================================================= */

    @Transactional
    public Long signup(
            SignupRequest request
    ) {

        /* 회원가입 비즈니스 Validation */
        validateSignup(request);


        /* =====================================================
           입력값 정리
        ===================================================== */

        String memberId =
                request.getMemberId().trim();

        String email =
                normalizeNullable(
                        request.getEmail()
                );


        /* =====================================================
           BCrypt 비밀번호 암호화
        ===================================================== */

        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );


        /* =====================================================
           Member Entity 생성
        ===================================================== */

        Member member =
                Member.builder()
                        .memberId(memberId)
                        .password(encodedPassword)
                        .name(
                                request
                                        .getName()
                                        .trim()
                        )
                        .nickname(
                                normalizeNullable(
                                        request.getNickname()
                                )
                        )
                        .email(email)
                        .phone(
                                request
                                        .getPhone()
                                        .trim()
                        )
                        .birthDate(
                                request.getBirthDate()
                        )
                        .gender(
                                normalizeNullable(
                                        request.getGender()
                                )
                        )
                        .agreeTermsYn("Y")
                        .agreePrivacyYn("Y")
                        .build();


        /* =====================================================
           DB 저장
        ===================================================== */

        Member savedMember =
                memberRepository.save(member);


        return savedMember.getNo();
    }


    /* =========================================================
       회원가입 Validation

       Service에서 처리하는 회원가입 비즈니스 규칙
    ========================================================= */

    private void validateSignup(
            SignupRequest request
    ) {

        /* 비밀번호와 비밀번호 확인 */
        validatePasswordMatch(request);


        /* 생년월일 */
        validateBirthDate(request);


        /* 아이디 중복 */
        validateDuplicateMemberId(request);


        /* 이메일 중복 */
        validateDuplicateEmail(request);
    }


    /* =========================================================
       비밀번호 일치 검사
    ========================================================= */

    private void validatePasswordMatch(
            SignupRequest request
    ) {

        if (!Objects.equals(
                request.getPassword(),
                request.getPasswordCheck()
        )) {

            throw new PasswordMismatchException();
        }
    }


    /* =========================================================
       생년월일 검사

       허용 범위:
       1900-01-01 ~ 어제

       SignupRequest의 @Past도 검사하지만
       Service에서도 회원가입 규칙을 다시 보호한다.
    ========================================================= */

    private void validateBirthDate(
            SignupRequest request
    ) {

        LocalDate birthDate =
                request.getBirthDate();


        /* 생년월일이 선택사항이면 null 허용 */
        if (birthDate == null) {

            return;
        }


        LocalDate minimumBirthDate =
                LocalDate.of(
                        1900,
                        1,
                        1
                );


        /*
         * 1900-01-01 이전
         */
        if (birthDate.isBefore(
                minimumBirthDate
        )) {

            throw new InvalidBirthDateException();
        }


        /*
         * 오늘 또는 미래
         *
         * @Past에서도 검사하지만
         * Service를 직접 호출하는 상황까지 대비
         */
        if (!birthDate.isBefore(
                LocalDate.now()
        )) {

            throw new InvalidBirthDateException();
        }
    }


    /* =========================================================
       아이디 중복 검사
    ========================================================= */

    private void validateDuplicateMemberId(
            SignupRequest request
    ) {

        String memberId =
                request
                        .getMemberId()
                        .trim();


        if (memberRepository.existsByMemberId(
                memberId
        )) {

            throw new DuplicateMemberIdException();
        }
    }


    /* =========================================================
       이메일 중복 검사

       이메일은 선택사항이므로
       입력된 경우에만 검사
    ========================================================= */

    private void validateDuplicateEmail(
            SignupRequest request
    ) {

        String email =
                normalizeNullable(
                        request.getEmail()
                );


        if (email == null) {

            return;
        }


        if (memberRepository.existsByEmail(
                email
        )) {

            throw new DuplicateEmailException();
        }
    }


    /* =========================================================
       빈 문자열을 NULL로 변환

       null
       ""
       "   "
          ↓
       null

       "test"
          ↓
       "test"
    ========================================================= */

    private String normalizeNullable(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return null;
        }


        return value.trim();
    }


    /* =========================================================
       회원 조회
    ========================================================= */

    public MemberResponse getMember(
            Long memberNo
    ) {

        Member member =
                findMember(memberNo);


        return MemberResponse.from(
                member
        );
    }


    /* =========================================================
       회원정보 수정 Form 조회
    ========================================================= */

    public MemberUpdateRequest getMemberUpdateRequest(
            Long memberNo
    ) {

        Member member =
                findMember(memberNo);


        return MemberUpdateRequest.from(
                member
        );
    }


    /* =========================================================
       회원정보 수정

       true  = 수정 성공
       false = 현재 비밀번호 불일치
    ========================================================= */

    @Transactional
    public boolean updateMember(
            Long memberNo,
            MemberUpdateRequest request
    ) {

        Member member =
                findMember(memberNo);


        /* =====================================================
           현재 비밀번호 확인
        ===================================================== */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                member.getPassword()
        )) {

            return false;
        }


        String email =
                normalizeNullable(
                        request.getEmail()
                );


        /* =====================================================
           이메일 중복 확인

           현재 로그인한 회원의 이메일은 제외한다.
        ===================================================== */

        if (email != null
                && memberRepository
                .existsByEmailAndNoNot(
                        email,
                        memberNo
                )) {

            throw new DuplicateEmailException();
        }


        /* =====================================================
           회원정보 수정
        ===================================================== */

        member.updateProfile(
                request
                        .getName()
                        .trim(),

                normalizeNullable(
                        request.getNickname()
                ),

                email,

                request
                        .getPhone()
                        .trim(),

                request.getBirthDate(),

                normalizeNullable(
                        request.getGender()
                )
        );


        /*
         * @Transactional 안에서 관리 중인 Entity이므로
         * JPA Dirty Checking으로 UPDATE된다.
         *
         * memberRepository.save(member);
         *
         * 를 별도로 호출하지 않아도 된다.
         */

        return true;
    }


    /* =========================================================
       회원 탈퇴

       true  = 탈퇴 성공
       false = 비밀번호 불일치
    ========================================================= */

    @Transactional
    public boolean withdraw(
            Long memberNo,
            PasswordConfirmRequest request
    ) {

        Member member =
                findMember(memberNo);


        /* =====================================================
           현재 비밀번호 확인
        ===================================================== */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                member.getPassword()
        )) {

            return false;
        }


        /* =====================================================
           회원 탈퇴

           물리 DELETE가 아니라
           Member Entity의 withdraw()에서
           WITHDRAWN 상태로 변경
        ===================================================== */

        member.withdraw();


        return true;
    }


    /* =========================================================
       회원 Entity 공통 조회
    ========================================================= */

    private Member findMember(
            Long memberNo
    ) {

        return memberRepository
                .findById(memberNo)
                .orElseThrow(
                        MemberNotFoundException::new
                );
    }
}