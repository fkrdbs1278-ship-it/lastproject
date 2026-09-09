package com.young04.lastproject.global.security;

import com.young04.lastproject.member.entity.Member;
import com.young04.lastproject.member.entity.MemberStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Member member;


    /* =========================================================
       권한
       USER  -> ROLE_USER
       ADMIN -> ROLE_ADMIN
    ========================================================= */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + member.getRole().name()
                )
        );
    }


    /* =========================================================
       DB에 저장된 BCrypt 비밀번호
    ========================================================= */

    @Override
    public String getPassword() {
        return member.getPassword();
    }


    /* =========================================================
       Spring Security에서 사용하는 로그인 아이디
    ========================================================= */

    @Override
    public String getUsername() {
        return member.getMemberId();
    }


    /* =========================================================
       회원 번호

       나중에 마이페이지, 리뷰 작성 등에서 사용
    ========================================================= */

    public Long getMemberNo() {
        return member.getNo();
    }


    /* =========================================================
       계정 만료 여부

       현재 프로젝트에서는 계정 만료 기능 없음
    ========================================================= */

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    /* =========================================================
       계정 잠금 여부

       BLOCKED 회원은 로그인 불가능
    ========================================================= */

    @Override
    public boolean isAccountNonLocked() {

        return member.getStatus()
                != MemberStatus.BLOCKED;
    }


    /* =========================================================
       비밀번호 만료 여부

       현재 비밀번호 만료 기능 없음
    ========================================================= */

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    /* =========================================================
       계정 활성화 여부

       ACTIVE 회원만 로그인 가능

       WITHDRAWN 회원은 로그인 불가능
    ========================================================= */

    @Override
    public boolean isEnabled() {

        return member.getStatus()
                == MemberStatus.ACTIVE;
    }
}
