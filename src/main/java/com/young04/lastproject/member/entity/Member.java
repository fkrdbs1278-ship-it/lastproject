package com.young04.lastproject.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    /* =========================================================
       회원 번호 PK
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* =========================================================
       로그인 정보
    ========================================================= */

    @Column(
            name = "MEMBER_ID",
            nullable = false,
            unique = true,
            length = 100
    )
    private String memberId;


    @Column(
            name = "PASSWORD",
            nullable = false,
            length = 255
    )
    private String password;


    /* =========================================================
       회원 기본 정보
    ========================================================= */

    @Column(
            name = "NAME",
            nullable = false,
            length = 50
    )
    private String name;


    @Column(
            name = "NICKNAME",
            length = 50
    )
    private String nickname;


    @Column(
            name = "EMAIL",
            unique = true,
            length = 100
    )
    private String email;


    @Column(
            name = "PHONE",
            nullable = false,
            length = 20
    )
    private String phone;


    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;


    @Column(
            name = "GENDER",
            length = 10
    )
    private String gender;


    /* =========================================================
       권한
       USER  : 일반 사용자
       ADMIN : 관리자
    ========================================================= */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ROLE",
            nullable = false,
            length = 20
    )
    private MemberRole role;


    /* =========================================================
       회원 상태
       ACTIVE    : 정상
       BLOCKED   : 차단
       WITHDRAWN : 탈퇴
    ========================================================= */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "STATUS",
            nullable = false,
            length = 20
    )
    private MemberStatus status;


    /* =========================================================
       약관 동의

       DB 컬럼이 VARCHAR2가 아닌 CHAR(1)이므로
       columnDefinition을 반드시 CHAR(1)로 설정
    ========================================================= */

    @Column(
            name = "AGREE_TERMS_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String agreeTermsYn;


    @Column(
            name = "AGREE_PRIVACY_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String agreePrivacyYn;


    /* =========================================================
       로그인 관리
    ========================================================= */

    @Column(
            name = "LOGIN_FAIL_COUNT",
            nullable = false
    )
    private Integer loginFailCount;


    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;


    /* =========================================================
       날짜 정보
    ========================================================= */

    @Column(
            name = "REGDATE",
            nullable = false
    )
    private LocalDateTime regdate;


    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;


    @Column(name = "WITHDRAW_DATE")
    private LocalDateTime withdrawDate;


    /* =========================================================
       회원 생성

       Member.builder()
             .memberId(...)
             .password(...)
             ...
             .build()

       형식으로 사용
    ========================================================= */

    @Builder
    public Member(
            String memberId,
            String password,
            String name,
            String nickname,
            String email,
            String phone,
            LocalDate birthDate,
            String gender,
            String agreeTermsYn,
            String agreePrivacyYn
    ) {

        this.memberId = memberId;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;

        this.agreeTermsYn = agreeTermsYn;
        this.agreePrivacyYn = agreePrivacyYn;

        // 일반 회원가입이므로 기본 권한 USER
        this.role = MemberRole.USER;

        // 가입 직후 정상 회원
        this.status = MemberStatus.ACTIVE;

        // 가입 직후 로그인 실패 횟수 0
        this.loginFailCount = 0;
    }

    /*회원 정보 수정*/
    public void updateProfile(
            String name,
            String nickname,
            String email,
            String phone,
            LocalDate birthDate,
            String gender
    ){
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    /*회원 탈퇴
    실제 MEMBER 행은 삭제하지 않는다. ACTIVE -> WITHDRAWN
     */
    public void withdraw(){

            this.status = MemberStatus.WITHDRAWN;

            this.withdrawDate = LocalDateTime.now();
    }


    /* =========================================================
    로그인 성공 처리
    ========================================================= */

    public void loginSuccess() {

        // 로그인 성공 시 실패 횟수 초기화
        this.loginFailCount = 0;

        // 마지막 로그인 시간 저장
        this.lastLoginDate = LocalDateTime.now();
    }


    /* =========================================================
    로그인 실패 처리
    ========================================================= */

    public void loginFailure() {

        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }

        this.loginFailCount++;
    }

    /* =========================================================
       INSERT 직전 자동 실행

       혹시 Java에서 값이 빠져도
       DB에 NULL이 들어가지 않도록 기본값 설정
    ========================================================= */

    @PrePersist
    protected void prePersist() {

        if (role == null) {
            role = MemberRole.USER;
        }

        if (status == null) {
            status = MemberStatus.ACTIVE;
        }

        if (agreeTermsYn == null) {
            agreeTermsYn = "N";
        }

        if (agreePrivacyYn == null) {
            agreePrivacyYn = "N";
        }

        if (loginFailCount == null) {
            loginFailCount = 0;
        }

        if (regdate == null) {
            regdate = LocalDateTime.now();
        }
    }


    /* =========================================================
       UPDATE 직전 자동 실행
    ========================================================= */

    @PreUpdate
    protected void preUpdate() {
        updateDate = LocalDateTime.now();
    }
}
