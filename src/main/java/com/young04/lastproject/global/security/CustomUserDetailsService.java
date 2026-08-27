package com.young04.lastproject.global.security;

import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService
        implements UserDetailsService {

    private final MemberRepository memberRepository;


    /* =========================================================
       Spring Security가 로그인할 때 자동 호출

       username에는 우리가 로그인 화면에서 입력한
       memberId가 전달된다.
    ========================================================= */

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Member member = memberRepository
                .findByMemberId(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "존재하지 않는 회원입니다."
                        )
                );

        return new CustomUserDetails(member);
    }
}
