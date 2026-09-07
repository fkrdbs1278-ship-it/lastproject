package com.young04.lastproject.member.repository;

import com.young04.lastproject.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

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

    /* 이름 + 휴대전화번호로 회원 조회

        DB에
        010-1234-5678
        형태로 저장되어 있어도

        01012345678
        형태로 비교할 수 있도록
        '-'를 제거한 뒤 검색한다. */

    @Query("""
        select m
        from Member m
        where m.name = :name
          and replace(m.phone, '-', '') = :phoneDigits
        """)
    List<Member> findAllByNameAndPhoneDigits(
            @Param("name") String name,
            @Param("phoneDigits") String phoneDigits
    );


    /*  아이디 + 휴대전화번호로 회원 조회

        전화번호의 '-'는 제거하고 비교한다. */

    @Query("""
        select m
        from Member m
        where m.memberId = :memberId
          and replace(m.phone, '-', '') = :phoneDigits
        """)
    Optional<Member> findByMemberIdAndPhoneDigits(
            @Param("memberId") String memberId,
            @Param("phoneDigits") String phoneDigits
    );


}
