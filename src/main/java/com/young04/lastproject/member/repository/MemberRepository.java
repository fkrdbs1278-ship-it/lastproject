package com.young04.lastproject.member.repository;

import com.young04.lastproject.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    /*
     * 로그인 아이디로 회원 조회
     *
     * 사용 위치:
     * - 로그인
     * - Spring Security
     */
    Optional<Member> findByMemberId(String memberId);


    /*
     * 아이디 중복 여부 확인
     *
     * 사용 위치:
     * - 회원가입
     * - 아이디 중복 확인
     */
    boolean existsByMemberId(String memberId);


    /*
     * 이메일 중복 여부 확인
     *
     * 사용 위치:
     * - 회원가입
     */
    boolean existsByEmail(String email);



    /*현재 회원을 제외한 다른 회원이
     *같은 이메일을 사용 중인지 검사
     */
    boolean existsByEmailAndNoNot(
            String email,
            Long no
    );


}
