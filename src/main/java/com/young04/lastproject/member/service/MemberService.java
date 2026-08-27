package com.young04.lastproject.member.service;

import com.young04.lastproject.global.exception.member.DuplicateEmailException;
import com.young04.lastproject.global.exception.member.DuplicateMemberIdException;
import com.young04.lastproject.global.exception.member.PasswordMismatchException;
import com.young04.lastproject.global.exception.member.MemberNotFoundException;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /* 회원가입 */

    @Transactional
    public Long signup(SignupRequest request) {

        String memberId = request.getMemberId().trim();
        String email = normalizeNullable(request.getEmail());

        // 아이디 중복 확인
        if (memberRepository.existsByMemberId(memberId)) {
            throw new DuplicateMemberIdException();
        }

        // 이메일을 입력한 경우에만 중복 확인
        if (email != null
                && memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        // 비밀번호 확인
        if (!request.getPassword()
                .equals(request.getPasswordCheck())) {
            throw new PasswordMismatchException();
        }

        // BCrypt 암호화
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .memberId(memberId)
                .password(encodedPassword)
                .name(request.getName().trim())
                .nickname(normalizeNullable(request.getNickname()))
                .email(email)
                .phone(request.getPhone().trim())
                .birthDate(request.getBirthDate())
                .gender(normalizeNullable(request.getGender()))
                .agreeTermsYn("Y")
                .agreePrivacyYn("Y")
                .build();

        Member savedMember =
                memberRepository.save(member);

        return savedMember.getNo();
    }


    /* 빈 문자열을 NULL로 변환 */

    private String normalizeNullable(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }



    /* 회원 조회 */

    public MemberResponse getMember(
            Long memberNo
    ) {

        Member member =
                findMember(memberNo);

        return MemberResponse.from(member);
    }


    /* 회원정보 수정 Form 조회 */

    public MemberUpdateRequest getMemberUpdateRequest(
            Long memberNo
    ) {

        Member member =
                findMember(memberNo);

        return MemberUpdateRequest.from(member);
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


        /* 현재 비밀번호 확인 */

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


        /* 이메일 중복 확인 */

        if (email != null
                && memberRepository
                .existsByEmailAndNoNot(
                        email,
                        memberNo
                )) {

            throw new DuplicateEmailException();
        }


        /* 회원정보 수정 */

        member.updateProfile(
                request.getName().trim(),
                normalizeNullable(
                        request.getNickname()
                ),
                email,
                request.getPhone().trim(),
                request.getBirthDate(),
                normalizeNullable(
                        request.getGender()
                )
        );


        /*
         * JPA Dirty Checking으로
         * 별도의 save() 없이 UPDATE 된다.
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


        /* 현재 비밀번호 확인 */

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                member.getPassword()
        )) {

            return false;
        }


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