package com.young04.lastproject.loginhistory.entity;

import com.young04.lastproject.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory {

    /* =========================================================
       PK
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO")
    private Long no;


    /* =========================================================
       회원

       로그인 실패 시 존재하지 않는 아이디일 수도 있으므로
       NULL 허용
    ========================================================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;


    /* =========================================================
       로그인 시 입력한 아이디

       회원이 존재하지 않는 경우에도 입력값을 기록하기 위해
       MEMBER_NO와 별도로 저장
    ========================================================= */

    @Column(
            name = "MEMBER_ID",
            length = 100
    )
    private String memberId;


    /* =========================================================
       로그인 성공 여부

       Y = 성공
       N = 실패

       Oracle DB가 CHAR(1)이므로
       columnDefinition을 CHAR(1)로 지정
    ========================================================= */

    @Column(
            name = "SUCCESS_YN",
            nullable = false,
            columnDefinition = "CHAR(1)"
    )
    private String successYn;


    /* =========================================================
       접속 IP
    ========================================================= */

    @Column(
            name = "IP_ADDRESS",
            length = 50
    )
    private String ipAddress;


    /* =========================================================
       브라우저 정보
    ========================================================= */

    @Column(
            name = "USER_AGENT",
            length = 500
    )
    private String userAgent;


    /* =========================================================
       로그인 실패 이유
    ========================================================= */

    @Column(
            name = "FAILURE_REASON",
            length = 500
    )
    private String failureReason;


    /* =========================================================
       로그인 시각
    ========================================================= */

    @Column(
            name = "LOGIN_DATE",
            nullable = false
    )
    private LocalDateTime loginDate;


    /* =========================================================
       Builder
    ========================================================= */

    @Builder
    private LoginHistory(
            Member member,
            String memberId,
            String successYn,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {

        this.member = member;
        this.memberId = memberId;
        this.successYn = successYn;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failureReason = failureReason;
    }


    /* =========================================================
       INSERT 직전
    ========================================================= */

    @PrePersist
    protected void prePersist() {

        if (this.loginDate == null) {
            this.loginDate = LocalDateTime.now();
        }
    }


    /* =========================================================
       로그인 성공 이력 생성
    ========================================================= */

    public static LoginHistory success(
            Member member,
            String memberId,
            String ipAddress,
            String userAgent
    ) {

        return LoginHistory.builder()
                .member(member)
                .memberId(memberId)
                .successYn("Y")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failureReason(null)
                .build();
    }


    /* =========================================================
       로그인 실패 이력 생성
    ========================================================= */

    public static LoginHistory failure(
            Member member,
            String memberId,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {

        return LoginHistory.builder()
                .member(member)
                .memberId(memberId)
                .successYn("N")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .build();
    }
}
